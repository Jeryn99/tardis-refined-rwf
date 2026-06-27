package dev.jeryn.mc.rwf.client;

import dev.jeryn.mc.rwf.client.sound.FlyingSound;
import dev.jeryn.mc.rwf.common.entity.TardisEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;
import whocraft.tardis_refined.registry.TRSoundRegistry;

public class FlightModeClient {

    public static FlyingSound TARDIS_SINGLE_FLYING =
            new FlyingSound(TRSoundRegistry.TARDIS_SINGLE_FLY.get(), SoundSource.AMBIENT);

    public static FlyingSound LOW_FUEL_NOISE =
            new FlyingSound(TRSoundRegistry.ALARM.get(), SoundSource.AMBIENT);

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        SoundManager soundManager = mc.getSoundManager();

        if (player == null || mc.level == null) return;

        if (player.getFirstPassenger() instanceof TardisEntity tardis) {

            Options options = mc.options;
            options.setCameraType(CameraType.THIRD_PERSON_BACK);
            options.fov().set(90);

            if (!soundManager.isActive(TARDIS_SINGLE_FLYING)) {
                soundManager.play(
                        TARDIS_SINGLE_FLYING
                                .setPlayer(player)
                                .setLevel(mc.level)
                );
            }

            if (!soundManager.isActive(LOW_FUEL_NOISE)) {
                soundManager.play(
                        LOW_FUEL_NOISE
                                .setPlayer(player)
                                .setLevel(mc.level)
                );
            }

        } else {
            soundManager.stop(TARDIS_SINGLE_FLYING);
            soundManager.stop(LOW_FUEL_NOISE);
        }
    }
}
