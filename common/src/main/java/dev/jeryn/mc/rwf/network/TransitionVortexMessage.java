package dev.jeryn.mc.rwf.network;

import dev.jeryn.mc.rwf.common.entity.FlightTracker;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import whocraft.tardis_refined.common.capability.player.TardisPlayerInfo;
import whocraft.tardis_refined.common.capability.tardis.TardisLevelOperator;
import whocraft.tardis_refined.common.network.MessageC2S;
import whocraft.tardis_refined.common.network.MessageContext;
import whocraft.tardis_refined.common.network.MessageType;
import whocraft.tardis_refined.common.tardis.TardisNavLocation;
import whocraft.tardis_refined.common.util.DimensionUtil;

public class TransitionVortexMessage extends MessageC2S {
    public TransitionVortexMessage() {
    }

    public TransitionVortexMessage(FriendlyByteBuf buf) {
    }

    public @NotNull MessageType getType() {
        return RWFNetwork.VORTEX_TRANSITION;
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public void handle(MessageContext context) {
        ServerPlayer player = context.getPlayer();

        ResourceKey<Level> tardisDimKey = FlightTracker.getTardisDimensionFor(player.getUUID());
        if (tardisDimKey == null) return;

        ServerLevel tardisDim = DimensionUtil.getLevel(tardisDimKey);

        TardisLevelOperator.get(tardisDim).ifPresent((tardisLevelOperator) -> {
            TardisPlayerInfo.get(context.getPlayer()).ifPresent((tardisInfo) -> {
                tardisInfo.startShellView(
                        player,
                        tardisLevelOperator,
                        new TardisNavLocation(player.blockPosition(), Direction.NORTH, player.level().dimension()),
                        true
                );
            });
        });

        FlightTracker.stopFlying(tardisDimKey, player.getServer());
    }
}
