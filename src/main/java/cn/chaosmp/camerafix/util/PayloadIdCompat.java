package cn.chaosmp.camerafix.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class PayloadIdCompat {
    private static final Class<?> ID_CLASS = findIdClass();
    private static final Constructor<?> TYPE_CONSTRUCTOR = findTypeConstructor();
    private static final Method FORGE_HOOKS_CODEC = findForgeHooksCodec();
    private static final Method FROM_NS_AND_PATH = findFromNamespaceAndPath();
    private static final Method PARSE = findParse();
    private static final Method TYPE_ID = findTypeId();
    private static final Method DISCARDED_CODEC = findDiscardedCodec();

    private PayloadIdCompat() {
    }

    public static Object createNekoId(String path) {
        try {
            return FROM_NS_AND_PATH.invoke(null, "neko", path);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create identifier", exception);
        }
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
        try {
            return PARSE.invoke(null, buf.readUtf(Short.MAX_VALUE));
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to parse identifier", exception);
        }
    }

    public static void write(FriendlyByteBuf buf, Object id) {
        buf.writeUtf(id.toString());
    }

    public static Object id(CustomPacketPayload.Type<? extends CustomPacketPayload> type) {
        try {
            return TYPE_ID.invoke(type);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to get id from Type", exception);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T discardedCodec(Object id, int maxPayloadSize) {
        try {
            return (T) DISCARDED_CODEC.invoke(null, id, maxPayloadSize);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create discarded codec", exception);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T forgeHooksCodec(Object id, int maxPayloadSize) {
        if (FORGE_HOOKS_CODEC == null) {
            return null;
        }
        try {
            return (T) FORGE_HOOKS_CODEC.invoke(null, id, maxPayloadSize);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke ForgeHooks.getCustomPayloadCodec", exception);
        }
    }

    public static boolean hasForgeHooksCodec() {
        return FORGE_HOOKS_CODEC != null;
    }

    private static Class<?> findIdClass() {
        Class<?> type = findMinecraftClass("resources.ResourceLocation", "resources.Identifier");
        if (type == null) {
            throw new IllegalStateException("Minecraft identifier class is missing");
        }
        return type;
    }

    private static Method findFromNamespaceAndPath() {
        Method method = findStaticMethod(ID_CLASS, new Class<?>[]{String.class, String.class}, "fromNamespaceAndPath");
        if (method == null) {
            throw new IllegalStateException("Identifier factory method is missing: " + ID_CLASS.getName());
        }
        return method;
    }

    private static Method findParse() {
        Method method = findStaticMethod(ID_CLASS, new Class<?>[]{String.class}, "parse", "tryParse");
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
        Class<?> discardedPayload = findMinecraftClass("network.protocol.common.custom.DiscardedPayload");
        if (discardedPayload == null) {
            throw new IllegalStateException("DiscardedPayload class is missing");
        }
        Method method = findStaticMethod(discardedPayload, new Class<?>[]{ID_CLASS, int.class}, "codec");
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
        return findStaticMethod(type, new Class<?>[]{ID_CLASS, int.class}, "getCustomPayloadCodec");
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

    private static Class<?> findMinecraftClass(String... paths) {
        ClassLoader loader = PayloadIdCompat.class.getClassLoader();
        for (String path : paths) {
            // Use string concat to bypass Loom remapping
            Class<?> type = findClass("net.minecraft." + path);
            if (type != null) return type;
        }
        return null;
    }

    private static Class<?> findClass(String name) {
        try {
            return Class.forName(name, false, PayloadIdCompat.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static Method findStaticMethod(Class<?> type, Class<?>[] parameterTypes, String... names) {
        if (type == null) return null;
        for (String name : names) {
            try {
                Method method = type.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) return method;
            } catch (NoSuchMethodException ignored) {
            }
            try {
                Method method = type.getMethod(name, parameterTypes);
                method.setAccessible(true);
                if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) return method;
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }

    private static Method findMethod(Class<?> type, Class<?>[] parameterTypes, String... names) {
        if (type == null) return null;
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
