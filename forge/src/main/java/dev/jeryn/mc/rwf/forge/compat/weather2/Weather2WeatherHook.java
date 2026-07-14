package dev.jeryn.mc.rwf.forge.compat.weather2;

import dev.jeryn.mc.rwf.common.weather.TornadoThreat;
import dev.jeryn.mc.rwf.common.weather.WeatherHook;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Weather2WeatherHook implements WeatherHook {

    @Override
    public Vec3 getWindForce(Level level, BlockPos pos) {
        return Weather2ReflectionBridge.getWindForce(level, pos);
    }

    @Override
    public TornadoThreat getNearestTornadoThreat(Level level, Vec3 pos, double maxDist) {
        return Weather2ReflectionBridge.getNearestTornadoThreat(level, pos, maxDist);
    }

    @Override
    public Vec3 applyTornadoSpin(Level level, Vec3 pos, Vec3 motion, boolean forPlayer, double maxDist) {
        return Weather2ReflectionBridge.applyTornadoSpin(level, pos, motion, forPlayer, maxDist);
    }
}
