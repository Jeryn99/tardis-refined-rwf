package dev.jeryn.mc.rwf.network;

import dev.jeryn.mc.rwf.common.entity.FlightTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import whocraft.tardis_refined.common.capability.tardis.TardisLevelOperator;
import whocraft.tardis_refined.common.network.MessageC2S;
import whocraft.tardis_refined.common.network.MessageContext;
import whocraft.tardis_refined.common.network.MessageType;

public class RWFOpenDoor extends MessageC2S {

    public RWFOpenDoor(FriendlyByteBuf friendlyByteBuf) {
    }

    public RWFOpenDoor() {

    }

    @NotNull
    @Override
    public MessageType getType() {
        return RWFNetwork.OPEN_RWF;
    }

    @Override
    public void toBytes(FriendlyByteBuf friendlyByteBuf) {

    }

    @Override
    public void handle(MessageContext messageContext) {
        ServerPlayer player = messageContext.getPlayer();
        ResourceKey<Level> tardisDim = FlightTracker.getTardisDimensionFor(player.getUUID());
        if (tardisDim != null) {
            ServerLevel level = player.getServer().getLevel(tardisDim);
            if (level == null) return;

            TardisLevelOperator.get(level).ifPresent(tardisLevelOperator -> {
                tardisLevelOperator.setDoorClosed(tardisLevelOperator.getInternalDoor().isOpen());
            });
        }
    }
}
