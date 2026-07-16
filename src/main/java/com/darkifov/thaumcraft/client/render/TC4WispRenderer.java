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

/** Full-bright animated billboard preserving TC4's coloured Wisp shell and white core. */
public class TC4WispRenderer extends EntityRenderer<TC4WispEntity> {
    private static final ResourceLocation WISP =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/misc/wisp.png");
    private static final ResourceLocation PARTICLES =
            new ResourceLocation("minecraft", "textures/particle/particles.png");

    public TC4WispRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(TC4WispEntity entity, float yaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));

        Aspect aspect = entity.getAspect();
        int colour = aspect == null ? 0xFFFFFF : aspect.nativeColor();
        int red = colour >> 16 & 255;
        int green = colour >> 8 & 255;
        int blue = colour & 255;
        float pulse = 0.75F + Mth.sin((entity.tickCount + partialTicks) * 0.2F) * 0.08F;
        renderQuad(poseStack, buffers, WISP, pulse, 0.0F, 0.0F, 1.0F, 1.0F,
                red, green, blue, 220);
        renderQuad(poseStack, buffers, WISP, pulse * 0.45F, 0.0F, 0.0F, 1.0F, 1.0F,
                255, 255, 255, 245);

        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, buffers, 0xF000F0);
    }

    private static void renderQuad(PoseStack poseStack, MultiBufferSource buffers,
                                   ResourceLocation texture, float size,
                                   float u0, float v0, float u1, float v1,
                                   int red, int green, int blue, int alpha) {
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucentEmissive(texture));
        Matrix4f matrix = poseStack.last().pose();
        float half = size * 0.5F;
        vertex(matrix, consumer, -half, -half, u0, v1, red, green, blue, alpha);
        vertex(matrix, consumer, half, -half, u1, v1, red, green, blue, alpha);
        vertex(matrix, consumer, half, half, u1, v0, red, green, blue, alpha);
        vertex(matrix, consumer, -half, half, u0, v0, red, green, blue, alpha);
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
