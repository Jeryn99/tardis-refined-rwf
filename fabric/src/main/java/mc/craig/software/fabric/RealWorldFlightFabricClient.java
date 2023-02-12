package mc.craig.software.fabric;

import mc.craig.software.client.RenderTardis;
import mc.craig.software.common.entity.RWFEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class RealWorldFlightFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(RWFEntityTypes.TARDIS.get(), RenderTardis::new);

    }
}
