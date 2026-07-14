package dev.jeryn.mc.rwf.client.model;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.jeryn.mc.rwf.RealWorldFlight;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class RWFModelRegistry {

    public static ModelLayerLocation PILOT = player("pilot");

    private static ModelLayerLocation player(String name) {
        return createLocation(name, "player");
    }

    private static ModelLayerLocation createLocation(String name, String layer) {
        return new ModelLayerLocation(new ResourceLocation(RealWorldFlight.MOD_ID, name), layer);
    }

    public static void init() {

    }

    @ExpectPlatform
    public static ModelLayerLocation register(ModelLayerLocation location, Supplier<LayerDefinition> definitionSupplier) {
        throw new RuntimeException("Cannot do this");
    }
}
