package cn.chaosmp.camerafix.fabric;

import cn.chaosmp.camerafix.util.PayloadIdCompat;
import cn.chaosmp.camerafix.util.ProtocolPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;

final class FabricProtocolPackets {
    static final CustomPacketPayload.Type<PlacePayload> PLACE_TYPE = PayloadIdCompat.createNekoType("place");
    static final CustomPacketPayload.Type<InteractPayload> INTERACT_TYPE = PayloadIdCompat.createNekoType("interact");

    private FabricProtocolPackets() {
    }

    static void register() {
        PayloadTypeRegistry.playC2S().register(PLACE_TYPE, PlacePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(INTERACT_TYPE, InteractPayload.CODEC);
    }

    static void initSender() {
        ProtocolPackets.initSender((useItemOn, useItem, yaw, pitch, sneak) -> {
            if (useItemOn != null) {
                ClientPlayNetworking.send(new PlacePayload(useItemOn, yaw, pitch, sneak));
                return true;
            } else if (useItem != null) {
                ClientPlayNetworking.send(new InteractPayload(useItem, yaw, pitch, sneak));
                return true;
            }
            return false;
        });
    }

    record InteractPayload(ServerboundUseItemPacket packet, float yaw, float pitch, boolean sneak)
            implements CustomPacketPayload {

        static final StreamCodec<FriendlyByteBuf, InteractPayload> CODEC =
                CustomPacketPayload.codec(InteractPayload::write, InteractPayload::read);

        static InteractPayload read(FriendlyByteBuf buf) {
            ServerboundUseItemPacket packet = ServerboundUseItemPacket.STREAM_CODEC.cast().decode(buf);
            float yaw = buf.readFloat();
            float pitch = buf.readFloat();
            boolean sneak = buf.readBoolean();
            return new InteractPayload(packet, yaw, pitch, sneak);
        }

        void write(FriendlyByteBuf buf) {
            ServerboundUseItemPacket.STREAM_CODEC.cast().encode(buf, packet);
            buf.writeFloat(yaw);
            buf.writeFloat(pitch);
            buf.writeBoolean(sneak);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return INTERACT_TYPE;
        }
    }

    record PlacePayload(ServerboundUseItemOnPacket packet, float yaw, float pitch, boolean sneak)
            implements CustomPacketPayload {

        static final StreamCodec<FriendlyByteBuf, PlacePayload> CODEC =
                CustomPacketPayload.codec(PlacePayload::write, PlacePayload::read);

        static PlacePayload read(FriendlyByteBuf buf) {
            ServerboundUseItemOnPacket packet = ServerboundUseItemOnPacket.STREAM_CODEC.cast().decode(buf);
            float yaw = buf.readFloat();
            float pitch = buf.readFloat();
            boolean sneak = buf.readBoolean();
            return new PlacePayload(packet, yaw, pitch, sneak);
        }

        void write(FriendlyByteBuf buf) {
            ServerboundUseItemOnPacket.STREAM_CODEC.cast().encode(buf, packet);
            buf.writeFloat(yaw);
            buf.writeFloat(pitch);
            buf.writeBoolean(sneak);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return PLACE_TYPE;
        }
    }
}
