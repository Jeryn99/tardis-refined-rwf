package dev.jeryn.mc.rwf.common.entity;

import dev.jeryn.mc.rwf.RealWorldFlight;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import whocraft.tardis_refined.registry.DeferredRegistry;
import whocraft.tardis_refined.registry.RegistrySupplier;

public class RWFEntityTypes {
    public static DeferredRegistry<EntityType<?>> ENTITY_TYPES = DeferredRegistry.create(RealWorldFlight.MOD_ID, BuiltInRegistries.ENTITY_TYPE.key());
    public static RegistrySupplier<EntityType<TardisEntity>> TARDIS = ENTITY_TYPES.register("tardis", () -> EntityType.Builder.of(TardisEntity::new, MobCategory.MISC).sized(0.6F, 1.95F).build(RealWorldFlight.MOD_ID + ":tardis"));


}
