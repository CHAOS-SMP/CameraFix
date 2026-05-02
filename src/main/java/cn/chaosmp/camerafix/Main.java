package cn.chaosmp.camerafix;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class Main {
    public static final String MOD_ID = "camerafix";
    public static final String MOD_NAME = "CameraFix";
    public static volatile boolean ENABLE_HOOK = true;

    private Main() {
    }

    public static boolean shouldUseProtocol() {
        return ENABLE_HOOK && !Minecraft.getInstance().isSingleplayer();
    }

    public static void logBanner() {
        System.out.println("COPYRIGHT 2026@CHAOS-SMP");
        System.out.println("POWERED BY CHAOS-SMP (chaos-smp.cn)");
    }

    public static void onClientJoin() {
        Minecraft.getInstance().getChatListener().handleSystemMessage(Component.literal("COPYRIGHT 2026@CHAOS-SMP"), false);
        Minecraft.getInstance().getChatListener().handleSystemMessage(Component.literal("POWERED BY CHAOS-SMP (chaos-smp.cn)"), false);
    }

    public static String setHookEnabled(boolean enabled) {
        ENABLE_HOOK = enabled;
        return enabled ? "ON" : "OFF";
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void registerClientCommand(CommandDispatcher<?> dispatcher) {
        if (dispatcher.getRoot().getChild("cmr") != null) {
            return;
        }
        ((CommandDispatcher) dispatcher).register(Commands.literal("cmr").then(Commands.argument("value", BoolArgumentType.bool()).executes(context -> {
            boolean flag = BoolArgumentType.getBool(context, "value");
            String status = setHookEnabled(flag);
            Minecraft.getInstance().getChatListener().handleSystemMessage(Component.literal(status), false);
            return Command.SINGLE_SUCCESS;
        })));
    }
}

