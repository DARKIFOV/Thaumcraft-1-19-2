package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.NodeTransducerBlockEntity;
import com.darkifov.thaumcraft.client.render.model.TC4NodeStabilizerModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Exact model-path port of TC4 TileNodeConverterRenderer. */
public final class NodeTransducerRenderer implements BlockEntityRenderer<NodeTransducerBlockEntity> {
    private static final ResourceLocation BASE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/node_converter.png");
    private static final ResourceLocation OVERLAY = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/node_converter_over.png");

    public NodeTransducerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(NodeTransducerBlockEntity transducer, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float offset = transducer.pistonOffset(partialTick);
        long time = transducer.getLevel() == null ? 0L : transducer.getLevel().getGameTime();

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.0D, 0.5D);
        renderStandalone(transducer.status(), offset, time, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    /** Shared world/item path for TC4's node_stabilizer.obj converter mesh. */
    public static void renderStandalone(int status, float pistonOffset, long animationTime,
                                        PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float offset = Mth.clamp(pistonOffset, 0.0F, NodeTransducerBlockEntity.MAX_EXTENSION_TICKS / 137.0F);
        int[] rgb = statusColor(status);

        poseStack.pushPose();
        poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));

        VertexConsumer base = buffer.getBuffer(RenderType.entityCutoutNoCull(BASE));
        VertexConsumer glow = buffer.getBuffer(RenderType.entityCutoutNoCull(OVERLAY));
        NodeStabilizerRenderer.renderMesh(poseStack, base, TC4NodeStabilizerModel.LOCK_TRIANGLES,
                packedLight, 255, 255, 255, 255);

        // TC4 changed the lightmap coordinate, not vertex alpha. The previous
        // port used FULL_BRIGHT plus translucent alpha on a cutout layer, which
        // flattened the converter into solid magenta/orange slabs.
        float lockPulse = Mth.sin(animationTime / 3.0F) * 0.1F + 0.9F;
        int lockLight = tc4OverlayLight(offset, lockPulse);
        NodeStabilizerRenderer.renderMesh(poseStack, glow, TC4NodeStabilizerModel.LOCK_TRIANGLES,
                lockLight, rgb[0], rgb[1], rgb[2], 255);

        for (int arm = 0; arm < 4; arm++) {
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(arm * 90.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(45.0F));
            poseStack.translate(0.0D, 0.0D, offset);

            NodeStabilizerRenderer.renderMesh(poseStack, base, TC4NodeStabilizerModel.PISTON_TRIANGLES,
                    packedLight, 255, 255, 255, 255);
            float pulse = Mth.sin((animationTime + arm * 5.0F) / 3.0F) * 0.1F + 0.9F;
            int overlayLight = tc4OverlayLight(offset, pulse);
            NodeStabilizerRenderer.renderMesh(poseStack, glow, TC4NodeStabilizerModel.PISTON_TRIANGLES,
                    overlayLight, rgb[0], rgb[1], rgb[2], 255);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static int tc4OverlayLight(float pistonOffset, float pulse) {
        int coordinate = Mth.clamp(50 + (int)(170.0F * pistonOffset * 2.5F * pulse), 50, 220);
        return LightTexture.pack(Mth.clamp(Math.round(coordinate / 16.0F), 0, 15), 0);
    }

    private static int[] statusColor(int status) {
        return switch (status) {
            case 2 -> new int[] {255, 0, 77};
            case 1 -> new int[] {255, 153, 26};
            default -> new int[] {128, 255, 128};
        };
    }

    @Override
    public boolean shouldRenderOffScreen(NodeTransducerBlockEntity blockEntity) {
        return true;
    }
}
