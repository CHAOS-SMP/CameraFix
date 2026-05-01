package cn.chaosmp.camerafix.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public final class VanillaPayloads {
    public static final CustomPacketPayload.Type<PlacePayload> PLACE_TYPE =
            PayloadIdCompat.createNekoType("place");
    public static final CustomPacketPayload.Type<InteractPayload> INTERACT_TYPE =
            PayloadIdCompat.createNekoType("interact");

    private static final int MAX_PAYLOAD_SIZE = 32767;
    private static final boolean NEOFORGE_RUNTIME = ReflectionCompat.findClass("net.neoforged.neoforge.common.NeoForge") != null;

    private VanillaPayloads() {
    }

    public static boolean isForgeRuntime() {
        return PayloadIdCompat.hasForgeHooksCodec();
    }

    public static boolean isVanillaCodecRuntime() {
        return PayloadIdCompat.hasForgeHooksCodec() || NEOFORGE_RUNTIME;
    }

    public static StreamCodec<FriendlyByteBuf, ServerboundCustomPayloadPacket> createServerboundPacketCodec() {
        Map<Object, StreamCodec<? super FriendlyByteBuf, ? extends CustomPacketPayload>> codecs = Map.of(
                PayloadIdCompat.id(BrandPayload.TYPE), BrandPayload.STREAM_CODEC,
                PayloadIdCompat.id(PLACE_TYPE), PlacePayload.CODEC,
                PayloadIdCompat.id(INTERACT_TYPE), InteractPayload.CODEC
        );
        StreamCodec<FriendlyByteBuf, CustomPacketPayload> payloadCodec = new StreamCodec<>() {
            @Override
            public CustomPacketPayload decode(FriendlyByteBuf buf) {
                Object id = PayloadIdCompat.read(buf);
                return decodePayload(codecs, id, buf);
            }

            @Override
            public void encode(FriendlyByteBuf buf, CustomPacketPayload payload) {
                CustomPacketPayload.Type<? extends CustomPacketPayload> type = payload.type();
                Object id = PayloadIdCompat.id(type);
                PayloadIdCompat.write(buf, id);
                encodePayload(codecs, id, buf, payload);
            }
        };
        return payloadCodec.map(ServerboundCustomPayloadPacket::new, ServerboundCustomPayloadPacket::payload);
    }

    @SuppressWarnings("unchecked")
    private static CustomPacketPayload decodePayload(
            Map<Object, StreamCodec<? super FriendlyByteBuf, ? extends CustomPacketPayload>> codecs,
            Object id,
            FriendlyByteBuf buf
    ) {
        StreamCodec<? super FriendlyByteBuf, ? extends CustomPacketPayload> codec = codecs.get(id);
        if (codec != null) {
            return codec.decode(buf);
        }
        return fallbackCodec(id).decode(buf);
    }

    @SuppressWarnings("unchecked")
    private static void encodePayload(
            Map<Object, StreamCodec<? super FriendlyByteBuf, ? extends CustomPacketPayload>> codecs,
            Object id,
            FriendlyByteBuf buf,
            CustomPacketPayload payload
    ) {
        StreamCodec<? super FriendlyByteBuf, ? extends CustomPacketPayload> codec = codecs.get(id);
        if (codec == null) {
            codec = fallbackCodec(id);
        }
        ((StreamCodec<FriendlyByteBuf, CustomPacketPayload>) codec).encode(buf, payload);
    }

    @SuppressWarnings("unchecked")
    private static StreamCodec<FriendlyByteBuf, ? extends CustomPacketPayload> fallbackCodec(Object id) {
        StreamCodec<FriendlyByteBuf, ? extends CustomPacketPayload> forgeCodec = PayloadIdCompat.forgeHooksCodec(id, MAX_PAYLOAD_SIZE);
        if (forgeCodec != null) {
            return forgeCodec;
        }
        return PayloadIdCompat.discardedCodec(id, MAX_PAYLOAD_SIZE);
    }

    public record InteractPayload(ServerboundUseItemPacket packet, float yaw, float pitch, boolean sneak)
            implements CustomPacketPayload {

        public static final StreamCodec<FriendlyByteBuf, InteractPayload> CODEC =
                CustomPacketPayload.codec(InteractPayload::writePayload, InteractPayload::read);

        public static InteractPayload read(FriendlyByteBuf buf) {
            ServerboundUseItemPacket packet = ServerboundUseItemPacket.STREAM_CODEC.cast().decode(buf);
            float yaw = buf.readFloat();
            float pitch = buf.readFloat();
            boolean sneak = buf.readBoolean();
            return new InteractPayload(packet, yaw, pitch, sneak);
        }

        private static void write(FriendlyByteBuf buf, ServerboundUseItemPacket packet, float yaw, float pitch, boolean sneak) {
            ServerboundUseItemPacket.STREAM_CODEC.cast().encode(buf, packet);
            buf.writeFloat(yaw);
            buf.writeFloat(pitch);
            buf.writeBoolean(sneak);
        }

        private void writePayload(FriendlyByteBuf buf) {
            write(buf, packet, yaw, pitch, sneak);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return INTERACT_TYPE;
        }
    }

    public record PlacePayload(ServerboundUseItemOnPacket packet, float yaw, float pitch, boolean sneaking)
            implements CustomPacketPayload {

        public static final StreamCodec<FriendlyByteBuf, PlacePayload> CODEC =
                CustomPacketPayload.codec(PlacePayload::writePayload, PlacePayload::read);

        public static PlacePayload fromPacket(ServerboundUseItemOnPacket packet, float yaw, float pitch, boolean sneaking) {
            return new PlacePayload(packet, yaw, pitch, sneaking);
        }

        public static PlacePayload read(FriendlyByteBuf buf) {
            ServerboundUseItemOnPacket packet = decodeUseItemOn1218(buf);
            float yaw = buf.readFloat();
            float pitch = buf.readFloat();
            boolean sneaking = buf.readBoolean();
            return new PlacePayload(packet, yaw, pitch, sneaking);
        }

        private void writePayload(FriendlyByteBuf buf) {
            encodeUseItemOn1218(buf, packet);
            buf.writeFloat(yaw);
            buf.writeFloat(pitch);
            buf.writeBoolean(sneaking);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return PLACE_TYPE;
        }

        private static ServerboundUseItemOnPacket decodeUseItemOn1218(FriendlyByteBuf buf) {
            InteractionHand hand = buf.readEnum(InteractionHand.class);
            BlockPos pos = buf.readBlockPos();
            Direction face = buf.readEnum(Direction.class);
            float hitX = buf.readFloat();
            float hitY = buf.readFloat();
            float hitZ = buf.readFloat();
            boolean inside = buf.readBoolean();
            buf.readBoolean();
            BlockHitResult hit = new BlockHitResult(
                    new Vec3(pos.getX() + hitX, pos.getY() + hitY, pos.getZ() + hitZ),
                    face,
                    pos,
                    inside
            );
            int sequence = buf.readVarInt();
            return new ServerboundUseItemOnPacket(hand, hit, sequence);
        }

        private static void encodeUseItemOn1218(FriendlyByteBuf buf, ServerboundUseItemOnPacket packet) {
            buf.writeEnum(packet.getHand());

            BlockHitResult hit = packet.getHitResult();
            BlockPos pos = hit.getBlockPos();
            buf.writeBlockPos(pos);
            buf.writeEnum(hit.getDirection());

            Vec3 location = hit.getLocation();
            buf.writeFloat((float) (location.x - pos.getX()));
            buf.writeFloat((float) (location.y - pos.getY()));
            buf.writeFloat((float) (location.z - pos.getZ()));
            buf.writeBoolean(hit.isInside());
            buf.writeBoolean(false);
            buf.writeVarInt(packet.getSequence());
        }

    }
}
