package dev.jeryn.mc.rwf.client.sound;

import dev.jeryn.mc.rwf.client.ClientFlightData;
import dev.jeryn.mc.rwf.client.ClientFlightTracker;
import dev.jeryn.mc.rwf.common.entity.TardisEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import whocraft.tardis_refined.client.TardisClientData;
import whocraft.tardis_refined.client.sounds.LoopingTardisInteriorSound;
import whocraft.tardis_refined.registry.TRSoundRegistry;

public class FlyingSound extends LoopingTardisInteriorSound {
    private float smoothed = 0;

    public FlyingSound(SoundEvent soundEvent, SoundSource soundSource) {
        super(soundEvent, soundSource);
        looping = true;
    }

    @Override
    public void playSoundInstance(Player player) {


        Entity entity = player.getFirstPassenger();

        if (sound.getLocation().getPath().contains("alarm")) {
            this.setLocation(player.position());

            if (entity instanceof TardisEntity tardis) {
                TardisClientData tardisClientData = TardisClientData.getInstance(tardis.getTardisDimension());

                int fuel = (int) tardisClientData.getFuel();

                if (fuel > 0 && fuel < 100) {
                    setVolume(1);
                } else {
                    setVolume(0);
                }

                boolean isFreefalling = ClientFlightTracker.get(tardis.getTardisDimension())
                        .map(ClientFlightData::isFreefalling)
                        .orElse(false);

                if(isFreefalling){
                    setVolume(0);
                }
            }
        }

        // Previously this whole block was nested inside the "alarm" check above, so it
        // could never actually run for the flying-engine sound instance (its path never
        // contains "alarm") — the speed-reactive engine volume was permanently dead code.
        if (sound.getLocation().getPath().contains("fly")) {
            this.setLocation(player.position());

            Vec3 vel = player.getDeltaMovement();
            float speed = (float) Math.sqrt(vel.x * vel.x + vel.z * vel.z);

            smoothed = smoothed * 0.85F + speed * 0.15F;

            float idle = 0.15F;
            float movement = Mth.clamp(smoothed * 2.5F, 0.0F, 0.6F);

            float volume = idle + movement;

            setVolume(Mth.clamp(volume, 0.15F, 0.75F));

            if (entity instanceof TardisEntity tardis) {
                boolean isFreefalling = ClientFlightTracker.get(tardis.getTardisDimension())
                        .map(ClientFlightData::isFreefalling)
                        .orElse(false);

                if(isFreefalling){
                    setVolume(0);
                }
            }
        }
    }

    @Override
    public boolean canPlaySound() {
        return true;
    }
}