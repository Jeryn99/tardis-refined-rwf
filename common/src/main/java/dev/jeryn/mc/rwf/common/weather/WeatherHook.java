package dev.jeryn.mc.rwf.common.weather;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface WeatherHook {

    Vec3 getWindForce(Level level, BlockPos pos);

    TornadoThreat getNearestTornadoThreat(Level level, Vec3 pos, double maxDist);

    Vec3 applyTornadoSpin(Level level, Vec3 pos, Vec3 motion, boolean forPlayer, double maxDist);
}
