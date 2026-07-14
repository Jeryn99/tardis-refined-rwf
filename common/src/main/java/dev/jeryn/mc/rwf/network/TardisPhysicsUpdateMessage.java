package dev.jeryn.mc.rwf.network;

import dev.jeryn.mc.rwf.client.RenderTardis;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import whocraft.tardis_refined.common.network.MessageContext;
import whocraft.tardis_refined.common.network.MessageS2C;
import whocraft.tardis_refined.common.network.MessageType;

import java.util.UUID;

public class TardisPhysicsUpdateMessage extends MessageS2C {

    private final Vec3 position;
    private final float[] matrix;
    private final UUID pilot;

    public TardisPhysicsUpdateMessage(Vec3 position, float[] matrix, UUID pilot) {
        this.position = position;
        this.matrix = matrix;
        this.pilot = pilot;
    }

    public TardisPhysicsUpdateMessage(FriendlyByteBuf buf) {
        this.position = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.matrix = new float[16];
        for (int i = 0; i < 16; i++) this.matrix[i] = buf.readFloat();
        this.pilot = buf.readUUID();
    }

    @NotNull
    @Override
    public MessageType getType() {
        return RWFNetwork.TARDIS_PHYSICS_UPDATE;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(position.x);
        buf.writeDouble(position.y);
        buf.writeDouble(position.z);
        for (float f : matrix) buf.writeFloat(f);
        buf.writeUUID(pilot);
    }

    @Override
    public void handle(MessageContext context) {
        RenderTardis.setPhysicsMatrix(pilot, matrix);
    }
}
