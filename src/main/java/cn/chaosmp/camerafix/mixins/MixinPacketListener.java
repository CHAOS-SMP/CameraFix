package cn.chaosmp.camerafix.mixins;

import cn.chaosmp.camerafix.Main;
import cn.chaosmp.camerafix.util.CameraCompat;
import cn.chaosmp.camerafix.util.PacketCompat;
import cn.chaosmp.camerafix.util.ProtocolPackets;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class MixinPacketListener {

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        System.err.println("[CameraFix] MIXIN FIRED: " + packet.getClass().getSimpleName());
        if (!Main.shouldUseProtocol()) {
            return;
        }
        float yaw = ProtocolPackets.YAW;
        float pitch = ProtocolPackets.PITCH;

        if (packet instanceof ServerboundMovePlayerPacket) {
            ServerboundMovePlayerPacket movePacket = (ServerboundMovePlayerPacket) packet;
            if (movePacket.hasRotation()) {
                ProtocolPackets.YAW = movePacket.getYRot(Float.NaN);
                ProtocolPackets.PITCH = movePacket.getXRot(Float.NaN);
                Minecraft minecraft = Minecraft.getInstance();
                Camera camera = minecraft.gameRenderer.getMainCamera();
                ProtocolPackets.syncMoveRotation(movePacket, CameraCompat.yRot(camera), CameraCompat.xRot(camera));
                return;
            }
        }
        if (packet instanceof ServerboundPlayerInputPacket e) {
            ProtocolPackets.SNEAK = PacketCompat.readSneaking(e);
        }
        boolean shouldRewrite = !Float.isNaN(yaw) && !Float.isNaN(pitch);
        if (packet instanceof ServerboundUseItemOnPacket e) {
            if (shouldRewrite && ProtocolPackets.SEND != null) {
                System.err.println("[CameraFix] INTERCEPT PLACE: yaw=" + yaw + " pitch=" + pitch + " sneak=" + ProtocolPackets.SNEAK);
                if (ProtocolPackets.SEND.send(e, null, yaw, pitch, ProtocolPackets.SNEAK)) {
                    ci.cancel();
                }
                return;
            }
        }
        if (packet instanceof ServerboundUseItemPacket e) {
            if (shouldRewrite && ProtocolPackets.SEND != null) {
                if (ProtocolPackets.SEND.send(null, e, yaw, pitch, ProtocolPackets.SNEAK)) {
                    ci.cancel();
                }
            }
        }
    }
}
