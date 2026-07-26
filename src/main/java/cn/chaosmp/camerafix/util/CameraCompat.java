package cn.chaosmp.camerafix.util;

import cn.chaosmp.camerafix.mixins.CameraAccessor;

import java.lang.reflect.Method;

public final class CameraCompat {
    private static final Method Y_ROT_METHOD = findNoArgFloatMethod("yRot", "getYRot", "yaw", "method_19329");
    private static final Method X_ROT_METHOD = findNoArgFloatMethod("xRot", "getXRot", "pitch", "method_19330");

    private CameraCompat() {
    }

    public static float yRot(Object camera) {
        if (camera instanceof CameraAccessor accessor) {
            return accessor.camerafix$getYRot();
        }
        return invokeFloat(Y_ROT_METHOD, camera, "camera yRot");
    }

    public static float xRot(Object camera) {
        if (camera instanceof CameraAccessor accessor) {
            return accessor.camerafix$getXRot();
        }
        return invokeFloat(X_ROT_METHOD, camera, "camera xRot");
    }

    private static Method findNoArgFloatMethod(String... names) {
        Class<?> cameraClass = ReflectionCompat.findClass("net.minecraft.client.Camera", "net.minecraft.class_4184");
        if (cameraClass == null) {
            return null;
        }
        for (String name : names) {
            try {
                Method method = cameraClass.getDeclaredMethod(name);
                if (method.getReturnType() == float.class) {
                    method.setAccessible(true);
                    return method;
                }
            } catch (NoSuchMethodException ignored) {
            }
            try {
                Method method = cameraClass.getMethod(name);
                if (method.getReturnType() == float.class) {
                    method.setAccessible(true);
                    return method;
                }
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }

    private static float invokeFloat(Method method, Object owner, String description) {
        if (method == null) {
            throw new IllegalStateException("Missing " + description + " accessor");
        }
        try {
            return ((Float) method.invoke(owner)).floatValue();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to read " + description, exception);
        }
    }
}
