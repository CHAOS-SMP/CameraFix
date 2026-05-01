package cn.chaosmp.camerafix.neoforge.mixins;

import cn.chaosmp.camerafix.Main;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.neoforged.neoforge.client.ClientCommandHandler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientCommandHandler.class, remap = false)
public abstract class MixinNeoForgeClientCommandHandler {
    @Inject(
            method = "mergeServerCommands",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/neoforged/neoforge/client/ClientCommandHandler;commands:Lcom/mojang/brigadier/CommandDispatcher;",
                    opcode = Opcodes.PUTSTATIC,
                    shift = At.Shift.AFTER
            )
    )
    private static void camerafix$registerClientCommand(
            CommandDispatcher<SharedSuggestionProvider> serverCommands,
            CommandBuildContext buildContext,
            CallbackInfoReturnable<CommandDispatcher<SharedSuggestionProvider>> cir
    ) {
        Main.registerClientCommand(ClientCommandHandler.getDispatcher());
    }
}
