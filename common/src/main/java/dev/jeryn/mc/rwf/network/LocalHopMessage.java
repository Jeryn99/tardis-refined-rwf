package dev.jeryn.mc.rwf.network;

import dev.jeryn.mc.rwf.common.entity.FlightTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import whocraft.tardis_refined.common.network.MessageC2S;
import whocraft.tardis_refined.common.network.MessageContext;
import whocraft.tardis_refined.common.network.MessageType;

public class LocalHopMessage extends MessageC2S {

    private BlockPos targetPos;
    private Direction hitFace;

    public LocalHopMessage() {
    }

    public LocalHopMessage(BlockPos targetPos, Direction hitFace) {
        this.targetPos = targetPos;
        this.hitFace = hitFace;
    }

    public LocalHopMessage(FriendlyByteBuf buf) {
        this.targetPos = buf.readBlockPos();
        this.hitFace = Direction.from3DDataValue(buf.readByte());
    }

    @NotNull
    @Override
    public MessageType getType() {
        return RWFNetwork.LOCAL_HOP;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(targetPos);
        buf.writeByte(hitFace.get3DDataValue());
    }

    @Override
    public void handle(MessageContext context) {
        ServerPlayer player = context.getPlayer();
        FlightTracker.attemptLocalHop(player, targetPos, hitFace);
    }
}
