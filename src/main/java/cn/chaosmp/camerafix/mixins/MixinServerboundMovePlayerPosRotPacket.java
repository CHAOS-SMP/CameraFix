package cn.chaosmp.camerafix.mixins;

import cn.chaosmp.camerafix.Main;
import cn.chaosmp.camerafix.util.ProtocolPackets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundMovePlayerPacket.PosRot.class)
public class MixinServerboundMovePlayerPosRotPacket {
    @ModifyArg(
            method = "write",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/FriendlyByteBuf;writeFloat(F)Lnet/minecraft/network/FriendlyByteBuf;",
                    ordinal = 0
            ),
            index = 0
    )
    private float writeYaw(float original) {
        return rewriteRotation(original, true);
    }

    @ModifyArg(
            method = "write",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/FriendlyByteBuf;writeFloat(F)Lnet/minecraft/network/FriendlyByteBuf;",
                    ordinal = 1
            ),
            index = 0
    )
    private float writePitch(float original) {
        return rewriteRotation(original, false);
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void clearMoveRotation(FriendlyByteBuf buf, CallbackInfo ci) {
        ProtocolPackets.clearMoveRotation((ServerboundMovePlayerPacket) (Object) this);
    }

    private float rewriteRotation(float original, boolean yaw) {
        if (!Main.shouldUseProtocol()) {
            return original;
        }
        ServerboundMovePlayerPacket bridge = (ServerboundMovePlayerPacket) (Object) this;
        ProtocolPackets.MoveRotation rotation = ProtocolPackets.getMoveRotation(bridge);
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
