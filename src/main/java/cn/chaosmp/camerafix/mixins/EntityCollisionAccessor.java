package cn.chaosmp.camerafix.mixins;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityCollisionAccessor {
    @Accessor("horizontalCollision")
    boolean horizontalCollision();
}
