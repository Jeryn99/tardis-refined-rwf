package dev.jeryn.mc.rwf.common.entity;

import dev.jeryn.mc.rwf.client.ClientFlightData;
import dev.jeryn.mc.rwf.network.SyncFlightDataMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import whocraft.tardis_refined.client.TardisClientData;
import whocraft.tardis_refined.common.blockentity.door.TardisInternalDoor;
import whocraft.tardis_refined.common.capability.tardis.TardisLevelOperator;
import whocraft.tardis_refined.common.capability.tardis.upgrades.Upgrade;
import whocraft.tardis_refined.common.tardis.TardisNavLocation;
import whocraft.tardis_refined.common.tardis.themes.ShellTheme;
import whocraft.tardis_refined.patterns.ShellPattern;
import whocraft.tardis_refined.registry.RegistrySupplier;
import whocraft.tardis_refined.registry.TRDimensionTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static dev.jeryn.mc.rwf.common.upgrade.RWFUpgrades.*;

public class FlightTracker {

    public static final Map<RegistrySupplier<Upgrade>, Float> FUEL_REDUCTION = Map.of(
            FLIGHT_EFFICIENCY_1, 0.05f,
            FLIGHT_EFFICIENCY_2, 0.10f,
            FLIGHT_EFFICIENCY_3, 0.20f,
            FLIGHT_EFFICIENCY_4, 0.35f,
            FLIGHT_EFFICIENCY_5, 0.50f
    );

    public static final int HOP_FADE_TICKS = 10;
    public static final int HOP_TRAVEL_TICKS = 60;
    private static final int HOP_COOLDOWN_TICKS = 60;
    private static final double MAX_HOP_DISTANCE = 64.0;

    public static HashMap<ResourceKey<Level>, FlightData> IN_FLIGHT = new HashMap<>();

    public static void setInFlight(FlightData flightData, ResourceKey<Level> key) {
        IN_FLIGHT.put(key, flightData);
        broadcastSync(flightData.player().server);
    }

    public static void stopFlying(ResourceKey<Level> levelResourceKey, MinecraftServer server) {
        IN_FLIGHT.remove(levelResourceKey);
        broadcastSync(server);
    }

    public static boolean isFlying(ResourceKey<Level> levelResourceKey) {
        return IN_FLIGHT.containsKey(levelResourceKey);
    }

