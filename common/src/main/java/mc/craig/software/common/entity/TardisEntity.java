package mc.craig.software.common.entity;

import mc.craig.software.client.FlightModeClient;
import mc.craig.software.util.RWFTeleport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
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
    private int timeCrouching = 0;

    public TardisEntity(EntityType<TardisEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        if (isPassenger()) {

            Entity controllingPlayer = getVehicle();

            Vec3 motion = controllingPlayer.getDeltaMovement();
            float tilt = (float) (motion.x * 25);
            setXRot(-tilt);

            if (!controllingPlayer.isOnGround()) {
                setYRot(controllingPlayer.getYRot());
                timeCrouching = 0;
            } else {
                if (controllingPlayer.isCrouching()) {
                    timeCrouching++;
                }
            }

            if(timeCrouching >= 100){
                finishFlight();
            }

            if (level.isClientSide) {
                FlightModeClient.tick();
            }


            flightEffects(controllingPlayer);


            if (level instanceof ServerLevel serverLevel) {
                if (getTardisLevel().dimensionTypeId() != DimensionTypes.TARDIS) {
                    remove(RemovalReason.DISCARDED);
                }
                TardisLevelOperator.get(getTardisLevel()).ifPresent(tardisLevelOperator -> {
                    collisionTeleport(controllingPlayer, tardisLevelOperator);
                    syncFromData(serverLevel, tardisLevelOperator);
                });
            }


        } else {
            remove(RemovalReason.DISCARDED);
        }

        super.tick();

    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    private void flightEffects(Entity controllingPlayer) {
        if (controllingPlayer.horizontalCollision) {
            if (level != null) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, controllingPlayer.getX(), controllingPlayer.getY() + 1.0D, controllingPlayer.getZ(), 0.2D, 1.0D, 0.0D);
                level.addParticle(ParticleTypes.LARGE_SMOKE, controllingPlayer.getX(), controllingPlayer.getY() - 1.0D, controllingPlayer.getZ(), 0.0D, 0.5D, 0.0D);

                level.addParticle(ParticleTypes.SMOKE, controllingPlayer.getX(), controllingPlayer.getY() + 1.0D, controllingPlayer.getZ(), 0.0D, 0.2D, 0.0D);
                level.addParticle(ParticleTypes.SMOKE, controllingPlayer.getX(), controllingPlayer.getY() - 1.0D, controllingPlayer.getZ(), 0.0D, 0.2D, 0.0D);

                level.addParticle(ParticleTypes.LAVA, controllingPlayer.getX(), controllingPlayer.getY() + 1.0D, controllingPlayer.getZ(), 0.0D, 0.0D, 0.0D);
                level.addParticle(ParticleTypes.LAVA, controllingPlayer.getX(), controllingPlayer.getY() - 1.0D, controllingPlayer.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }

        if (controllingPlayer.tickCount % 40 == 0) {
            playSound(SoundRegistry.TARDIS_SINGLE_FLY.get(), 0.2F, 1F);
        }
    }

    private void syncFromData(ServerLevel serverLevel, TardisLevelOperator tardisLevelOperator) {
        // Match Tardis
        TardisInternalDoor door = tardisLevelOperator.getInternalDoor();
        if (door != null) {
            setDoorOpen(door.isOpen());
        }
        setShellTheme(tardisLevelOperator.getExteriorManager().getCurrentTheme());
        updateLastKnownPosition(serverLevel, tardisLevelOperator);
    }

    private void updateLastKnownPosition(ServerLevel serverLevel, TardisLevelOperator tardisLevelOperator) {
        if (serverLevel.dimensionTypeId() != DimensionTypes.TARDIS) {
            TardisNavLocation navLocation = new TardisNavLocation(blockPosition(), Direction.fromYRot(getYRot()), serverLevel);
            if (navLocation != null) {
                tardisLevelOperator.getExteriorManager().setLastKnownLocation(navLocation);
            }
        }
    }

    private void collisionTeleport(Entity controllingPlayer, TardisLevelOperator tardisLevelOperator) {
        if (isOpen()) {
            //TP Entities
            AABB aabb = controllingPlayer.getBoundingBox();

            List<Entity> entities = controllingPlayer.level.getEntitiesOfClass(Entity.class, aabb.inflate(5));

            for (Entity entity : entities) {
                if (entity == this || entity.is(controllingPlayer)) {
                    continue;
                }
                teleportToInterior(tardisLevelOperator, entity);
            }
        }
    }

    private static void teleportToInterior(TardisLevelOperator tardisLevelOperator, Entity entity) {
        Level tpLevel = tardisLevelOperator.getLevel();
        if (tpLevel instanceof ServerLevel finalTpLevel) {
            BlockPos pos = tardisLevelOperator.getInternalDoor().getDoorPosition();
            pos = pos.relative(tardisLevelOperator.getInternalDoor().getEntryRotation(), 1);
            RWFTeleport.performTeleport(entity, finalTpLevel, pos.getX(), pos.getY(), pos.getZ(), entity.getYRot(), entity.getXRot());
        }
    }


    @Override
    public boolean canCollideWith(Entity entity) {
        return super.canCollideWith(entity);
    }

    public boolean checkEntityCollision(Entity me, Entity you) {
        if (canCollideWith(you)) {
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

    public void setDoorOpen(boolean open) {
        getEntityData().set(DOOR, open);
    }

    public boolean isOpen() {
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
            tardisLevelOperator.setDoorClosed(true);
            TardisEntity tardis = new TardisEntity(RWFEntityTypes.TARDIS.get(), tardisLvl);
            tardis.setShellTheme(tardisLevelOperator.getExteriorManager().getCurrentTheme());
            tardis.setDimension(tardisLevelOperator.getLevel().dimension());
            tardis.setPos(lastKnown.position.getX(), lastKnown.position.getY(), lastKnown.position.getZ());
            FlightTracker.setInFlight(tardis, tardisLvl.dimension());
            serverPlayer.teleportTo(lastKnown.level, lastKnown.position.getX(), lastKnown.position.getY(), lastKnown.position.getZ(), 0, 0);
            FlightTracker.setUpPlayerForFlight(serverPlayer);
            lastKnown.level.addFreshEntity(tardis);
            tardis.startRiding(serverPlayer, true);
        });
    }

    public void finishFlight() {
        ServerLevel tardisLvl = getTardisLevel();
        TardisLevelOperator.get(tardisLvl).ifPresent(tardisLevelOperator -> {

            if (this.getVehicle() instanceof ServerPlayer serverPlayer) {
                FlightTracker.restorePlayer(serverPlayer);
                teleportToInterior(tardisLevelOperator, serverPlayer);
            }

            if (level instanceof ServerLevel serverLevel) {
                updateLastKnownPosition(serverLevel, tardisLevelOperator);
                FlightTracker.stopFlying(tardisLevelOperator.getLevel().dimension());
            }

            discard();

        });
    }
}
