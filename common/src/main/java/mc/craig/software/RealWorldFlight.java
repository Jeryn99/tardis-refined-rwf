package mc.craig.software;

import mc.craig.software.common.entity.RWFEntityTypes;

public class RealWorldFlight {
    public static final String MOD_ID = "tardis_refined_rwf";

    public static void init() {
        RWFNetwork.init();
        RWFEntityTypes.ENTITY_TYPES.register();
    }
}
