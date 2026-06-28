package dev.jeryn.mc.rwf.fabric;

import dev.jeryn.mc.rwf.client.RenderTardis;
import dev.jeryn.mc.rwf.client.model.RWFModelRegistry;
import dev.jeryn.mc.rwf.common.entity.RWFEntityTypes;
import dev.jeryn.mc.rwf.fabric.client.RWFClientEvents;
import dev.jeryn.mc.rwf.fabric.client.RWFKeyMappingsFabric;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class RealWorldFlightFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        RWFModelRegistry.init();

        EntityRendererRegistry.register(RWFEntityTypes.TARDIS.get(), RenderTardis::new);
        RWFClientEvents.init();
        RWFKeyMappingsFabric.init();


    }
}
