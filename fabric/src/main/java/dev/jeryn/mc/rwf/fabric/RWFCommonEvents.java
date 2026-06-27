package dev.jeryn.mc.rwf.fabric;

import dev.jeryn.mc.rwf.common.entity.TardisEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class RWFCommonEvents {

    public static void init() {

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (handler.getPlayer().getFirstPassenger() instanceof TardisEntity tardis) {
                tardis.finishFlight(handler.getPlayer().serverLevel(), false);
            }
        });
    }
}