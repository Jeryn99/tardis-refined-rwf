package dev.jeryn.mc.rwf.forge;

import dev.jeryn.mc.rwf.RealWorldFlight;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RealWorldFlight.MOD_ID)
public class RealWorldFlightForge {

    public RealWorldFlightForge() {
        RealWorldFlight.init();
        FMLJavaModLoadingContext.get().getModEventBus()
                .addListener(this::setup);
    }

    private void setup(FMLCommonSetupEvent event) {

    }
}