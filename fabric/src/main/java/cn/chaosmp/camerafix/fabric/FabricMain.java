package cn.chaosmp.camerafix.fabric;

import cn.chaosmp.camerafix.Main;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public final class FabricMain implements ModInitializer {
    @Override
    public void onInitialize() {
        Main.logBanner();

        FabricProtocolPackets.register();
        FabricProtocolPackets.initSender();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> Main.onClientJoin());
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> Main.registerClientCommand(dispatcher));
    }
}
