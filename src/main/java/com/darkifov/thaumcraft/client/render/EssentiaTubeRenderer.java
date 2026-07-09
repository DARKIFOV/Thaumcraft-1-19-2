package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectColor;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.EssentiaTubeBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Stage200 Forge 1.19.2 adapter for TC4 TileTubeFilter/Restrict/Oneway/Buffer/Valve visuals.
 * It keeps the baked multipart tube model and overlays original-style label, buffer-aspect and
 * valve/choke visual markers using original TC4 texture paths.
 */
public class EssentiaTubeRenderer implements BlockEntityRenderer<EssentiaTubeBlockEntity> {
    private static final ResourceLocation ESSENTIA_TEXTURE =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/block/essentia_fill.png");
    private static final ResourceLocation ORIGINAL_LABEL_TEXTURE =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/label.png");

    public EssentiaTubeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(EssentiaTubeBlockEntity tube, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tube.subtype().storesBufferEssentia() && tube.bufferAmount() > 0 && tube.bufferAspect() != null) {
            renderBufferAspect(tube, poseStack, buffer, packedLight);
        }
        if (tube.subtype().usesAspectFilter() && tube.aspectFilter() != null) {
            renderFilterLabel(tube.aspectFilter(), poseStack, buffer, packedLight);
        }
        if (tube.subtype().redstoneValve() || tube.subtype().restrictsSuction() || tube.isVenting()) {
            renderValveOrChoke(tube, poseStack, buffer, packedLight);
        }
    }

    private void renderBufferAspect(EssentiaTubeBlockEntity tube, PoseStack poseStack, MultiBufferSource buffer, int light) {
        Aspect aspect = tube.bufferAspect();
        int color = AspectColor.argb(aspect, 180);
        float fill = tube.bufferFillRatio();
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        float radius = 0.10F + 0.08F * fill;
        renderBox(poseStack, buffer.getBuffer(RenderType.entityTranslucent(ESSENTIA_TEXTURE)),
                -radius, -radius, -radius, radius, radius, radius, color, light);
        poseStack.popPose();
    }

    private void renderFilterLabel(Aspect aspect, PoseStack poseStack, MultiBufferSource buffer, int light) {
        int color = AspectColor.argb(aspect, 230);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.18D);
        VertexConsumer label = buffer.getBuffer(RenderType.entityTranslucent(ORIGINAL_LABEL_TEXTURE));
        quad(poseStack.last().pose(), label,
                -0.18F, -0.18F, 0.0F,
                 0.18F, -0.18F, 0.0F,
                 0.18F,  0.18F, 0.0F,
                -0.18F,  0.18F, 0.0F,
                255, 255, 255, 220, light);
        VertexConsumer fill = buffer.getBuffer(RenderType.entityTranslucent(ESSENTIA_TEXTURE));
        quad(poseStack.last().pose(), fill,
                -0.08F, -0.08F, 0.002F,
                 0.08F, -0.08F, 0.002F,
                 0.08F,  0.08F, 0.002F,
                -0.08F,  0.08F, 0.002F,
                (color >> 16) & 255, (color >> 8) & 255, color & 255, (color >> 24) & 255, light);
        poseStack.popPose();
    }

    private void renderValveOrChoke(EssentiaTubeBlockEntity tube, PoseStack poseStack, MultiBufferSource buffer, int light) {
        int alpha = tube.isFlowAllowed() ? 135 : 210;
        int rgb = tube.isVenting() ? tube.ventingColor() : tube.isFlowAllowed() ? 0x3366FF : 0xAA2222;
        int color = (alpha << 24) | rgb;
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        float size = tube.subtype().redstoneValve() ? 0.245F : 0.205F;
        renderBox(poseStack, buffer.getBuffer(RenderType.entityTranslucent(ESSENTIA_TEXTURE)),
                -size, -0.025F, -size, size, 0.025F, size, color, light);
        poseStack.popPose();
    }

    private void renderBox(PoseStack poseStack, VertexConsumer consumer,
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
