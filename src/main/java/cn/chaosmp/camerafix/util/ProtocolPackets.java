package cn.chaosmp.camerafix.util;

import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;

import java.util.Map;
import java.util.WeakHashMap;

public final class ProtocolPackets {
    public static float YAW = Float.NaN;
    public static float PITCH = Float.NaN;
    public static boolean SNEAK = false;
    private static final Map<ServerboundMovePlayerPacket, MoveRotation> MOVE_ROTATIONS = new WeakHashMap<>();

    public record MoveRotation(float yaw, float pitch) {
    }

    @FunctionalInterface
    public interface PacketSender {
        boolean send(ServerboundUseItemOnPacket useItemOn, ServerboundUseItemPacket useItem, float yaw, float pitch, boolean sneak);
    }

    public static PacketSender SEND;

    private ProtocolPackets() {
    }

    public static void initSender(PacketSender sender) {
        SEND = sender;
    }

    public static void syncMoveRotation(ServerboundMovePlayerPacket packet, float yaw, float pitch) {
        synchronized (MOVE_ROTATIONS) {
            MOVE_ROTATIONS.put(packet, new MoveRotation(yaw, pitch));
        }
    }

    public static MoveRotation getMoveRotation(ServerboundMovePlayerPacket packet) {
        synchronized (MOVE_ROTATIONS) {
            return MOVE_ROTATIONS.get(packet);
        }
    }

    public static void clearMoveRotation(ServerboundMovePlayerPacket packet) {
        synchronized (MOVE_ROTATIONS) {
            MOVE_ROTATIONS.remove(packet);
        }
    }
}
