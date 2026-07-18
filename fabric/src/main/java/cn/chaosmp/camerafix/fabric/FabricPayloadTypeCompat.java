package cn.chaosmp.camerafix.fabric;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

final class FabricPayloadTypeCompat {
    private static final String NAMESPACE = "neko";
    private static final Class<?> ID_CLASS = findIdClass();
    private static final Constructor<?> TYPE_CONSTRUCTOR = findTypeConstructor();
    private static final Method FROM_NAMESPACE_AND_PATH = findFromNamespaceAndPath();

    private FabricPayloadTypeCompat() {
    }

    static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createNekoType(String path) {
        try {
            Object id = FROM_NAMESPACE_AND_PATH.invoke(null, NAMESPACE, path);
            @SuppressWarnings("unchecked")
            CustomPacketPayload.Type<T> type = (CustomPacketPayload.Type<T>) TYPE_CONSTRUCTOR.newInstance(id);
            return type;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create Fabric payload type", exception);
        }
    }

    private static Class<?> findIdClass() {
        return requireClass(
                "net.minecraft.resources.ResourceLocation",
                "net.minecraft.resources.Identifier",
                "net.minecraft.class_2960"
        );
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

    private static Method findFromNamespaceAndPath() {
        Method method = findStaticMethod(ID_CLASS, new Class<?>[]{String.class, String.class}, "fromNamespaceAndPath", "method_60655");
        if (method == null) {
            throw new IllegalStateException("Identifier factory method is missing: " + ID_CLASS.getName());
        }
        return method;
    }

    private static Class<?> requireClass(String... names) {
        for (String name : names) {
            try {
                return Class.forName(name, false, FabricPayloadTypeCompat.class.getClassLoader());
            } catch (ClassNotFoundException ignored) {
            }
        }
        throw new IllegalStateException("Minecraft identifier class is missing");
    }

    private static Method findStaticMethod(Class<?> type, Class<?>[] parameterTypes, String... names) {
        for (String name : names) {
            try {
                Method method = type.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                    return method;
                }
            } catch (NoSuchMethodException ignored) {
            }

            try {
                Method method = type.getMethod(name, parameterTypes);
                method.setAccessible(true);
                if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                    return method;
                }
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }
}
