package cn.chaosmp.camerafix;

import cn.chaosmp.camerafix.util.ProtocolPackets;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.concurrent.atomic.AtomicBoolean;

public class Main implements ModInitializer {
    public static final AtomicBoolean SENT_POS_SYNC = new AtomicBoolean(true);
    public static boolean ENABLE_HOOK = true;

    public static boolean shouldUseProtocol() {
        return ENABLE_HOOK && !Minecraft.getInstance().isSingleplayer();
    }

    @Override
    public void onInitialize() {
        System.out.println("COPYRIGHT 2026@CHAOS-SMP");
        System.out.println("POWERED BY CHAOS-SMP (chaos-smp.cn)");
        ProtocolPackets.init();
        ClientTickEvents.START_CLIENT_TICK.register(i -> {
            SENT_POS_SYNC.set(true);
        });
        ClientPlayConnectionEvents.JOIN.register((join, a, v) -> {
            Minecraft.getInstance().getChatListener().handleSystemMessage(Component.literal("COPYRIGHT 2026@CHAOS-SMP"), false);
            Minecraft.getInstance().getChatListener().handleSystemMessage(Component.literal("POWERED BY CHAOS-SMP (chaos-smp.cn)"), false);
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("cmr").then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                            .executes(context -> {
                                boolean flag = BoolArgumentType.getBool(context, "value");
                                ENABLE_HOOK = flag;
                                Minecraft.getInstance().getChatListener().handleSystemMessage(Component.literal(flag ? "ON" : "OFF"), false);
                                return Command.SINGLE_SUCCESS;
                            })
                    )
            );
        });
    }
}

