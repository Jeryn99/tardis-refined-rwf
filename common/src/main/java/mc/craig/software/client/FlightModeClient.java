package mc.craig.software.client;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;

public class FlightModeClient {

    public static void tick() {
        ClientLevel level = Minecraft.getInstance().level;
        LocalPlayer player = Minecraft.getInstance().player;

        player.getAbilities().setFlyingSpeed(0.16F);
        Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
        Minecraft.getInstance().options.fov().set(90);

        if(player.horizontalCollision) {
            if (level != null) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 1.0D, player.getZ(), 0.2D, 1.0D, 0.0D);
                level.addParticle(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() - 1.0D, player.getZ(), 0.0D, 0.5D, 0.0D);

                level.addParticle(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.0D, player.getZ(), 0.0D, 0.2D, 0.0D);
                level.addParticle(ParticleTypes.SMOKE, player.getX(), player.getY() - 1.0D, player.getZ(), 0.0D, 0.2D, 0.0D);

                level.addParticle(ParticleTypes.LAVA, player.getX(), player.getY() + 1.0D, player.getZ(), 0.0D, 0.0D, 0.0D);
                level.addParticle(ParticleTypes.LAVA, player.getX(), player.getY() - 1.0D, player.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }
    }

}
