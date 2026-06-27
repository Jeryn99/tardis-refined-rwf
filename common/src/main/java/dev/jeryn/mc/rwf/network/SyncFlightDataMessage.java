package dev.jeryn.mc.rwf.network;

import dev.jeryn.mc.rwf.RWFNetwork;
import dev.jeryn.mc.rwf.client.ClientFlightData;
import dev.jeryn.mc.rwf.client.ClientFlightTracker;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import whocraft.tardis_refined.common.network.MessageContext;
import whocraft.tardis_refined.common.network.MessageS2C;
import whocraft.tardis_refined.common.network.MessageType;

import java.util.HashMap;
import java.util.Map;

public class SyncFlightDataMessage extends MessageS2C {

    private final Map<ResourceKey<Level>, ClientFlightData> inFlight;

    public SyncFlightDataMessage(Map<ResourceKey<Level>, ClientFlightData> inFlight) {
        this.inFlight = inFlight;
    }

    public SyncFlightDataMessage(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        inFlight = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
            inFlight.put(key, ClientFlightData.fromBytes(buf));
        }
    }

    @NotNull
    @Override
    public MessageType getType() {
        return RWFNetwork.SYNC_FLIGHT_DATA;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(inFlight.size());
        for (var entry : inFlight.entrySet()) {
            buf.writeResourceLocation(entry.getKey().location());
            entry.getValue().toBytes(buf);
        }
    }

    @Override
    public void handle(MessageContext context) {
        ClientFlightTracker.setFlying(inFlight);
    }
}
