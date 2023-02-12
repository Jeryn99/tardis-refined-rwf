package mc.craig.software.fabric;

import mc.craig.software.client.FlightModeClient;
import mc.craig.software.network.RWFOpenDoor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public class FabricEvents {

    private static long lastOpened = System.currentTimeMillis();

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            // Client Effects
            FlightModeClient.tick();

            // Handle Door Opening
            long currentTime = System.currentTimeMillis();
            if (Minecraft.getInstance().options.keyAttack.isDown() && currentTime - lastOpened >= 5000) {
                new RWFOpenDoor().send();
                lastOpened = currentTime;
            }


        });
    }

}
