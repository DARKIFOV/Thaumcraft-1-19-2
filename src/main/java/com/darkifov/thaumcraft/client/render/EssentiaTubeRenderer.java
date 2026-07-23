package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectColor;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.EssentiaTubeBlockEntity;
import com.darkifov.thaumcraft.essentia.TC4EssentiaTubeParity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
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
    private static final ResourceLocation ORIGINAL_VALVE_TEXTURE =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/valve.png");

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
        if (tube.subtype().storesBufferEssentia()) {
            renderBufferChokes(tube, poseStack, buffer, packedLight);
        }
        if (tube.subtype().redstoneValve()) {
            renderValve(tube, partialTick, poseStack, buffer, packedLight);
        }
        if (tube.isVenting()) {
            renderVentingCore(tube, poseStack, buffer, packedLight);
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

    private void renderValve(EssentiaTubeBlockEntity tube, float partialTick, PoseStack poseStack,
                             MultiBufferSource buffer, int light) {
        float rotation = tube.valveRotation(partialTick);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        orientLikeOriginalValve(poseStack, tube.facing());
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-rotation * TC4EssentiaTubeParity.VALVE_RENDER_ROTATION_MULTIPLIER));
        poseStack.translate(0.0D, -(rotation / TC4EssentiaTubeParity.VALVE_ROTATION_MAX)
                * TC4EssentiaTubeParity.VALVE_RENDER_TRAVEL, 0.0D);
        VertexConsumer valve = buffer.getBuffer(RenderType.entityCutoutNoCull(ORIGINAL_VALVE_TEXTURE));
        renderBox(poseStack, valve, -0.0625F, 0.125F, -0.0625F,
                0.0625F, 0.25F, 0.0625F, 0xFFFFFFFF, light);
        poseStack.popPose();
    }

    private void renderBufferChokes(EssentiaTubeBlockEntity tube, PoseStack poseStack,
                                    MultiBufferSource buffer, int light) {
        VertexConsumer valve = buffer.getBuffer(RenderType.entityCutoutNoCull(ORIGINAL_VALVE_TEXTURE));
        for (Direction direction : Direction.values()) {
            int choke = tube.chokeState(direction);
            if (choke == 0 || !tube.isSideOpen(direction) || !tube.canConnectSideLikeTC4(direction)) {
                continue;
            }
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.5D, 0.5D);
            orientLikeOriginalValve(poseStack, direction.getOpposite());
            int color = choke == 2 ? 0xFFFF4D4D : 0xFF4D4DFF;
            renderBox(poseStack, valve, -0.075F, -0.5F, -0.075F,
                    0.075F, -0.375F, 0.075F, color, light);
            poseStack.popPose();
        }
    }

    private void renderVentingCore(EssentiaTubeBlockEntity tube, PoseStack poseStack,
                                   MultiBufferSource buffer, int light) {
        int color = (160 << 24) | (tube.ventingColor() & 0xFFFFFF);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        renderBox(poseStack, buffer.getBuffer(RenderType.entityTranslucent(ESSENTIA_TEXTURE)),
                -0.18F, -0.18F, -0.18F, 0.18F, 0.18F, 0.18F, color, light);
        poseStack.popPose();
    }

    private void orientLikeOriginalValve(PoseStack poseStack, Direction facing) {
        if (facing.getStepY() == 0) {
            poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
        } else {
            poseStack.mulPose(Vector3f.XN.rotationDegrees(90.0F));
            poseStack.mulPose((facing.getStepY() > 0 ? Vector3f.XP : Vector3f.XN).rotationDegrees(90.0F));
        }
        Vector3f axis = new Vector3f(facing.getStepX(), facing.getStepY(), facing.getStepZ());
        poseStack.mulPose(axis.rotationDegrees(90.0F));
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
