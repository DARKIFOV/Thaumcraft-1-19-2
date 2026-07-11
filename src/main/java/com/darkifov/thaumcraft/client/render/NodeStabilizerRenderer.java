package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.NodeStabilizerBlockEntity;
import com.darkifov.thaumcraft.client.render.model.TC4NodeStabilizerModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Direct 1.19.2 transcription of TC4's TileNodeStabilizerRenderer.
 *
 * <p>The previous rebuild drew four generic cuboids over a placeholder block
 * model. This renderer uses the original {@code node_stabilizer.obj} geometry,
 * its original UVs and the original 0..37 piston animation. The static block
 * JSON is intentionally empty in world space, so this renderer owns the whole
 * visual and cannot overlap a duplicate placeholder model.</p>
 */
public final class NodeStabilizerRenderer implements BlockEntityRenderer<NodeStabilizerBlockEntity> {
    private static final ResourceLocation BASE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/node_stabilizer.png");
    private static final ResourceLocation OVERLAY = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/node_stabilizer_over.png");
    private static final ResourceLocation BUBBLE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/misc/node_bubble.png");

    public NodeStabilizerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(NodeStabilizerBlockEntity stabilizer, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float animationTicks = stabilizer.extensionTicks(partialTick);
        float extension = animationTicks / NodeStabilizerBlockEntity.MAX_EXTENSION_TICKS;
        long gameTime = stabilizer.getLevel() == null ? 0L : stabilizer.getLevel().getGameTime();

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        renderStandalone(stabilizer.lockLevel() == 2, animationTicks, gameTime,
                poseStack, buffer, packedLight);
        poseStack.popPose();

        if (extension > 0.0F) {
            renderField(stabilizer, extension, partialTick, poseStack, buffer);
        }
    }

