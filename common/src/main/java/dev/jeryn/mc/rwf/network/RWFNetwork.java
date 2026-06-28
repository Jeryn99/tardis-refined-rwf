package dev.jeryn.mc.rwf.network;

import dev.jeryn.mc.rwf.RealWorldFlight;
import net.minecraft.resources.ResourceLocation;
import whocraft.tardis_refined.common.network.MessageType;
import whocraft.tardis_refined.common.network.NetworkManager;

public class RWFNetwork {

    public static final NetworkManager NETWORK = NetworkManager.create(new ResourceLocation(RealWorldFlight.MOD_ID, "channel"));
    public static MessageType TARDIS_PHYSICS_UPDATE, SYNC_TARDIS_PHYSICS, SET_FREEFALL, START_RWF, OPEN_RWF, STOP_RWF, VORTEX_TRANSITION, SYNC_FLIGHT_DATA;

    public static void init() {
        START_RWF = NETWORK.registerC2S("start_rwf", StartRWFMessage::new);
        OPEN_RWF = NETWORK.registerC2S("open_rwf", RWFOpenDoor::new);
        STOP_RWF = NETWORK.registerC2S("stop_rwf", StopRWFMessage::new);
        VORTEX_TRANSITION = NETWORK.registerC2S("vortex_transition", TransitionVortexMessage::new);
        SYNC_FLIGHT_DATA = NETWORK.registerS2C("sync_flight_data", SyncFlightDataMessage::new);
        SET_FREEFALL = NETWORK.registerC2S("set_freefall", SetFreefallMessage::new);
        SYNC_TARDIS_PHYSICS = NETWORK.registerC2S("sync_tardis_physics", SyncTardisPhysicsMessage::new);
        TARDIS_PHYSICS_UPDATE = NETWORK.registerS2C("tardis_physics_update", TardisPhysicsUpdateMessage::new);
    }
}