    public static boolean isPlayerFlying(UUID uuid) {
        for (Map.Entry<ResourceKey<Level>, FlightData> dataEntry : IN_FLIGHT.entrySet()) {
            if (dataEntry.getValue().player().getUUID().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFlying(Entity entity) {
        return entity instanceof Player && isPlayerFlying(entity.getUUID());
    }

    public static boolean isFlyingEitherSide(Entity entity) {
        if (!(entity instanceof Player player)) return false;
        if (player.level().isClientSide()) {
            return dev.jeryn.mc.rwf.client.ClientFlightTracker.getForPlayer(player.getUUID()).isPresent();
        }
        return isPlayerFlying(player.getUUID());
    }

    public static Optional<FlightData> getFlightData(UUID playerUuid) {
        for (FlightData data : IN_FLIGHT.values()) {
            if (data.player().getUUID().equals(playerUuid)) {
                return Optional.of(data);
            }
        }
        return Optional.empty();
    }

    public static ResourceKey<Level> getTardisDimensionFor(UUID playerUuid) {
        for (Map.Entry<ResourceKey<Level>, FlightData> entry : IN_FLIGHT.entrySet()) {
            if (entry.getValue().player().getUUID().equals(playerUuid)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static void attemptLocalHop(ServerPlayer pilot, BlockPos targetPos, Direction hitFace) {
        if (!isPlayerFlying(pilot.getUUID())) {
            return;
        }

        Optional<FlightData> dataOptional = getFlightData(pilot.getUUID());
        if (dataOptional.isEmpty()) {
            return;
        }
        ShellState shell = dataOptional.get().shell();

        long now = pilot.level().getGameTime();
        if (now < shell.nextHopAllowedTick) {
            rejectHop(pilot);
            return;
        }
        if (shell.hopExecuteAtTick > 0) {
            rejectHop(pilot);
            return;
        }

        BlockPos standPos = targetPos.relative(hitFace);
        double targetX = standPos.getX() + 0.5;
        double targetY = standPos.getY();
        double targetZ = standPos.getZ() + 0.5;

        double distance = pilot.position().distanceTo(new Vec3(targetX, targetY, targetZ));
        if (distance > MAX_HOP_DISTANCE) {
            rejectHop(pilot);
            return;
        }

        if (!dev.jeryn.mc.rwf.util.RWFTeleport.canTeleportTo(standPos, pilot.level(), pilot)) {
            rejectHop(pilot);
            return;
        }

        pilot.level().playSound(null, pilot.getX(), pilot.getY(), pilot.getZ(),
                whocraft.tardis_refined.registry.TRSoundRegistry.TARDIS_TAKEOFF.get(),
                net.minecraft.sounds.SoundSource.AMBIENT, 1.0F, 1.0F);

        shell.pendingHopTarget = standPos;
        shell.hopExecuteAtTick = now + HOP_FADE_TICKS + HOP_TRAVEL_TICKS;
        shell.nextHopAllowedTick = shell.hopExecuteAtTick + HOP_COOLDOWN_TICKS;
        shell.recoveryTicks = HOP_FADE_TICKS + HOP_TRAVEL_TICKS;

        broadcastSync(pilot.server);
    }

    private static void rejectHop(ServerPlayer pilot) {
        pilot.playNotifySound(net.minecraft.sounds.SoundEvents.VILLAGER_NO,
                net.minecraft.sounds.SoundSource.PLAYERS, 0.6F, 1.0F);
    }

    private static void tickPendingHop(ServerPlayer pilot, ShellState shell) {
        if (shell.hopExecuteAtTick <= 0) return;

        long currentTime = pilot.level().getGameTime();
        if (currentTime < shell.hopExecuteAtTick) return;

        BlockPos target = shell.pendingHopTarget;
        shell.pendingHopTarget = null;
        shell.hopExecuteAtTick = 0L;
        if (target == null) {
            return;
        }
        if (!(pilot.level() instanceof ServerLevel level)) {
            return;
        }

        double x = target.getX() + 0.5;
        double y = target.getY();
        double z = target.getZ() + 0.5;

        // Terrain may have changed during the travel delay (blocks placed,
        // explosions, etc), so re-validate right before actually moving the
        // pilot instead of trusting the check from when the hop was queued.
        boolean moved = dev.jeryn.mc.rwf.util.RWFTeleport.performTeleport(
                pilot, level, x, y, z, pilot.getYRot(), pilot.getXRot());

        if (!moved) {
            rejectHop(pilot);
            return;
        }

        level.playSound(null, x, y, z,
                whocraft.tardis_refined.registry.TRSoundRegistry.TARDIS_LAND.get(),
                net.minecraft.sounds.SoundSource.AMBIENT, 1.0F, 1.0F);
    }

    public static boolean transitionToVortex(ResourceKey<Level> tardisDimension) {
        TardisClientData tardisClientData = TardisClientData.getInstance(tardisDimension);
        return tardisClientData.isFlying();
    }

    public static void loggedOut(FlightData flightData) {
        ServerPlayer pilot = flightData.player();
        MinecraftServer server = pilot.server;
        ResourceKey<Level> tardisDim = getTardisDimensionFor(pilot.getUUID());

        if (tardisDim != null) {
            IN_FLIGHT.remove(tardisDim);
        } else {
            IN_FLIGHT.values().removeIf(data -> data.player().getUUID().equals(pilot.getUUID()));
        }

        try {
            restorePlayer(pilot);
        } catch (Throwable t) {
            System.out.println("[RWF] loggedOut: restorePlayer failed " + t);
        }

        try {
            ResourceKey<Level> originDim = flightData.originDimension();
            ServerLevel originLevel = originDim != null ? server.getLevel(originDim) : null;
            if (originLevel != null) {
                Vec3 originPos = flightData.originPos();
                if (pilot.level() != originLevel) {
                    pilot.setServerLevel(originLevel);
                }
                pilot.moveTo(originPos.x, originPos.y, originPos.z, flightData.originYaw(), flightData.originPitch());
            }
        } catch (Throwable t) {
            System.out.println("[RWF] loggedOut: return-to-origin failed " + t);
        }

        try {
            broadcastSync(server);
        } catch (Throwable t) {
            System.out.println("[RWF] loggedOut: broadcastSync failed " + t);
        }
    }

    private static void broadcastSync(MinecraftServer server) {
        if (server == null) return;
        Map<ResourceKey<Level>, ClientFlightData> payload = new HashMap<>();
        IN_FLIGHT.forEach((key, data) -> payload.put(key, ClientFlightData.from(data, key)));
        new SyncFlightDataMessage(payload).sendToAll();
    }

    public static void updateFreefall(ResourceKey<Level> key, boolean isFreefalling, MinecraftServer server) {
        FlightData existing = IN_FLIGHT.get(key);
        if (existing == null) return;

        FlightData updated = existing.withFreefalling(isFreefalling);

        if (!isFreefalling) {
            existing.shell().recoveryTicks = 40;
        }

        IN_FLIGHT.put(key, updated);
        broadcastSync(server);
    }

    public static void setUpPlayerForFlight(ServerPlayer serverPlayer) {
        Abilities abilities = serverPlayer.getAbilities();
        abilities.mayfly = true;
        abilities.flying = true;
        abilities.invulnerable = true;
        abilities.mayBuild = false;
        abilities.setFlyingSpeed(0.16F);
        serverPlayer.onUpdateAbilities();
    }

    public static void restorePlayer(ServerPlayer serverPlayer) {
        Abilities abilities = serverPlayer.getAbilities();
        serverPlayer.gameMode.getGameModeForPlayer().updatePlayerAbilities(serverPlayer.getAbilities());
        abilities.setFlyingSpeed(0.05F);
        serverPlayer.onUpdateAbilities();
    }

    public static void startFlight(ServerLevel tardisLvl, ServerPlayer serverPlayer) {
        TardisLevelOperator.get(tardisLvl).ifPresent(tardisLevelOperator -> {
            try {
                TardisNavLocation lastKnown = tardisLevelOperator.getPilotingManager().getCurrentLocation();

                if (lastKnown == null || lastKnown.getLevel() == null) {
                    System.out.println("[RWF] startFlight abort: currentLocation null");
                    return;
                }

                tardisLevelOperator.setDoorClosed(true);
                tardisLevelOperator.getExteriorManager().removeExteriorBlock();

                FlightData data = new FlightData(serverPlayer, false);

                try {
                    applyShellAesthetic(data.shell(), tardisLevelOperator);
                } catch (Throwable t) {
                    System.out.println("[RWF] applyShellAesthetic failed " + t);
                }

                setInFlight(data, tardisLvl.dimension());

                serverPlayer.teleportTo(
                        lastKnown.getLevel(),
                        lastKnown.getPosition().getX(),
                        lastKnown.getPosition().getY(),
                        lastKnown.getPosition().getZ(),
                        0, 0
                );

                setUpPlayerForFlight(serverPlayer);

            } catch (Throwable t) {
                System.out.println("[RWF] startFlight threw " + t);
            }
        });
    }

    public static void finishFlight(ServerPlayer pilot, boolean isTransition) {
        ResourceKey<Level> tardisDim = getTardisDimensionFor(pilot.getUUID());
        MinecraftServer server = pilot.server;

        restorePlayer(pilot);

        if (tardisDim == null) return;

        FlightData data = IN_FLIGHT.get(tardisDim);
        IN_FLIGHT.remove(tardisDim);

        ServerLevel tardisLvl = server.getLevel(tardisDim);
        if (tardisLvl == null) {
            broadcastSync(server);
            return;
        }

        TardisLevelOperator.get(tardisLvl).ifPresent(op -> {
            if (!isTransition) {
                try {
                    Direction facing = Direction.fromYRot(pilot.getYRot());
                    ServerLevel landLevel = pilot.serverLevel();

                    BlockPos ground = findGroundBelow(landLevel, pilot.blockPosition());
                    TardisNavLocation landing = new TardisNavLocation(ground, facing, landLevel);

                    op.getPilotingManager().setCurrentLocation(landing);
                    op.getExteriorManager().placeExteriorBlockForLanding(landing);

                    returnPilot(pilot, data, op);
                } catch (Throwable t) {
                    System.out.println("[RWF] finishFlight: landing failed " + t);
                }
            }
        });

        broadcastSync(server);
    }

    private static void returnPilot(ServerPlayer pilot, FlightData data, TardisLevelOperator op) {
        if (data != null && data.originDimension() != null) {
            ServerLevel origin = pilot.getServer().getLevel(data.originDimension());

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

    private static void teleportToInterior(TardisLevelOperator op, Entity e) {
        if (op == null || e == null) return;

        Level level = op.getLevel();
        if (!(level instanceof ServerLevel server)) return;

        var door = op.getInternalDoor();
        if (door == null) return;

        BlockPos pos = door.getTeleportPosition();
        if (pos == null) return;

        dev.jeryn.mc.rwf.util.RWFTeleport.performTeleport(
                e, server, pos.getX(), pos.getY(), pos.getZ(), e.getYRot(), e.getXRot());
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

    public static void serverTick(MinecraftServer server) {
        if (IN_FLIGHT.isEmpty()) return;

        for (Map.Entry<ResourceKey<Level>, FlightData> entry : new HashMap<>(IN_FLIGHT).entrySet()) {
            ResourceKey<Level> tardisDim = entry.getKey();
            FlightData data = entry.getValue();

            ServerPlayer pilot = server.getPlayerList().getPlayer(data.player().getUUID());
            if (pilot == null) continue;

            if (!(pilot.level() instanceof ServerLevel serverLevel)) continue;

            ShellState shell = data.shell();

            if (shell.recoveryTicks > 0) {
                shell.recoveryTicks--;
                if (shell.recoveryTicks == 0) {
                    broadcastSync(server);
                }
            }

            tickPendingHop(pilot, shell);

            if (!data.isFreefalling() && server.getTickCount() % 20 == 0) {
                serverLevel.playSound(pilot, pilot.getX(), pilot.getY(), pilot.getZ(),
                        whocraft.tardis_refined.registry.TRSoundRegistry.TARDIS_SINGLE_FLY.get(),
                        net.minecraft.sounds.SoundSource.AMBIENT, 1.0F, 1.0F);
            }

            ServerLevel tardisLvl = server.getLevel(tardisDim);
            if (tardisLvl == null || tardisLvl.dimensionTypeId() != TRDimensionTypes.TARDIS) {
                finishFlight(pilot, false);
                continue;
            }

            flightEffects(pilot, serverLevel);
            collisionDamage(pilot, serverLevel);

            Optional<TardisLevelOperator> tardisOp = TardisLevelOperator.get(tardisLvl);

            tardisOp.ifPresent(op -> {
                op.getPilotingManager().setCurrentLocation(
                        new TardisNavLocation(pilot.blockPosition(), Direction.NORTH, pilot.level().dimension()));

                collisionTeleport(pilot, op, shell);
                syncShellState(tardisDim, shell, op);

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

                Vec3 current = pilot.position();

                boolean isMoving =
                        Math.abs(current.x - shell.lastPos.x) > 0.01 ||
                                Math.abs(current.y - shell.lastPos.y) > 0.01 ||
                                Math.abs(current.z - shell.lastPos.z) > 0.01;

                shell.lastPos = current;

                boolean inOpenAirNotFalling =
                        pilot.level().getBlockState(pilot.blockPosition().below()).isAir() && pilot.fallDistance <= 0;

                if (isMoving || inOpenAirNotFalling) {
                    op.getPilotingManager().removeFuel(fuelCost);
                }
            });
        }
    }

    private static void syncShellState(ResourceKey<Level> tardisDim, ShellState shell, TardisLevelOperator operator) {
        boolean changed = false;

        ResourceLocation theme = operator.getAestheticHandler().getShellTheme();
        if (theme != null && !theme.equals(shell.shellTheme)) {
            shell.shellTheme = theme;
            changed = true;
        }

        ShellPattern pattern = operator.getAestheticHandler().shellPattern();
        if (pattern != null && pattern.id() != null && !pattern.id().equals(shell.shellPattern)) {
            shell.shellPattern = pattern.id();
            changed = true;
        }

        TardisInternalDoor door = operator.getInternalDoor();
        if (door != null && door.isOpen() != shell.doorOpen) {
            shell.doorOpen = door.isOpen();
            changed = true;
        }

        if (changed) {
            FlightData data = IN_FLIGHT.get(tardisDim);
            if (data != null) {
                broadcastSync(data.player().server);
            }
        }
    }

    private static void applyShellAesthetic(ShellState shell, TardisLevelOperator operator) {
        ResourceLocation theme = operator.getAestheticHandler().getShellTheme();
        if (theme != null) shell.shellTheme = theme;

        ShellPattern pattern = operator.getAestheticHandler().shellPattern();
        if (pattern != null && pattern.id() != null) shell.shellPattern = pattern.id();
    }

    private static void flightEffects(ServerPlayer pilot, ServerLevel level) {
        // Level#addParticle is a client-only no-op - server code has to go
        // through ServerLevel#sendParticles for anything to actually render.
        if (pilot.horizontalCollision) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                    pilot.getX(), pilot.getY() + 1, pilot.getZ(), 4, 0.2, 0.2, 0.2, 0.02);
        }
    }

    private static void collisionDamage(Entity pilot, ServerLevel level) {
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

    private static void collisionTeleport(Entity pilot, TardisLevelOperator op, ShellState shell) {
        if (!shell.doorOpen) return;

        // A full nearby-entity scan every single tick is unnecessary for
        // something that's really "did anyone walk through the open door" -
        // every 5 ticks is still responsive and cuts the scan rate 5x.
        if (pilot.level().getGameTime() % 5 != 0) return;

        AABB box = pilot.getBoundingBox();

        // LivingEntity only - a raw Entity sweep also scoops up item drops,
        // XP orbs, and arrows sitting near the open door.
        for (LivingEntity e : pilot.level().getEntitiesOfClass(LivingEntity.class, box.inflate(5))) {
            if (e.is(pilot)) continue;
            teleportToInterior(op, e);
        }
    }

    /* ---------------- FLIGHT DATA ---------------- */

    public static class ShellState {
        public ResourceLocation shellTheme = ShellTheme.FACTORY.getId();
        public ResourceLocation shellPattern = new ResourceLocation("tardis_refined", "default");
        public boolean doorOpen = false;
        public int recoveryTicks = 0;
        public Vec3 lastPos = Vec3.ZERO;
        public long nextHopAllowedTick = 0L;
        public BlockPos pendingHopTarget = null;
        public long hopExecuteAtTick = 0L;
    }

    public static class FlightData {

        private final ServerPlayer player;
        private final ResourceKey<Level> originDimension;
        private final Vec3 originPos;
        private final float originYaw;
        private final float originPitch;
        private final boolean isFreefalling;
        private final ShellState shell;

        public FlightData(ServerPlayer serverPlayer, boolean isFreefalling) {
            this.player = serverPlayer;
            this.originDimension = serverPlayer.level().dimension();
            this.originPos = serverPlayer.position();
            this.originYaw = serverPlayer.getYRot();
            this.originPitch = serverPlayer.getXRot();
            this.isFreefalling = isFreefalling;
            this.shell = new ShellState();
        }

        private FlightData(ServerPlayer player, ResourceKey<Level> originDimension,
                            Vec3 originPos, float originYaw, float originPitch, boolean isFreefalling,
                            ShellState shell) {
            this.player = player;
            this.originDimension = originDimension;
            this.originPos = originPos;
            this.originYaw = originYaw;
            this.originPitch = originPitch;
            this.isFreefalling = isFreefalling;
            this.shell = shell;
        }

        public FlightData withFreefalling(boolean isFreefalling) {
            return new FlightData(player, originDimension, originPos, originYaw, originPitch, isFreefalling, shell);
        }

        public ServerPlayer player() {
            return player;
        }

        public ResourceKey<Level> originDimension() {
            return originDimension;
        }

        public Vec3 originPos() {
            return originPos;
        }

        public float originYaw() {
            return originYaw;
        }

        public float originPitch() {
            return originPitch;
        }

        public boolean isFreefalling() {
            return isFreefalling;
        }

        public ShellState shell() {
            return shell;
        }
    }
}
