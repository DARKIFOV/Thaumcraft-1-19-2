package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.CrucibleBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Stage127: TC4 TileCrucibleRenderer-style liquid surface for Forge 1.19.2.
 *
 * Original TC4 renders a vanilla water icon at TileCrucible#getFluidHeight(), then
 * darkens the surface as tagAmount approaches the 100-tag overflow point. This
 * renderer keeps that layout and adds aspect-weighted colour so the modern port
 * gives immediate visual feedback for the essentia in the crucible.
 */
public class CrucibleRenderer implements BlockEntityRenderer<CrucibleBlockEntity> {
    private static final ResourceLocation WATER_TEXTURE =
            new ResourceLocation("minecraft", "textures/block/water_still.png");
    private static final ResourceLocation FROTH_TEXTURE =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/misc/r_crucible.png");

    public CrucibleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CrucibleBlockEntity crucible, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!crucible.hasWater()) {
            return;
        }

        float height = Mth.clamp(crucible.fluidHeight(), 0.31F, 1.001F);
        float topY = Mth.clamp(height, 0.31F, 0.96F);
        int color = crucible.liquidColorArgb(crucible.isBoiling() ? 205 : 178);
        float pulse = crucible.isBoiling()
                ? 0.004F * Mth.sin((crucible.boilTicks() + partialTick) * 0.45F)
                : 0.0F;

        poseStack.pushPose();
        poseStack.translate(0.5D, topY + pulse, 0.5D);
        renderSurface(poseStack, buffer.getBuffer(RenderType.entityTranslucent(WATER_TEXTURE)), 0.385F, color, packedLight);

        if (crucible.isBoiling()) {
            int frothColor = crucible.liquidColorArgb(120);
            float frothScale = 0.25F + Math.min(0.16F, crucible.aspects().totalAmount() / 420.0F);
            poseStack.translate(0.0D, 0.004D, 0.0D);
            renderSurface(poseStack, buffer.getBuffer(RenderType.entityTranslucent(FROTH_TEXTURE)), frothScale, frothColor, packedLight);
        }
        poseStack.popPose();
    }

    private void renderSurface(PoseStack poseStack, VertexConsumer consumer, float halfSize, int color, int light) {
        Matrix4f matrix = poseStack.last().pose();
        int a = (color >> 24) & 255;
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;
        vertex(matrix, consumer, -halfSize, 0.0F, -halfSize, 0.0F, 0.0F, r, g, b, a, light);
        vertex(matrix, consumer, -halfSize, 0.0F, halfSize, 0.0F, 1.0F, r, g, b, a, light);
        vertex(matrix, consumer, halfSize, 0.0F, halfSize, 1.0F, 1.0F, r, g, b, a, light);
        vertex(matrix, consumer, halfSize, 0.0F, -halfSize, 1.0F, 0.0F, r, g, b, a, light);
        vertex(matrix, consumer, halfSize, 0.0F, -halfSize, 1.0F, 0.0F, r, g, b, a, light);
        vertex(matrix, consumer, halfSize, 0.0F, halfSize, 1.0F, 1.0F, r, g, b, a, light);
        vertex(matrix, consumer, -halfSize, 0.0F, halfSize, 0.0F, 1.0F, r, g, b, a, light);
        vertex(matrix, consumer, -halfSize, 0.0F, -halfSize, 0.0F, 0.0F, r, g, b, a, light);
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

    @Override
    public boolean shouldRenderOffScreen(CrucibleBlockEntity crucible) {
        return true;
    }
}
