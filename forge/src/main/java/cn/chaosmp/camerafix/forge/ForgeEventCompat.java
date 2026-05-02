package cn.chaosmp.camerafix.forge;

import cn.chaosmp.camerafix.util.ReflectionCompat;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;

import java.lang.reflect.Field;
import java.util.function.Consumer;

final class ForgeEventCompat {
    private ForgeEventCompat() {
    }

    static void addClientJoinListener(Consumer<ClientPlayerNetworkEvent.LoggingIn> listener) {
        addListener(ClientPlayerNetworkEvent.LoggingIn.class, listener);
    }

    static void addRegisterClientCommandsListener(Consumer<RegisterClientCommandsEvent> listener) {
        addListener(RegisterClientCommandsEvent.class, listener);
    }

    private static void addListener(Class<?> eventClass, Consumer<?> listener) {
        Object bus = findEventBus(eventClass);
        if (bus == null) {
            throw new IllegalStateException("Unable to find Forge event bus for " + eventClass.getName());
        }
        ReflectionCompat.addListener(bus, listener);
    }

    private static Object findEventBus(Class<?> eventClass) {
        Field eventBusField = ReflectionCompat.findStaticField(eventClass, "BUS");
        if (eventBusField != null) {
            return ReflectionCompat.get(eventBusField, null);
        }

        Class<?> minecraftForge = ReflectionCompat.findClass("net.minecraftforge.common.MinecraftForge");
        Field legacyBusField = ReflectionCompat.findStaticField(minecraftForge, "EVENT_BUS");
        if (legacyBusField != null) {
            return ReflectionCompat.get(legacyBusField, null);
        }
        return null;
    }

}
