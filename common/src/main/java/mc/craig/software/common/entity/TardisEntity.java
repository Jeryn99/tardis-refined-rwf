package mc.craig.software.common.entity;

import mc.craig.software.client.FlightModeClient;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import whocraft.tardis_refined.common.blockentity.door.TardisInternalDoor;
import whocraft.tardis_refined.common.capability.TardisLevelOperator;
import whocraft.tardis_refined.common.tardis.TardisNavLocation;
import whocraft.tardis_refined.common.tardis.themes.ShellTheme;
import whocraft.tardis_refined.common.util.MiscHelper;
import whocraft.tardis_refined.common.util.Platform;
import whocraft.tardis_refined.registry.DimensionTypes;
import whocraft.tardis_refined.registry.SoundRegistry;

import java.util.List;

public class TardisEntity extends Entity {

    public static final EntityDataAccessor<String> SHELL_THEME = SynchedEntityData.defineId(TardisEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> DIMENSION = SynchedEntityData.defineId(TardisEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Boolean> DOOR = SynchedEntityData.defineId(TardisEntity.class, EntityDataSerializers.BOOLEAN);

    public TardisEntity(EntityType<TardisEntity> entityType, Level level) {
        super(entityType, level);
    }
    @Override
    public void tick() {
        super.tick();

        if (isPassenger()) {
            Vec3 motion = getVehicle().getDeltaMovement();
            float tilt = (float) (motion.x * 25);
            setXRot(-tilt);

            if (!getVehicle().isOnGround()) {
                setYRot(getVehicle().getYRot());
            }

            if (level.isClientSide) {
                FlightModeClient.tick();
            }

            if (getVehicle().tickCount % 40 == 0) {
                playSound(SoundRegistry.TARDIS_SINGLE_FLY.get(), 0.2F, 1F);
            }
        }


        if (level instanceof ServerLevel serverLevel) {

            if (getTardisLevel().dimensionTypeId() != DimensionTypes.TARDIS) {
                remove(RemovalReason.DISCARDED);
            }

            TardisLevelOperator.get(getTardisLevel()).ifPresent(tardisLevelOperator -> {

                //TP Entities
                List<Entity> entities = serverLevel.getEntities(null, getBoundingBox());
                //TODO


                // Match Tardis
                TardisInternalDoor door = tardisLevelOperator.getInternalDoor();
                if(door != null) {
                    setDoorOpen(door.isOpen());
                }
                setShellTheme(tardisLevelOperator.getExteriorManager().getCurrentTheme());
                if (serverLevel.dimensionTypeId() != DimensionTypes.TARDIS) {
                    TardisNavLocation navLocation = new TardisNavLocation(blockPosition(), Direction.fromYRot(getYRot()), serverLevel);
                    if (navLocation != null) {
                        tardisLevelOperator.getExteriorManager().setLastKnownLocation(navLocation);
                    }
                }
            });
        }

    }


    @Override
    public boolean canCollideWith(Entity entity) {
        return super.canCollideWith(entity);
    }

    public boolean checkEntityCollision(Entity me, Entity you) {
        if(canCollideWith(you)) {
            AABB boundingBox1 = me.getBoundingBox();
            AABB boundingBox2 = you.getBoundingBox();
            return boundingBox1.intersects(boundingBox2);
        }
        return false;
    }

    @Override
    public void move(MoverType moverType, Vec3 vec3) {
        super.move(moverType, vec3);
    }

    public void setDimension(ResourceKey resourceKey) {
        getEntityData().set(DIMENSION, resourceKey.location().toString());
    }

    @Override
    public double getMyRidingOffset() {
        return -1;
    }


    public ShellTheme getShellTheme() {
        return ShellTheme.findOr(getEntityData().get(SHELL_THEME), ShellTheme.FACTORY);
    }

    public void setShellTheme(ShellTheme shellTheme) {
        getEntityData().set(SHELL_THEME, shellTheme.getSerializedName());
    }

    public void setShellTheme(String shellTheme) {
        setShellTheme(ShellTheme.findOr(shellTheme, ShellTheme.FACTORY));
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DIMENSION, Level.OVERWORLD.location().toString());
        entityData.define(SHELL_THEME, ShellTheme.FACTORY.getSerializedName());
        entityData.define(DOOR, false);
    }

    public void setDoorOpen(boolean open){
        getEntityData().set(DOOR, open);
    }

    public boolean isOpen(){
        return getEntityData().get(DOOR);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        setShellTheme(compoundTag.getString("shell_theme"));
        setDoorOpen(compoundTag.getBoolean("open"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putString("shell_theme", getShellTheme().getSerializedName());
        compoundTag.putBoolean("open", isOpen());
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return MiscHelper.spawnPacket(this);
    }

    public ServerLevel getTardisLevel() {
        return Platform.getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(getEntityData().get(DIMENSION))));
    }

    public static ServerLevel getLevel(ResourceLocation resourceLocation) {
        return Platform.getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, resourceLocation));
    }

    public static void createTardis(ServerLevel tardisLvl, ServerPlayer serverPlayer) {
        TardisLevelOperator.get(tardisLvl).ifPresent(tardisLevelOperator -> {
            TardisNavLocation lastKnown = tardisLevelOperator.getExteriorManager().getLastKnownLocation();
            TardisEntity tardis = new TardisEntity(RWFEntityTypes.TARDIS.get(), tardisLvl);
            tardis.setShellTheme(tardisLevelOperator.getExteriorManager().getCurrentTheme());
            tardis.setDimension(tardisLevelOperator.getLevel().dimension());
            tardis.setPos(lastKnown.position.getX(), lastKnown.position.getY(), lastKnown.position.getZ());
            serverPlayer.teleportTo(lastKnown.level, lastKnown.position.getX(), lastKnown.position.getY(), lastKnown.position.getZ(), 0, 0);
            lastKnown.level.addFreshEntity(tardis);
            tardis.startRiding(serverPlayer, true);
        });
    }
}
