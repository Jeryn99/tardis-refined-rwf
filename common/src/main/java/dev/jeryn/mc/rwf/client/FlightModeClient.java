package dev.jeryn.mc.rwf.client;

import dev.jeryn.mc.rwf.client.sound.FlyingSound;
import dev.jeryn.mc.rwf.common.entity.TardisEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;
import whocraft.tardis_refined.client.TardisClientData;
import whocraft.tardis_refined.registry.TRSoundRegistry;

public class FlightModeClient {

    public static FlyingSound TARDIS_SINGLE_FLYING =
            new FlyingSound(TRSoundRegistry.TARDIS_SINGLE_FLY.get(), SoundSource.AMBIENT);

    public static FlyingSound LOW_FUEL_NOISE =
            new FlyingSound(TRSoundRegistry.ALARM.get(), SoundSource.AMBIENT);

    private static boolean savedSettings = false;
    private static CameraType previousCameraType = CameraType.FIRST_PERSON;
    private static int previousFov = 70;

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        SoundManager soundManager = mc.getSoundManager();

        if (player == null || mc.level == null) return;

        Options options = mc.options;

        if (player.getFirstPassenger() instanceof TardisEntity tardis) {

            if (!savedSettings) {
                previousCameraType = options.getCameraType();
                previousFov = options.fov().get();
                savedSettings = true;
            }

            options.setCameraType(CameraType.THIRD_PERSON_BACK);
            options.fov().set(90);

            if (!soundManager.isActive(TARDIS_SINGLE_FLYING)) {
                soundManager.play(
                        TARDIS_SINGLE_FLYING
                                .setPlayer(player)
                                .setLevel(mc.level)
                );
            }

            // This alarm previously played constantly for the entire flight instead of
            // only when fuel actually ran low - it never checked the fuel level at all.
            TardisClientData tardisClientData = TardisClientData.getInstance(tardis.getTardisDimension());
            boolean lowFuel = (tardisClientData.getFuel() / 1000.0f) <= 0.20f;

            if (lowFuel) {
                if (!soundManager.isActive(LOW_FUEL_NOISE)) {
                    soundManager.play(
                            LOW_FUEL_NOISE
                                    .setPlayer(player)
                                    .setLevel(mc.level)
                    );
                }
            } else {
                soundManager.stop(LOW_FUEL_NOISE);
            }

        } else {
            soundManager.stop(TARDIS_SINGLE_FLYING);
            soundManager.stop(LOW_FUEL_NOISE);

            if (savedSettings) {
                options.setCameraType(previousCameraType);
                options.fov().set(previousFov);
                savedSettings = false;
            }
        }
    }
}
