package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.InfusionMatrixBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Stage208 renderer port of TC4 TileRunicMatrixRenderer/ModelCube.
 *
 * <p>Original behaviour kept here: eight 0.45-scale cubelets arranged at +/-0.25,
 * startup yaw/pitch/roll, active instability wobble, translucent overlay pass and
 * a crafting halo sourced from textures/models/infuser.png.</p>
 */
public class InfusionMatrixRenderer implements BlockEntityRenderer<InfusionMatrixBlockEntity> {
    private static final ResourceLocation INFUSER_TEXTURE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/models/infuser.png");

    public InfusionMatrixRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(InfusionMatrixBlockEntity matrix, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        float ticks = Minecraft.getInstance().player == null ? partialTicks : Minecraft.getInstance().player.tickCount + partialTicks;
        float startUp = matrix.updateAndGetRenderStartUp();
        int light = matrix.active() ? LightTexture.FULL_BRIGHT : packedLight;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees((ticks % 360.0F) * startUp));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(35.0F * startUp));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(45.0F * startUp));

        float instability = Math.min(6.0F, 1.0F + matrix.currentInstability() * 0.66F * (Math.min(matrix.craftCount(), 50) / 50.0F));
        VertexConsumer solid = bufferSource.getBuffer(RenderType.entityCutoutNoCull(INFUSER_TEXTURE));
        VertexConsumer glow = bufferSource.getBuffer(RenderType.entityTranslucent(INFUSER_TEXTURE));

        for (int a = 0; a < 2; a++) {
            for (int b = 0; b < 2; b++) {
                for (int c = 0; c < 2; c++) {
                    float wobbleX = matrix.active() ? Mth.sin((ticks + a * 10.0F) / (15.0F - instability / 2.0F)) * 0.01F * startUp * instability : 0.0F;
                    float wobbleY = matrix.active() ? Mth.sin((ticks + b * 10.0F) / (14.0F - instability / 2.0F)) * 0.01F * startUp * instability : 0.0F;
                    float wobbleZ = matrix.active() ? Mth.sin((ticks + c * 10.0F) / (13.0F - instability / 2.0F)) * 0.01F * startUp * instability : 0.0F;
                    renderPiece(poseStack, solid, a, b, c, wobbleX, wobbleY, wobbleZ, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
                    if (matrix.active()) {
                        float alpha = (Mth.sin((ticks + a * 2.0F + b * 3.0F + c * 4.0F) / 4.0F) * 0.1F + 0.2F) * startUp;
                        renderPiece(poseStack, glow, a, b, c, wobbleX, wobbleY, wobbleZ, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0.8F, 0.1F, 1.0F, alpha);
                    }
                }
            }
        }
        poseStack.popPose();

        if (matrix.crafting()) {
            renderHalo(matrix, ticks, poseStack, bufferSource);
        }
    }

    private static void renderPiece(PoseStack poseStack, VertexConsumer consumer, int a, int b, int c,
                                    float wobbleX, float wobbleY, float wobbleZ, int light, int overlay,
                                    float red, float green, float blue, float alpha) {
        int aa = a == 0 ? -1 : 1;
        int bb = b == 0 ? -1 : 1;
        int cc = c == 0 ? -1 : 1;
        poseStack.pushPose();
        poseStack.translate(wobbleX + aa * 0.25F, wobbleY + bb * 0.25F, wobbleZ + cc * 0.25F);
        if (a > 0) {
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
        }
        if (b > 0) {
            poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
        }
        if (c > 0) {
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
        }
        poseStack.scale(0.45F, 0.45F, 0.45F);
        cube(poseStack, consumer, -0.5F, -0.5F, -0.5F, 0.5F, 0.5F, 0.5F, light, overlay, red, green, blue, alpha, false);
        poseStack.popPose();
    }

    private static void renderHalo(InfusionMatrixBlockEntity matrix, float ticks, PoseStack poseStack, MultiBufferSource bufferSource) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(INFUSER_TEXTURE));
        int rays = Minecraft.useFancyGraphics() ? 20 : 10;
        float growth = Math.min(matrix.craftCount(), 50) / 50.0F;
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        for (int i = 0; i < rays; i++) {
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.XP.rotationDegrees((i * 37.0F + ticks * 0.7F) % 360.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees((i * 71.0F + ticks * 0.4F) % 360.0F));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees((i * 53.0F + ticks * 0.9F) % 360.0F));
            float length = (0.35F + (i % 5) * 0.04F) * growth;
            float width = (0.05F + (i % 3) * 0.02F) * growth;
            triangle(poseStack, consumer, length, width, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void triangle(PoseStack poseStack, VertexConsumer consumer, float length, float width, int light) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        vertex(consumer, matrix, normal, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.65F, 0.5F, 0.5F, light, 0.0F, 1.0F, 0.0F);
        vertex(consumer, matrix, normal, -width, length, -width, 0.52F, 0.2F, 1.0F, 0.0F, 0.0F, 1.0F, light, 0.0F, 1.0F, 0.0F);
        vertex(consumer, matrix, normal, width, length, -width, 0.52F, 0.2F, 1.0F, 0.0F, 1.0F, 1.0F, light, 0.0F, 1.0F, 0.0F);
    }

    private static void cube(PoseStack poseStack, VertexConsumer consumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                             int light, int overlay, float red, float green, float blue, float alpha, boolean halo) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        float u0 = halo ? 0.5F : 0.0F;
        float u1 = halo ? 1.0F : 0.5F;
        float v0 = 0.0F;
        float v1 = 0.5F;

        quad(consumer, matrix, normal, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, red, green, blue, alpha, u0, v0, u1, v1, light, overlay, 0.0F, 0.0F, 1.0F);
        quad(consumer, matrix, normal, maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, red, green, blue, alpha, u0, v0, u1, v1, light, overlay, 0.0F, 0.0F, -1.0F);
        quad(consumer, matrix, normal, maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, red, green, blue, alpha, u0, v0, u1, v1, light, overlay, 1.0F, 0.0F, 0.0F);
        quad(consumer, matrix, normal, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, red, green, blue, alpha, u0, v0, u1, v1, light, overlay, -1.0F, 0.0F, 0.0F);
        quad(consumer, matrix, normal, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ, red, green, blue, alpha, u0, v0, u1, v1, light, overlay, 0.0F, 1.0F, 0.0F);
        quad(consumer, matrix, normal, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, red, green, blue, alpha, u0, v0, u1, v1, light, overlay, 0.0F, -1.0F, 0.0F);
    }

    private static void quad(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                             float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4,
                             float red, float green, float blue, float alpha, float u0, float v0, float u1, float v1, int light, int overlay,
                             float normalX, float normalY, float normalZ) {
        vertex(consumer, matrix, normal, x1, y1, z1, red, green, blue, alpha, u0, v1, light, normalX, normalY, normalZ);
        vertex(consumer, matrix, normal, x2, y2, z2, red, green, blue, alpha, u1, v1, light, normalX, normalY, normalZ);
        vertex(consumer, matrix, normal, x3, y3, z3, red, green, blue, alpha, u1, v0, light, normalX, normalY, normalZ);
        vertex(consumer, matrix, normal, x4, y4, z4, red, green, blue, alpha, u0, v0, light, normalX, normalY, normalZ);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                               float x, float y, float z, float red, float green, float blue, float alpha,
                               float u, float v, int light, float normalX, float normalY, float normalZ) {
        consumer.vertex(matrix, x, y, z).color(red, green, blue, alpha).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, normalX, normalY, normalZ).endVertex();
    }
}