    /**
     * Shared world/item renderer for the original TC4 stabilizer mesh.
     *
     * <p>The caller places the model origin. World rendering uses the block's
     * lower centre; item rendering uses the centre of the item transform. This
     * keeps one geometry/UV path for placed, held, dropped and inventory views
     * and prevents the old placeholder item model from returning.</p>
     */
    public static void renderStandalone(boolean advanced, float animationTicks, long animationTime,
                                        PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float pistonOffset = Mth.clamp(animationTicks, 0.0F,
                NodeStabilizerBlockEntity.MAX_EXTENSION_TICKS) / 100.0F;

        poseStack.pushPose();
        // Exact TC4 TileNodeStabilizerRenderer transform: block lower-centre,
        // then 90 degrees around the negative X axis. The OBJ Z axis becomes
        // world-up, keeping the lock on the block instead of hanging beside it.
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));

        VertexConsumer baseConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(BASE));
        renderMesh(poseStack, baseConsumer, TC4NodeStabilizerModel.LOCK_TRIANGLES,
                packedLight, 255, 255, 255, 255);

        for (int arm = 0; arm < 4; arm++) {
            poseStack.pushPose();
            // Exact original transform order: rotate around local Z, then Y,
            // then slide the piston out by count / 100.
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(arm * 90.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(45.0F));
            poseStack.translate(0.0D, 0.0D, pistonOffset);

            renderMesh(poseStack, baseConsumer, TC4NodeStabilizerModel.PISTON_TRIANGLES,
                    packedLight, 255, 255, 255, 255);

            float pulse = Mth.sin((animationTime + arm * 5.0F) / 3.0F) * 0.1F + 0.9F;
            float extension = Mth.clamp(animationTicks / NodeStabilizerBlockEntity.MAX_EXTENSION_TICKS,
                    0.0F, 1.0F);
            // Original renderer starts at brightness 50 and gains up to 170 as
            // the piston extends. The old adapter forced an opaque full-bright
            // overlay even while retracted, producing the solid red/white clone.
            int overlayAlpha = Mth.clamp(50 + (int) (170.0F * extension * pulse), 50, 220);
            int red = 255;
            int green = advanced ? 51 : 255;
            int blue = advanced ? 51 : 255;
            VertexConsumer overlayConsumer = buffer.getBuffer(RenderType.entityTranslucent(OVERLAY));
            renderMesh(poseStack, overlayConsumer, TC4NodeStabilizerModel.PISTON_TRIANGLES,
                    LightTexture.FULL_BRIGHT, red, green, blue, overlayAlpha);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    static void renderMesh(PoseStack poseStack, VertexConsumer consumer, float[] triangles,
                                   int light, int red, int green, int blue, int alpha) {
        if (triangles.length % (TC4NodeStabilizerModel.STRIDE * 3) != 0) {
            throw new IllegalStateException("Invalid embedded TC4 node stabilizer triangle data");
        }
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        int triangleStride = TC4NodeStabilizerModel.STRIDE * 3;
        for (int triangle = 0; triangle < triangles.length; triangle += triangleStride) {
            emitModelVertex(matrix, normalMatrix, consumer, triangles, triangle,
                    light, red, green, blue, alpha);
            emitModelVertex(matrix, normalMatrix, consumer, triangles,
                    triangle + TC4NodeStabilizerModel.STRIDE,
                    light, red, green, blue, alpha);
            emitModelVertex(matrix, normalMatrix, consumer, triangles,
                    triangle + TC4NodeStabilizerModel.STRIDE * 2,
                    light, red, green, blue, alpha);
            // RenderType's entity formats are quad-based. Repeating the final
            // corner preserves the original OBJ triangle without introducing
            // visible geometry.
            emitModelVertex(matrix, normalMatrix, consumer, triangles,
                    triangle + TC4NodeStabilizerModel.STRIDE * 2,
                    light, red, green, blue, alpha);
        }
    }

    private static void emitModelVertex(Matrix4f matrix, Matrix3f normalMatrix, VertexConsumer consumer,
                                        float[] data, int offset, int light,
                                        int red, int green, int blue, int alpha) {
        consumer.vertex(matrix, data[offset], data[offset + 1], data[offset + 2])
                .color(red, green, blue, alpha)
                .uv(data[offset + 3], data[offset + 4])
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normalMatrix, data[offset + 5], data[offset + 6], data[offset + 7])
                .endVertex();
    }

    private static void renderField(NodeStabilizerBlockEntity stabilizer, float extension,
                                    float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 1.5D, 0.5D);
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());

        long gameTime = stabilizer.getLevel() == null ? 0L : stabilizer.getLevel().getGameTime();
        float alphaPulse = Mth.sin((gameTime + partialTick) / 8.0F) * 0.1F + 0.5F;
        int alpha = Mth.clamp((int) (255.0F * extension * alphaPulse), 1, 255);
        int color = stabilizer.lockLevel() == 1 ? 0xFFFFFF : 0xFF4444;
        int red = (color >> 16) & 255;
        int green = (color >> 8) & 255;
        int blue = color & 255;
        float size = 0.9F;

        PoseStack.Pose pose = poseStack.last();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(BUBBLE));
        emitQuadVertex(pose.pose(), pose.normal(), consumer, -size, -size, 0.0F,
                0.0F, 1.0F, red, green, blue, alpha);
        emitQuadVertex(pose.pose(), pose.normal(), consumer, size, -size, 0.0F,
                1.0F, 1.0F, red, green, blue, alpha);
        emitQuadVertex(pose.pose(), pose.normal(), consumer, size, size, 0.0F,
                1.0F, 0.0F, red, green, blue, alpha);
        emitQuadVertex(pose.pose(), pose.normal(), consumer, -size, size, 0.0F,
                0.0F, 0.0F, red, green, blue, alpha);
        poseStack.popPose();
    }

    private static void emitQuadVertex(Matrix4f matrix, Matrix3f normalMatrix, VertexConsumer consumer,
                                       float x, float y, float z, float u, float v,
                                       int red, int green, int blue, int alpha) {
        consumer.vertex(matrix, x, y, z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normalMatrix, 0.0F, 0.0F, 1.0F)
                .endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(NodeStabilizerBlockEntity blockEntity) {
        return true;
    }
}
