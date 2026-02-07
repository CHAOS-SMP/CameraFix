package cn.chaosmp.camerafix.util;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

import java.lang.reflect.Field;

public final class PacketFlagUtil {
    public static Field ServerboundMovePlayerPacket$field$HorizontalCollision;

    static {
        try {
            PacketFlagUtil.ServerboundMovePlayerPacket$field$HorizontalCollision = ServerboundMovePlayerPacket.class.getDeclaredField("horizontalCollision");
        } catch (Throwable ignored) {
            PacketFlagUtil.ServerboundMovePlayerPacket$field$HorizontalCollision = null;
        }
    }

    public static boolean hasHorizontalCollision() {
        return PacketFlagUtil.ServerboundMovePlayerPacket$field$HorizontalCollision != null;
    }

    public static int ServerboundMovePlayerRotPacket$1_21_R1$packFlags(boolean bl) {
        return bl ? 1 : 0;
    }

    public static int ServerboundMovePlayerRotPacket$1_21_R2$packFlags(boolean bl, boolean bl2) {
        int i = 0;
        if (bl) i |= 1;
        if (bl2) i |= 2;
        return i;
    }
}
