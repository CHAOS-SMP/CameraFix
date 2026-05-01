package cn.chaosmp.camerafix.forge;

import cn.chaosmp.camerafix.util.VanillaPayloads;
import cn.chaosmp.camerafix.util.ProtocolPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

final class ForgeProtocolPackets {
    private ForgeProtocolPackets() {
    }

    static void initSender() {
        ProtocolPackets.initSender((useItemOn, useItem, yaw, pitch, sneak) -> {
            if (useItemOn != null) {
                Minecraft.getInstance().getConnection().send(
                        new ServerboundCustomPayloadPacket(VanillaPayloads.PlacePayload.fromPacket(useItemOn, yaw, pitch, sneak))
                );
                return true;
            } else if (useItem != null) {
                Minecraft.getInstance().getConnection().send(
                        new ServerboundCustomPayloadPacket(new VanillaPayloads.InteractPayload(useItem, yaw, pitch, sneak))
                );
                return true;
            }
            return false;
        });
    }
}
