package cn.chaosmp.camerafix.fabric;

import com.mojang.brigadier.CommandDispatcher;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;

final class FabricEventCompat {
    private FabricEventCompat() {
    }

    static void registerJoin(Runnable callback) {
        registerEvent(
                "net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents",
                "JOIN",
                "net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents$Join",
                proxyArgs -> {
                    callback.run();
                    return null;
                }
        );
    }

    @SuppressWarnings("unchecked")
    static void registerClientCommand(Consumer<CommandDispatcher<?>> callback) {
        registerEvent(
                "net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback",
                "EVENT",
                "net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback",
                proxyArgs -> {
                    callback.accept((CommandDispatcher<?>) proxyArgs[0]);
                    return null;
                }
        );
    }

    private static void registerEvent(String ownerClassName, String fieldName, String callbackInterfaceName, Callback callback) {
        try {
            Class<?> ownerClass = Class.forName(ownerClassName, false, FabricEventCompat.class.getClassLoader());
            Field field = ownerClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object event = field.get(null);

            Method register = findRegisterMethod(event.getClass());
            Class<?> callbackType = Class.forName(callbackInterfaceName, false, FabricEventCompat.class.getClassLoader());
            if (!callbackType.isInterface()) {
                throw new IllegalStateException("Fabric callback target is not an interface: " + callbackInterfaceName);
            }
            Object listener = Proxy.newProxyInstance(
                    FabricEventCompat.class.getClassLoader(),
                    new Class<?>[]{callbackType},
                    new EventHandler(callback)
            );
            register.invoke(event, listener);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to register Fabric event " + ownerClassName + "#" + fieldName, exception);
        }
    }

    private static Method findRegisterMethod(Class<?> type) {
        for (Method method : type.getMethods()) {
            if (method.getName().equals("register") && method.getParameterCount() == 1) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new IllegalStateException("Fabric event does not expose register(callback): " + type.getName());
    }

    @FunctionalInterface
    private interface Callback {
        Object invoke(Object[] args);
    }

    private record EventHandler(Callback callback) implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "toString" -> "CameraFixFabricEventProxy";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> null;
                };
            }
            return callback.invoke(args == null ? new Object[0] : args);
        }
    }
}
