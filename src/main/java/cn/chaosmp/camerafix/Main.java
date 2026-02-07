package cn.chaosmp.camerafix;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.concurrent.atomic.AtomicBoolean;

public class Main implements ModInitializer {
    public static final AtomicBoolean SENT_POS_SYNC = new AtomicBoolean(true);
    @Override
    public void onInitialize() {
        System.out.println("COPYRIGHT 2026@CHAOS-SMP");
        System.out.println("POWERED BY CHAOS-SMP (chaos-smp.cn)");
        ClientTickEvents.START_CLIENT_TICK.register(i -> {
            SENT_POS_SYNC.set(true);
        });
    }
}
