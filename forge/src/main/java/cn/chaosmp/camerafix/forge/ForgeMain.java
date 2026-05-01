package cn.chaosmp.camerafix.forge;

import cn.chaosmp.camerafix.Main;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(Main.MOD_ID)
public final class ForgeMain {
    public ForgeMain() {
        Main.logBanner();

        ForgeProtocolPackets.initSender();

        ForgeEventCompat.addClientJoinListener(this::onClientJoin);
        ForgeEventCompat.addRegisterClientCommandsListener(this::onRegisterClientCommands);
    }

    private void onClientJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        Main.onClientJoin();
        ForgeCommandCompat.registerFallback();
    }

    private void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        Main.registerClientCommand(dispatcher);
    }
}
