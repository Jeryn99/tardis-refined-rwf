package mc.craig.software.fabric;

import mc.craig.software.client.RenderTardis;
import mc.craig.software.common.entity.RWFEntityTypes;
import mc.craig.software.common.entity.TardisEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class RealWorldFlightFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(RWFEntityTypes.TARDIS.get(), RenderTardis::new);

    }
}
