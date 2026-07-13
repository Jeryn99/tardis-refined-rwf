package dev.jeryn.mc.rwf.common.entity;

import dev.jeryn.mc.rwf.client.ClientFlightData;
import dev.jeryn.mc.rwf.network.SyncFlightDataMessage;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import whocraft.tardis_refined.client.TardisClientData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlightTracker {

    public static HashMap<ResourceKey<Level>, FlightData> IN_FLIGHT = new HashMap<>();

    public static void setInFlight(FlightData flightData, ResourceKey<Level> key) {
        IN_FLIGHT.put(key, flightData);
        broadcastSync(flightData.player().server);
    }

    public static void stopFlying(ResourceKey<Level> levelResourceKey, MinecraftServer server) {
        IN_FLIGHT.remove(levelResourceKey);
        broadcastSync(server);
    }

    public static boolean isFlying(ResourceKey<Level> levelResourceKey) {
        return IN_FLIGHT.containsKey(levelResourceKey);
    }

    public static boolean isPlayerFlying(UUID uuid) {
        for (Map.Entry<ResourceKey<Level>, FlightData> dataEntry : IN_FLIGHT.entrySet()) {
            if (dataEntry.getValue().player().getUUID().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    public static boolean transitionToVortex(TardisEntity tardis) {
        ResourceKey<Level> tardisDim = tardis.getTardisDimension();
        TardisClientData tardisClientData = TardisClientData.getInstance(tardisDim);

        if (tardisClientData.isFlying()) {
            //     new TransitionVortexMessage().send();
            //      new StopRWFMessage(true).send();
            return true;
        }
        return false;
    }

    public static void loggedOut(FlightData flightData) {
        MinecraftServer server = flightData.player().server;
        flightData.tardis.finishFlight(flightData.player().serverLevel(), false);
        flightData.tardis.discard();
        broadcastSync(server);
    }

    private static void broadcastSync(MinecraftServer server) {
        if (server == null) return;
        Map<ResourceKey<Level>, ClientFlightData> payload = new HashMap<>();
        IN_FLIGHT.forEach((key, data) -> payload.put(key, ClientFlightData.from(data, key)));
        new SyncFlightDataMessage(payload).sendToAll();
    }

    public static void updateFreefall(ResourceKey<Level> key, boolean isFreefalling, MinecraftServer server) {
        FlightData existing = IN_FLIGHT.get(key);
        if (existing == null) return;

        // Preserve the original take-off dimension/pos/rotation. Re-running the
        // main constructor here would re-capture those from the player's CURRENT
        // (mid-flight) position, silently corrupting the return location every
        // time freefall is toggled - this was the cause of players sometimes
        // landing back in the wrong dimension.
        FlightData updated = existing.withFreefalling(isFreefalling);
        IN_FLIGHT.put(key, updated);
        broadcastSync(server);
    }

    public static void setUpPlayerForFlight(ServerPlayer serverPlayer) {
        Abilities abilities = serverPlayer.getAbilities();
        abilities.mayfly = true;
        abilities.flying = true;
        abilities.invulnerable = true;
        abilities.mayBuild = false;
        abilities.setFlyingSpeed(0.16F);
        serverPlayer.onUpdateAbilities();
    }

    public static void restorePlayer(ServerPlayer serverPlayer) {
        Abilities abilities = serverPlayer.getAbilities();
        serverPlayer.gameMode.getGameModeForPlayer().updatePlayerAbilities(serverPlayer.getAbilities());
        abilities.setFlyingSpeed(0.05F);
        serverPlayer.onUpdateAbilities();
    }

    public static class FlightData {

        private final TardisEntity tardis;
        private final ServerPlayer player;
        private final ResourceKey<Level> originDimension;
        private final Vec3 originPos;
        private final float originYaw;
        private final float originPitch;
        private final boolean isFreefalling;

        public FlightData(TardisEntity tardis, ServerPlayer serverPlayer, boolean isFreefalling) {
            this.tardis = tardis;
            this.player = serverPlayer;
            this.originDimension = serverPlayer.level().dimension();
            this.originPos = serverPlayer.position();
            this.originYaw = serverPlayer.getYRot();
            this.originPitch = serverPlayer.getXRot();
            this.isFreefalling = isFreefalling;
        }

        private FlightData(TardisEntity tardis, ServerPlayer player, ResourceKey<Level> originDimension,
                            Vec3 originPos, float originYaw, float originPitch, boolean isFreefalling) {
            this.tardis = tardis;
            this.player = player;
            this.originDimension = originDimension;
            this.originPos = originPos;
            this.originYaw = originYaw;
            this.originPitch = originPitch;
            this.isFreefalling = isFreefalling;
        }

        /**
         * Returns a copy of this FlightData with only the freefall flag changed.
         * Use this instead of the main constructor when updating in-flight state,
         * so the original take-off dimension/position/rotation are never recomputed
         * from the player's current (mid-flight) position.
         */
        public FlightData withFreefalling(boolean isFreefalling) {
            return new FlightData(tardis, player, originDimension, originPos, originYaw, originPitch, isFreefalling);
        }

        public TardisEntity tardis() {
            return tardis;
        }

        public ServerPlayer player() {
            return player;
        }

        public ResourceKey<Level> originDimension() {
            return originDimension;
        }

        public Vec3 originPos() {
            return originPos;
        }

        public float originYaw() {
            return originYaw;
        }

        public float originPitch() {
            return originPitch;
        }

        public boolean isFreefalling() {
            return isFreefalling;
        }
    }
}
