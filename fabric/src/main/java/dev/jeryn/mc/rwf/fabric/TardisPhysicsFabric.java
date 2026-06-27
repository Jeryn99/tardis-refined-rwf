package dev.jeryn.mc.rwf.fabric;

import net.fabricmc.api.ModInitializer;

import java.util.logging.Logger;

public class TardisPhysicsFabric implements ModInitializer {

    private static final Logger LOGGER = Logger.getLogger("TardisPhysicsFabric");

    @Override
    public void onInitialize() {
        LOGGER.info("[TardisPhysics] Registered server tick and lifecycle listeners.");
        RWFCommonEvents.init();
    }
}
