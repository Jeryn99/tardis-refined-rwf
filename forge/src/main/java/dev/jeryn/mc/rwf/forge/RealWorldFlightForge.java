package dev.jeryn.mc.rwf.forge;

import dev.jeryn.mc.rwf.RealWorldFlight;
import dev.jeryn.mc.rwf.common.weather.WeatherHooks;
import dev.jeryn.mc.rwf.forge.compat.weather2.Weather2ReflectionBridge;
import dev.jeryn.mc.rwf.forge.compat.weather2.Weather2WeatherHook;
import net.minecraftforge.fml.ModList;
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
        if (ModList.get().isLoaded("weather2")) {
            Weather2ReflectionBridge.init();
            if (Weather2ReflectionBridge.isAvailable()) {
                WeatherHooks.set(new Weather2WeatherHook());
            }
        }
    }
}