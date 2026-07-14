package dev.jeryn.mc.rwf.forge;

import dev.jeryn.mc.rwf.RealWorldFlight;
import dev.jeryn.mc.rwf.common.entity.FlightTracker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RealWorldFlight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RWFForgeCommonEvents {

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        try {
            FlightTracker.FlightData flightData = FlightTracker.IN_FLIGHT.values().stream()
                    .filter(data -> data.player().getUUID().equals(serverPlayer.getUUID()))
                    .findFirst()
                    .orElse(null);

            if (flightData != null) {
                FlightTracker.loggedOut(flightData);
            }
        } catch (Throwable t) {
            System.out.println("[RWF] onLoggedOut failed " + t);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        FlightTracker.serverTick(event.getServer());
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (FlightTracker.isFlying(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getItemStack().isEmpty() && FlightTracker.isFlying(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (FlightTracker.isFlying(event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (FlightTracker.isFlying(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (FlightTracker.isFlying(event.getEntity())) {
            event.setCanceled(true);
        }
    }

}