package dev.jeryn.mc.rwf.forge.client;

import dev.jeryn.mc.rwf.RealWorldFlight;
import dev.jeryn.mc.rwf.client.RWFKeyMappings;
import dev.jeryn.mc.rwf.client.RenderTardis;
import dev.jeryn.mc.rwf.common.entity.RWFEntityTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RealWorldFlight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RWFForgeClientSetup {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(RWFEntityTypes.TARDIS.get(), RenderTardis::new);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(RWFKeyMappings.EXIT_FLIGHT);
        event.register(RWFKeyMappings.FREE_FALL);
    }

}
