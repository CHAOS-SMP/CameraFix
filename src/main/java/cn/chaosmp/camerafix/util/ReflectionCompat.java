package cn.chaosmp.camerafix.util;

import cn.dg32z.nekoreflection.NekoReflection;
import cn.dg32z.nekoreflection.matcher.FieldMatchers;
import cn.dg32z.nekoreflection.matcher.MethodMatcher;
import cn.dg32z.nekoreflection.matcher.MethodMatchers;
import cn.dg32z.nekoreflection.wrapper.ReflectionClass;
import cn.dg32z.nekoreflection.wrapper.ReflectionMethod;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class ReflectionCompat {
    private ReflectionCompat() {
    }

    public static Class<?> findClass(String... names) {
        return NekoReflection.findClass(names);
    }

    public static Field findStaticField(Class<?> type, String... names) {
        ReflectionClass<?> reflectionClass = NekoReflection.ofNullable(type);
        if (reflectionClass == null) {
            return null;
        }
        return reflectionClass.findDeclaredField(FieldMatchers.named(names).and(FieldMatchers.isStatic()));
    }

    public static Field findFieldInHierarchy(Class<?> type, Class<?> fieldType, String... names) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            ReflectionClass<?> reflectionClass = NekoReflection.ofNullable(current);
            if (reflectionClass == null) {
                continue;
            }
            Field field = reflectionClass.findDeclaredField(FieldMatchers.named(names).and(FieldMatchers.type(fieldType)));
            if (field != null) {
                return field;
            }
        }
        return null;
    }

    public static Method findMethod(Class<?> type, Class<?> returnType, Class<?>[] parameterTypes, String... names) {
        return findMethod(type, false, returnType, parameterTypes, names);
    }

    public static Method findDeclaredMethod(Class<?> type, Class<?> returnType, Class<?>[] parameterTypes, String... names) {
        return findMethod(type, true, returnType, parameterTypes, names);
    }

    private static Method findMethod(Class<?> type, boolean declared, Class<?> returnType, Class<?>[] parameterTypes, String... names) {
        ReflectionClass<?> reflectionClass = NekoReflection.ofNullable(type);
        if (reflectionClass == null) {
            return null;
        }
        MethodMatcher matcher = MethodMatchers.named(names).and(MethodMatchers.parameterTypes(parameterTypes));
        if (returnType != null) {
            matcher = matcher.and(MethodMatchers.returnType(returnType));
        }
        return declared ? reflectionClass.findDeclaredMethod(matcher) : reflectionClass.findMethod(matcher);
    }

    public static Method findDeclaredNoArgMethodInHierarchy(Class<?> type, String... names) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            Method method = findDeclaredMethod(current, null, new Class<?>[0], names);
            if (method != null) {
                return method;
            }
        }
        return null;
    }

    public static MethodHandle handle(Method method) {
        ReflectionMethod reflectionMethod = ReflectionMethod.ofNullable(method);
        return reflectionMethod == null ? null : reflectionMethod.handle();
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(Method method, Object owner, Object... args) {
        return NekoReflection.instance().reflectionManager().invokeMethod(method, owner, args);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Field field, Object owner) {
        return NekoReflection.instance().reflectionManager().getFieldValue(field, owner);
    }

    @SuppressWarnings("unchecked")
    public static <T> T invokeHandle(MethodHandle handle, Object... args) {
        try {
            return (T) handle.invokeWithArguments(args);
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to invoke method handle", throwable);
        }
    }

    public static void addListener(Object bus, Consumer<?> listener) {
        Method addListener = findMethod(bus.getClass(), null, new Class<?>[]{Consumer.class}, "addListener");
        if (addListener == null) {
            throw new IllegalStateException("Event bus does not expose addListener(Consumer): " + bus.getClass().getName());
        }
        invoke(addListener, bus, listener);
    }
}
