package com.darkifov.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;

/** Exact modern BEWLR equivalent of TC4's candle inventory render path. */
public final class TallowCandleItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static TallowCandleItemRenderer instance;

    private TallowCandleItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    public static TallowCandleItemRenderer instance() {
        if (instance == null) {
            instance = new TallowCandleItemRenderer();
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType,
                             PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
        poseStack.pushPose();
        // BlockRenderer.drawFaces translates the original inventory cuboids by
        // (-0.5, -0.5, -0.5). Context-specific approximations are intentionally
        // avoided; Minecraft's item context matrix remains responsible for GUI/hand placement.
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        TallowCandleRenderer.renderItem(stack, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }
}
