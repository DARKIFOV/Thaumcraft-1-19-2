package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.AspectVisuals;
import com.darkifov.thaumcraft.AspectStack;
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

public class AuraNodeRenderer implements BlockEntityRenderer<AuraNodeBlockEntity> {
    private static final ResourceLocation TEXTURE_NORMAL =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/block/aura_node_sprite_normal.png");
    private static final ResourceLocation TEXTURE_PURE =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/block/aura_node_sprite_pure.png");
    private static final ResourceLocation TEXTURE_TAINTED =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/block/aura_node_sprite_tainted.png");
    private static final ResourceLocation TEXTURE_HUNGRY =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/block/aura_node_sprite_hungry.png");
    private static final ResourceLocation TEXTURE_DARK =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/block/aura_node_sprite_dark.png");
    private static final ResourceLocation TEXTURE_UNSTABLE =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/block/aura_node_sprite_unstable.png");

    public AuraNodeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AuraNodeBlockEntity node, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        long time = node.getLevel() == null ? 0L : node.getLevel().getGameTime();
        float scanBoost = node.scanned() ? 0.16F : 0.0F;
        float modifierScale = node.typedNodeModifier().sizeScale();
        float energizedBoost = node.isEnergized() ? 0.24F : 0.0F;
        float size = (0.58F + scanBoost + energizedBoost + Math.min(0.62F, node.visualSize() / 165.0F)) * modifierScale;
        int color = AspectVisuals.blendedColor(node.aspects(), 255);
        ResourceLocation texture = textureFor(node.typedNodeType().name());

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees((time + partialTick) * 1.6F));
        float stabilityPulse = 0.72F + (100 - Math.max(0, node.stability())) / 350.0F;
        renderPlane(poseStack, buffer, packedLight, texture, size, color, stabilityPulse);
        int specialTypeTint = colorFor(node.typedNodeType().name());
        if (!"NORMAL".equals(node.typedNodeType().name())) {
            renderPlane(poseStack, buffer, packedLight, texture, size * 1.12F, specialTypeTint, 0.22F);
        }
        if (node.isEnergized()) {
            renderPlane(poseStack, buffer, packedLight, texture, size * 1.35F, 0xFFBFFFFF, 0.32F);
        }

        poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
        renderPlane(poseStack, buffer, packedLight, texture, size * 0.82F, color, 0.48F);

        poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
        renderPlane(poseStack, buffer, packedLight, texture, size * 0.68F, 0xFFFFFFFF, node.typedNodeModifier().name().equals("FADING") ? 0.22F : 0.34F);
        renderAspectWisps(node, time, partialTick, poseStack, buffer, packedLight, size);
        poseStack.popPose();
    }

    private void renderAspectWisps(AuraNodeBlockEntity node, long time, float partialTick, PoseStack poseStack,
                                   MultiBufferSource buffer, int packedLight, float baseSize) {
        int index = 0;
        for (AspectStack stack : node.aspects().all()) {
            if (index >= 6) {
                return;
            }
            float angle = (time + partialTick) * (1.8F + index * 0.18F) + index * 60.0F;
            float radius = baseSize * (0.32F + index * 0.035F);
            float y = (float) Math.sin((time + partialTick + index * 11.0F) * 0.065F) * 0.09F;
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.YP.rotationDegrees(angle));
            poseStack.translate(radius, y, 0.0D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-angle));
            renderPlane(poseStack, buffer, packedLight, textureFor(node.typedNodeType().name()), 0.08F + Math.min(0.08F, stack.amount() / 260.0F), stack.aspect().argbColor(), 0.62F);
            poseStack.popPose();
            index++;
        }
    }

    private void renderPlane(PoseStack poseStack, MultiBufferSource buffer, int packedLight, ResourceLocation texture,
                             float size, int color, float alphaScale) {
        VertexData data = VertexData.from(color, alphaScale);
        VertexConsumerHelper.quad(poseStack.last().pose(), buffer.getBuffer(RenderType.entityTranslucent(texture)),
                -size, -size, 0.0F,
                size, -size, 0.0F,
                size, size, 0.0F,
                -size, size, 0.0F,
                data.r, data.g, data.b, data.a, packedLight);
    }

    private int colorFor(String type) {
        if ("PURE".equals(type)) {
            return 0xFF9EEBFF;
        }

        if ("TAINTED".equals(type)) {
            return 0xFF8F38B8;
        }

        if ("HUNGRY".equals(type)) {
            return 0xFF4F2A6E;
        }

        if ("DARK".equals(type)) {
            return 0xFF5F3E8A;
        }

        if ("UNSTABLE".equals(type)) {
            return 0xFFFFB84A;
        }

        return 0xFFBFA6FF;
    }

    private ResourceLocation textureFor(String type) {
        if ("PURE".equals(type)) {
            return TEXTURE_PURE;
        }
        if ("TAINTED".equals(type)) {
            return TEXTURE_TAINTED;
        }
        if ("HUNGRY".equals(type)) {
            return TEXTURE_HUNGRY;
        }
        if ("DARK".equals(type)) {
            return TEXTURE_DARK;
        }
        if ("UNSTABLE".equals(type)) {
            return TEXTURE_UNSTABLE;
        }
        return TEXTURE_NORMAL;
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
                         int r, int g, int b, int a, int light) {
            vertex(matrix, consumer, x1, y1, z1, 0.0F, 1.0F, r, g, b, a, light);
            vertex(matrix, consumer, x2, y2, z2, 1.0F, 1.0F, r, g, b, a, light);
            vertex(matrix, consumer, x3, y3, z3, 1.0F, 0.0F, r, g, b, a, light);
            vertex(matrix, consumer, x4, y4, z4, 0.0F, 0.0F, r, g, b, a, light);
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
