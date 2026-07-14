package dev.jeryn.mc.rwf.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.jeryn.mc.rwf.client.model.RWFModelRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import whocraft.tardis_refined.common.blockentity.console.GlobalConsoleBlockEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ConsolePilotRenderer {

    private static ConsolePilotModel<?> pilotModel;

    private static List<ModelPart> pilotModelParts;

    private static final long PROFILE_CACHE_TTL_MS = TimeUnit.MINUTES.toMillis(5);
    private static final long SKIN_CACHE_TTL_MS = TimeUnit.MINUTES.toMillis(5);

    private record CachedValue<T>(T value, long timestamp) {
        boolean isExpired(long ttlMillis) {
            return System.currentTimeMillis() - timestamp > ttlMillis;
        }
    }

    private static final Map<UUID, CachedValue<GameProfile>> profileCache = new ConcurrentHashMap<>();

    public static void init(EntityModelSet modelSet) {
        pilotModel = new ConsolePilotModel<>(modelSet.bakeLayer(RWFModelRegistry.PILOT));
        pilotModelParts = pilotModel.root().getAllParts().toList();
    }

    public static void render(
            GlobalConsoleBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || pilotModel == null) {
            init(mc.getEntityModels());
            return;
        }

        Level level = blockEntity.getLevel();
        if (level == null) return;

        UUID pilot = ClientFlightTracker.get(level.dimension())
                .map(ClientFlightData::pilot)
                .orElse(null);
        if (pilot == null) return;

        renderPilot(mc, pilot, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderPilot(
            Minecraft mc,
            UUID pilot,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        ConsolePilotModel<?> model = pilotModel;
        for (ModelPart part : pilotModelParts) {
            part.resetPose();
        }

        float ageInTicks = mc.player.tickCount + partialTick;
        model.setupAnim(null, 0, 0, ageInTicks, 0, 0);

        poseStack.pushPose();
        poseStack.translate(0.0F, 0F, -2);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        PlayerInfo playerInfo = mc.getConnection() != null ? mc.getConnection().getPlayerInfo(pilot) : null;
        ResourceLocation skinTexture = getPlayerSkin(playerInfo != null ? playerInfo.getProfile() : null);

        VertexConsumer innerConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(skinTexture));
        model.renderToBuffer(poseStack, innerConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);

        // ── Name tag ──────────────────────────────────────────────────────────────
        if (!mc.options.hideGui) {
            String playerName = playerInfo != null ? playerInfo.getProfile().getName() : "";
            String name = !playerName.isEmpty() ? playerName : pilot.toString();

            renderNameTag(
                    ChatFormatting.YELLOW + name + ChatFormatting.DARK_GRAY + " [" + ChatFormatting.GOLD + "Pilot" + ChatFormatting.DARK_GRAY + "]",
                    poseStack, bufferSource, packedLight
            );
        }
        // ─────────────────────────────────────────────────────────────────────────

        poseStack.popPose();
    }

    private static void renderNameTag(
            String name,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        Component nameComponent = Component.literal(name);

        poseStack.pushPose();

        poseStack.translate(0.0F, -1F, 0.0F);
        poseStack.scale(-0.025F, 0.025F, 0.025F);

        Matrix4f matrix = poseStack.last().pose();
        float bgOpacity = mc.options.getBackgroundOpacity(0.25F);
        int bgColor = (int) (bgOpacity * 255.0F) << 24;

        float halfWidth = (float) (-font.width(nameComponent)) / 2.0F;
        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());

        font.drawInBatch(
                nameComponent,
                halfWidth, 0F,
                0x20FFFFFF,
                false,
                matrix,
                bufferSource,
                Font.DisplayMode.SEE_THROUGH,
                bgColor,
                packedLight
        );

        font.drawInBatch(
                nameComponent,
                halfWidth, 0F,
                0xFFFFFFFF,
                false,
                matrix,
                bufferSource,
                Font.DisplayMode.NORMAL,
                0,
                packedLight
        );

        poseStack.popPose();
    }

    private static GameProfile getOrCreateProfile(UUID pilot) {
        CachedValue<GameProfile> cached = profileCache.get(pilot);
        if (cached != null && !cached.isExpired(PROFILE_CACHE_TTL_MS)) {
            return cached.value();
        }

        GameProfile gameProfile = new GameProfile(pilot, "");
        profileCache.put(pilot, new CachedValue<>(gameProfile, System.currentTimeMillis()));

        SkullBlockEntity.updateGameprofile(gameProfile, profile -> {
            if (profile != null) {
                profileCache.put(pilot, new CachedValue<>(profile, System.currentTimeMillis()));
            }
        });

        return gameProfile;
    }

    private static final Map<UUID, CachedValue<ResourceLocation>> skinCache = new ConcurrentHashMap<>();

    public static void loadSkin(@Nullable GameProfile gameProfile) {
        if (gameProfile == null) return;

        UUID uuid = UUIDUtil.getOrCreatePlayerUUID(gameProfile);
        CachedValue<ResourceLocation> cached = skinCache.get(uuid);
        if (cached != null && !cached.isExpired(SKIN_CACHE_TTL_MS)) return; // Already loading or fresh

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getSkinManager().registerSkins(
                gameProfile,
                (type, location, texture) -> {
                    if (type == MinecraftProfileTexture.Type.SKIN) {
                        skinCache.put(uuid, new CachedValue<>(location, System.currentTimeMillis()));
                    }
                },
                true
        );
    }

    public static ResourceLocation getPlayerSkin(@Nullable GameProfile gameProfile) {

        if(true){
            return Minecraft.getInstance().getSkinManager().getInsecureSkinLocation(gameProfile);
        }

        if (gameProfile != null) {
            UUID uuid = UUIDUtil.getOrCreatePlayerUUID(gameProfile);

            loadSkin(gameProfile);

            CachedValue<ResourceLocation> cached = skinCache.get(uuid);
            if (cached != null) {
                return cached.value();
            }
            return DefaultPlayerSkin.getDefaultSkin(uuid);
        } else {
            return DefaultPlayerSkin.getDefaultSkin();
        }
    }
}