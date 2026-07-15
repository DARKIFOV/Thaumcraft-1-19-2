package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.AspectOrbEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Pixel-faithful billboard port of TC4 RenderAspectOrb. */
public class AspectOrbRenderer extends EntityRenderer<AspectOrbEntity> {
    private static final ResourceLocation PARTICLES = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/misc/particles.png");

    public AspectOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.1F;
        shadowStrength = 0.5F;
    }

    @Override
    public void render(AspectOrbEntity orb, float yaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        Aspect aspect = orb.getAspect();
        int frame = (int) (System.nanoTime() / 25_000_000L % 16L);
        float u0 = frame / 16.0F;
        float u1 = (frame + 1) / 16.0F;
        float v0 = 0.5F;
        float v1 = 0.5625F;
        // TC4 evaluates the multiplication before the division, so the entire
        // lifetime ratio is floating-point. Parenthesizing the integer quotient
        // here would incorrectly collapse the animation to two sizes.
        float remaining = (float) (AspectOrbEntity.MAX_AGE - orb.orbAge());
        float scale = 0.1F + 0.3F * remaining / (float) AspectOrbEntity.MAX_AGE;
        int color = aspect.nativeColor();
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;
        // TC4 adds exactly 120 to the raw block-light coordinate and clamps it
        // to 240. Keeping the raw 0..240 coordinate preserves the original
        // half-light-step instead of approximating it as +8 modern light levels.
        int blockLightCoordinate = packedLight & 0xFFFF;
        int skyLightCoordinate = packedLight & 0xFFFF0000;
        int boostedLight = Math.min(240, blockLightCoordinate + 120) | skyLightCoordinate;

        poseStack.pushPose();
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.scale(scale, scale, scale);
        RenderType renderType = TC4NodeRenderTypes.node(PARTICLES, !aspect.usesAlphaBlend(), false);
        VertexConsumer consumer = buffer.getBuffer(renderType);
        Matrix4f matrix = poseStack.last().pose();
        vertex(matrix, consumer, -0.5F, -0.25F, 0.0F, u0, v1, red, green, blue, 128, boostedLight);
        vertex(matrix, consumer, 0.5F, -0.25F, 0.0F, u1, v1, red, green, blue, 128, boostedLight);
        vertex(matrix, consumer, 0.5F, 0.75F, 0.0F, u1, v0, red, green, blue, 128, boostedLight);
        vertex(matrix, consumer, -0.5F, 0.75F, 0.0F, u0, v0, red, green, blue, 128, boostedLight);
        poseStack.popPose();
        super.render(orb, yaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z,
                               float u, float v, int red, int green, int blue, int alpha, int light) {
        consumer.vertex(matrix, x, y, z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(AspectOrbEntity entity) {
        return PARTICLES;
    }
}
