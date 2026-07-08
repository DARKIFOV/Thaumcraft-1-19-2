package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.AspectStack;
import com.darkifov.thaumcraft.AspectVisuals;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.aura.AuraNodeType;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * TC4 1.7.10 TileNodeRenderer adapter.
 * Uses the original textures/misc/nodes.png 32-frame sprite sheet instead of fake block sprites.
 */
public class AuraNodeRenderer implements BlockEntityRenderer<AuraNodeBlockEntity> {
    private static final ResourceLocation NODE_SHEET =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/misc/nodes.png");
    private static final int FRAMES = 32;

    public AuraNodeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AuraNodeBlockEntity node, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        long time = node.getLevel() == null ? 0L : node.getLevel().getGameTime();
        int frame = (int) Math.floorMod((time / 2L) + node.getBlockPos().getX() + node.getBlockPos().getZ(), FRAMES);
        float modifierScale = node.typedNodeModifier().sizeScale();
        float energizedBoost = node.isEnergized() ? 0.24F : 0.0F;
        float size = (0.56F + energizedBoost + Math.min(0.54F, node.visualSize() / 170.0F)) * modifierScale;
        int aspectColor = AspectVisuals.blendedColor(node.aspects(), 255);
        int typeStrip = stripFor(node.typedNodeType());

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        float rotation = (time + partialTick) * 1.55F;

        poseStack.mulPose(Vector3f.YP.rotationDegrees(rotation));
        renderSheetPlane(poseStack, buffer, packedLight, size, aspectColor, 0.54F, frame, 0);
        renderSheetPlane(poseStack, buffer, packedLight, size * 1.08F, colorFor(node.typedNodeType()), 0.34F, frame, typeStrip);

        poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
        renderSheetPlane(poseStack, buffer, packedLight, size * 0.82F, aspectColor, 0.44F, (frame + 7) % FRAMES, 0);

        poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
        float modifierAlpha = node.typedNodeModifier().name().equals("FADING") ? 0.16F : 0.30F;
        renderSheetPlane(poseStack, buffer, packedLight, size * 0.68F, 0xFFFFFFFF, modifierAlpha, (frame + 13) % FRAMES, typeStrip);

        renderAspectWisps(node, time, partialTick, poseStack, buffer, packedLight, size, frame);
        poseStack.popPose();
    }

    private void renderAspectWisps(AuraNodeBlockEntity node, long time, float partialTick, PoseStack poseStack,
                                   MultiBufferSource buffer, int packedLight, float baseSize, int frame) {
        int index = 0;
        for (AspectStack stack : node.aspects().all()) {
            if (index >= 6) {
                return;
            }
            float angle = (time + partialTick) * (1.8F + index * 0.18F) + index * 60.0F;
            float radius = baseSize * (0.30F + index * 0.035F);
            float y = (float) Math.sin((time + partialTick + index * 11.0F) * 0.065F) * 0.09F;
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.YP.rotationDegrees(angle));
            poseStack.translate(radius, y, 0.0D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-angle));
            renderSheetPlane(poseStack, buffer, packedLight,
                    0.08F + Math.min(0.08F, stack.amount() / 260.0F),
                    stack.aspect().argbColor(), 0.62F, (frame + index * 3) % FRAMES, 0);
            poseStack.popPose();
            index++;
        }
    }

    private void renderSheetPlane(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                  float size, int color, float alphaScale, int frame, int strip) {
        VertexData data = VertexData.from(color, alphaScale);
        float u0 = frame / (float) FRAMES;
        float u1 = (frame + 1) / (float) FRAMES;
        float v0 = strip / (float) FRAMES;
        float v1 = (strip + 1) / (float) FRAMES;
        VertexConsumerHelper.quad(poseStack.last().pose(), buffer.getBuffer(RenderType.entityTranslucent(NODE_SHEET)),
                -size, -size, 0.0F,
                size, -size, 0.0F,
                size, size, 0.0F,
                -size, size, 0.0F,
                u0, v0, u1, v1,
                data.r, data.g, data.b, data.a, packedLight);
    }

    private int stripFor(AuraNodeType type) {
        return switch (type) {
            case UNSTABLE -> 6;
            case DARK -> 2;
            case TAINTED -> 5;
            case PURE -> 4;
            case HUNGRY -> 3;
            default -> 1;
        };
    }

    private int colorFor(AuraNodeType type) {
        return switch (type) {
            case PURE -> 0xFF9EEBFF;
            case TAINTED -> 0xFF8F38B8;
            case HUNGRY -> 0xFF4F2A6E;
            case DARK -> 0xFF5F3E8A;
            case UNSTABLE -> 0xFFFFB84A;
            default -> 0xFFBFA6FF;
        };
    }

    private record VertexData(int r, int g, int b, int a) {
        static VertexData from(int color, float alphaScale) {
            int a = Math.min(255, Math.max(0, (int) (((color >> 24) & 255) * alphaScale)));
            int r = (color >> 16) & 255;
            int g = (color >> 8) & 255;
            int b = color & 255;
            return new VertexData(r, g, b, a);
        }
    }

    private static final class VertexConsumerHelper {
        private VertexConsumerHelper() {
        }

        static void quad(Matrix4f matrix, com.mojang.blaze3d.vertex.VertexConsumer consumer,
                         float x1, float y1, float z1,
                         float x2, float y2, float z2,
                         float x3, float y3, float z3,
                         float x4, float y4, float z4,
                         float u0, float v0, float u1, float v1,
                         int r, int g, int b, int a, int light) {
            vertex(matrix, consumer, x1, y1, z1, u0, v1, r, g, b, a, light);
            vertex(matrix, consumer, x2, y2, z2, u1, v1, r, g, b, a, light);
            vertex(matrix, consumer, x3, y3, z3, u1, v0, r, g, b, a, light);
            vertex(matrix, consumer, x4, y4, z4, u0, v0, r, g, b, a, light);
        }

        private static void vertex(Matrix4f matrix, com.mojang.blaze3d.vertex.VertexConsumer consumer,
                                   float x, float y, float z, float u, float v,
                                   int r, int g, int b, int a, int light) {
            consumer.vertex(matrix, x, y, z)
                    .color(r, g, b, a)
                    .uv(u, v)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(light)
                    .normal(0.0F, 0.0F, 1.0F)
                    .endVertex();
        }
    }
}
