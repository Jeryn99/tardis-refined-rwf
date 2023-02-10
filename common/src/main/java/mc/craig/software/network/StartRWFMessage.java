package mc.craig.software.network;

import mc.craig.software.RWFNetwork;
import mc.craig.software.common.entity.TardisEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import whocraft.tardis_refined.common.network.MessageC2S;
import whocraft.tardis_refined.common.network.MessageContext;
import whocraft.tardis_refined.common.network.MessageType;

public class StartRWFMessage extends MessageC2S {


    public StartRWFMessage(){}

    public StartRWFMessage(FriendlyByteBuf friendlyByteBuf) {
    }

    @NotNull
    @Override
    public MessageType getType() {
        return RWFNetwork.START_RWF;
    }

    @Override
    public void toBytes(FriendlyByteBuf friendlyByteBuf) {

    }

    @Override
    public void handle(MessageContext messageContext) {
        ServerPlayer player = messageContext.getPlayer();
        if(player.level instanceof ServerLevel serverLevel){
            TardisEntity.createTardis(serverLevel, player);
        }
    }
}
