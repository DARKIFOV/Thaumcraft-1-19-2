package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.blockentity.ArcanePedestalBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;

public class ArcanePedestalRenderer implements BlockEntityRenderer<ArcanePedestalBlockEntity> {
    private final ItemRenderer itemRenderer;

    public ArcanePedestalRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ArcanePedestalBlockEntity pedestal, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack stack = pedestal.stored();

        if (stack.isEmpty()) {
            return;
        }

        long time = pedestal.getLevel() == null ? 0L : pedestal.getLevel().getGameTime();

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.12D, 0.5D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees((time + partialTick) * 4.0F));
        poseStack.scale(0.55F, 0.55F, 0.55F);

        itemRenderer.renderStatic(
                stack,
                ItemTransforms.TransformType.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                0
        );

        poseStack.popPose();
    }
}
