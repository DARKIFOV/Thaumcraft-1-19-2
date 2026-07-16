package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.TC4WispEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** TC4 4.2.3.5 full-bright additive Wisp billboard with exact atlas UVs. */
public class TC4WispRenderer extends EntityRenderer<TC4WispEntity> {
    private static final ResourceLocation WISP =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/misc/wisp.png");
    private static final ResourceLocation PARTICLES =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/misc/particles.png");

    public TC4WispRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(TC4WispEntity entity, float yaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        if (!entity.isAlive()) {
            return;
        }

        poseStack.pushPose();
        // TC4 translates the sprite to y + 0.45. The modern entity origin is at its feet.
        poseStack.translate(0.0D, 0.45D, 0.0D);
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));

        Aspect aspect = entity.getAspect();
        int colour = aspect == null ? 0xFFFFFF : aspect.nativeColor();
        int red = colour >> 16 & 255;
        int green = colour >> 8 & 255;
        int blue = colour & 255;
        if (entity.hurtTime > 0) {
            red = 255;
            green = green * 255 / 300;
            blue = blue * 255 / 300;
        }

        int frame = Math.floorMod(entity.tickCount, 16);

        // Core: one frame from the original 4x4 wisp atlas, scale ±1.0.
        float coreU0 = (frame % 4) / 4.0F;
        float coreU1 = coreU0 + 0.25F;
        float coreV0 = (frame / 4) / 4.0F;
        float coreV1 = coreV0 + 0.25F;
        renderQuad(poseStack, buffers, WISP, 1.0F,
                coreU0, coreV0, coreU1, coreV1, red, green, blue, 255);

        // Halo: frame from row 5 of the original 16x16 particles atlas.
        float haloU0 = frame / 16.0F;
        float haloU1 = haloU0 + 1.0F / 16.0F;
        float pulse = 0.4F + Mth.sin((entity.tickCount + partialTicks) / 10.0F) * 0.1F;
        renderQuad(poseStack, buffers, PARTICLES, pulse,
                haloU0, 5.0F / 16.0F, haloU1, 6.0F / 16.0F,
                255, 255, 255, 255);

        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, buffers, 0xF000F0);
    }

    private static void renderQuad(PoseStack poseStack, MultiBufferSource buffers,
                                   ResourceLocation texture, float halfSize,
                                   float u0, float v0, float u1, float v1,
                                   int red, int green, int blue, int alpha) {
        RenderType renderType = TC4NodeRenderTypes.node(texture, true, false);
        VertexConsumer consumer = buffers.getBuffer(renderType);
        Matrix4f matrix = poseStack.last().pose();

        // Preserve the original horizontal UV orientation from RenderWisp.drawBillboard.
        vertex(matrix, consumer, -halfSize, -halfSize, u1, v1, red, green, blue, alpha);
        vertex(matrix, consumer, halfSize, -halfSize, u0, v1, red, green, blue, alpha);
        vertex(matrix, consumer, halfSize, halfSize, u0, v0, red, green, blue, alpha);
        vertex(matrix, consumer, -halfSize, halfSize, u1, v0, red, green, blue, alpha);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer,
                               float x, float y, float u, float v,
                               int red, int green, int blue, int alpha) {
        consumer.vertex(matrix, x, y, 0.0F)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(0, 10)
                .uv2(0xF000F0)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(TC4WispEntity entity) {
        return WISP;
    }
}
