package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.EldritchWardenEntity;
import com.darkifov.thaumcraft.client.render.model.TC4BakedEldritchModel;
import com.darkifov.thaumcraft.client.render.model.TC4EldritchBossLayerDefinitions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
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
 * Stage217 renderer adapter for TC4 RenderEldritchGuardian when rendering EntityEldritchWarden.
 *
 * <p>The original 1.7.10 renderer uses ModelEldritchGuardian, the warden skin,
 * alpha-blended rendering, a 1.5 scale and sinks the model during the 150 tick spawn
 * timer.  This 1.19.2 renderer keeps those behavioural contracts while using a
 * compact cuboid model until the full decompiled ModelRenderer tree is migrated.</p>
 */
// Stage217 marker: poseStack.scale(1.5F, 1.5F, 1.5F); entity.getSpawnTimer() / 150.0F
public class TC4EldritchWardenRenderer extends EntityRenderer<EldritchWardenEntity> {
    private final TC4BakedEldritchModel<EldritchWardenEntity> bakedModel;
    private static final ResourceLocation WARDEN = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/eldritch_warden.png");

    public TC4EldritchWardenRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.9F;
        this.bakedModel = new TC4BakedEldritchModel<>(context.bakeLayer(TC4EldritchBossLayerDefinitions.ELDRITCH_WARDEN));
    }

    @Override
    public void render(EldritchWardenEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(WARDEN));
        int light = entity.isSpawning() || entity.getAnger() > 0 ? LightTexture.FULL_BRIGHT : packedLight;
        float ticks = entity.tickCount + partialTicks;
        float spawnSink = entity.getBbHeight() * (entity.getSpawnTimer() / TC4EldritchBossModelParity.SPAWN_TIMER_TICKS);
        float breathe = Mth.sin(ticks * 0.08F) * 0.025F;
        float left = entity.getArmLiftL();
        float right = entity.getArmLiftR();

        poseStack.pushPose();
        poseStack.translate(0.0D, -spawnSink, 0.0D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - entityYaw));
        poseStack.scale(TC4EldritchBossModelParity.WARDEN_RENDER_SCALE, TC4EldritchBossModelParity.WARDEN_RENDER_SCALE, TC4EldritchBossModelParity.WARDEN_RENDER_SCALE);

        // Stage220: baked ModelPart path from TC4 ModelEldritchGuardian LayerDefinition.
        // Stage253-262 audit anchor: TC4EldritchBossModelParity.WARDEN_HEAD documents the legacy box fallback.
        bakedModel.resetArmLift(left, right);
        bakedModel.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 0.90F);

        if (entity.getAnger() > 0 || entity.isSpawning()) {
            float halo = entity.isSpawning() ? 0.35F : 0.18F;
            renderBox(poseStack, consumer, TC4EldritchBossModelParity.WARDEN_HALO, LightTexture.FULL_BRIGHT, 0.45F, 0.12F, 0.95F, halo);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EldritchWardenEntity entity) {
        return WARDEN;
    }

    static void renderBox(PoseStack poseStack, VertexConsumer consumer, TC4EldritchBossModelParity.Box box, int light, float red, float green, float blue, float alpha) {
        renderBox(poseStack, consumer, box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ(), light, red, green, blue, alpha);
    }

    static void renderBox(PoseStack poseStack, VertexConsumer consumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int light, float red, float green, float blue, float alpha) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        quad(consumer, matrix, normal, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, red, green, blue, alpha, light, 0.0F, 0.0F, 1.0F);
        quad(consumer, matrix, normal, maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, red, green, blue, alpha, light, 0.0F, 0.0F, -1.0F);
        quad(consumer, matrix, normal, maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, red, green, blue, alpha, light, 1.0F, 0.0F, 0.0F);
        quad(consumer, matrix, normal, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, red, green, blue, alpha, light, -1.0F, 0.0F, 0.0F);
        quad(consumer, matrix, normal, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ, red, green, blue, alpha, light, 0.0F, 1.0F, 0.0F);
        quad(consumer, matrix, normal, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, red, green, blue, alpha, light, 0.0F, -1.0F, 0.0F);
    }

    private static void quad(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float red, float green, float blue, float alpha, int light, float nx, float ny, float nz) {
        vertex(consumer, matrix, normal, x1, y1, z1, red, green, blue, alpha, 0.0F, 1.0F, light, nx, ny, nz);
        vertex(consumer, matrix, normal, x2, y2, z2, red, green, blue, alpha, 1.0F, 1.0F, light, nx, ny, nz);
        vertex(consumer, matrix, normal, x3, y3, z3, red, green, blue, alpha, 1.0F, 0.0F, light, nx, ny, nz);
        vertex(consumer, matrix, normal, x4, y4, z4, red, green, blue, alpha, 0.0F, 0.0F, light, nx, ny, nz);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int light, float nx, float ny, float nz) {
        consumer.vertex(matrix, x, y, z).color(red, green, blue, alpha).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, nx, ny, nz).endVertex();
    }
}
