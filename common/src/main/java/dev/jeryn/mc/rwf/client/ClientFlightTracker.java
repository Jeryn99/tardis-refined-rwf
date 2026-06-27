package dev.jeryn.mc.rwf.client;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClientFlightTracker {

    private static Map<ResourceKey<Level>, ClientFlightData> IN_FLIGHT = Collections.emptyMap();

    public static void setFlying(Map<ResourceKey<Level>, ClientFlightData> data) {
        IN_FLIGHT = new HashMap<>(data);
    }

    public static boolean isFlying(ResourceKey<Level> key) {
        return IN_FLIGHT.containsKey(key);
    }

    public static Optional<ClientFlightData> get(ResourceKey<Level> key) {
        return Optional.ofNullable(IN_FLIGHT.get(key));
    }

    public static Map<ResourceKey<Level>, ClientFlightData> getAll() {
        return Collections.unmodifiableMap(IN_FLIGHT);
    }
}
