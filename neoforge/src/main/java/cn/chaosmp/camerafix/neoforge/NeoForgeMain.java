package cn.chaosmp.camerafix.neoforge;

import cn.chaosmp.camerafix.Main;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(Main.MOD_ID)
public final class NeoForgeMain {
    public NeoForgeMain(IEventBus modBus) {
        Main.logBanner();

        NeoForgeProtocolPackets.initSender();

        modBus.addListener(this::onRegisterPayloadHandlers);
        NeoForge.EVENT_BUS.addListener(this::onClientJoin);
        NeoForge.EVENT_BUS.addListener(this::onRegisterClientCommands);
    }

    private void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        NeoForgeProtocolPackets.register(event);
    }

    private void onClientJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        Main.onClientJoin();
        NeoForgeCommandCompat.registerFallback();
    }

    private void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        Main.registerClientCommand(dispatcher);
    }
}
