package dev.jeryn.mc.rwf.forge;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


/**
 * forge.dev.jeryn.mc.rwf.TardisPhysicsForge
 * <p>
 * Forge mod initialiser for the physics addon.
 * <p>
 * Registers:
 * • {@link TickEvent.ServerTickEvent} (ONLY phase) → advances all active
 * freefall simulations at 20 TPS.
 * • {@link ServerStoppingEvent} → cleans up on shutdown.
 * <p>
 * Annotate your @Mod class with this or register via the Forge event bus
 * in your main @Mod constructor.
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TardisPhysicsForge {

    private static final float DELTA_SECONDS = 1f / 20f;


    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        // TardisFreefallManager.INSTANCE.destroyAll();
    }
}
