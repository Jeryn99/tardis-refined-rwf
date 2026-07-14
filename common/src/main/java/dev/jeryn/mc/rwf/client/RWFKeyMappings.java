package dev.jeryn.mc.rwf.client;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class RWFKeyMappings {

    public static final KeyMapping EXIT_FLIGHT = new KeyMapping(
            "key.tardis_refined_rwf.exit_flight",
            GLFW.GLFW_KEY_TAB,
            "key.categories.tardis_refined_rwf"
    );

    public static final KeyMapping FREE_FALL = new KeyMapping(
            "key.tardis_refined_rwf.free_fall",
            GLFW.GLFW_KEY_Q,
            "key.categories.tardis_refined_rwf"
    );

    public static final KeyMapping LOCAL_HOP = new KeyMapping(
            "key.tardis_refined_rwf.local_hop",
            GLFW.GLFW_KEY_R,
            "key.categories.tardis_refined_rwf"
    );

}
