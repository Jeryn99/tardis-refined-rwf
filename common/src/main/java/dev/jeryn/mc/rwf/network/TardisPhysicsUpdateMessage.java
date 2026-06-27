package dev.jeryn.mc.rwf.network;

import dev.jeryn.mc.rwf.RWFNetwork;
import dev.jeryn.mc.rwf.common.entity.TardisEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import whocraft.tardis_refined.common.network.MessageContext;
import whocraft.tardis_refined.common.network.MessageS2C;
import whocraft.tardis_refined.common.network.MessageType;

public class TardisPhysicsUpdateMessage extends MessageS2C {

    private final Vec3 position;
    private final float[] matrix;
    private final int entityId;

    public TardisPhysicsUpdateMessage(Vec3 position, float[] matrix, int entityId) {
        this.position = position;
        this.matrix = matrix;
        this.entityId = entityId;
    }

    public TardisPhysicsUpdateMessage(FriendlyByteBuf buf) {
        this.position = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.matrix = new float[16];
        for (int i = 0; i < 16; i++) this.matrix[i] = buf.readFloat();
        this.entityId = buf.readInt();
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
        buf.writeInt(entityId);
    }

    @Override
    public void handle(MessageContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Entity entity = mc.level.getEntity(entityId);
        if (!(entity instanceof TardisEntity tardis)) return;

        tardis.setPos(position);
        tardis.physicsMatrix = matrix;
    }
}