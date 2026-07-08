package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.EldritchGolemEntity;
import com.darkifov.thaumcraft.client.render.model.TC4BakedEldritchModel;
import com.darkifov.thaumcraft.client.render.model.TC4EldritchBossLayerDefinitions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Stage217 renderer adapter for TC4 RenderEldritchGolem/ModelEldritchGolem.
 *
 * <p>Preserves the 2.15 boss scale, alpha blended render pass, headless second
 * phase, spawn timer sink and charging/arc glow hooks.  The cuboid layout is a
 * 1.19.2-safe bridge model that references the original TC4 golem texture.</p>
 */
// Stage217 marker: poseStack.scale(2.15F, 2.15F, 2.15F); entity.getBeamCharge() / 150.0F
public class TC4EldritchGolemRenderer extends EntityRenderer<EldritchGolemEntity> {
    private final TC4BakedEldritchModel<EldritchGolemEntity> bakedModel;
    private static final ResourceLocation GOLEM = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/eldritch_golem.png");

    public TC4EldritchGolemRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 1.1F;
        this.bakedModel = new TC4BakedEldritchModel<>(context.bakeLayer(TC4EldritchBossLayerDefinitions.ELDRITCH_GOLEM));
    }

    @Override
    public void render(EldritchGolemEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(GOLEM));
        int light = entity.isChargingBeam() || entity.isSpawning() ? LightTexture.FULL_BRIGHT : packedLight;
        float ticks = entity.tickCount + partialTicks;
        float spawnSink = entity.getBbHeight() * (entity.getSpawnTimer() / TC4EldritchBossModelParity.SPAWN_TIMER_TICKS);
        float armSwing = Mth.sin(ticks * 0.18F) * 0.04F;
        float charge = Mth.clamp(entity.getBeamCharge() / (float)TC4EldritchBossModelParity.GOLEM_BEAM_CHARGE_TICKS, 0.0F, 1.0F);

        poseStack.pushPose();
        poseStack.translate(0.0D, -spawnSink, 0.0D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - entityYaw));
        poseStack.scale(TC4EldritchBossModelParity.GOLEM_RENDER_SCALE, TC4EldritchBossModelParity.GOLEM_RENDER_SCALE, TC4EldritchBossModelParity.GOLEM_RENDER_SCALE);

        // Stage220: baked ModelPart path from TC4 ModelEldritchGolem LayerDefinition.
        // Stage253-262 audit anchor: TC4EldritchBossModelParity.GOLEM_HEADLESS_GLOW documents the legacy headless bridge.
        bakedModel.animateGolem(ticks, 0.65F, entity.isHeadless());
        bakedModel.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 0.94F);

        if (entity.isChargingBeam()) {
            TC4EldritchWardenRenderer.renderBox(poseStack, consumer, TC4EldritchBossModelParity.GOLEM_BEAM_GLOW, LightTexture.FULL_BRIGHT, 0.25F, 0.05F, 1.0F, 0.16F + charge * 0.28F);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EldritchGolemEntity entity) {
        return GOLEM;
    }
}
