package cn.chaosmp.camerafix.neoforge;

import cn.chaosmp.camerafix.util.ProtocolPackets;
import cn.chaosmp.camerafix.util.VanillaPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

final class NeoForgeProtocolPackets {
    private NeoForgeProtocolPackets() {
    }

    static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.commonToServer(VanillaPayloads.PLACE_TYPE, VanillaPayloads.PlacePayload.CODEC, NeoForgeProtocolPackets::ignorePayload);
        registrar.commonToServer(VanillaPayloads.INTERACT_TYPE, VanillaPayloads.InteractPayload.CODEC, NeoForgeProtocolPackets::ignorePayload);
    }

    static void initSender() {
        ProtocolPackets.initSender((useItemOn, useItem, yaw, pitch, sneak) -> {
            if (useItemOn != null) {
                sendRaw(VanillaPayloads.PlacePayload.fromPacket(useItemOn, yaw, pitch, sneak));
                return true;
            } else if (useItem != null) {
                sendRaw(new VanillaPayloads.InteractPayload(useItem, yaw, pitch, sneak));
                return true;
            }
            return false;
        });
    }

    private static void sendRaw(CustomPacketPayload payload) {
        Minecraft.getInstance().getConnection().getConnection().send(new ServerboundCustomPayloadPacket(payload));
    }

    private static void ignorePayload(CustomPacketPayload payload, IPayloadContext context) {
    }
}
