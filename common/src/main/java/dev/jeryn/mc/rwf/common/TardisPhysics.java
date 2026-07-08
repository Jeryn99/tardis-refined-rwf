package dev.jeryn.mc.rwf.common;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.Clock;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import dev.jeryn.mc.rwf.client.BulletDebugDraw;
import dev.jeryn.mc.rwf.common.entity.TardisEntity;
import dev.jeryn.mc.rwf.network.SetFreefallMessage;
import dev.jeryn.mc.rwf.network.SyncTardisPhysicsMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import whocraft.tardis_refined.client.TardisClientData;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.*;

public class TardisPhysics {
    public static final BulletDebugDraw DEBUG_DRAW = new BulletDebugDraw();
    private static final Clock clock = new Clock();
    private static final BoxShape UNIT_BOX =
            new BoxShape(new Vector3f(0.5F, 0.5F, 0.5F));
    // Replace the collisionsAdded list with a map that holds the rigid body too
    private static final Map<String, RigidBody> blockColliders = new HashMap<>();
    public static boolean clientSideFreeFall = false;
    public static float heat = 0.0f;
    public static DynamicsWorld dynamicsWorld = null;
    public static RigidBody tardis_rigid_body = null;
    private static BroadphaseInterface overlappingPairCache = null;
    private static CollisionDispatcher dispatcher = null;
    private static ConstraintSolver constraintSolver = null;
    private static DefaultCollisionConfiguration collisionConfig = null;
    private static int physicsTick = 0;

    public static void toggleFreefall() {
        clientSideFreeFall = !clientSideFreeFall;

        new SetFreefallMessage(clientSideFreeFall).send();

        if (!clientSideFreeFall && dynamicsWorld != null && tardis_rigid_body != null) {
            dynamicsWorld.removeRigidBody(tardis_rigid_body);
            tardis_rigid_body = null;
        }
    }

    public static void resetPhysics() {
        if (dynamicsWorld != null) {
            if (tardis_rigid_body != null)
                dynamicsWorld.removeRigidBody(tardis_rigid_body);
            for (RigidBody body : blockColliders.values())
                dynamicsWorld.removeRigidBody(body);
        }

        tardis_rigid_body = null;
        dynamicsWorld = null;
        collisionConfig = null;
        dispatcher = null;
        overlappingPairCache = null;
        constraintSolver = null;
        blockColliders.clear();
        clock.reset();
        physicsTick = 0;
        clientSideFreeFall = false;
        heat = 0f;
    }

