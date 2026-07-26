package cn.chaosmp.camerafix.fabric;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.spongepowered.asm.mixin.Mixins;

public final class FabricPreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        switch (FabricRuntimeNamespace.detect()) {
            case INTERMEDIARY -> Mixins.addConfiguration("camerafix.intermediary.mixins.json");
            case NAMED -> Mixins.addConfiguration("camerafix.named.mixins.json");
        }
    }
}
