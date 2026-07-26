package cn.chaosmp.camerafix.mixins.named;

import cn.chaosmp.camerafix.Main;
import cn.chaosmp.camerafix.fabric.NamedRuntimeCompat;
import cn.chaosmp.camerafix.util.CameraCompat;
import cn.chaosmp.camerafix.util.PacketCompat;
import cn.chaosmp.camerafix.util.ProtocolPackets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl", remap = false)
public abstract class NamedMixinPacketListener {
    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void camerafix$onSendPacket(@Coerce Object packet, CallbackInfo ci) {
        if (!Main.shouldUseProtocol()) {
            return;
        }

        float yaw = ProtocolPackets.YAW;
        float pitch = ProtocolPackets.PITCH;

        if (NamedRuntimeCompat.isMovePacket(packet) && NamedRuntimeCompat.hasRotation(packet)) {
            ProtocolPackets.YAW = NamedRuntimeCompat.getYRot(packet);
            ProtocolPackets.PITCH = NamedRuntimeCompat.getXRot(packet);
            Object camera = NamedRuntimeCompat.mainCamera();
            ProtocolPackets.syncMoveRotation(packet, CameraCompat.yRot(camera), CameraCompat.xRot(camera));
            return;
        }

        if (NamedRuntimeCompat.isPlayerInputPacket(packet)) {
            ProtocolPackets.SNEAK = PacketCompat.readSneaking(packet);
        }

        boolean shouldRewrite = !Float.isNaN(yaw) && !Float.isNaN(pitch);
        if (NamedRuntimeCompat.isUseItemOnPacket(packet)) {
            if (shouldRewrite && ProtocolPackets.SEND != null && ProtocolPackets.SEND.send(packet, null, yaw, pitch, ProtocolPackets.SNEAK)) {
                ci.cancel();
            }
            return;
        }

        if (NamedRuntimeCompat.isUseItemPacket(packet)) {
            if (shouldRewrite && ProtocolPackets.SEND != null && ProtocolPackets.SEND.send(null, packet, yaw, pitch, ProtocolPackets.SNEAK)) {
                ci.cancel();
            }
        }
    }
}
