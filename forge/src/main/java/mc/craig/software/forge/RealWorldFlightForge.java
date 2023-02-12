package mc.craig.software.forge;

import mc.craig.software.RealWorldFlight;
import net.minecraftforge.fml.common.Mod;

@Mod(RealWorldFlight.MOD_ID)
public class RealWorldFlightForge {
    public RealWorldFlightForge() {
        RealWorldFlight.init();
    }
}