    public static void onClientTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.isPaused()) return;

        TardisEntity tardis = null;
        for (var p : mc.player.getPassengers()) {
            if (p instanceof TardisEntity te) {
                tardis = te;
                break;
            }
        }

        if (tardis == null) {
            if (dynamicsWorld != null) resetPhysics();
            return;
        }

        ensurePhysicsReady();

        TardisClientData tardisClientData = TardisClientData.getInstance(tardis.getTardisDimension());

        boolean isCollided = mc.player.horizontalCollision || mc.player.verticalCollision;
        boolean isFlying = mc.player.getAbilities().flying;

        if (isCollided && isFlying) {
            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();

            // Blue-ish "temporal energy" dust, scale controls particle size
            DustParticleOptions tardisDust = new DustParticleOptions(new org.joml.Vector3f(0.3F, 0.6F, 1.0F), 1.2F);

            mc.level.addParticle(ParticleTypes.LARGE_SMOKE, x, y + 1.0D, z, 0.2D, 1.0D, 0.0D);
            mc.level.addParticle(ParticleTypes.SMOKE, x, y + 1.0D, z, 0.0D, 0.2D, 0.0D);
            mc.level.addParticle(tardisDust, x, y + 1.0D, z, 0.0D, 0.0D, 0.0D);
            mc.level.addParticle(ParticleTypes.LARGE_SMOKE, x, y - 1.0D, z, 0.0D, 0.5D, 0.0D);
            mc.level.addParticle(ParticleTypes.SMOKE, x, y - 1.0D, z, 0.0D, 0.2D, 0.0D);
            mc.level.addParticle(tardisDust, x, y - 1.0D, z, 0.0D, 0.0D, 0.0D);
        }

        if (tardisClientData.getFuel() == 0) {
            clientSideFreeFall = true;
            new SetFreefallMessage(true).send();
        }

        if (clientSideFreeFall) {
            tickFreefall(mc.player, tardis);
        }
    }

    private static void tickFreefall(Player player, TardisEntity tardis) {

        float dt = (float) clock.getTimeMicroseconds() / 1_000_000.0F;
        clock.reset();
        dt = Math.min(dt, 0.05F);

        if (tardis_rigid_body == null) {

            Transform start = new Transform();
            start.setIdentity();
            start.origin.set((float) player.getX(),
                    (float) player.getY(),
                    (float) player.getZ());

            float mass = 100.0F;
            BoxShape shape = new BoxShape(new Vector3f(1.0F, 1.5F, 1.0F));

            Vector3f inertia = new Vector3f();
            shape.calculateLocalInertia(mass, inertia);

            tardis_rigid_body = new RigidBody(
                    mass,
                    new DefaultMotionState(start),
                    shape,
                    inertia
            );

            tardis_rigid_body.setActivationState(4);
            tardis_rigid_body.setAngularFactor(1.0f);

            Vec3 vel = player.getDeltaMovement();

            tardis_rigid_body.setLinearVelocity(new Vector3f(
                    (float) vel.x * 20f,
                    (float) vel.y * 20f,
                    (float) vel.z * 20f
            ));

            tardis_rigid_body.setAngularVelocity(new Vector3f(
                    (float) vel.z * 0.2f,
                    0.3f,
                    (float) -vel.x * 0.2f
            ));

            dynamicsWorld.addRigidBody(tardis_rigid_body);
        }

        BlockPos pos = player.blockPosition();
        var fluid = player.level().getFluidState(pos);

        boolean water = fluid.is(FluidTags.WATER);
        boolean lava = fluid.is(FluidTags.LAVA);

        float fluidDrag = 1.0f;
        float torqueMod = 1.0f;

        if (water) {

            float waterLevel = (float) (player.getY() + 1.0f);

            // stronger upward force when deeper
            float depth = Math.max(0f, (float) (waterLevel - player.getY()));

            // target float height (slightly above current water surface)
            float targetY = waterLevel + 1.2f;

            float currentY = tardis_rigid_body.getWorldTransform(new Transform()).origin.y;

            float lift = (targetY - currentY) * 6.0f;

            Vector3f buoyancy = new Vector3f(0, lift, 0);

            tardis_rigid_body.applyCentralForce(buoyancy);

            // damping so it doesn't jitter
            Vector3f vel = tardis_rigid_body.getLinearVelocity(new Vector3f());
            vel.y *= 0.85f;
            tardis_rigid_body.setLinearVelocity(vel);

            // calm rotation in water
            Vector3f ang = tardis_rigid_body.getAngularVelocity(new Vector3f());
            ang.scale(0.8f);
            tardis_rigid_body.setAngularVelocity(ang);
        }

        if (lava) {
            fluidDrag = 0.85f;
            torqueMod = 1.6f;

            heat = Math.min(1.0f, heat + 0.02f);

            Vector3f chaos = new Vector3f(
                    (float) (Math.random() - 0.5) * heat,
                    (float) (Math.random() - 0.5) * heat * 0.5f,
                    (float) (Math.random() - 0.5) * heat
            );

            tardis_rigid_body.applyTorque(chaos);
        } else {
            heat *= 0.98f;
        }

        tardis_rigid_body.setDamping(
                0.02f + heat * 0.08f,
                0.05f + heat * 0.15f
        );

        Vector3f vel = tardis_rigid_body.getLinearVelocity(new Vector3f());
        vel.scale(0.992f * fluidDrag);
        tardis_rigid_body.setLinearVelocity(vel);

        Vector3f torque = new Vector3f(
                vel.z * 0.015f,
                vel.x * 0.01f,
                -vel.x * 0.015f
        );

        torque.x += (float) ((Math.random() - 0.5) * 0.02 * torqueMod);
        torque.y += (float) ((Math.random() - 0.5) * 0.01 * torqueMod);
        torque.z += (float) ((Math.random() - 0.5) * 0.02 * torqueMod);

        tardis_rigid_body.applyTorque(torque);

        dynamicsWorld.stepSimulation(dt, 5);

        checkWallCollisions(player, tardis);

        Transform wt = new Transform();
        tardis_rigid_body.getWorldTransform(wt);

        Quat4f q = new Quat4f();
        wt.getRotation(q);

        Vector3f euler = quatToEuler(q);

        tardis.setYRot((float) Math.toDegrees(euler.y));
        tardis.setXRot((float) Math.toDegrees(euler.x));

        double x = wt.origin.x;
        double y = wt.origin.y;
        double z = wt.origin.z;

        player.setPos(x, y, z);
        player.setDeltaMovement(Vec3.ZERO);

        player.xOld = x;
        player.yOld = y;
        player.zOld = z;

        tardis.setPos(x, y, z);

        tardis_rigid_body.getWorldTransform(wt);
        float[] matrix = new float[16];
        wt.getOpenGLMatrix(matrix);

        new SyncTardisPhysicsMessage(new Vec3(x, y, z), matrix).send();

        physicsTick++;
        if (physicsTick >= 2) {
            physicsTick = 0;
            syncNearbyBlockColliders(player);  // ← renamed
        }
    }

    private static void ensurePhysicsReady() {
        if (dynamicsWorld != null) return;

        collisionConfig = new DefaultCollisionConfiguration();
        dispatcher = new CollisionDispatcher(collisionConfig);
        overlappingPairCache = new DbvtBroadphase();
        constraintSolver = new SequentialImpulseConstraintSolver();

        dynamicsWorld = new DiscreteDynamicsWorld(
                dispatcher,
                overlappingPairCache,
                constraintSolver,
                collisionConfig
        );
        dynamicsWorld.setDebugDrawer(DEBUG_DRAW);

        dynamicsWorld.setGravity(new Vector3f(0, -9.8f, 0));
        clock.reset();

    }

    private static void checkWallCollisions(Player player, TardisEntity tardis) {
        if (dynamicsWorld == null || tardis_rigid_body == null || dispatcher == null) return;


        int numManifolds = dispatcher.getNumManifolds();
        for (int i = 0; i < numManifolds; i++) {
            var manifold = dispatcher.getManifoldByIndexInternal(i);

            Object bodyA = manifold.getBody0();
            Object bodyB = manifold.getBody1();

            if (bodyA != tardis_rigid_body && bodyB != tardis_rigid_body) continue;

            int numContacts = manifold.getNumContacts();
            for (int c = 0; c < numContacts; c++) {
                var pt = manifold.getContactPoint(c);
                if (pt.distance1 > 0f) continue; // not actually touching

                Vector3f normal = new Vector3f(pt.normalWorldOnB);

                if (bodyA == tardis_rigid_body) normal.negate();

                float horizontal = (float) Math.sqrt(normal.x * normal.x + normal.z * normal.z);

                boolean isWall = horizontal > 0.6f && Math.abs(normal.y) < 0.4f;

                if (isWall) {
                    onWallCollision(normal, pt.appliedImpulse, player, tardis);
                }
            }
        }
    }

    private static float lastWallHitTime = 0f;

    private static final float WALL_HIT_COOLDOWN = 0.3f; // seconds, prevents scrape-spam

    private static void onWallCollision(Vector3f normal, float impulse, Player player, TardisEntity tardis) {
        //  if (impulse < 5f) return;

      /*  float now = (float) clock.getTimeMicroseconds() / 1_000_000.0F;
        if (now - lastWallHitTime < WALL_HIT_COOLDOWN) return;
        lastWallHitTime = now;*/

        // scale everything off impulse so a gentle scrape and a full-speed slam feel different
        float severity = Math.min(1.0f, impulse / 60f); // tune 60f against your typical hit magnitudes

        heat = Math.min(1.0f, heat + impulse * 0.002f);

        // --- punchy bounce off the wall instead of just absorbing the hit ---
        if (tardis_rigid_body != null) {
            Vector3f vel = tardis_rigid_body.getLinearVelocity(new Vector3f());
            float velAlongNormal = vel.dot(normal);
            if (velAlongNormal < 0) { // moving into the wall
                Vector3f reflect = new Vector3f(normal);
                reflect.scale(-velAlongNormal * (1.2f + severity * 0.6f)); // restitution-ish kick
                vel.add(reflect);
                tardis_rigid_body.setLinearVelocity(vel);
            }

            // extra tumble on impact, direction biased by the wall normal (feels like a real crash)
            Vector3f crashTorque = new Vector3f(
                    normal.z * severity * 4f + (float) (Math.random() - 0.5) * severity * 2f,
                    (float) (Math.random() - 0.5) * severity * 3f,
                    -normal.x * severity * 4f + (float) (Math.random() - 0.5) * severity * 2f
            );
            tardis_rigid_body.applyTorque(crashTorque);
        }

        // --- sparks at the impact point ---
        spawnWallImpactParticles(player, normal, severity);
    }

    private static void spawnWallImpactParticles(Player player, Vector3f normal, float severity) {
        var level = player.level();

        // push the spawn point out from the player toward the wall, roughly chest height
        double originX = player.getX() + normal.x * 1.0;
        double originY = player.getY() + 1.0;
        double originZ = player.getZ() + normal.z * 1.0;

        int count = 8 + (int) (severity * 16);

        for (int i = 0; i < count; i++) {
            // scatter around the impact point, biased outward along the normal
            double ox = normal.x * 0.3 + (Math.random() - 0.5) * 0.6;
            double oy = (Math.random() - 0.5) * 0.8;
            double oz = normal.z * 0.3 + (Math.random() - 0.5) * 0.6;

            // velocity kicks outward along the normal with some random scatter
            double vx = normal.x * (0.1 + Math.random() * 0.15 * severity);
            double vy = 0.05 + Math.random() * 0.1 * severity;
            double vz = normal.z * (0.1 + Math.random() * 0.15 * severity);

            level.addParticle(
                    net.minecraft.core.particles.ParticleTypes.CRIT,
                    originX + ox, originY + oy, originZ + oz,
                    vx, vy, vz
            );
        }
        // heavier hits get a burst of smoke too, for weight
       // if (severity > 0.5f) {
            int smokeCount = (int) (severity * 6);
            for (int i = 0; i < smokeCount; i++) {
                double ox = normal.x * 0.3 + (Math.random() - 0.5) * 0.4;
                double oy = (Math.random() - 0.5) * 0.6;
                double oz = normal.z * 0.3 + (Math.random() - 0.5) * 0.4;

                level.addParticle(
                        net.minecraft.core.particles.ParticleTypes.SMOKE,
                        originX + ox, originY + oy, originZ + oz,
                        normal.x * 0.05, 0.03, normal.z * 0.05
                );
         //   }
        }

        // scorch-y flame flecks on the hardest hits, ties nicely into your heat system
     //   if (severity > 0.8f) {
            for (int i = 0; i < 4; i++) {
                double ox = normal.x * 0.3 + (Math.random() - 0.5) * 0.3;
                double oy = (Math.random() - 0.5) * 0.4;
                double oz = normal.z * 0.3 + (Math.random() - 0.5) * 0.3;

                level.addParticle(
                        net.minecraft.core.particles.ParticleTypes.SMALL_FLAME,
                        originX + ox, originY + oy, originZ + oz,
                        0, 0.02, 0
                );
        //    }
        }
    }

    private static void syncNearbyBlockColliders(Player player) {
        Set<String> shouldExist = new HashSet<>();

        for (int i = -4; i <= 4; i++) {
            for (int j = -4; j <= 4; j++) {
                for (int k = -4; k <= 4; k++) {
                    int bx = (int) player.getX() + i;
                    int by = (int) player.getY() + j;
                    int bz = (int) player.getZ() + k;
                    String key = bx + "," + by + "," + bz;
                    BlockPos pos = new BlockPos(bx, by, bz);

                    boolean solid = !player.level().getBlockState(pos).getBlock().equals(Blocks.AIR)
                            && !player.level().getBlockState(pos)
                            .getCollisionShape(player.level(), pos).isEmpty();

                    if (solid) {
                        shouldExist.add(key);
                        if (!blockColliders.containsKey(key)) {
                            // Add new collider
                            Transform t = new Transform();
                            t.setIdentity();
                            t.origin.set(new Vector3f(bx, by, bz));
                            RigidBody body = new RigidBody(0.0F, new DefaultMotionState(t), UNIT_BOX);
                            body.setFriction(1.5F);
                            dynamicsWorld.addRigidBody(body);
                            blockColliders.put(key, body);
                        }
                    }
                }
            }
        }

        // Remove colliders for blocks that no longer exist
        Iterator<Map.Entry<String, RigidBody>> it = blockColliders.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, RigidBody> entry = it.next();
            if (!shouldExist.contains(entry.getKey())) {
                dynamicsWorld.removeRigidBody(entry.getValue());
                it.remove();
            }
        }
    }

    public static Vector3f quatToEuler(Quat4f q) {
        float sqw = q.w * q.w, sqx = q.x * q.x,
                sqy = q.y * q.y, sqz = q.z * q.z;

        float x = (float) Math.atan2(2.0 * (q.y * q.z + q.x * q.w),
                (-sqx - sqy + sqz + sqw));

        float y = (float) Math.asin(-2.0 * (q.x * q.z - q.y * q.w));

        float z = (float) Math.atan2(2.0 * (q.x * q.y + q.z * q.w),
                (sqx - sqy - sqz + sqw));

        return new Vector3f(x, y, z);
    }

    public static void onExplosion(double ex, double ey, double ez, float power) {
        if (tardis_rigid_body == null || dynamicsWorld == null) return;

        // Wake the body up — sleeping bodies ignore forces/impulses
        tardis_rigid_body.activate(true);

        Transform t = new Transform();
        tardis_rigid_body.getWorldTransform(t);

        float dx = (float) (t.origin.x - ex);
        float dy = (float) (t.origin.y - ey);
        float dz = (float) (t.origin.z - ez);

        float distSq = dx * dx + dy * dy + dz * dz;
        float dist = Math.max(1.0f, (float) Math.sqrt(distSq));

        float strength = (power * 25.0f) / distSq;

        Vector3f impulse = new Vector3f(
                dx / dist * strength,
                dy / dist * strength,
                dz / dist * strength
        );

        tardis_rigid_body.applyCentralImpulse(impulse);

        tardis_rigid_body.applyTorque(new Vector3f(
                (float) (Math.random() - 0.5) * power * 10f,
                (float) (Math.random() - 0.5) * power * 10f,
                (float) (Math.random() - 0.5) * power * 10f
        ));

        heat = Math.min(1.0f, heat + power * 0.1f);
    }
}