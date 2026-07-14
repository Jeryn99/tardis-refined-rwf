package dev.jeryn.mc.rwf.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

    public static Optional<ClientFlightData> getForPlayer(UUID playerUuid) {
        return IN_FLIGHT.values().stream()
                .filter(data -> data.pilot().equals(playerUuid))
                .findFirst();
    }

    public static void applySpectatorPitchTilt(Player player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != player) return;
        if (getForPlayer(player.getUUID()).isEmpty()) return;
        if (!player.getAbilities().flying) return;

        Vec3 delta = player.getDeltaMovement();
        double horizontalSpeed = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        if (horizontalSpeed < 1.0E-6) return;

        float pitchRad = player.getXRot() * ((float) Math.PI / 180F);
        double climb = -Math.sin(pitchRad) * horizontalSpeed;
        double newHorizontalSpeed = horizontalSpeed * Math.cos(pitchRad);
        double scale = newHorizontalSpeed / horizontalSpeed;

        player.setDeltaMovement(delta.x * scale, delta.y + climb, delta.z * scale);
    }
}
