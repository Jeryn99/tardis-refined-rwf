package dev.jeryn.mc.rwf.forge;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TardisPhysicsForge {

    private static final float DELTA_SECONDS = 1f / 20f;

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        // TardisFreefallManager.INSTANCE.destroyAll();
    }
}
