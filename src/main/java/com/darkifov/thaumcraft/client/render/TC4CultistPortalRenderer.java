package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.CultistPortalEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Stage283-302 renderer adapter for original RenderCultistPortal. */
public class TC4CultistPortalRenderer extends EntityRenderer<CultistPortalEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/misc/cultist_portal.png");

    public TC4CultistPortalRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(CultistPortalEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        long time = System.nanoTime() / 50000000L;
        int frame = 15 - (int)(time % 16L);
        float u0 = frame / 16.0F;
        float u1 = u0 + 0.0625F;
        float scale = Math.max(0.05F, entity.getTc4RenderScale(partialTicks));
        float yScale = 1.5F;
        if (entity.hurtTime > 0) {
            double d = Math.sin(entity.hurtTime * 72.0D * Math.PI / 180.0D);
            yScale -= (float)d / 4.0F;
        }
        if (entity.getPulse() > 0) {
            double d = Math.sin(entity.getPulse() * 36.0D * Math.PI / 180.0D);
            yScale += (float)d / 4.0F;
        }
        float half = scale;
        float height = scale * yScale;
        float alpha = entity.getTc4RenderAlpha();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        int light = LightTexture.FULL_BRIGHT;
        vertex(matrix, consumer, -half, 0.0F, 0.0F, u1, 1.0F, light, alpha);
        vertex(matrix, consumer, half, 0.0F, 0.0F, u1, 0.0F, light, alpha);
        vertex(matrix, consumer, half, height, 0.0F, u0, 0.0F, light, alpha);
        vertex(matrix, consumer, -half, height, 0.0F, u0, 1.0F, light, alpha);
        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float u, float v, int light, float alpha) {
        int a = Math.max(0, Math.min(255, (int)(alpha * 255.0F)));
        consumer.vertex(matrix, x, y, z).color(255, 255, 255, a).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(CultistPortalEntity entity) {
        return TEXTURE;
    }
}
