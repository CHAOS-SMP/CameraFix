package cn.chaosmp.camerafix.util;

import java.util.Map;
import java.util.WeakHashMap;

public final class ProtocolPackets {
    public static float YAW = Float.NaN;
    public static float PITCH = Float.NaN;
    public static boolean SNEAK = false;
    private static final Map<Object, MoveRotation> MOVE_ROTATIONS = new WeakHashMap<>();

    public record MoveRotation(float yaw, float pitch) {
    }

    @FunctionalInterface
    public interface PacketSender {
        boolean send(Object useItemOn, Object useItem, float yaw, float pitch, boolean sneak);
    }

    public static PacketSender SEND;

    private ProtocolPackets() {
    }

    public static void initSender(PacketSender sender) {
        SEND = sender;
    }

    public static void syncMoveRotation(Object packet, float yaw, float pitch) {
        synchronized (MOVE_ROTATIONS) {
            MOVE_ROTATIONS.put(packet, new MoveRotation(yaw, pitch));
        }
    }

    public static MoveRotation getMoveRotation(Object packet) {
        synchronized (MOVE_ROTATIONS) {
            return MOVE_ROTATIONS.get(packet);
        }
    }

    public static void clearMoveRotation(Object packet) {
        synchronized (MOVE_ROTATIONS) {
            MOVE_ROTATIONS.remove(packet);
        }
    }
}
