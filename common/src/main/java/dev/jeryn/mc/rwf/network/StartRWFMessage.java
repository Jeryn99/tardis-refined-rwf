package dev.jeryn.mc.rwf.network;

import dev.jeryn.mc.rwf.common.entity.FlightTracker;
import dev.jeryn.mc.rwf.common.upgrade.RWFUpgrades;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import whocraft.tardis_refined.common.capability.tardis.TardisLevelOperator;
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
        if(player.level() instanceof ServerLevel serverLevel){
            if(!FlightTracker.isFlying(serverLevel.dimension())) {

                // Reciprocal guard to TardisPilotingManagerMixin: block starting RWF
                // if someone is already mid-flight on the base mod's own console
                // throttle for this TARDIS, so the two systems can't both claim the
                // same shell at once (see multiplayer duplicate-shell bug).
                boolean consoleFlightActive = TardisLevelOperator.get(serverLevel)
                        .map(op -> op.getPilotingManager().isInFlight())
                        .orElse(false);

                if (consoleFlightActive) {
                    player.displayClientMessage(
                            Component.translatable("message.tardis_refined_rwf.console_flight_active"), true);
                    return;
                }

                boolean unlocked = TardisLevelOperator.get(serverLevel)
                        .map(op -> op.getUpgradeHandler().isUpgradeUnlocked(RWFUpgrades.FLIGHT_UNLOCK.get()))
                        .orElse(false);

                if (!unlocked) {
                    player.displayClientMessage(
                            Component.translatable("message.tardis_refined_rwf.flight_locked"), true);
                    return;
                }

                FlightTracker.startFlight(serverLevel, player);
            }
        }
    }
}
