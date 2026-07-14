package dev.jeryn.mc.rwf.common.weather;

import net.minecraft.world.phys.Vec3;

public record TornadoThreat(Vec3 funnelCenter, double distance, float intensity) {
}
