package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.blockentity.NodeJarBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/** TC4 TileJarRenderer node branch, including the one-second collapse scale. */
public final class NodeJarRenderer implements BlockEntityRenderer<NodeJarBlockEntity> {
    public NodeJarRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(NodeJarBlockEntity jar, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!jar.hasNode()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        float scale = jar.captureScale(partialTick);
        poseStack.scale(scale, scale, scale);
        NodeJarItemRenderer.renderJarShell(poseStack, buffer, packedLight);
        NodeJarItemRenderer.renderContainedNode(jar.nodeTag(), poseStack, buffer);
        poseStack.popPose();
    }
}
