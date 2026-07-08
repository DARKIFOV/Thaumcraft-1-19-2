package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.EldritchGuardianEntity;
import com.darkifov.thaumcraft.client.render.model.TC4BakedEldritchModel;
import com.darkifov.thaumcraft.client.render.model.TC4EldritchBossLayerDefinitions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;

/**
 * Stage219 direct renderer for the non-boss TC4 EntityEldritchGuardian.
 *
 * <p>Original anchor: RenderEldritchGuardian uses skin[0], shadow 0.5F,
 * alpha blending and the distance fade branch outside the Outer Lands.  Stage217
 * still registered guardians as a block placeholder; this renderer switches the
 * active 1.19.2 port to the same guardian/warden model family and keeps the
 * original alpha-distance formula as a client-safe adapter.</p>
 */
// Stage219 marker: RenderEldritchGuardian skin[0], shadow 0.5F, HARD 576 / EASY-NORMAL 1024 distance fade
public class TC4EldritchGuardianRenderer extends EntityRenderer<EldritchGuardianEntity> {
    private final TC4BakedEldritchModel<EldritchGuardianEntity> bakedModel;
    private static final ResourceLocation GUARDIAN = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/eldritch_guardian.png");

    public TC4EldritchGuardianRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.bakedModel = new TC4BakedEldritchModel<>(context.bakeLayer(TC4EldritchBossLayerDefinitions.ELDRITCH_GUARDIAN));
    }

    @Override
    public void render(EldritchGuardianEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float alpha = guardianAlpha(entity);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(GUARDIAN));
        int light = entity.tickCount % 40 < 5 ? LightTexture.FULL_BRIGHT : packedLight;
        float ticks = entity.tickCount + partialTicks;
        float breathe = Mth.sin(ticks * 0.08F) * 0.018F;
        float armSwing = Mth.sin(ticks * 0.13F) * 0.035F;

        poseStack.pushPose();
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - entityYaw));
        poseStack.scale(TC4EldritchBossModelParity.GUARDIAN_RENDER_SCALE, TC4EldritchBossModelParity.GUARDIAN_RENDER_SCALE, TC4EldritchBossModelParity.GUARDIAN_RENDER_SCALE);

        // Stage220: baked ModelPart path from TC4 ModelEldritchGuardian LayerDefinition.
        bakedModel.resetArmLift(armSwing, -armSwing);
        bakedModel.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, alpha);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EldritchGuardianEntity entity) {
        return GUARDIAN;
    }

    private static float guardianAlpha(EldritchGuardianEntity entity) {
        if (entity.level.dimension().location().toString().contains("outer")) {
            return 1.0F;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return 1.0F;
        }
        double distanceSqr = entity.distanceToSqr(player);
        if (distanceSqr < 256.0D) {
            return 0.6F;
        }
        float max = entity.level.getDifficulty() == Difficulty.HARD ? 576.0F : 1024.0F;
        float start = 256.0F;
        float fade = (float)(1.0D - Math.min(max - start, distanceSqr - start) / (max - start)) * 0.6F;
        return Mth.clamp(fade, 0.0F, 0.6F);
    }
}
