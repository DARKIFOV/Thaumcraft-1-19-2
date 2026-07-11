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
        int[] rgb = statusColor(transducer.status());

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.0D, 0.5D);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));

        VertexConsumer base = buffer.getBuffer(RenderType.entityCutoutNoCull(BASE));
        NodeStabilizerRenderer.renderMesh(poseStack, base, TC4NodeStabilizerModel.LOCK_TRIANGLES,
                packedLight, 255, 255, 255, 255);

        VertexConsumer glow = buffer.getBuffer(RenderType.entityTranslucent(OVERLAY));
        int lockAlpha = Mth.clamp(70 + (int)(150.0F * offset * 2.5F), 70, 220);
        NodeStabilizerRenderer.renderMesh(poseStack, glow, TC4NodeStabilizerModel.LOCK_TRIANGLES,
                LightTexture.FULL_BRIGHT, rgb[0], rgb[1], rgb[2], lockAlpha);

        for (int arm = 0; arm < 4; arm++) {
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(arm * 90.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(45.0F));
            poseStack.translate(0.0D, 0.0D, offset);

            NodeStabilizerRenderer.renderMesh(poseStack, base, TC4NodeStabilizerModel.PISTON_TRIANGLES,
                    packedLight, 255, 255, 255, 255);
            float pulse = Mth.sin((time + arm * 5.0F) / 3.0F) * 0.1F + 0.9F;
            int alpha = Mth.clamp(50 + (int)(170.0F * offset * 2.5F * pulse), 50, 220);
            NodeStabilizerRenderer.renderMesh(poseStack, glow, TC4NodeStabilizerModel.PISTON_TRIANGLES,
                    LightTexture.FULL_BRIGHT, rgb[0], rgb[1], rgb[2], alpha);
            poseStack.popPose();
        }
        poseStack.popPose();
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
