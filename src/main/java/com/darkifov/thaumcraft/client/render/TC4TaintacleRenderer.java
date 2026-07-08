package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.TaintacleEntity;
import com.darkifov.thaumcraft.entity.TaintacleSmallEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Stage323-342 ModelTaintacle numeric adapter.
 *
 * <p>Stage303 still rendered the body as billboard quads.  TC4 1.7.10 uses
 * ModelTaintacle segmented ModelRenderer parts: the base grows out of the
 * ground, each body segment bends by a deterministic tick wave, the upper
 * segments taper, and agitation/hurt state makes the whole chain flail.  Forge
 * 1.19.2 does not have the legacy ModelRenderer stack, so this renderer keeps
 * the original data contract and renders a small 8-sided prism chain using the
 * same numeric anchors instead of inventing a flat placeholder model.</p>
 */
public class TC4TaintacleRenderer<T extends TaintacleEntity> extends EntityRenderer<T> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/taintacle.png");
    private static final ResourceLocation ELDRITCH_TEXTURE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/eldritch_taintacle.png");

    private static final int NORMAL_SEGMENTS = 8;
    private static final int SMALL_SEGMENTS = 4;
    private static final int SIDES = 8;

    public TC4TaintacleRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.25F;
    }

    @Override
    public void render(T entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - yaw));
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
        int light = entity.getAgitationState() ? LightTexture.FULL_BRIGHT : packedLight;
        renderSegmentedTaintacle(entity, partialTicks, poseStack, consumer, light);
        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void renderSegmentedTaintacle(TaintacleEntity entity, float partialTicks, PoseStack poseStack, VertexConsumer consumer, int light) {
        boolean small = entity instanceof TaintacleSmallEntity;
        int segments = small ? SMALL_SEGMENTS : NORMAL_SEGMENTS;
        float segmentHeight = small ? 0.25F : 0.38F;
        float baseRadius = small ? 0.105F : 0.315F;
        float age = entity.tickCount + partialTicks;
        float grow = Mth.clamp(age / (entity.getBbHeight() * 10.0F), 0.0F, 1.0F);
        float flail = entity.getFlailIntensity();
        float previousX = 0.0F;
        float previousZ = 0.0F;
        float previousY = 0.0F;

        for (int i = 0; i < segments; i++) {
            float t0 = i / (float) segments;
            float t1 = (i + 1) / (float) segments;
            float visible = Mth.clamp(grow * segments - i, 0.0F, 1.0F);
            if (visible <= 0.0F) {
                continue;
            }

            float nextY = (i + visible) * segmentHeight;
            float bendScale = (small ? 0.025F : 0.065F) * flail * (0.35F + t1);
            float waveA = age * 0.145F + i * 0.72F;
            float waveB = age * 0.117F + i * 0.91F;
            float nextX = Mth.sin(waveA) * bendScale * i;
            float nextZ = Mth.cos(waveB) * bendScale * i;
            float radius0 = Math.max(0.035F, baseRadius * (1.0F - t0 * 0.55F));
            float radius1 = Math.max(0.025F, baseRadius * (1.0F - t1 * 0.66F));

            renderPrismSegment(poseStack.last().pose(), consumer,
                    previousX, previousY, previousZ, radius0,
                    nextX, nextY, nextZ, radius1,
                    i / (float) segments, (i + visible) / (float) segments, light);

            previousX = nextX;
            previousZ = nextZ;
            previousY = nextY;
        }
    }

    private static void renderPrismSegment(Matrix4f matrix, VertexConsumer consumer,
                                           float x0, float y0, float z0, float r0,
                                           float x1, float y1, float z1, float r1,
                                           float v0, float v1, int light) {
        for (int side = 0; side < SIDES; side++) {
            double a0 = (Math.PI * 2.0D * side) / SIDES;
            double a1 = (Math.PI * 2.0D * (side + 1)) / SIDES;
            float sx0 = Mth.cos((float) a0);
            float sz0 = Mth.sin((float) a0);
            float sx1 = Mth.cos((float) a1);
            float sz1 = Mth.sin((float) a1);
            float u0 = side / (float) SIDES;
            float u1 = (side + 1) / (float) SIDES;
            vertex(matrix, consumer, x0 + sx0 * r0, y0, z0 + sz0 * r0, u0, v0, light, sx0, 0.0F, sz0);
            vertex(matrix, consumer, x0 + sx1 * r0, y0, z0 + sz1 * r0, u1, v0, light, sx1, 0.0F, sz1);
            vertex(matrix, consumer, x1 + sx1 * r1, y1, z1 + sz1 * r1, u1, v1, light, sx1, 0.0F, sz1);
            vertex(matrix, consumer, x1 + sx0 * r1, y1, z1 + sz0 * r1, u0, v1, light, sx0, 0.0F, sz0);
        }
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float u, float v,
                               int light, float nx, float ny, float nz) {
        consumer.vertex(matrix, x, y, z)
                .color(255, 255, 255, 235)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(nx, ny, nz)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return entity.getPersistentData().getBoolean("TC4EldritchSkin") ? ELDRITCH_TEXTURE : TEXTURE;
    }
}
