package dev.jeryn.mc.rwf.network;

import dev.jeryn.mc.rwf.common.entity.FlightTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import whocraft.tardis_refined.common.network.MessageC2S;
import whocraft.tardis_refined.common.network.MessageContext;
import whocraft.tardis_refined.common.network.MessageType;

public class SetFreefallMessage extends MessageC2S {

    private boolean isFreefalling;

    public SetFreefallMessage() {
    }

    public SetFreefallMessage(boolean isFreefalling) {
        this.isFreefalling = isFreefalling;
    }

    public SetFreefallMessage(FriendlyByteBuf buf) {
        this.isFreefalling = buf.readBoolean();
    }

    @NotNull
    @Override
    public MessageType getType() {
        return RWFNetwork.SET_FREEFALL;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(isFreefalling);
    }

    @Override
    public void handle(MessageContext context) {
        ServerPlayer player = context.getPlayer();
        var tardisDim = FlightTracker.getTardisDimensionFor(player.getUUID());
        if (tardisDim == null) return;

        FlightTracker.updateFreefall(tardisDim, isFreefalling, player.server);
    }
}