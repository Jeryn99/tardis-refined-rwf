package dev.jeryn.mc.rwf.forge;

import dev.jeryn.mc.rwf.RealWorldFlight;
import dev.jeryn.mc.rwf.common.entity.FlightTracker;
import dev.jeryn.mc.rwf.common.entity.TardisEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RealWorldFlight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RWFForgeCommonEvents {

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (!(serverPlayer.getFirstPassenger() instanceof TardisEntity)) return;

        FlightTracker.FlightData flightData = FlightTracker.IN_FLIGHT.values().stream()
                .filter(data -> data.player().getUUID().equals(serverPlayer.getUUID()))
                .findFirst()
                .orElse(null);

        if (flightData != null) {
            FlightTracker.loggedOut(flightData);
        }
    }

}