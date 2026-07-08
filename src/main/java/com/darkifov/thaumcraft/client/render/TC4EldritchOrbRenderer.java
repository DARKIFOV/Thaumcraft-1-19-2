package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.Projectile;

/** Lightweight billboard renderer for Stage216 EldritchOrb/GolemOrb projectiles. */
public class TC4EldritchOrbRenderer<T extends Projectile> extends EntityRenderer<T> {
    private static final ResourceLocation PARTICLES = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/misc/particles.png");

    public TC4EldritchOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(T entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        float frameU = (entity.tickCount % 8) / 16.0F;
        float scale = entity.getBbWidth() <= 0.3F ? 0.55F : 0.75F;
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(PARTICLES));
        Matrix4f matrix = poseStack.last().pose();
        float half = scale * 0.5F;
        vertex(matrix, consumer, -half, -half, 0.0F, frameU, 0.25F, 180, 80, 255, 210, packedLight);
        vertex(matrix, consumer, half, -half, 0.0F, frameU + 0.0625F, 0.25F, 180, 80, 255, 210, packedLight);
        vertex(matrix, consumer, half, half, 0.0F, frameU + 0.0625F, 0.1875F, 180, 80, 255, 210, packedLight);
        vertex(matrix, consumer, -half, half, 0.0F, frameU, 0.1875F, 180, 80, 255, 210, packedLight);
        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float u, float v,
                               int r, int g, int b, int a, int light) {
        consumer.vertex(matrix, x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(0.0F, 0.0F, 1.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return PARTICLES;
    }
}
