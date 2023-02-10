package mc.craig.software.fabric;

import mc.craig.software.RealWorldFlight;
import net.fabricmc.api.ModInitializer;

public class RealWorldFlightFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        RealWorldFlight.init();
    }
}
