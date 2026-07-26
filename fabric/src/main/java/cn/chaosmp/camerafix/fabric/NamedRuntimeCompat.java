package cn.chaosmp.camerafix.fabric;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class NamedRuntimeCompat {
    public static final Class<?> MOVE_PACKET_CLASS = requireClass("net.minecraft.network.protocol.game.ServerboundMovePlayerPacket");
    public static final Class<?> PLAYER_INPUT_PACKET_CLASS = requireClass("net.minecraft.network.protocol.game.ServerboundPlayerInputPacket");
    public static final Class<?> USE_ITEM_ON_PACKET_CLASS = requireClass("net.minecraft.network.protocol.game.ServerboundUseItemOnPacket");
    public static final Class<?> USE_ITEM_PACKET_CLASS = requireClass("net.minecraft.network.protocol.game.ServerboundUseItemPacket");
    private static final Class<?> MINECRAFT_CLASS = requireClass("net.minecraft.client.Minecraft");
    private static final Method MINECRAFT_GET_INSTANCE = requireMethod(MINECRAFT_CLASS, new Class<?>[0], "getInstance");
    private static final Field GAME_RENDERER_FIELD = requireField(MINECRAFT_CLASS, "gameRenderer");
    private static final Method MAIN_CAMERA_METHOD = requireMethod(GAME_RENDERER_FIELD.getType(), new Class<?>[0], "mainCamera", "getMainCamera");
    private static final Method HAS_ROTATION_METHOD = requireMethod(MOVE_PACKET_CLASS, new Class<?>[0], "hasRotation");
    private static final Method GET_Y_ROT_METHOD = requireMethod(MOVE_PACKET_CLASS, new Class<?>[]{float.class}, "getYRot");
    private static final Method GET_X_ROT_METHOD = requireMethod(MOVE_PACKET_CLASS, new Class<?>[]{float.class}, "getXRot");

    private NamedRuntimeCompat() {
    }

    public static boolean isMovePacket(Object packet) {
        return MOVE_PACKET_CLASS.isInstance(packet);
    }

    public static boolean isPlayerInputPacket(Object packet) {
        return PLAYER_INPUT_PACKET_CLASS.isInstance(packet);
    }

    public static boolean isUseItemOnPacket(Object packet) {
        return USE_ITEM_ON_PACKET_CLASS.isInstance(packet);
    }

    public static boolean isUseItemPacket(Object packet) {
        return USE_ITEM_PACKET_CLASS.isInstance(packet);
    }

    public static boolean hasRotation(Object packet) {
        return (Boolean) invoke(HAS_ROTATION_METHOD, packet);
    }

    public static float getYRot(Object packet) {
        return ((Float) invoke(GET_Y_ROT_METHOD, packet, Float.NaN)).floatValue();
    }

    public static float getXRot(Object packet) {
        return ((Float) invoke(GET_X_ROT_METHOD, packet, Float.NaN)).floatValue();
    }

    public static Object mainCamera() {
        Object minecraft = invoke(MINECRAFT_GET_INSTANCE, null);
        Object gameRenderer = get(GAME_RENDERER_FIELD, minecraft);
        return invoke(MAIN_CAMERA_METHOD, gameRenderer);
    }

    public static Object invoke(Method method, Object owner, Object... args) {
        try {
            return method.invoke(owner, args);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke " + method, exception);
        }
    }

    public static Object get(Field field, Object owner) {
        try {
            return field.get(owner);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to read " + field, exception);
        }
    }

    public static Class<?> requireClass(String name) {
        try {
            return Class.forName(name, false, NamedRuntimeCompat.class.getClassLoader());
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Missing named runtime class " + name, exception);
        }
    }

    public static Field requireField(Class<?> type, String... names) {
        for (String name : names) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
            try {
                Field field = type.getField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new IllegalStateException("Missing field on " + type.getName());
    }

    public static Method requireMethod(Class<?> type, Class<?>[] parameterTypes, String... names) {
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
        throw new IllegalStateException("Missing method on " + type.getName());
    }
}
