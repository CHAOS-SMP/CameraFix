package cn.chaosmp.camerafix.util;


import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;


public class ProtocolPackets {
    public static float YAW = Float.NaN; //global yaw; - ref
    public static float PITCH = Float.NaN; //global pitch; - ref
    public static boolean SNEAK = false;

    public static ServerboundCustomPayloadPacket sendPlacePayload(BlockHitResult blockHitResult,
                                                                  boolean interactionHand,
                                                                  int seq,
                                                                  long ts,
                                                                  float yaw,
                                                                  float pitch,
                                                                  boolean sneak) {

        PlacePayload payload = new PlacePayload(
                blockHitResult,
                interactionHand,
                seq,
                ts,
                yaw,
                pitch,
                sneak
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

    public record PlacePayload(
            net.minecraft.world.phys.BlockHitResult hitResult,
            boolean interactionHand,
            int seq,
            long ts,
            float yaw,
            float pitch,
            boolean sneak
    ) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<PlacePayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.tryBuild("neko", "place"));

        public static final StreamCodec<FriendlyByteBuf, PlacePayload> CODEC =
                CustomPacketPayload.codec(PlacePayload::write, PlacePayload::read);

        public static PlacePayload read(FriendlyByteBuf buf) {
            BlockHitResult hitResult = readBlockHitResult(buf);
            boolean interactionHand = buf.readBoolean();
            int seq = buf.readVarInt();
            long ts = buf.readLong();
            float yaw = buf.readFloat();
            float pitch = buf.readFloat();
            boolean sneak = buf.readBoolean();
            return new PlacePayload(hitResult, interactionHand, seq, ts, yaw, pitch, sneak);
        }

        private static void writeBlockHitResult(FriendlyByteBuf buf, BlockHitResult result) {

            buf.writeVarInt(result.getDirection().ordinal());

            BlockPos pos = result.getBlockPos();
            buf.writeLong(pos.asLong());

            boolean miss = result.getType() != HitResult.Type.BLOCK;
            buf.writeBoolean(miss);

            buf.writeBoolean(result.isInside());

            boolean worldBorderHit = false;
            buf.writeBoolean(worldBorderHit);


            Vec3 location = result.getLocation();
            buf.writeFloat((float) (location.x - pos.getX()));
            buf.writeFloat((float) (location.y - pos.getY()));
            buf.writeFloat((float) (location.z - pos.getZ()));
        }

        private static BlockHitResult readBlockHitResult(FriendlyByteBuf buf) {

            Direction direction = Direction.values()[buf.readVarInt()];

            long posLong = buf.readLong();
            BlockPos pos = BlockPos.of(posLong);

            boolean miss = buf.readBoolean();
            boolean inside = buf.readBoolean();
            boolean worldBorderHit = buf.readBoolean();

            float offsetX = buf.readFloat();
            float offsetY = buf.readFloat();
            float offsetZ = buf.readFloat();

            double x = pos.getX() + offsetX;
            double y = pos.getY() + offsetY;
            double z = pos.getZ() + offsetZ;

            return new BlockHitResult(
                    new Vec3(x, y, z),
                    direction,
                    pos,
                    inside
            );
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public void write(FriendlyByteBuf buf) {
            writeBlockHitResult(buf, hitResult);
            buf.writeBoolean(interactionHand);
            buf.writeVarInt(seq);
            buf.writeLong(ts);
            buf.writeFloat(yaw);
            buf.writeFloat(pitch);
            buf.writeBoolean(sneak);
        }
    }
}