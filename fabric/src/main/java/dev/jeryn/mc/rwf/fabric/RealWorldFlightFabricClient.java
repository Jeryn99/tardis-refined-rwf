package dev.jeryn.mc.rwf.fabric;

import dev.jeryn.mc.rwf.client.model.RWFModelRegistry;
import dev.jeryn.mc.rwf.fabric.client.RWFClientEvents;
import dev.jeryn.mc.rwf.fabric.client.RWFKeyMappingsFabric;
import net.fabricmc.api.ClientModInitializer;

public class RealWorldFlightFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        RWFModelRegistry.init();

        RWFClientEvents.init();
        RWFKeyMappingsFabric.init();

    }
}
