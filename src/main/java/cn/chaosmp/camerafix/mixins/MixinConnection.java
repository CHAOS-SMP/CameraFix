package cn.chaosmp.camerafix.mixins;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static cn.chaosmp.camerafix.Main.SENT_POS_SYNC;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class MixinConnection  {

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if(packet instanceof ServerboundMovePlayerPacket){
            if(!SENT_POS_SYNC.compareAndSet(true,false)){
                ci.cancel();
                return;
            }
        }
    }

}
