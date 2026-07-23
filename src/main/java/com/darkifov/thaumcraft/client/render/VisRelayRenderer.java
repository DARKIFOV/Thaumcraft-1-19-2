package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.blockentity.VisRelayBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/** Original vis_relay OBJ plus the TC4 beam1.png parent link and aspect pulse. */
public final class VisRelayRenderer implements BlockEntityRenderer<VisRelayBlockEntity> {
    public VisRelayRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(VisRelayBlockEntity relay, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        VisChargeRelayRenderer.renderStandalone(relay.pulseAspect(), relay.pulseStrength(partialTick),
                poseStack, buffer, packedLight);
        TC4VisRelayBeamRenderer.render(relay, partialTick, poseStack, buffer);
    }

    @Override public int getViewDistance() { return 64; }
}
