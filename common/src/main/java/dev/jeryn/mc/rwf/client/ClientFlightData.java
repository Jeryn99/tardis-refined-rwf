package dev.jeryn.mc.rwf.client;

import dev.jeryn.mc.rwf.common.entity.FlightTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

import static net.minecraft.core.registries.Registries.DIMENSION;

public record ClientFlightData(
        ResourceKey<Level> tardisDimension,
        ResourceKey<Level> originDimension,
        Vec3 originPos,
        float originYaw,
        float originPitch,
        boolean isFreefalling,
        UUID pilot
) {
    public static ClientFlightData fromBytes(FriendlyByteBuf buf) {
        ResourceKey<Level> tardisDim = ResourceKey.create(DIMENSION, buf.readResourceLocation());
        ResourceKey<Level> originDim = ResourceKey.create(DIMENSION, buf.readResourceLocation());
        Vec3 originPos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        float originYaw = buf.readFloat();
        float originPitch = buf.readFloat();
        boolean isFreefalling = buf.readBoolean();
        UUID playerUUID = buf.readUUID();
        return new ClientFlightData(tardisDim, originDim, originPos, originYaw, originPitch, isFreefalling, playerUUID);
    }

    public static ClientFlightData from(FlightTracker.FlightData data, ResourceKey<Level> tardisDimension) {
        return new ClientFlightData(
                tardisDimension,
                data.originDimension(),
                data.originPos(),
                data.originYaw(),
                data.originPitch(),
                data.isFreefalling(),
                data.player().getUUID()
        );
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(tardisDimension.location());
        buf.writeResourceLocation(originDimension.location());
        buf.writeDouble(originPos.x);
        buf.writeDouble(originPos.y);
        buf.writeDouble(originPos.z);
        buf.writeFloat(originYaw);
        buf.writeFloat(originPitch);
        buf.writeBoolean(isFreefalling);
        buf.writeUUID(pilot);
    }
}
