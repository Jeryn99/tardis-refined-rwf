package dev.jeryn.mc.rwf.network;

import dev.jeryn.mc.rwf.RWFNetwork;
import dev.jeryn.mc.rwf.common.entity.TardisEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import whocraft.tardis_refined.common.network.MessageC2S;
import whocraft.tardis_refined.common.network.MessageContext;
import whocraft.tardis_refined.common.network.MessageType;

public class StopRWFMessage extends MessageC2S {

    private final boolean isTransition;

    public StopRWFMessage(boolean isTransition) {
        this.isTransition = isTransition;
    }

    public StopRWFMessage(FriendlyByteBuf buf) {
        this.isTransition = buf.readBoolean();
    }

    @NotNull
    @Override
    public MessageType getType() {
        return RWFNetwork.STOP_RWF;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(isTransition);
    }

    @Override
    public void handle(MessageContext context) {
        ServerPlayer player = context.getPlayer();
        if (player.getFirstPassenger() instanceof TardisEntity tardis) {
            tardis.finishFlight(player.serverLevel(), isTransition);
        }
    }
}
