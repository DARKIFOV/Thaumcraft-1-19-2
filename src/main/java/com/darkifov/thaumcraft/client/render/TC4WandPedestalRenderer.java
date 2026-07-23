package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.blockentity.TC4WandPedestalBlockEntity;
import com.darkifov.thaumcraft.client.TC4AuraNodeHudParity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Exact visual adapter for TC4 TileWandPedestalRenderer. */
public final class TC4WandPedestalRenderer implements BlockEntityRenderer<TC4WandPedestalBlockEntity> {
    private static final int TC4_LINK_QUALITY = 16;
    private static final float TC4_BEAM_WIDTH = 0.15F;
    private static final float TC4_BEAM_SPEED = -0.02F;

    private final ItemRenderer itemRenderer;
    public TC4WandPedestalRenderer(BlockEntityRendererProvider.Context context) { itemRenderer = context.getItemRenderer(); }

    @Override public void render(TC4WandPedestalBlockEntity pedestal, float partialTick, PoseStack poseStack,
                                 MultiBufferSource buffer, int light, int overlay) {
        ItemStack stack = pedestal.stored();
        if (stack.isEmpty()) return;

        Minecraft minecraft = Minecraft.getInstance();
        float ticks = (minecraft.player == null
                ? (pedestal.getLevel() == null ? 0L : pedestal.getLevel().getGameTime())
                : minecraft.player.tickCount) + partialTick;
        float bob = Mth.sin((ticks % 32767.0F) / 16.0F) * 0.05F;

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.15D + bob, 0.5D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(ticks % 360.0F));
        poseStack.scale(0.62F, 0.62F, 0.62F);
        itemRenderer.renderStatic(stack, ItemTransforms.TransformType.GROUND, light,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, 0);
        poseStack.popPose();

        BlockPos source = pedestal.drainSource();
        if (source != null && pedestal.isDrainVisualActive()) {
            renderDrainLine(pedestal, source, bob, poseStack, buffer, LightTexture.FULL_BRIGHT);
        }
    }

    private static void renderDrainLine(TC4WandPedestalBlockEntity pedestal, BlockPos source, float bob,
                                        PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        BlockPos pedestalPos = pedestal.getBlockPos();
        Vec3 startLocal = new Vec3(0.5D, 1.65D - bob * 2.0F, 0.5D);
        Vec3 endLocal = new Vec3(
                source.getX() - pedestalPos.getX() + 0.5D,
                source.getY() - pedestalPos.getY() + 0.5D,
                source.getZ() - pedestalPos.getZ() + 0.5D);
        Vec3 startWorld = Vec3.atLowerCornerOf(pedestalPos).add(startLocal);
        renderOriginalFloatyLine(startLocal, endLocal, startWorld, 1.0F, pedestal.drainColor(),
                poseStack, buffer, packedLight);
    }

    /** Numeric Forge 1.19.2 adapter for TC4 UtilsFX.drawFloatyLine. */
    private static void renderOriginalFloatyLine(Vec3 startLocal, Vec3 endLocal, Vec3 startWorld,
                                                 float distanceFactor, int color, PoseStack poseStack,
                                                 MultiBufferSource buffer, int packedLight) {
        Vec3 delta = startLocal.subtract(endLocal);
        float dist = (float) delta.length();
        if (dist < 1.0E-4F || distanceFactor <= 0.0F) return;

        float blocks = Math.round(dist);
        float length = Math.max(1.0F, blocks * (TC4_LINK_QUALITY / 2.0F));
        int steps = Math.max(1, Mth.floor(length * Mth.clamp(distanceFactor, 0.0F, 1.0F)));
        float time = (float) (System.nanoTime() / 30_000_000L);
        float phase = (time % 32767.0F) / 5.0F;
        float qualityHalf = TC4_LINK_QUALITY / 2.0F;
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;

        VertexConsumer consumer = buffer.getBuffer(
                TC4NodeRenderTypes.node(TC4AuraNodeHudParity.ORIGINAL_WISPY, true, false));
        Matrix4f matrix = poseStack.last().pose();
        Vec3 previous = null;
        float previousU = 0.0F;
        float previousAlpha = 0.0F;

        for (int i = 0; i <= steps; i++) {
            float f2 = i / length;
            float f3 = 1.0F - Math.abs(i - length / 2.0F) / (length / 2.0F);
            f3 = Mth.clamp(f3, 0.0F, 1.0F);
            double waveBase = dist * (1.0F - f2) * qualityHalf - phase;
            double dx = delta.x + Mth.sin((float) ((startWorld.z % 16.0D + waveBase) / 4.0D)) * 0.5F * f3;
            double dy = delta.y + Mth.sin((float) ((startWorld.x % 16.0D + waveBase) / 3.0D)) * 0.5F * f3;
            double dz = delta.z + Mth.sin((float) ((startWorld.y % 16.0D + waveBase) / 2.0D)) * 0.5F * f3;
            Vec3 current = endLocal.add(dx * f2, dy * f2, dz * f2);
            float currentU = (1.0F - f2) * dist - time * TC4_BEAM_SPEED;

            if (previous != null) {
                VertexConsumerHelper.beamQuad(matrix, consumer,
                        previous.add(0.0D, -TC4_BEAM_WIDTH, 0.0D),
                        current.add(0.0D, -TC4_BEAM_WIDTH, 0.0D),
                        current.add(0.0D, TC4_BEAM_WIDTH, 0.0D),
                        previous.add(0.0D, TC4_BEAM_WIDTH, 0.0D),
                        previousU, currentU, previousAlpha, f3, r, g, b, packedLight);
                VertexConsumerHelper.beamQuad(matrix, consumer,
                        previous.add(-TC4_BEAM_WIDTH, 0.0D, 0.0D),
                        current.add(-TC4_BEAM_WIDTH, 0.0D, 0.0D),
                        current.add(TC4_BEAM_WIDTH, 0.0D, 0.0D),
                        previous.add(TC4_BEAM_WIDTH, 0.0D, 0.0D),
                        previousU, currentU, previousAlpha, f3, r, g, b, packedLight);
            }
            previous = current;
            previousU = currentU;
            previousAlpha = f3;
        }
    }

    private static final class VertexConsumerHelper {
        private VertexConsumerHelper() {}

        static void beamQuad(Matrix4f matrix, VertexConsumer consumer,
                             Vec3 p1, Vec3 p2, Vec3 p3, Vec3 p4,
                             float u0, float u1, float alpha0, float alpha1,
                             int r, int g, int b, int light) {
            int a0 = Mth.clamp((int)(alpha0 * 255.0F), 0, 255);
            int a1 = Mth.clamp((int)(alpha1 * 255.0F), 0, 255);
            vertex(matrix, consumer, p1, u0, 1.0F, r, g, b, a0, light);
            vertex(matrix, consumer, p2, u1, 1.0F, r, g, b, a1, light);
            vertex(matrix, consumer, p3, u1, 0.0F, r, g, b, a1, light);
            vertex(matrix, consumer, p4, u0, 0.0F, r, g, b, a0, light);
        }

        private static void vertex(Matrix4f matrix, VertexConsumer consumer, Vec3 point,
                                   float u, float v, int r, int g, int b, int a, int light) {
            consumer.vertex(matrix, (float)point.x, (float)point.y, (float)point.z)
                    .color(r, g, b, a).uv(u, v)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                    .normal(0.0F, 0.0F, 1.0F).endVertex();
        }
    }

    @Override public int getViewDistance() { return 64; }
}
