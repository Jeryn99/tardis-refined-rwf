package dev.jeryn.mc.rwf.common.entity;

import dev.jeryn.mc.rwf.util.RWFTeleport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import whocraft.tardis_refined.common.blockentity.door.TardisInternalDoor;
import whocraft.tardis_refined.common.capability.tardis.TardisLevelOperator;
import whocraft.tardis_refined.common.capability.tardis.upgrades.Upgrade;
import whocraft.tardis_refined.common.tardis.TardisNavLocation;
import whocraft.tardis_refined.common.tardis.themes.ShellTheme;
import whocraft.tardis_refined.common.util.MiscHelper;
import whocraft.tardis_refined.common.util.Platform;
import whocraft.tardis_refined.patterns.ShellPattern;
import whocraft.tardis_refined.registry.RegistrySupplier;
import whocraft.tardis_refined.registry.TRDimensionTypes;

import java.util.List;
import java.util.Map;

import static dev.jeryn.mc.rwf.common.upgrade.RWFUpgrades.*;

public class TardisEntity extends Entity {

    public static final Map<RegistrySupplier<Upgrade>, Float> FUEL_REDUCTION = Map.of(
            FLIGHT_EFFICIENCY_1, 0.05f,
            FLIGHT_EFFICIENCY_2, 0.10f,
            FLIGHT_EFFICIENCY_3, 0.20f,
            FLIGHT_EFFICIENCY_4, 0.35f,
            FLIGHT_EFFICIENCY_5, 0.50f
    );

