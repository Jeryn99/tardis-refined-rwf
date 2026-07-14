package dev.jeryn.mc.rwf.forge.compat.weather2;

import dev.jeryn.mc.rwf.common.weather.TornadoThreat;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class Weather2ReflectionBridge {

    private static volatile boolean available = false;

    private static Method windReader_getWeatherManagerFor;
    private static Method weatherManager_getWindManager;
    private static Method weatherManager_getClosestStorm;
    private static Method windManager_getWindForce;
    private static Method stormObject_isTornadoFormingOrGreater;
    private static Method stormObject_getFunnelCenter;
    private static Method stormObject_spinObject;
    private static Field stormObject_pos;
    private static Field stormObject_levelCurIntensityStage;
    private static int stateForming;
    private static int stateStage5;

    private Weather2ReflectionBridge() {
    }

    public static synchronized void init() {
        if (available) return;

        try {
            Class<?> windReaderClass = Class.forName("weather2.util.WindReader");
            Class<?> weatherManagerClass = Class.forName("weather2.weathersystem.WeatherManager");
            Class<?> windManagerClass = Class.forName("weather2.weathersystem.wind.WindManager");
            Class<?> stormObjectClass = Class.forName("weather2.weathersystem.storm.StormObject");

            windReader_getWeatherManagerFor = windReaderClass.getMethod("getWeatherManagerFor", Level.class);
            weatherManager_getWindManager = weatherManagerClass.getMethod("getWindManager");
            weatherManager_getClosestStorm = weatherManagerClass.getMethod("getClosestStorm", Vec3.class, double.class, int.class);
            windManager_getWindForce = windManagerClass.getMethod("getWindForce", BlockPos.class);
            stormObject_isTornadoFormingOrGreater = stormObjectClass.getMethod("isTornadoFormingOrGreater");
            stormObject_getFunnelCenter = stormObjectClass.getMethod("getFunnelCenter", Vec3.class);
            stormObject_spinObject = stormObjectClass.getMethod("spinObject",
                    Vec3.class, Vec3.class, boolean.class, float.class, float.class, boolean.class, float.class);
            stormObject_pos = stormObjectClass.getField("pos");
            stormObject_levelCurIntensityStage = stormObjectClass.getField("levelCurIntensityStage");

            stateForming = stormObjectClass.getField("STATE_FORMING").getInt(null);
            stateStage5 = stormObjectClass.getField("STATE_STAGE5").getInt(null);

            available = true;
        } catch (Throwable t) {
            available = false;
        }
    }

    public static boolean isAvailable() {
        return available;
    }

    public static Vec3 getWindForce(Level level, BlockPos pos) {
        if (!available) return Vec3.ZERO;
        try {
            Object weatherManager = windReader_getWeatherManagerFor.invoke(null, level);
            if (weatherManager == null) return Vec3.ZERO;

            Object windManager = weatherManager_getWindManager.invoke(weatherManager);
            if (windManager == null) return Vec3.ZERO;

            Object force = windManager_getWindForce.invoke(windManager, pos);
            return force instanceof Vec3 vec ? vec : Vec3.ZERO;
        } catch (Throwable t) {
            return Vec3.ZERO;
        }
    }

    public static TornadoThreat getNearestTornadoThreat(Level level, Vec3 pos, double maxDist) {
        if (!available) return null;
        try {
            Object weatherManager = windReader_getWeatherManagerFor.invoke(null, level);
            if (weatherManager == null) return null;

            Object storm = weatherManager_getClosestStorm.invoke(weatherManager, pos, maxDist, stateForming);
            if (storm == null) return null;

            boolean isTornado = (boolean) stormObject_isTornadoFormingOrGreater.invoke(storm);
            if (!isTornado) return null;

            Object funnelCenterObj = stormObject_getFunnelCenter.invoke(storm, pos);
            Object stormPosObj = stormObject_pos.get(storm);
            int stage = stormObject_levelCurIntensityStage.getInt(storm);

            if (!(funnelCenterObj instanceof Vec3 funnelCenter)) return null;

            double distance = stormPosObj instanceof Vec3 stormPos ? stormPos.distanceTo(pos) : maxDist;
            float intensity = Mth.clamp((float) (stage - stateForming) / (float) (stateStage5 - stateForming), 0F, 1F);

            return new TornadoThreat(funnelCenter, distance, intensity);
        } catch (Throwable t) {
            return null;
        }
    }

    public static Vec3 applyTornadoSpin(Level level, Vec3 pos, Vec3 motion, boolean forPlayer, double maxDist) {
        if (!available) return motion;
        try {
            Object weatherManager = windReader_getWeatherManagerFor.invoke(null, level);
            if (weatherManager == null) return motion;

            Object storm = weatherManager_getClosestStorm.invoke(weatherManager, pos, maxDist, stateForming);
            if (storm == null) return motion;

            boolean isTornado = (boolean) stormObject_isTornadoFormingOrGreater.invoke(storm);
            if (!isTornado) return motion;

            Object result = stormObject_spinObject.invoke(storm, pos, motion, forPlayer, 1.0F, 1.0F, false, 0.0F);
            return result instanceof Vec3 spun ? spun : motion;
        } catch (Throwable t) {
            return motion;
        }
    }
}
