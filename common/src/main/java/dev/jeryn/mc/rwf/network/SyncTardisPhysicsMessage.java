package dev.jeryn.mc.rwf.network;

import dev.jeryn.mc.rwf.common.entity.TardisEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import whocraft.tardis_refined.common.network.MessageC2S;
import whocraft.tardis_refined.common.network.MessageContext;
import whocraft.tardis_refined.common.network.MessageType;

public class SyncTardisPhysicsMessage extends MessageC2S {

    private final Vec3 position;
    private final float[] matrix;

    public SyncTardisPhysicsMessage(Vec3 position, float[] matrix) {
        this.position = position;
        this.matrix = matrix;
    }

    public SyncTardisPhysicsMessage(FriendlyByteBuf buf) {
        this.position = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.matrix = new float[16];
        for (int i = 0; i < 16; i++) this.matrix[i] = buf.readFloat();
    }

    @NotNull
    @Override
    public MessageType getType() {
        return RWFNetwork.SYNC_TARDIS_PHYSICS;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(position.x);
        buf.writeDouble(position.y);
        buf.writeDouble(position.z);
        for (float f : matrix) buf.writeFloat(f);
    }

    @Override
    public void handle(MessageContext context) {
        ServerPlayer pilot = context.getPlayer();
        if (!(pilot.getFirstPassenger() instanceof TardisEntity tardis)) return;

        tardis.setPos(position);

        for (ServerPlayer player : pilot.server.getPlayerList().getPlayers()) {
            if (pilot.getUUID() == player.getUUID()) continue;
            new TardisPhysicsUpdateMessage(position, matrix, tardis.getId())
                    .send(player);
        }
    }
}
