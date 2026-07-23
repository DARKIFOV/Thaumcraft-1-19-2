package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.entity.GolemBobberEntity;
import com.darkifov.thaumcraft.entity.ThaumGolemEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Modern renderer for TC4's particle-atlas bobber and sixteen-segment black line. */
public final class TC4GolemBobberRenderer extends EntityRenderer<GolemBobberEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/fishing_hook.png");

    public TC4GolemBobberRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(GolemBobberEntity bobber, float yaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        renderBobber(poseStack, buffers, packedLight);
        ThaumGolemEntity fisher = bobber.getFisher();
        if (fisher != null) {
            renderLine(bobber, fisher, partialTicks, poseStack, buffers);
        }
        super.render(bobber, yaw, partialTicks, poseStack, buffers, packedLight);
    }

    private void renderBobber(PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(TEXTURE));
        vertex(consumer, pose, -0.5F, -0.5F, 0.0F, 1.0F, packedLight);
        vertex(consumer, pose, 0.5F, -0.5F, 1.0F, 1.0F, packedLight);
        vertex(consumer, pose, 0.5F, 0.5F, 1.0F, 0.0F, packedLight);
        vertex(consumer, pose, -0.5F, 0.5F, 0.0F, 0.0F, packedLight);
        poseStack.popPose();
    }

    private void renderLine(GolemBobberEntity bobber, ThaumGolemEntity fisher, float partialTicks,
                            PoseStack poseStack, MultiBufferSource buffers) {
        double bobberX = Mth.lerp(partialTicks, bobber.xOld, bobber.getX());
        double bobberY = Mth.lerp(partialTicks, bobber.yOld, bobber.getY()) + 0.25D;
        double bobberZ = Mth.lerp(partialTicks, bobber.zOld, bobber.getZ());
        double baseX = Mth.lerp(partialTicks, fisher.xOld, fisher.getX());
        double baseY = Mth.lerp(partialTicks, fisher.yOld, fisher.getY());
        double baseZ = Mth.lerp(partialTicks, fisher.zOld, fisher.getZ());
        float bodyYaw = Mth.lerp(partialTicks, fisher.yBodyRotO, fisher.yBodyRot) * Mth.DEG_TO_RAD;
        double sin = Mth.sin(bodyYaw);
        double cos = Mth.cos(bodyYaw);
        double fisherX = baseX - cos * 0.25D - sin * 0.70D;
        double fisherY = baseY + fisher.getEyeHeight() - 0.40D;
        double fisherZ = baseZ - sin * 0.25D + cos * 0.70D;

        float dx = (float)(fisherX - bobberX);
        float dy = (float)(fisherY - bobberY);
        float dz = (float)(fisherZ - bobberZ);
        VertexConsumer line = buffers.getBuffer(RenderType.lines());
        PoseStack.Pose pose = poseStack.last();
        for (int i = 0; i < 16; i++) {
            float t0 = i / 16.0F;
            float t1 = (i + 1) / 16.0F;
            float x0 = dx * t0;
            float y0 = dy * (t0 * t0 + t0) * 0.5F + 0.25F;
            float z0 = dz * t0;
            float x1 = dx * t1;
            float y1 = dy * (t1 * t1 + t1) * 0.5F + 0.25F;
            float z1 = dz * t1;
            float nx = x1 - x0;
            float ny = y1 - y0;
            float nz = z1 - z0;
            float length = Mth.sqrt(nx * nx + ny * ny + nz * nz);
            if (length > 1.0E-5F) {
                nx /= length;
                ny /= length;
                nz /= length;
            }
            lineVertex(line, pose, x0, y0, z0, nx, ny, nz);
            lineVertex(line, pose, x1, y1, z1, nx, ny, nz);
        }
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose,
                               float x, float y, float u, float v, int packedLight) {
        consumer.vertex(pose.pose(), x, y, 0.0F)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    private static void lineVertex(VertexConsumer consumer, PoseStack.Pose pose,
                                   float x, float y, float z, float nx, float ny, float nz) {
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        consumer.vertex(matrix, x, y, z)
                .color(0, 0, 0, 255)
                .normal(normal, nx, ny, nz)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(GolemBobberEntity entity) {
        return TEXTURE;
    }
}
