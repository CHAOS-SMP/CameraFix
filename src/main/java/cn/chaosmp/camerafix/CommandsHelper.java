package cn.chaosmp.camerafix;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import java.lang.reflect.Method;

public final class CommandsHelper {
    private static final Method LITERAL_METHOD = findLiteralMethod();
    private static final Method ARGUMENT_METHOD = findArgumentMethod();

    private CommandsHelper() {
    }

    public static LiteralArgumentBuilder<?> literal(String name) {
        try {
            return (LiteralArgumentBuilder<?>) LITERAL_METHOD.invoke(null, name);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create literal command", exception);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> RequiredArgumentBuilder<?, T> argument(String name, T type) {
        try {
            return (RequiredArgumentBuilder<?, T>) ARGUMENT_METHOD.invoke(null, name, type);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create argument", exception);
        }
    }

    private static Method findLiteralMethod() {
        Class<?> commandsClass = findClass("net.minecraft.commands.Commands", "net.minecraft.class_2170");
        if (commandsClass == null) {
            throw new IllegalStateException("Commands class not found");
        }
        for (String name : new String[]{"literal", "method_9247"}) {
            try {
                Method method = commandsClass.getDeclaredMethod(name, String.class);
                method.setAccessible(true);
                if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) return method;
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new IllegalStateException("Commands.literal() not found");
    }

    private static Method findArgumentMethod() {
        Class<?> commandsClass = findClass("net.minecraft.commands.Commands", "net.minecraft.class_2170");
        if (commandsClass == null) {
            throw new IllegalStateException("Commands class not found");
        }
        Class<?> argumentTypeClass = findClass("com.mojang.brigadier.arguments.ArgumentType");
        if (argumentTypeClass == null) {
            throw new IllegalStateException("ArgumentType class not found");
        }
        for (String name : new String[]{"argument", "method_9244"}) {
            try {
                Method method = commandsClass.getDeclaredMethod(name, String.class, argumentTypeClass);
                method.setAccessible(true);
                if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) return method;
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new IllegalStateException("Commands.argument() not found");
    }

    private static Class<?> findClass(String... names) {
        for (String name : names) {
            try {
                return Class.forName(name, false, CommandsHelper.class.getClassLoader());
            } catch (ClassNotFoundException ignored) {
            }
        }
        return null;
    }
}