    public static final EntityDataAccessor<String> SHELL_THEME =
            SynchedEntityData.defineId(TardisEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> SHELL_PATTERN =
            SynchedEntityData.defineId(TardisEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> DIMENSION =
            SynchedEntityData.defineId(TardisEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Boolean> DOOR =
            SynchedEntityData.defineId(TardisEntity.class, EntityDataSerializers.BOOLEAN);

    public static final EntityDataAccessor<Integer> RECOVERY_TICKS =
            SynchedEntityData.defineId(TardisEntity.class, EntityDataSerializers.INT);

    public float[] physicsMatrix = null;
    private Vec3 last = new Vec3(0,0,0);


    public void writePhysToNBT(CompoundTag tag) {
        if (physicsMatrix != null) {
            int[] encoded = new int[physicsMatrix.length];
            for (int i = 0; i < physicsMatrix.length; i++) {
                encoded[i] = Float.floatToIntBits(physicsMatrix[i]);
            }
            tag.putIntArray("physicsMatrix", encoded);
        }
    }

    public void readPhysFromNBT(CompoundTag tag) {
        if (tag.contains("physicsMatrix")) {
            int[] encoded = tag.getIntArray("physicsMatrix");
            physicsMatrix = new float[encoded.length];
            for (int i = 0; i < encoded.length; i++) {
                physicsMatrix[i] = Float.intBitsToFloat(encoded[i]);
            }
        } else {
            physicsMatrix = null;
        }
    }


    public TardisEntity(EntityType<TardisEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static void createTardis(ServerLevel tardisLvl, ServerPlayer serverPlayer) {
        TardisLevelOperator.get(tardisLvl).ifPresent(tardisLevelOperator -> {
            try {
                TardisNavLocation lastKnown = tardisLevelOperator.getPilotingManager().getCurrentLocation();

                if (lastKnown == null || lastKnown.getLevel() == null) {
                    System.out.println("[RWF] createTardis abort: currentLocation null");
                    return;
                }

                tardisLevelOperator.setDoorClosed(true);
                tardisLevelOperator.getExteriorManager().removeExteriorBlock();

                TardisEntity tardis = new TardisEntity(RWFEntityTypes.TARDIS.get(), tardisLvl);
                tardis.setDimension(tardisLvl.dimension());
                tardis.setPos(
                        lastKnown.getPosition().getX(),
                        lastKnown.getPosition().getY(),
                        lastKnown.getPosition().getZ()
                );

                FlightTracker.setInFlight(
                        new FlightTracker.FlightData(tardis, serverPlayer, false),
                        tardisLvl.dimension()
                );

                serverPlayer.teleportTo(
                        lastKnown.getLevel(),
                        lastKnown.getPosition().getX(),
                        lastKnown.getPosition().getY(),
                        lastKnown.getPosition().getZ(),
                        0, 0
                );

                FlightTracker.setUpPlayerForFlight(serverPlayer);

                ServerLevel flightLevel = serverPlayer.serverLevel();

                tardis.setPos(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ());
                flightLevel.addFreshEntity(tardis);

                forceMount(tardis, serverPlayer);
                resyncPassengers(serverPlayer);

                try {
                    tardis.applyShellAesthetic(tardisLevelOperator);
                } catch (Throwable t) {
                    System.out.println("[RWF] applyShellAesthetic failed (mount kept) " + t.toString());
                }

            } catch (Throwable t) {
                System.out.println("[RWF] createTardis threw " + t);
            }
        });
    }

    private static void teleportToInterior(TardisLevelOperator op, Entity e) {
        if (op == null || e == null) {
            return;
        }

        Level level = op.getLevel();
        if (!(level instanceof ServerLevel server)) {
            return;
        }

        var door = op.getInternalDoor();
        if (door == null) {
            return;
        }

        BlockPos pos = door.getTeleportPosition();
        if (pos == null) {
            return;
        }

        RWFTeleport.performTeleport(
                e,
                server,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                e.getYRot(),
                e.getXRot()
        );
    }

    public static ServerLevel getLevel(ResourceLocation rl) {
        return Platform.getServer().getLevel(
                ResourceKey.create(Registries.DIMENSION, rl));
    }

    private static void forceMount(TardisEntity tardis, ServerPlayer player) {
        if (tardis.getVehicle() == player) return;
        tardis.setPose(Pose.STANDING);
        tardis.vehicle = player;
        player.addPassenger(tardis);
    }

    private static void forceDismount(TardisEntity tardis) {
        Entity v = tardis.vehicle;
        if (v != null) {
            tardis.vehicle = null;
            tardis.stopRiding();
        }
    }

    private static void resyncPassengers(Entity vehicle) {
        if (vehicle.level() instanceof ServerLevel server) {
            server.getChunkSource().broadcastAndSend(
                    vehicle,
                    new ClientboundSetPassengersPacket(vehicle)
            );
        }
    }

    private static BlockPos findGroundBelow(ServerLevel level, BlockPos start) {

        BlockPos.MutableBlockPos pos = start.mutable();
        int minY = level.getMinBuildHeight() + 1;

        while (pos.getY() > minY) {

            BlockState here = level.getBlockState(pos);
            BlockState below = level.getBlockState(pos.below());

            boolean open = here.isAir() || here.canBeReplaced();
            boolean solid = !below.isAir() && !below.canBeReplaced();

            if (open && solid) return pos.immutable();

            pos.move(Direction.DOWN);
        }

        return start;
    }

    /* ---------------- BLOCK IMPACT SYSTEM ---------------- */

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            FlightTracker.transitionToVortex(this);
        }


        if (!(level() instanceof ServerLevel serverLevel)) return;

        if(getRecoveryTicks() > 0){
            setRecoveryTicks(getRecoveryTicks() - 1);
        }

        if (isPassenger()) {

            Entity controllingPlayer = getVehicle();
            if (controllingPlayer == null) return;

            Vec3 motion = controllingPlayer.getDeltaMovement();

            setXRot((float) -(motion.x * 25));
            setYRot(controllingPlayer.getYRot());

            flightEffects(controllingPlayer);
            groundEffects(controllingPlayer);


            // blockImpact((ServerLevel) controllingPlayer.level(), controllingPlayer);

            if (tickCount <= 60 && tickCount % 10 == 0) {
                resyncPassengers(controllingPlayer);
            }

            if (getTardisLevel(serverLevel).dimensionTypeId() != TRDimensionTypes.TARDIS) {
                System.out.println("[RWF] Killing Tardis due to no Dimension");
                finishFlight(serverLevel, false);
                discard();
                return;
            }

            collisionDamage(controllingPlayer, serverLevel);

            TardisLevelOperator.get(getTardisLevel(serverLevel)).ifPresent(op -> {
                op.getPilotingManager().setCurrentLocation(new TardisNavLocation(this.blockPosition(), Direction.NORTH, this.level().dimension()));
                collisionTeleport(controllingPlayer, op);
                syncFromData(serverLevel, op);

                float reduction = 0f;

                if (op.getUpgradeHandler().isUpgradeUnlocked(FLIGHT_EFFICIENCY_5.get())) {
                    reduction = FUEL_REDUCTION.get(FLIGHT_EFFICIENCY_5);
                } else if (op.getUpgradeHandler().isUpgradeUnlocked(FLIGHT_EFFICIENCY_4.get())) {
                    reduction = FUEL_REDUCTION.get(FLIGHT_EFFICIENCY_4);
                } else if (op.getUpgradeHandler().isUpgradeUnlocked(FLIGHT_EFFICIENCY_3.get())) {
                    reduction = FUEL_REDUCTION.get(FLIGHT_EFFICIENCY_3);
                } else if (op.getUpgradeHandler().isUpgradeUnlocked(FLIGHT_EFFICIENCY_2.get())) {
                    reduction = FUEL_REDUCTION.get(FLIGHT_EFFICIENCY_2);
                } else if (op.getUpgradeHandler().isUpgradeUnlocked(FLIGHT_EFFICIENCY_1.get())) {
                    reduction = FUEL_REDUCTION.get(FLIGHT_EFFICIENCY_1);
                }

                float fuelCost = (1f - reduction);

                Vec3 current = controllingPlayer.position();


                boolean isMoving =
                        Math.abs(current.x - last.x) > 0.01 ||
                                Math.abs(current.y - last.y) > 0.01 ||
                                Math.abs(current.z - last.z) > 0.01;

                last = current;


                if (isMoving || controllingPlayer.level().getBlockState(blockPosition().below()).isAir() && controllingPlayer.fallDistance <= 0) {
                    op.getPilotingManager().removeFuel(fuelCost);
                }
            });

        } else {

            FlightTracker.FlightData data =
                    FlightTracker.IN_FLIGHT.get(getTardisLevel(serverLevel).dimension());

            if (data == null) {
                discard();
                return;
            }

            ServerPlayer player =
                    serverLevel.getServer().getPlayerList().getPlayer(data.player().getUUID());

            if (player != null) {
                forceMount(this, player);
                resyncPassengers(player);
            }

            if (tickCount >= 100) {
                finishFlight(serverLevel, false);
                discard();
            }
        }
    }

    private void syncFromData(ServerLevel serverLevel, TardisLevelOperator tardisLevelOperator) {
        TardisInternalDoor door = tardisLevelOperator.getInternalDoor();
        if (door != null) {
            setDoorOpen(door.isOpen());
        }

        applyShellAesthetic(tardisLevelOperator);
        updateLastKnownPosition(serverLevel, tardisLevelOperator);
    }

    /* ---------------- EFFECTS ---------------- */

    public ShellTheme getShellTheme() {
        return ShellTheme.getShellTheme(getShellThemeId());
    }

    public void setShellTheme(ResourceLocation rl) {
        getEntityData().set(SHELL_THEME, rl.toString());
    }

    public void setRecoveryTicks(int ticks) {
        getEntityData().set(RECOVERY_TICKS, ticks);
    }

    public Integer getRecoveryTicks() {
        return getEntityData().get(RECOVERY_TICKS);
    }

    /* ---------------- CORE DAMAGE ---------------- */

    public ResourceKey<Level> getTardisDimension() {
        return ResourceKey.create(
                Registries.DIMENSION,
                new ResourceLocation(getEntityData().get(DIMENSION))
        );
    }

    private void applyShellAesthetic(TardisLevelOperator operator) {
        ResourceLocation theme = operator.getAestheticHandler().getShellTheme();
        if (theme != null) {
            setShellTheme(theme);
        }

        ShellPattern pattern = operator.getAestheticHandler().shellPattern();
        if (pattern != null && pattern.id() != null) {
            setShellPattern(pattern.id());
        }
    }

    private void updateLastKnownPosition(ServerLevel serverLevel, TardisLevelOperator operator) {
        if (serverLevel.dimensionTypeId() != TRDimensionTypes.TARDIS) {
            TardisNavLocation navLocation =
                    new TardisNavLocation(
                            blockPosition(),
                            Direction.fromYRot(getYRot()),
                            serverLevel
                    );

            operator.getPilotingManager().setCurrentLocation(navLocation);
        }
    }

    /* ---------------- DATA ---------------- */

    private void blockImpact(ServerLevel level, Entity pilot) {

        Vec3 motion = pilot.getDeltaMovement();
        double speed = motion.length() * 23;

        //    if (!pilot.isSprinting()) return;
        //   if (speed < 0.25D) return;

        Vec3 center = pilot.position();
        double radius = 2.5D + speed * 3.5D;

        BlockPos min = BlockPos.containing(center.subtract(radius, radius, radius));
        BlockPos max = BlockPos.containing(center.add(radius, radius, radius));

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {

                    pos.set(x, y, z);

                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) continue;

                    double dist = pos.distToCenterSqr(center);
                    double maxDist = radius * radius;

                    if (dist > maxDist) continue;

                    // normalize blast falloff (0 = center, 1 = edge)
                    double t = 1.0D - (Math.sqrt(dist) / radius);

                    level.removeBlock(pos, false);
                    spawnFlyingBlock(level, pos.immutable(), state, pilot, speed, t);
                }
            }
        }
    }

    private void spawnFlyingBlock(ServerLevel level,
                                  BlockPos pos,
                                  BlockState state,
                                  Entity pilot,
                                  double speed,
                                  double intensity) {

        FallingBlockEntity entity = FallingBlockEntity.fall(level, pos, state);

        entity.dropItem = false;
        entity.setHurtsEntities(1.0F, 8);

        Vec3 center = pilot.position();

        Vec3 dir = new Vec3(
                pos.getX() + 0.5 - center.x,
                0.2D + intensity * 0.6D,
                pos.getZ() + 0.5 - center.z
        ).normalize();

        double power = (0.6D + speed * 0.3D) * intensity;

        // add slight swirl so it feels “unstable / sci-fi”
        double swirl = (level.random.nextDouble() - 0.5D) * 0.8D;

        entity.setDeltaMovement(
                dir.x * power + swirl,
                dir.y * power,
                dir.z * power + swirl
        );

        level.addFreshEntity(entity);
    }

    private void flightEffects(Entity controllingPlayer) {

        if (controllingPlayer.horizontalCollision) {
            controllingPlayer.level().addParticle(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                    controllingPlayer.getX(),
                    controllingPlayer.getY() + 1,
                    controllingPlayer.getZ(),
                    0.2, 0.5, 0.0);
        }
    }

    private void groundEffects(Entity controllingPlayer) {

        if (!level().isClientSide) return;

        Vec3 motion = controllingPlayer.getDeltaMovement();
        if (motion.horizontalDistanceSqr() > 0.03D) return;

        BlockPos start = controllingPlayer.blockPosition();
        BlockState ground = null;
        double surfaceY = 0;

        BlockPos.MutableBlockPos cursor = start.mutable();

        for (int i = 0; i < 3; i++) {
            cursor.move(Direction.DOWN);
            BlockState state = level().getBlockState(cursor);

            if (!state.isAir() && !state.canBeReplaced()) {
                ground = state;
                surfaceY = cursor.getY() + 1.0D;
                break;
            }
        }

        if (ground == null) return;

        double gap = controllingPlayer.getY() - surfaceY;
        if (gap < 0.2D || gap > 2.5D) return;

        for (int i = 0; i < 6; i++) {

            double angle = random.nextDouble() * Math.PI * 2;
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);

            level().addParticle(new net.minecraft.core.particles.BlockParticleOption(
                            net.minecraft.core.particles.ParticleTypes.BLOCK, ground),
                    controllingPlayer.getX(),
                    surfaceY + 0.1D,
                    controllingPlayer.getZ(),
                    dx * 0.4, 0.05, dz * 0.4);
        }
    }

    private void collisionDamage(Entity pilot, ServerLevel level) {

        // Only apply crash damage/knockback when the pilot has actually hit something.
        // Previously this fired on speed alone, so every fast flight tick one-shot
        // any nearby mob even with no contact at all.
        if (!(pilot.horizontalCollision || pilot.verticalCollision)) return;

        Vec3 motion = pilot.getDeltaMovement();
        double speed = motion.length();

        if (speed < 0.4D) return;

        Vec3 dir = motion.normalize();

        DamageSource source = (pilot instanceof Player player)
                ? level.damageSources().playerAttack(player)
                : level.damageSources().generic();

        AABB box = pilot.getBoundingBox().inflate(0.8D);

        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {

            if (e == pilot || e instanceof Player) continue;

            e.invulnerableTime = 0;
            e.hurt(source, (float) (8.0D + speed * 14.0D));
            e.setDeltaMovement(dir.x * 2.5D, 0.7D, dir.z * 2.5D);
            e.hurtMarked = true;
        }
    }

    private void collisionTeleport(Entity controllingPlayer, TardisLevelOperator op) {

        if (!isOpen()) return;

        AABB box = controllingPlayer.getBoundingBox();

        List<Entity> entities = controllingPlayer.level()
                .getEntitiesOfClass(Entity.class, box.inflate(5));

        for (Entity e : entities) {
            if (e == this || e.is(controllingPlayer)) continue;
            teleportToInterior(op, e);
        }
    }

    public void setDimension(ResourceKey<Level> key) {
        getEntityData().set(DIMENSION, key.location().toString());
    }

    public boolean isOpen() {
        return getEntityData().get(DOOR);
    }

    public void setDoorOpen(boolean open) {
        getEntityData().set(DOOR, open);
    }

    public ResourceLocation getShellThemeId() {
        return new ResourceLocation(getEntityData().get(SHELL_THEME));
    }

    public ResourceLocation getShellPatternId() {
        return new ResourceLocation(getEntityData().get(SHELL_PATTERN));
    }

    /* ---------------- DIMENSION HELPERS ---------------- */

    public void setShellPattern(ResourceLocation rl) {
        getEntityData().set(SHELL_PATTERN, rl.toString());
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DIMENSION, Level.OVERWORLD.location().toString());
        entityData.define(SHELL_THEME, ShellTheme.FACTORY.getId().toString());
        entityData.define(SHELL_PATTERN, "tardis_refined:default");
        entityData.define(DOOR, false);
        entityData.define(RECOVERY_TICKS, 0);
    }



    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setShellTheme(new ResourceLocation(tag.getString("shell_theme")));
        setDoorOpen(tag.getBoolean("open"));
        setRecoveryTicks(tag.getInt("recovery_ticks"));
        readPhysFromNBT(tag);
    }

    /* ---------------- FORCE MOUNT ---------------- */

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("shell_theme", getShellThemeId().toString());
        tag.putBoolean("open", isOpen());
        tag.putInt("recovery_ticks", getRecoveryTicks());
        writePhysToNBT(tag);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return MiscHelper.spawnPacket(this);
    }

    public ServerLevel getTardisLevel(ServerLevel server) {
        return server.getServer().getLevel(
                ResourceKey.create(Registries.DIMENSION,
                        new ResourceLocation(getEntityData().get(DIMENSION))));
    }

    /* ---------------- FLIGHT CONTROL ---------------- */

    @Override
    public double getMyRidingOffset() {
        return -1.35;
    }

    public void finishFlight(ServerLevel server, boolean isTransition) {

        ServerLevel tardisLvl = getTardisLevel(server);

        FlightTracker.FlightData data =
                FlightTracker.IN_FLIGHT.get(tardisLvl.dimension());

        TardisLevelOperator.get(tardisLvl).ifPresent(op -> {

            ServerPlayer pilot =
                    (this.getVehicle() instanceof ServerPlayer sp)
                            ? sp
                            : (data != null ? data.player() : null);

            if (pilot != null) {

                Direction facing = Direction.fromYRot(pilot.getYRot());
                ServerLevel landLevel = pilot.serverLevel();

                if (!isTransition) {
                    BlockPos ground = findGroundBelow(landLevel, pilot.blockPosition());

                    TardisNavLocation landing =
                            new TardisNavLocation(ground, facing, landLevel);

                    op.getPilotingManager().setCurrentLocation(landing);
                    op.getExteriorManager().placeExteriorBlockForLanding(landing);

                    forceDismount(this);
                    returnPilot(pilot, data, op);
                }
                FlightTracker.restorePlayer(pilot);

            }

            FlightTracker.stopFlying(op.getLevel().dimension(), server.getServer());
            discard();
        });
    }

    private void returnPilot(ServerPlayer pilot,
                             FlightTracker.FlightData data,
                             TardisLevelOperator op) {

        if (data != null && data.originDimension() != null) {

            ServerLevel origin =
                    pilot.getServer().getLevel(data.originDimension());

            if (origin != null) {
                pilot.teleportTo(origin,
                        data.originPos().x,
                        data.originPos().y,
                        data.originPos().z,
                        data.originYaw(),
                        data.originPitch());
                return;
            }
        }

        teleportToInterior(op, pilot);
    }
}