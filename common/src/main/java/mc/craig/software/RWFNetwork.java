package mc.craig.software;

import mc.craig.software.network.StartRWFMessage;
import net.minecraft.resources.ResourceLocation;
import whocraft.tardis_refined.common.network.MessageType;
import whocraft.tardis_refined.common.network.NetworkManager;

public class RWFNetwork {

    public static final NetworkManager NETWORK = NetworkManager.create(new ResourceLocation(RealWorldFlight.MOD_ID, "channel"));
    public static MessageType START_RWF;


    public static void init(){
        START_RWF = NETWORK.registerC2S("start_rwf", StartRWFMessage::new);
    }

}
