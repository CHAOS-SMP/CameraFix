package cn.chaosmp.camerafix.mixins;

import cn.chaosmp.camerafix.util.VanillaPayloads;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(ServerboundCustomPayloadPacket.class)
public abstract class MixinServerboundCustomPayloadPacket {
    @Shadow
    @Final
    @Mutable
    public static StreamCodec<FriendlyByteBuf, ServerboundCustomPayloadPacket> STREAM_CODEC;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void camerafix$installForgePayloadCodecs(CallbackInfo ci) {
        if (VanillaPayloads.isVanillaCodecRuntime()) {
            StreamCodec<FriendlyByteBuf, ServerboundCustomPayloadPacket> codec = VanillaPayloads.createServerboundPacketCodec();
            STREAM_CODEC = codec;
            setOptionalConfigStreamCodec(codec);
        }
    }

    private static void setOptionalConfigStreamCodec(StreamCodec<FriendlyByteBuf, ServerboundCustomPayloadPacket> codec) {
        try {
            Field field = ServerboundCustomPayloadPacket.class.getDeclaredField("CONFIG_STREAM_CODEC");
            field.setAccessible(true);
            field.set(null, codec);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
