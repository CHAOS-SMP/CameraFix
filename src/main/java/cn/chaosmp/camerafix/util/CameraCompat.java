package cn.chaosmp.camerafix.util;

import cn.chaosmp.camerafix.mixins.CameraAccessor;
import net.minecraft.client.Camera;

public final class CameraCompat {
    private CameraCompat() {
    }

    public static float yRot(Camera camera) {
        return ((CameraAccessor) camera).camerafix$getYRot();
    }

    public static float xRot(Camera camera) {
        return ((CameraAccessor) camera).camerafix$getXRot();
    }
}
