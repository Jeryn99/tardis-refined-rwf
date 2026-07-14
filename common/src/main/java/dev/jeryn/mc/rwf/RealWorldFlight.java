package dev.jeryn.mc.rwf;

import dev.jeryn.mc.rwf.common.upgrade.RWFUpgrades;
import dev.jeryn.mc.rwf.network.RWFNetwork;

public class RealWorldFlight {
    public static final String MOD_ID = "tardis_refined_rwf";

    public static void init() {
        RWFNetwork.init();
        RWFUpgrades.RWF_UPGRADES.registerToModBus();
    }
}
