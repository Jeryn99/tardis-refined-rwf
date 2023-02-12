package mc.craig.software.common.entity;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.level.Level;

import java.util.HashMap;

public class FlightTracker {

    public static HashMap<ResourceKey<Level>, FlightData> IN_FLIGHT = new HashMap<>();

    public static void setInFlight(FlightData flightData, ResourceKey<Level> uuid) {
        IN_FLIGHT.put(uuid, flightData);
    }

    public static void stopFlying(ResourceKey<Level> levelResourceKey) {
        IN_FLIGHT.remove(levelResourceKey);
    }

    public static boolean isFlying(ResourceKey<Level> levelResourceKey) {
        return IN_FLIGHT.containsKey(levelResourceKey);
    }

    public static void loggedOut(FlightData flightData){
        flightData.tardis.finishFlight(flightData.player().getLevel());
        flightData.tardis.discard();
    }

    public static class FlightData {

        private final TardisEntity tardis;
        private final ServerPlayer player;

        public FlightData(TardisEntity tardis, ServerPlayer serverPlayer){
            this.tardis = tardis;
            this.player = serverPlayer;
        }

        public TardisEntity tardis() {
            return tardis;
        }

        public ServerPlayer player() {
            return player;
        }
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

    public static void restorePlayer(ServerPlayer serverPlayer){
        Abilities abilities = serverPlayer.getAbilities();
        serverPlayer.gameMode.getGameModeForPlayer().updatePlayerAbilities(serverPlayer.getAbilities());
        abilities.setFlyingSpeed(0.05F);
        serverPlayer.onUpdateAbilities();
    }

}
