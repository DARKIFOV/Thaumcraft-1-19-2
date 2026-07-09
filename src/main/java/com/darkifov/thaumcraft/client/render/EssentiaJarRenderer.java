package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectColor;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class EssentiaJarRenderer implements BlockEntityRenderer<EssentiaJarBlockEntity> {
    private static final ResourceLocation FILL_TEXTURE =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/block/essentia_fill.png");
    private static final ResourceLocation ORIGINAL_LABEL_TEXTURE =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/label.png");

    public EssentiaJarRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(EssentiaJarBlockEntity jar, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Aspect aspect = jar.storedAspect();

        if (aspect != null && jar.amount() > 0) {
            float fill = jar.fillRatio();
            int color = AspectColor.argb(aspect, 170);
            float minY = 0.12F;
            float maxY = 0.12F + 0.68F * fill;

            poseStack.pushPose();
            poseStack.translate(0.5D, 0.0D, 0.5D);
            renderLiquidBox(poseStack, buffer.getBuffer(RenderType.entityTranslucent(FILL_TEXTURE)),
                    -0.31F, minY, -0.31F,
                    0.31F, maxY, 0.31F,
                    color, packedLight);
            poseStack.popPose();
        }

        if (jar.hasFilter()) {
            renderJarLabel(jar.filterAspect(), jar.labelFacing(), poseStack, buffer, packedLight);
        }
    }


    private void renderJarLabel(Aspect filter, Direction facing, PoseStack poseStack, MultiBufferSource buffer, int light) {
        if (filter == null) {
            return;
        }
        int color = AspectColor.argb(filter, 235);
        poseStack.pushPose();
        Direction safeFacing = facing == null || !facing.getAxis().isHorizontal() ? Direction.NORTH : facing;
        switch (safeFacing) {
            case SOUTH -> {
                poseStack.translate(0.5D, 0.40D, 0.815D);
                poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            }
            case EAST -> {
                poseStack.translate(0.815D, 0.40D, 0.5D);
                poseStack.mulPose(Vector3f.YP.rotationDegrees(270.0F));
            }
            case WEST -> {
                poseStack.translate(0.185D, 0.40D, 0.5D);
                poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
            }
            default -> poseStack.translate(0.5D, 0.40D, 0.185D);
        }
        VertexConsumer label = buffer.getBuffer(RenderType.entityTranslucent(ORIGINAL_LABEL_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        quad(matrix, label,
                -0.25F, -0.18F, 0.0F,
                 0.25F, -0.18F, 0.0F,
                 0.25F,  0.18F, 0.0F,
                -0.25F,  0.18F, 0.0F,
                255, 255, 255, 230, light);
        VertexConsumer fill = buffer.getBuffer(RenderType.entityTranslucent(FILL_TEXTURE));
        quad(matrix, fill,
                -0.09F, -0.09F, 0.002F,
                 0.09F, -0.09F, 0.002F,
                 0.09F,  0.09F, 0.002F,
                -0.09F,  0.09F, 0.002F,
                (color >> 16) & 255, (color >> 8) & 255, color & 255, (color >> 24) & 255, light);
        poseStack.popPose();
    }

    private void renderLiquidBox(PoseStack poseStack, VertexConsumer consumer,
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

    private void quad(Matrix4f matrix, VertexConsumer consumer,
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

    private void vertex(Matrix4f matrix, VertexConsumer consumer,
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
