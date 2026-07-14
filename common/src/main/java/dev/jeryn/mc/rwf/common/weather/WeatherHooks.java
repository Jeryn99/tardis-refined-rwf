package dev.jeryn.mc.rwf.common.weather;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class WeatherHooks {

    private static final WeatherHook NOOP = new WeatherHook() {
        @Override
        public Vec3 getWindForce(Level level, BlockPos pos) {
            return Vec3.ZERO;
        }

        @Override
        public TornadoThreat getNearestTornadoThreat(Level level, Vec3 pos, double maxDist) {
            return null;
        }

        @Override
        public Vec3 applyTornadoSpin(Level level, Vec3 pos, Vec3 motion, boolean forPlayer, double maxDist) {
            return motion;
        }
    };

    private static WeatherHook active = NOOP;

    private WeatherHooks() {
    }

    public static void set(WeatherHook hook) {
        active = hook != null ? hook : NOOP;
    }

    public static WeatherHook get() {
        return active;
    }
}
