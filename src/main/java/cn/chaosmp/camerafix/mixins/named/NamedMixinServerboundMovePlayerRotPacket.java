package cn.chaosmp.camerafix.mixins.named;

import cn.chaosmp.camerafix.Main;
import cn.chaosmp.camerafix.util.ProtocolPackets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Rot", remap = false)
public class NamedMixinServerboundMovePlayerRotPacket {
    @ModifyArg(
            method = "write(Lnet/minecraft/network/FriendlyByteBuf;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/FriendlyByteBuf;writeFloat(F)Lnet/minecraft/network/FriendlyByteBuf;",
                    ordinal = 0,
                    remap = false
            ),
            index = 0,
            remap = false
    )
    private float camerafix$writeYaw(float original) {
        return rewriteRotation(original, true);
    }

    @ModifyArg(
            method = "write(Lnet/minecraft/network/FriendlyByteBuf;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/FriendlyByteBuf;writeFloat(F)Lnet/minecraft/network/FriendlyByteBuf;",
                    ordinal = 1,
                    remap = false
            ),
            index = 0,
            remap = false
    )
    private float camerafix$writePitch(float original) {
        return rewriteRotation(original, false);
    }

    @Inject(method = "write(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("RETURN"), remap = false)
    private void camerafix$clearMoveRotation(@Coerce Object buf, CallbackInfo ci) {
        ProtocolPackets.clearMoveRotation(this);
    }

    private float rewriteRotation(float original, boolean yaw) {
        if (!Main.shouldUseProtocol()) {
            return original;
        }
        ProtocolPackets.MoveRotation rotation = ProtocolPackets.getMoveRotation(this);
        if (rotation == null) {
            return original;
        }
        float replacement = yaw ? rotation.yaw() : rotation.pitch();
        if (Float.isNaN(replacement) || Math.abs(original - replacement) <= 0.1f) {
            return original;
        }
        return replacement;
    }
}
