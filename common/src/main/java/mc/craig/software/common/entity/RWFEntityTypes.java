package mc.craig.software.common.entity;

import mc.craig.software.RealWorldFlight;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import whocraft.tardis_refined.registry.DeferredRegistry;
import whocraft.tardis_refined.registry.RegistrySupplier;

public class RWFEntityTypes {
    public static DeferredRegistry<EntityType<?>> ENTITY_TYPES = DeferredRegistry.create(RealWorldFlight.MOD_ID, Registry.ENTITY_TYPE_REGISTRY);
    public static RegistrySupplier<EntityType<TardisEntity>> TARDIS = ENTITY_TYPES.register("tardis", () -> EntityType.Builder.of((EntityType.EntityFactory<TardisEntity>) TardisEntity::new, MobCategory.MISC).sized(0.6F, 1.95F).build(RealWorldFlight.MOD_ID +":tardis"));


}
