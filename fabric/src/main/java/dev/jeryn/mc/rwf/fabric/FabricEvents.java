package dev.jeryn.mc.rwf.fabric;

import dev.jeryn.mc.rwf.client.FlightModeClient;
import dev.jeryn.mc.rwf.common.entity.FlightTracker;
import dev.jeryn.mc.rwf.common.entity.TardisEntity;
import dev.jeryn.mc.rwf.network.RWFOpenDoor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.Minecraft;

public class FabricEvents {

    private static long lastOpened = System.currentTimeMillis();

    public static void init() {

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (handler.player.getFirstPassenger() instanceof TardisEntity tardis) {
                tardis.finishFlight(handler.player.serverLevel(), false);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            // Client Effects
            FlightModeClient.tick();

            if (Minecraft.getInstance().player == null) {
                FlightTracker.IN_FLIGHT.clear();
            }

            // Handle Door Opening
            long currentTime = System.currentTimeMillis();
            if (Minecraft.getInstance().options.keyAttack.isDown() && currentTime - lastOpened >= 2000) {
                new RWFOpenDoor().send();
                lastOpened = currentTime;
            }
        });


    }

}
