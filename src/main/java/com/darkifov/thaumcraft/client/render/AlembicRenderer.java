package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectColor;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.AlembicBlockEntity;
import com.darkifov.thaumcraft.client.render.model.TC4AlembicModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Exact TC4 alembic OBJ/UV renderer plus the modern essentia fill layer. */
public class AlembicRenderer implements BlockEntityRenderer<AlembicBlockEntity> {
    private static final ResourceLocation MODEL_TEXTURE =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/models/alembic.png");
    private static final ResourceLocation FILL_TEXTURE =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/block/essentia_fill.png");

    public AlembicRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AlembicBlockEntity alembic, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderStandalone(poseStack, buffer, packedLight, packedOverlay);

        Aspect aspect = alembic.aspects().firstAspect();
        if (aspect == null || alembic.aspects().totalAmount() <= 0) {
            return;
        }

        float fill = Math.max(0.0F, Math.min(1.0F,
                alembic.aspects().totalAmount() / (float) AlembicBlockEntity.CAPACITY));
        int color = AspectColor.argb(aspect, 155);
        float maxY = 0.55F + 0.26F * fill;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        renderLiquidBox(poseStack, buffer.getBuffer(RenderType.entityTranslucent(FILL_TEXTURE)),
                -0.24F, 0.48F, -0.24F,
                0.24F, maxY, 0.24F,
                color, packedLight);
        poseStack.popPose();
    }

    public static void renderStandalone(PoseStack poseStack, MultiBufferSource buffer,
                                        int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        // The original OBJ stores Z as vertical. Rotate it into Minecraft Y.
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
        VertexConsumer model = buffer.getBuffer(RenderType.entityCutoutNoCull(MODEL_TEXTURE));
        TC4AlembicModel.renderAll(poseStack.last(), model, packedLight,
                packedOverlay == 0 ? OverlayTexture.NO_OVERLAY : packedOverlay);
        poseStack.popPose();
    }

    private static void renderLiquidBox(PoseStack poseStack, VertexConsumer consumer,
                                        float minX, float minY, float minZ,
                                        float maxX, float maxY, float maxZ,
                                        int color, int light) {
        Matrix4f matrix = poseStack.last().pose();
        int a = (color >> 24) & 255;
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;

        quad(matrix, consumer, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a, light);
        quad(matrix, consumer, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, minX, minY, maxZ, r, g, b, a, light);
        quad(matrix, consumer, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, maxX, minY, minZ, r, g, b, a, light);
        quad(matrix, consumer, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, minY, maxZ, r, g, b, a, light);
        quad(matrix, consumer, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, minX, minY, minZ, r, g, b, a, light);
    }

    private static void quad(Matrix4f matrix, VertexConsumer consumer,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             int r, int g, int b, int a, int light) {
        vertex(matrix, consumer, x1, y1, z1, 0.0F, 1.0F, r, g, b, a, light);
        vertex(matrix, consumer, x2, y2, z2, 1.0F, 1.0F, r, g, b, a, light);
        vertex(matrix, consumer, x3, y3, z3, 1.0F, 0.0F, r, g, b, a, light);
        vertex(matrix, consumer, x4, y4, z4, 0.0F, 0.0F, r, g, b, a, light);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer,
                               float x, float y, float z, float u, float v,
                               int r, int g, int b, int a, int light) {
        consumer.vertex(matrix, x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}
