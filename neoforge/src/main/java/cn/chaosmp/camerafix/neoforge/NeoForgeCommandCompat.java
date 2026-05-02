package cn.chaosmp.camerafix.neoforge;

import cn.chaosmp.camerafix.Main;
import cn.chaosmp.camerafix.util.ReflectionCompat;
import com.mojang.brigadier.CommandDispatcher;

import java.lang.reflect.Method;

final class NeoForgeCommandCompat {
    private NeoForgeCommandCompat() {
    }

    static void registerFallback() {
        Class<?> handler = ReflectionCompat.findClass("net.neoforged.neoforge.client.ClientCommandHandler");
        Method getDispatcher = ReflectionCompat.findMethod(handler, CommandDispatcher.class, new Class<?>[0], "getDispatcher");
        if (getDispatcher == null) {
            return;
        }
        CommandDispatcher<?> dispatcher = ReflectionCompat.invoke(getDispatcher, null);
        if (dispatcher != null) {
            Main.registerClientCommand(dispatcher);
        }
    }
}
