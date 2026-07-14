package dev.jeryn.mc.rwf.fabric;

import dev.jeryn.mc.rwf.RealWorldFlight;
import net.fabricmc.api.ModInitializer;

public class RealWorldFlightFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        RealWorldFlight.init();
        RWFCommonEvents.init();

    }
}
