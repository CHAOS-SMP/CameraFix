package cn.chaosmp.camerafix.util;


import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.resources.ResourceLocation;


public class ProtocolPackets {
    public static float YAW = Float.NaN; //global yaw; - ref
    public static float PITCH = Float.NaN; //global pitch; - ref
    public static boolean SNEAK = false;

    public static ServerboundCustomPayloadPacket wrapAsPlacementPayload(ServerboundUseItemOnPacket packet) {

        PlacePayload payload = new PlacePayload(
                packet,
                YAW,
                PITCH,
                SNEAK
        );

        return new ServerboundCustomPayloadPacket(payload);
    }
    public static ServerboundCustomPayloadPacket wrapAsInteractPayload(ServerboundUseItemPacket packet) {

        InteractPayload payload = new InteractPayload(
                packet,
                YAW,
                PITCH,
                SNEAK
        );

        return new ServerboundCustomPayloadPacket(payload);
    }

    public static void init() {
        PayloadTypeRegistry.playC2S().register(PlacePayload.TYPE, PlacePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PlacePayload.TYPE, PlacePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(InteractPayload.TYPE, InteractPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(InteractPayload.TYPE, InteractPayload.CODEC);
    }

    public record InteractPayload(ServerboundUseItemPacket packet, float yaw, float pitch,
                                  boolean sneak) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<InteractPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.tryBuild("neko", "interact"));

        public static final StreamCodec<FriendlyByteBuf, InteractPayload> CODEC =
                CustomPacketPayload.codec(InteractPayload::write, InteractPayload::read);

        public static InteractPayload read(FriendlyByteBuf buf) {
            ServerboundUseItemPacket decode = ServerboundUseItemPacket.STREAM_CODEC.cast().decode(buf);
            float yaw = buf.readFloat();
            float pitch = buf.readFloat();
            boolean sneak = buf.readBoolean();
            return new InteractPayload(decode, yaw, pitch, sneak);
        }

        public void write(FriendlyByteBuf buf) {
            ServerboundUseItemPacket.STREAM_CODEC.cast().encode(buf, packet);
            buf.writeFloat(yaw);
            buf.writeFloat(pitch);
            buf.writeBoolean(sneak);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record PlacePayload(ServerboundUseItemOnPacket packet,
            float yaw,
                               float pitch,
                               boolean sneak
    ) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<PlacePayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.tryBuild("neko", "place"));

        public static final StreamCodec<FriendlyByteBuf, PlacePayload> CODEC =
                CustomPacketPayload.codec(PlacePayload::write, PlacePayload::read);

        public static PlacePayload read(FriendlyByteBuf buf) {
            ServerboundUseItemOnPacket decode = ServerboundUseItemOnPacket.STREAM_CODEC.cast().decode(buf);
            float yaw = buf.readFloat();
            float pitch = buf.readFloat();
            boolean sneak = buf.readBoolean();
            return new PlacePayload(decode, yaw, pitch, sneak);
        }



        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public void write(FriendlyByteBuf buf) {
            ServerboundUseItemOnPacket.STREAM_CODEC.cast().encode(buf,packet);
            buf.writeFloat(yaw);
            buf.writeFloat(pitch);
            buf.writeBoolean(sneak);
        }
    }
}