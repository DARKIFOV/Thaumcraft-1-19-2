package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.blockentity.VisRelayBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/** Static original OBJ renderer for the energized-node relay. */
public final class VisRelayRenderer implements BlockEntityRenderer<VisRelayBlockEntity> {
    public VisRelayRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(VisRelayBlockEntity relay, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        VisChargeRelayRenderer.renderStandalone(null, 0.0F, poseStack, buffer, packedLight);
    }
}
