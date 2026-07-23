package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.render.model.TC4HoverHarnessBackModel;
import com.darkifov.thaumcraft.item.gear.HoverHarnessItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Player layer for the original TC4 harness OBJ, energized rings, and local lightning arcs. */
public final class TC4HoverHarnessLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation BACK_TEXTURE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/models/hoverharness2.png");
    private static final ResourceLocation RING_TEXTURE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/item/tc4/lightningring.png");
    private static final int RING_FRAMES = 16;

    public TC4HoverHarnessLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ItemStack harness = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(harness.getItem() instanceof HoverHarnessItem)) {
            return;
        }

        renderBackModel(poseStack, buffers, packedLight);
        if (HoverHarnessItem.isHoverEnabled(harness)) {
            renderActiveEffects(poseStack, buffers, player, ageInTicks);
        }
    }

    private void renderBackModel(PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        getParentModel().body.translateAndRotate(poseStack);
        // Preserve the original ModelHoverHarness GL transform order.
        poseStack.scale(0.1F, 0.1F, 0.1F);
        poseStack.mulPose(Vector3f.XN.rotationDegrees(90.0F));
        poseStack.translate(0.0D, 0.33D, -3.7D);
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(BACK_TEXTURE));
        TC4HoverHarnessBackModel.render(poseStack.last(), consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderActiveEffects(PoseStack poseStack, MultiBufferSource buffers,
                                     AbstractClientPlayer player, float ageInTicks) {
        poseStack.pushPose();
        getParentModel().body.translateAndRotate(poseStack);
        poseStack.translate(0.0D, 0.20D, 0.55D);

        int frame = Mth.floor(ageInTicks) & (RING_FRAMES - 1);
        VertexConsumer rings = buffers.getBuffer(RenderType.eyes(RING_TEXTURE));
        renderRing(poseStack.last(), rings, 2.5F, frame, 255, 255, 255);

        poseStack.pushPose();
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        poseStack.translate(0.0D, 0.0D, 0.03D);
        renderRing(poseStack.last(), rings, 1.5F, frame, 255, 128, 255);
        poseStack.popPose();

        renderLightning(poseStack, buffers, player);
        poseStack.popPose();
    }

    private static void renderRing(PoseStack.Pose pose, VertexConsumer consumer, float size, int frame,
                                   int red, int green, int blue) {
        float half = size * 0.5F;
        float v0 = frame / (float) RING_FRAMES;
        float v1 = (frame + 1) / (float) RING_FRAMES;
        ringVertex(pose, consumer, -half, half, 0.0F, 0.0F, v1, red, green, blue);
        ringVertex(pose, consumer, half, half, 0.0F, 1.0F, v1, red, green, blue);
        ringVertex(pose, consumer, half, -half, 0.0F, 1.0F, v0, red, green, blue);
        ringVertex(pose, consumer, -half, -half, 0.0F, 0.0F, v0, red, green, blue);
    }

    private static void ringVertex(PoseStack.Pose pose, VertexConsumer consumer,
                                   float x, float y, float z, float u, float v,
                                   int red, int green, int blue) {
        consumer.vertex(pose.pose(), x, y, z)
                .color(red, green, blue, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(pose.normal(), 0.0F, 0.0F, 1.0F)
                .endVertex();
    }

    private static void renderLightning(PoseStack poseStack, MultiBufferSource buffers,
                                        AbstractClientPlayer player) {
        long interval = player.tickCount / 3L;
        long seed = ((long) player.getId() * 341873128712L) ^ (interval * 132897987541L);
        RandomSource random = RandomSource.create(seed);
        float endX = (random.nextFloat() - 0.5F) * 4.0F;
        float endY = 1.2F + random.nextFloat() * 1.8F;
        float endZ = (random.nextFloat() - 0.5F) * 3.5F;

        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        PoseStack.Pose pose = poseStack.last();
        float px = (random.nextFloat() - 0.5F) * 0.35F;
        float py = 0.0F;
        float pz = 0.02F;
        final int segments = 8;
        for (int i = 1; i <= segments; i++) {
            float t = i / (float) segments;
            float taper = 1.0F - Math.abs(t * 2.0F - 1.0F);
            float nx = endX * t + (random.nextFloat() - 0.5F) * 0.38F * taper;
            float ny = endY * t + (random.nextFloat() - 0.5F) * 0.28F * taper;
            float nz = endZ * t + (random.nextFloat() - 0.5F) * 0.38F * taper;
            emitLine(lines, pose, px, py, pz, nx, ny, nz);
            px = nx;
            py = ny;
            pz = nz;
        }
    }

    private static void emitLine(VertexConsumer consumer, PoseStack.Pose pose,
                                 float x0, float y0, float z0, float x1, float y1, float z1) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float dz = z1 - z0;
        float length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        if (length > 1.0E-5F) {
            dx /= length;
            dy /= length;
            dz /= length;
        }
        lineVertex(consumer, pose, x0, y0, z0, dx, dy, dz);
        lineVertex(consumer, pose, x1, y1, z1, dx, dy, dz);
    }

    private static void lineVertex(VertexConsumer consumer, PoseStack.Pose pose,
                                   float x, float y, float z, float nx, float ny, float nz) {
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        consumer.vertex(matrix, x, y, z)
                .color(196, 224, 255, 230)
                .normal(normal, nx, ny, nz)
                .endVertex();
    }
}
