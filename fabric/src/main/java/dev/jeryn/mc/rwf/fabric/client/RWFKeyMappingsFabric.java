package dev.jeryn.mc.rwf.fabric.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class RWFKeyMappingsFabric {

    public static KeyMapping EXIT_FLIGHT;
    public static KeyMapping FREE_FALL;
    public static KeyMapping LOCAL_HOP;

    public static void init() {

        EXIT_FLIGHT = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.tardis_refined_rwf.exit_flight",
                GLFW.GLFW_KEY_TAB,
                "key.categories.tardis_refined_rwf"
        ));

        FREE_FALL = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.tardis_refined_rwf.free_fall",
                GLFW.GLFW_KEY_Q,
                "key.categories.tardis_refined_rwf"
        ));

        LOCAL_HOP = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.tardis_refined_rwf.local_hop",
                GLFW.GLFW_KEY_R,
                "key.categories.tardis_refined_rwf"
        ));
    }
}