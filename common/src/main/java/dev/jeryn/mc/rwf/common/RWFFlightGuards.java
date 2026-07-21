package dev.jeryn.mc.rwf.common;

import dev.jeryn.mc.rwf.common.entity.FlightTracker;
import whocraft.tardis_refined.api.event.EventResult;
import whocraft.tardis_refined.api.event.TardisCommonEvents;
import whocraft.tardis_refined.registry.TRControlRegistry;

public class RWFFlightGuards {

    public static void register() {
        TardisCommonEvents.PLAYER_CONTROL_INTERACT.register((operator, control, controlEntity) -> {
            if (control == TRControlRegistry.THROTTLE.get()
                    && FlightTracker.isFlying(operator.getLevelKey())) {
                return EventResult.cancel();
            }
            return EventResult.pass();
        });
    }
}
