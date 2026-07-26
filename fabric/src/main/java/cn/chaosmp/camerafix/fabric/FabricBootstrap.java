package cn.chaosmp.camerafix.fabric;

import cn.chaosmp.camerafix.Main;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;

public final class FabricBootstrap implements ModInitializer {
    @Override
    public void onInitialize() {
        Main.logBanner();

        switch (FabricRuntimeNamespace.detect()) {
            case INTERMEDIARY -> {
                FabricProtocolPackets.register();
                FabricProtocolPackets.initSender();
            }
            case NAMED -> {
                NamedFabricProtocolPackets.register();
                NamedFabricProtocolPackets.initSender();
            }
        }

        FabricEventCompat.registerJoin(Main::onClientJoin);
        FabricEventCompat.registerClientCommand(this::registerClientCommand);
    }

    private void registerClientCommand(CommandDispatcher<?> dispatcher) {
        Main.registerClientCommand(dispatcher);
    }
}
