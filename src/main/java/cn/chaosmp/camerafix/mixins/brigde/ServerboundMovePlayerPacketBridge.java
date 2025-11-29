package cn.chaosmp.camerafix.mixins.brigde;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundMovePlayerPacket.class)
public interface ServerboundMovePlayerPacketBridge {
    @Accessor
    float getXRot();

    @Accessor
    float getYRot();

    @Accessor("onGround")
    boolean onGround();
}
