package cn.chaosmp.camerafix.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class PayloadIdCompat {
    private static final Class<?> ID_CLASS = findIdClass();
    private static final Method CREATE_ID = findCreateId();
    private static final Method PARSE_ID = findParseId();
    private static final Method TYPE_ID = findTypeId();
    private static final Method DISCARDED_CODEC = findDiscardedCodec();
    private static final Method FORGE_HOOKS_CODEC = findForgeHooksCodec();
    private static final Constructor<?> TYPE_CONSTRUCTOR = findTypeConstructor();

    private PayloadIdCompat() {
    }

    public static Object createNekoId(String path) {
        return invoke(CREATE_ID, null, "neko", path);
    }

    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createNekoType(String path) {
        return createType(createNekoId(path));
    }

    @SuppressWarnings("unchecked")
    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createType(Object id) {
        try {
            return (CustomPacketPayload.Type<T>) TYPE_CONSTRUCTOR.newInstance(id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create CustomPacketPayload.Type", exception);
        }
    }

    public static Object read(FriendlyByteBuf buf) {
        return invoke(PARSE_ID, null, buf.readUtf(Short.MAX_VALUE));
    }

    public static void write(FriendlyByteBuf buf, Object id) {
        buf.writeUtf(id.toString());
    }

    public static Object id(CustomPacketPayload.Type<? extends CustomPacketPayload> type) {
        return invoke(TYPE_ID, type);
    }

    @SuppressWarnings("unchecked")
    public static <T> T discardedCodec(Object id, int maxPayloadSize) {
        return (T) invoke(DISCARDED_CODEC, null, id, maxPayloadSize);
    }

    @SuppressWarnings("unchecked")
    public static <T> T forgeHooksCodec(Object id, int maxPayloadSize) {
        return FORGE_HOOKS_CODEC == null ? null : (T) invoke(FORGE_HOOKS_CODEC, null, id, maxPayloadSize);
    }

    public static boolean hasForgeHooksCodec() {
        return FORGE_HOOKS_CODEC != null;
    }

    private static Class<?> findIdClass() {
        Class<?> type = findClass(
                "net.minecraft.resources.ResourceLocation",
                "net.minecraft.resources.Identifier"
        );
        if (type == null) {
            throw new IllegalStateException("Minecraft identifier class is missing");
        }
        return type;
    }

    private static Method findCreateId() {
        Method method = findMethod(ID_CLASS, new Class<?>[]{String.class, String.class}, "fromNamespaceAndPath");
        if (method == null) {
            throw new IllegalStateException("Identifier factory method is missing: " + ID_CLASS.getName());
        }
        return method;
    }

    private static Method findParseId() {
        Method method = findMethod(ID_CLASS, new Class<?>[]{String.class}, "parse", "tryParse");
        if (method == null) {
            throw new IllegalStateException("Identifier parser method is missing: " + ID_CLASS.getName());
        }
        return method;
    }

    private static Method findTypeId() {
        Method method = findMethod(CustomPacketPayload.Type.class, new Class<?>[0], "id");
        if (method == null) {
            throw new IllegalStateException("CustomPacketPayload.Type id() is missing");
        }
        return method;
    }

    private static Method findDiscardedCodec() {
        Class<?> discardedPayload = findClass("net.minecraft.network.protocol.common.custom.DiscardedPayload");
        Method method = findMethod(discardedPayload, new Class<?>[]{ID_CLASS, int.class}, "codec");
        if (method == null) {
            throw new IllegalStateException("DiscardedPayload codec factory is missing");
        }
        return method;
    }

    private static Method findForgeHooksCodec() {
        Class<?> type = findClass("net.minecraftforge.common.ForgeHooks");
        if (type == null) {
            return null;
        }
        return findMethod(type, new Class<?>[]{ID_CLASS, int.class}, "getCustomPayloadCodec");
    }

    private static Constructor<?> findTypeConstructor() {
        try {
            Constructor<?> constructor = CustomPacketPayload.Type.class.getDeclaredConstructor(ID_CLASS);
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException("CustomPacketPayload.Type constructor is missing", exception);
        }
    }

    private static Object invoke(Method method, Object owner, Object... args) {
        try {
            return method.invoke(owner, args);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke " + method, exception);
        }
    }

    private static Class<?> findClass(String... names) {
        ClassLoader loader = PayloadIdCompat.class.getClassLoader();
        for (String name : names) {
            try {
                return Class.forName(name, false, loader);
            } catch (ClassNotFoundException ignored) {
            }
        }
        return null;
    }

    private static Method findMethod(Class<?> type, Class<?>[] parameterTypes, String... names) {
        if (type == null) {
            return null;
        }
        for (String name : names) {
            try {
                Method method = type.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
            }
            try {
                Method method = type.getMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }
}
