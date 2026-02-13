package cn.chaosmp.camerafix.mixins;

import cn.chaosmp.camerafix.Main;
import cn.chaosmp.camerafix.util.ProtocolPackets;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static cn.chaosmp.camerafix.Main.SENT_POS_SYNC;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class MixinPacketListener {

    @Shadow
    public abstract void send(Packet<?> packet);

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (!Main.shouldUseProtocol()) {
            return;
        }
        if (packet instanceof ServerboundMovePlayerPacket) {
            if (((ServerboundMovePlayerPacket) packet).hasRotation()) {
                ProtocolPackets.YAW = (((ServerboundMovePlayerPacket) packet).getYRot(Float.NaN));
                ProtocolPackets.PITCH = ((ServerboundMovePlayerPacket) packet).getXRot(Float.NaN);
                return;
            }
            if (!SENT_POS_SYNC.compareAndSet(true, false)) {
                ci.cancel();
                return;
            }
        }
        if (packet instanceof ServerboundPlayerInputPacket e) {
            ProtocolPackets.SNEAK = e.input().shift();
        }
        if (packet instanceof ServerboundUseItemOnPacket e) {
            float yaw = ProtocolPackets.YAW;
            float pitch = ProtocolPackets.PITCH;
            if (!Float.isNaN(yaw) && !Float.isNaN(pitch)) {
                send(ProtocolPackets.sendPlacePayload(e.getHitResult(), e.getHand() == InteractionHand.MAIN_HAND, e.getSequence(), System.currentTimeMillis(), yaw, pitch, ProtocolPackets.SNEAK));
                ci.cancel();
            }
        }
        if (packet instanceof ServerboundUseItemPacket e) {
            float yaw = ProtocolPackets.YAW;
            float pitch = ProtocolPackets.PITCH;
            if (!Float.isNaN(yaw) && !Float.isNaN(pitch)) {
                send(new ServerboundCustomPayloadPacket(new ProtocolPackets.InteractPayload(e, yaw, pitch, ProtocolPackets.SNEAK)));
                ci.cancel();
            }
        }
    }
}
