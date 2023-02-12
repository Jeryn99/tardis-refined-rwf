package mc.craig.software.client;

import mc.craig.software.common.entity.TardisEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;

public class FlightModeClient {



    public static void tick() {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null && player.getFirstPassenger() instanceof TardisEntity tardis) {
            Options options = Minecraft.getInstance().options;
            options.setCameraType(CameraType.THIRD_PERSON_BACK);
            options.fov().set(90);
        }
    }

}
