package dev.jeryn.mc.rwf.client.sound;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import whocraft.tardis_refined.client.sounds.LoopingTardisInteriorSound;

public class FlyingSound extends LoopingTardisInteriorSound {
    private float smoothed = 0;

    public FlyingSound(SoundEvent soundEvent, SoundSource soundSource) {
        super(soundEvent, soundSource);
        looping = true;
    }

    @Override
    public void playSoundInstance(Player player) {
        this.setLocation(player.position());

        Vec3 vel = player.getDeltaMovement();
        float speed = (float) Math.sqrt(vel.x * vel.x + vel.z * vel.z);

        smoothed = smoothed * 0.85F + speed * 0.15F;

        float idle = 0.15F;
        float movement = Mth.clamp(smoothed * 2.5F, 0.0F, 0.6F);

        float volume = idle + movement;

        setVolume(Mth.clamp(volume, 0.15F, 0.75F));
    }

    @Override
    public boolean canPlaySound() {
        return true;
    }
}