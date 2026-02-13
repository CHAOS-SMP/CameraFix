package cn.chaosmp.camerafix.util;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

import java.lang.reflect.Field;

public final class Insecure {
    public static Field HORIZONAL_COLLISION;

    static {
        try {
            Insecure.HORIZONAL_COLLISION = ServerboundMovePlayerPacket.class.getDeclaredField("horizontalCollision");
        } catch (Throwable ignored) {
            Insecure.HORIZONAL_COLLISION = null;
        }
    }

    public static boolean hasHorizontalCollision() {
        return Insecure.HORIZONAL_COLLISION != null;
    }

    public static int packFlags(boolean bl) {
        return bl ? 1 : 0;
    }

    public static int packFlags0(boolean bl, boolean bl2) {
        int i = 0;
        if (bl) i |= 1;
        if (bl2) i |= 2;
        return i;
    }
}
