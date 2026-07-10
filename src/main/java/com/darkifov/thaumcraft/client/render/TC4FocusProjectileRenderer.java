package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.projectile.TC4EmberEntity;
import com.darkifov.thaumcraft.entity.projectile.TC4ExplosiveOrbEntity;
import com.darkifov.thaumcraft.entity.projectile.TC4FocusProjectileEntity;
import com.darkifov.thaumcraft.entity.projectile.TC4PrimalOrbEntity;
import com.darkifov.thaumcraft.entity.projectile.TC4PechBlastEntity;
import com.darkifov.thaumcraft.entity.projectile.TC4ShockOrbEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Stage176 TC4 focus projectile visual adapter.
 *
 * Original TC4 1.7.10 renderers used GL11, AdvancedModelLoader and ParticleEngine
 * sprite sheets directly.  Forge 1.19.2 cannot reuse that render path verbatim, so
 * this renderer preserves the original texture sheets, frame coordinates, scales and
 * projectile-specific color logic on a modern translucent quad renderer.
 */
public class TC4FocusProjectileRenderer<T extends TC4FocusProjectileEntity> extends EntityRenderer<T> {
    private static final ResourceLocation PARTICLES = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/misc/particles.png");
    private static final ResourceLocation PARTICLES2 = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/misc/particles2.png");

    public TC4FocusProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(T entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        if (entity instanceof TC4EmberEntity ember) {
            renderEmber(ember, poseStack, buffer);
        } else if (entity instanceof TC4ExplosiveOrbEntity explosive) {
            renderExplosiveOrb(explosive, poseStack, buffer);
        } else if (entity instanceof TC4ShockOrbEntity shock) {
            renderShockOrb(shock, partialTicks, poseStack, buffer);
        } else if (entity instanceof TC4PrimalOrbEntity primal) {
            renderPrimalOrb(primal, partialTicks, poseStack, buffer);
        } else if (entity instanceof TC4PechBlastEntity pech) {
            renderPechBlast(pech, partialTicks, poseStack, buffer);
        }
        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderEmber(TC4EmberEntity entity, PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        float duration = Math.max(1.0F, entity.getDuration());
        float progress = Mth.clamp(entity.tickCount / duration, 0.0F, 1.0F);
        int frame = Mth.clamp((int)(8.0F * progress), 0, 8);
        float u0 = (7 + frame) / 16.0F;
        float scale = 0.25F + progress;
        renderBillboard(poseStack, buffer, PARTICLES2, scale, u0, 0.5625F,
                u0 + 0.0625F, 0.625F, 255, 255, 255, 230, 220);
    }

    private void renderExplosiveOrb(TC4ExplosiveOrbEntity entity, PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        float frameU = 7.0F / 16.0F;
        float scale = (2.0F + Mth.sin(entity.tickCount * 0.35F) * 0.15F) * Math.max(0.75F, entity.getStrength());
        renderBillboard(poseStack, buffer, PARTICLES2, scale, frameU, 9.0F / 16.0F,
                frameU + 0.0625F, 10.0F / 16.0F, 255, 255, 255, 204, 220);
    }

    private void renderShockOrb(TC4ShockOrbEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        float frameU = (1 + entity.tickCount % 6) / 8.0F;
        float bob = Mth.sin((entity.tickCount + partialTicks) / 5.0F) * 0.2F + 0.2F;
        renderBillboard(poseStack, buffer, PARTICLES, 1.0F + bob, frameU, 0.875F, frameU + 0.125F, 1.0F, 255, 255, 255, 204, 220);
    }

    private void renderPrimalOrb(TC4PrimalOrbEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer) {
        float age = entity.tickCount + partialTicks;
        float ramp = Mth.clamp(age / 10.0F, 0.0F, 1.0F);
        int[] colors = {
                Aspect.AER.argbColor(), Aspect.TERRA.argbColor(), Aspect.IGNIS.argbColor(),
                Aspect.AQUA.argbColor(), Aspect.ORDO.argbColor(), Aspect.PERDITIO.argbColor()
        };
        for (int i = 0; i < 12; i++) {
            poseStack.pushPose();
            poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(age * 12.0F + i * 30.0F));
            int color = colors[i % colors.length];
            int r = (color >> 16) & 255;
            int g = (color >> 8) & 255;
            int b = color & 255;
            renderBillboard(poseStack, buffer, PARTICLES, (0.35F + i * 0.015F) * ramp, 0.0F, 0.125F, 0.0625F, 0.1875F, r, g, b, 105, 220);
            poseStack.popPose();
        }
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        float frameU = (entity.tickCount % 13) / 16.0F;
        renderBillboard(poseStack, buffer, PARTICLES, 0.5F, frameU, 0.125F, frameU + 0.0625F, 0.1875F,
                255, 255, 255, 204, 220);
    }


    private void renderPechBlast(TC4PechBlastEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        float pulse = 0.9F + Mth.sin((entity.tickCount + partialTicks) * 0.45F) * 0.15F;
        int r = entity.nightshade() ? 90 : 120;
        int g = entity.nightshade() ? 20 : 75;
        int b = entity.nightshade() ? 130 : 175;
        float frameU = (entity.tickCount % 8) / 16.0F;
        renderBillboard(poseStack, buffer, PARTICLES, pulse, frameU, 0.125F, frameU + 0.0625F, 0.1875F, r, g, b, 220, 220);
    }

    private void renderBillboard(PoseStack poseStack, MultiBufferSource buffer, ResourceLocation texture, float scale,
                                 float u0, float v0, float u1, float v1,
                                 int r, int g, int b, int a, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(texture));
        Matrix4f matrix = poseStack.last().pose();
        float half = scale * 0.5F;
        quad(matrix, consumer, -half, -half, 0.0F, half, half, 0.0F, u0, v0, u1, v1, r, g, b, a, packedLight);
    }

    private static void quad(Matrix4f matrix, VertexConsumer consumer,
                             float x0, float y0, float z0,
                             float x1, float y1, float z1,
                             float u0, float v0, float u1, float v1,
                             int r, int g, int b, int a, int light) {
        vertex(matrix, consumer, x0, y0, z0, u0, v1, r, g, b, a, light);
        vertex(matrix, consumer, x1, y0, z1, u1, v1, r, g, b, a, light);
        vertex(matrix, consumer, x1, y1, z1, u1, v0, r, g, b, a, light);
        vertex(matrix, consumer, x0, y1, z0, u0, v0, r, g, b, a, light);
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
        if (entity instanceof TC4EmberEntity) return PARTICLES2;
        if (entity instanceof TC4ExplosiveOrbEntity) return PARTICLES2;
        return PARTICLES;
    }
}
