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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ConsolePilotRenderer {

    private static ConsolePilotModel<?> pilotModel;

    public static void init(EntityModelSet modelSet) {
        pilotModel = new ConsolePilotModel<>(modelSet.bakeLayer(RWFModelRegistry.PILOT));
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
            init(Minecraft.getInstance().getEntityModels());
            return;
        }

        Level level = blockEntity.getLevel();
        if (level == null) return;

        UUID pilot = ClientFlightTracker.get(blockEntity.getLevel().dimension())
                .map(ClientFlightData::pilot)
                .orElse(null);
        if (pilot == null) return;

        renderPilot(pilot, partialTick, poseStack, bufferSource, packedLight, level);
    }


    @SuppressWarnings("unchecked")
    private static void renderPilot(
            UUID pilot,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            Level level
    ) {
        ConsolePilotModel<?> model = pilotModel;
        model.root().getAllParts().forEach(ModelPart::resetPose);

        float ageInTicks = Minecraft.getInstance().player.tickCount + partialTick;
        model.setupAnim(null, 0, 0, ageInTicks, 0, 0);

        poseStack.pushPose();
        poseStack.translate(0.0F, 0F, -2);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        GameProfile gameProfile = new GameProfile(pilot, "");
        SkullBlockEntity.updateGameprofile(gameProfile, gameProfile1 -> {
        });

        ResourceLocation skinTexture = getPlayerSkin(gameProfile);

        VertexConsumer innerConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(skinTexture));
        model.renderToBuffer(poseStack, innerConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);

        // ── Name tag ──────────────────────────────────────────────────────────────
        String name = Minecraft.getInstance().getConnection() != null
                && Minecraft.getInstance().getConnection().getPlayerInfo(pilot) != null
                && !Minecraft.getInstance().getConnection().getPlayerInfo(pilot).getProfile().getName().isEmpty()
                ? Minecraft.getInstance().getConnection().getPlayerInfo(pilot).getProfile().getName()
                : pilot.toString();

        if (!name.isEmpty() && !Minecraft.getInstance().options.hideGui) {
            renderNameTag(
                ChatFormatting.YELLOW + name + ChatFormatting.DARK_GRAY + " [" + ChatFormatting.GOLD + "In Flight" + ChatFormatting.DARK_GRAY + "]",
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
        Font font = Minecraft.getInstance().font;
        Component nameComponent = Component.literal(name);

        poseStack.pushPose();

        poseStack.translate(0.0F, -1F, 0.0F);


        poseStack.scale(-0.025F, 0.025F, 0.025F);

        Matrix4f matrix = poseStack.last().pose();
        float bgOpacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int bgColor = (int) (bgOpacity * 255.0F) << 24;

        float halfWidth = (float) (-font.width(nameComponent)) / 2.0F;
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());

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

    private static final Map<UUID, ResourceLocation> skinCache = new ConcurrentHashMap<>();

    public static void loadSkin(@Nullable GameProfile gameProfile) {
        if (gameProfile == null) return;

        UUID uuid = UUIDUtil.getOrCreatePlayerUUID(gameProfile);
        if (skinCache.containsKey(uuid)) return; // Already loading or loaded

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getSkinManager().registerSkins(
                gameProfile,
                (type, location, texture) -> {
                    if (type == MinecraftProfileTexture.Type.SKIN) {
                        skinCache.put(uuid, location);
                    }
                },
                true // requireSecure - set false if you want offline/insecure skins too
        );
    }

    public static ResourceLocation getPlayerSkin(@Nullable GameProfile gameProfile) {
        if (gameProfile != null) {
            UUID uuid = UUIDUtil.getOrCreatePlayerUUID(gameProfile);

            loadSkin(gameProfile);

            ResourceLocation skin = skinCache.get(uuid);
            if (skin != null) {
                return skin;
            }
            return DefaultPlayerSkin.getDefaultSkin(uuid);
        } else {
            return DefaultPlayerSkin.getDefaultSkin();
        }
    }
}