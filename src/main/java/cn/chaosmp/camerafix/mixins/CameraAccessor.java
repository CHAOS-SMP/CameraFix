package cn.chaosmp.camerafix.mixins;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Camera.class)
public interface CameraAccessor {
    @Accessor("yRot")
    float camerafix$getYRot();

    @Accessor("xRot")
    float camerafix$getXRot();
}
