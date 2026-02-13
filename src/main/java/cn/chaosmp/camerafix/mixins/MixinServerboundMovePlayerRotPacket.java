package cn.chaosmp.camerafix.mixins;

import cn.chaosmp.camerafix.Main;
import cn.chaosmp.camerafix.mixins.brigde.ServerboundMovePlayerPacketBridge;
import cn.chaosmp.camerafix.util.Insecure;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundMovePlayerPacket.Rot.class)
public class MixinServerboundMovePlayerRotPacket {
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
        if (!Main.shouldUseProtocol()) {
            return;
        }
        ServerboundMovePlayerPacketBridge bridge = (ServerboundMovePlayerPacketBridge) this;
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

        if (Math.abs(bridge.getYRot() - camera.getYRot()) <= 0.1f &&
                Math.abs(bridge.getXRot() - camera.getXRot()) <= 0.1f
        ) {
            return;
        }

        friendlyByteBuf.writeFloat(camera.getYRot());
        friendlyByteBuf.writeFloat(camera.getXRot());

        if (Insecure.hasHorizontalCollision()) {
            try {
                friendlyByteBuf.writeByte(Insecure.packFlags0(
                        bridge.onGround(),
                        (boolean) Insecure.HORIZONAL_COLLISION.get(this)
                ));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else
            friendlyByteBuf.writeByte(Insecure.packFlags(bridge.onGround()));

        ci.cancel();
    }
}
