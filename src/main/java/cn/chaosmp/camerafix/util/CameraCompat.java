package cn.chaosmp.camerafix.util;

import net.minecraft.client.Camera;

import java.lang.reflect.Method;

public final class CameraCompat {
    private static final Method Y_ROT = findFloatGetter("getYRot", "yRot", "yaw");
    private static final Method X_ROT = findFloatGetter("getXRot", "xRot");

    private CameraCompat() {
    }

    public static float yRot(Camera camera) {
        return invokeFloat(Y_ROT, camera);
    }

    public static float xRot(Camera camera) {
        return invokeFloat(X_ROT, camera);
    }

    private static Method findFloatGetter(String... names) {
        for (String name : names) {
            try {
                Method method = Camera.class.getDeclaredMethod(name);
                if (method.getReturnType() == float.class) {
                    method.setAccessible(true);
                    return method;
                }
            } catch (NoSuchMethodException ignored) {
            }
            try {
                Method method = Camera.class.getMethod(name);
                if (method.getReturnType() == float.class) {
                    method.setAccessible(true);
                    return method;
                }
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new IllegalStateException("Camera rotation getter is missing");
    }

    private static float invokeFloat(Method method, Camera camera) {
        try {
            return (float) method.invoke(camera);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to read camera rotation", exception);
        }
    }
}
