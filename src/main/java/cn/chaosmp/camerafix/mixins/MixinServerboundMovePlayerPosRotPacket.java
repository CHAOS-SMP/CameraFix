package cn.chaosmp.camerafix.mixins;

import cn.chaosmp.camerafix.mixins.brigde.ServerboundMovePlayerPacketBridge;
import cn.chaosmp.camerafix.util.PacketFlagUtil;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundMovePlayerPacket.PosRot.class)
public class MixinServerboundMovePlayerPosRotPacket {
    @Inject(
            method = "write",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/FriendlyByteBuf;writeFloat(F)Lnet/minecraft/network/FriendlyByteBuf;",
                    ordinal = 0
            ),
            cancellable = true
    )
    private void write(FriendlyByteBuf friendlyByteBuf, CallbackInfo ci) {
        ServerboundMovePlayerPacketBridge bridge = (ServerboundMovePlayerPacketBridge) this;
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

        if (Math.abs(bridge.getYRot() - camera.getYRot()) <= 0.1f &&
                Math.abs(bridge.getXRot() - camera.getXRot()) <= 0.1f
        ) {
            return;
        }

        friendlyByteBuf.writeFloat(camera.getYRot());
        friendlyByteBuf.writeFloat(camera.getXRot());

        if (PacketFlagUtil.hasHorizontalCollision()) {
            try {
                friendlyByteBuf.writeByte(PacketFlagUtil.ServerboundMovePlayerRotPacket$1_21_R2$packFlags(
                        bridge.onGround(),
                        (boolean) PacketFlagUtil.ServerboundMovePlayerPacket$field$HorizontalCollision.get(this)
                ));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else
            friendlyByteBuf.writeByte(PacketFlagUtil.ServerboundMovePlayerRotPacket$1_21_R1$packFlags(bridge.onGround()));

        ci.cancel();
    }
}
