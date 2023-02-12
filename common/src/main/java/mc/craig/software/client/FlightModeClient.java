package mc.craig.software.client;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;

public class FlightModeClient {

    public static void tick() {
        ClientLevel level = Minecraft.getInstance().level;
        LocalPlayer player = Minecraft.getInstance().player;
        player.getAbilities().setFlyingSpeed(0.16F);
        Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
        Minecraft.getInstance().options.fov().set(90);
    }

}
