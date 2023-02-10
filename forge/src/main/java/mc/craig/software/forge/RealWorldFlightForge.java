package mc.craig.software.forge;

import dev.architectury.platform.forge.EventBuses;
import mc.craig.software.RealWorldFlight;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RealWorldFlight.MOD_ID)
public class RealWorldFlightForge {
    public RealWorldFlightForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(RealWorldFlight.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        RealWorldFlight.init();
    }
}
