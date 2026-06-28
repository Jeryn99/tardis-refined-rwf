package dev.jeryn.mc.rwf.client.model.fabric;

import java.util.Objects;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

public class RWFModelRegistryImpl {

    public static ModelLayerLocation register(ModelLayerLocation location, Supplier<LayerDefinition> definition) {
       System.out.println("GAH! " + location.toString());
        Objects.requireNonNull(definition);
        EntityModelLayerRegistry.registerModelLayer(location, definition::get);
        return location;
    }
}
