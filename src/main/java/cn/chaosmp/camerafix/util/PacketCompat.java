package cn.chaosmp.camerafix.util;

import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class PacketCompat {
    private static final Field MOVE_HORIZONTAL_COLLISION_FIELD = ReflectionCompat.findFieldInHierarchy(ServerboundMovePlayerPacket.class, boolean.class, "horizontalCollision");
    private static final Method SHIFT_KEY_METHOD = ReflectionCompat.findDeclaredNoArgMethodInHierarchy(ServerboundPlayerInputPacket.class, "isShiftKeyDown", "method_12370");
    private static final MethodHandle SHIFT_KEY_HANDLE = ReflectionCompat.handle(SHIFT_KEY_METHOD);
    private static final Method INPUT_METHOD = ReflectionCompat.findDeclaredNoArgMethodInHierarchy(ServerboundPlayerInputPacket.class, "input", "comp_3139");
    private static final MethodHandle INPUT_HANDLE = ReflectionCompat.handle(INPUT_METHOD);
    private static final Method INPUT_SHIFT_METHOD = INPUT_METHOD == null ? null : ReflectionCompat.findDeclaredNoArgMethodInHierarchy(INPUT_METHOD.getReturnType(), "shift", "comp_3164");
    private static final MethodHandle INPUT_SHIFT_HANDLE = ReflectionCompat.handle(INPUT_SHIFT_METHOD);

    private PacketCompat() {
    }

    public static boolean readSneaking(ServerboundPlayerInputPacket packet) {
        if (SHIFT_KEY_HANDLE != null) {
            return ReflectionCompat.invokeHandle(SHIFT_KEY_HANDLE, packet);
        }
        if (INPUT_HANDLE != null && INPUT_SHIFT_HANDLE != null) {
            Object input = ReflectionCompat.invokeHandle(INPUT_HANDLE, packet);
            return ReflectionCompat.invokeHandle(INPUT_SHIFT_HANDLE, input);
        }
        return false;
    }

    public static boolean hasHorizontalCollisionFlag() {
        return MOVE_HORIZONTAL_COLLISION_FIELD != null;
    }

    public static boolean horizontalCollision(ServerboundMovePlayerPacket packet) {
        if (MOVE_HORIZONTAL_COLLISION_FIELD == null) {
            return false;
        }
        return ReflectionCompat.get(MOVE_HORIZONTAL_COLLISION_FIELD, packet);
    }

    public static int packFlags(boolean onGround) {
        return onGround ? 1 : 0;
    }

    public static int packFlags(boolean onGround, boolean horizontalCollision) {
        int flags = packFlags(onGround);
        if (horizontalCollision) {
            flags |= 2;
        }
        return flags;
    }

}
