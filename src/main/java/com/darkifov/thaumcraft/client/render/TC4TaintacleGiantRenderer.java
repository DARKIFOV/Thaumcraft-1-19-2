package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.TaintacleGiantEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Stage283-302 billboard/segment bridge for TC4 RenderTaintacle / ModelTaintacle. */
public class TC4TaintacleGiantRenderer extends EntityRenderer<TaintacleGiantEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/eldritch_taintacle.png");

    public TC4TaintacleGiantRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.55F;
    }

    @Override
    public void render(TaintacleGiantEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - yaw));
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        int light = entity.getAnger() > 0 ? LightTexture.FULL_BRIGHT : packedLight;
        float time = entity.tickCount + partialTicks;
        for (int i = 0; i < 9; i++) {
            poseStack.pushPose();
            float y = i * 0.55F;
            float radius = 0.42F - i * 0.022F;
            float wave = Mth.sin(time * 0.11F + i * 0.65F) * 0.08F * entity.getFlailIntensity();
            poseStack.translate(wave, y, 0.0D);
            quad(poseStack.last().pose(), consumer, radius, 0.62F, light);
            poseStack.popPose();
        }
        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void quad(Matrix4f matrix, VertexConsumer consumer, float half, float height, int light) {
        vertex(matrix, consumer, -half, 0.0F, 0.0F, 0.0F, 1.0F, light);
        vertex(matrix, consumer, half, 0.0F, 0.0F, 1.0F, 1.0F, light);
        vertex(matrix, consumer, half, height, 0.0F, 1.0F, 0.0F, light);
        vertex(matrix, consumer, -half, height, 0.0F, 0.0F, 0.0F, light);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float u, float v, int light) {
        consumer.vertex(matrix, x, y, z).color(255, 255, 255, 235).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(TaintacleGiantEntity entity) {
        return TEXTURE;
    }
}
