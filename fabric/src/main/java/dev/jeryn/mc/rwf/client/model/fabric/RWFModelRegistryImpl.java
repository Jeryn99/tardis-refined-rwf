package dev.jeryn.mc.rwf.client.model.fabric;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

import java.util.Objects;
import java.util.function.Supplier;

public class RWFModelRegistryImpl {

    public static ModelLayerLocation register(ModelLayerLocation location, Supplier<LayerDefinition> definition) {
        Objects.requireNonNull(definition);
        EntityModelLayerRegistry.registerModelLayer(location, definition::get);
        return location;
    }
}
