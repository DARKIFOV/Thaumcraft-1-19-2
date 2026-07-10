package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.blockentity.FocalManipulatorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/** Original TileFocalManipulatorRenderer floating and rotating focus. */
public class FocalManipulatorRenderer implements BlockEntityRenderer<FocalManipulatorBlockEntity> {
    private final ItemRenderer itemRenderer;

    public FocalManipulatorRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(FocalManipulatorBlockEntity tile, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        ItemStack focus = tile.getItem(FocalManipulatorBlockEntity.SLOT_FOCUS);
        if (focus.isEmpty()) return;
        float ticks = (tile.getLevel() == null ? 0.0F : tile.getLevel().getGameTime()) + partialTick;
        pose.pushPose();
        pose.translate(0.5D, 1.0D, 0.5D);
        pose.mulPose(Vector3f.YP.rotationDegrees(ticks % 360.0F));
        pose.translate(0.0D, Mth.sin(ticks / 14.0F) * 0.2F + 0.2F, 0.0D);
        pose.scale(0.65F, 0.65F, 0.65F);
        itemRenderer.renderStatic(focus, ItemTransforms.TransformType.GROUND, packedLight,
                OverlayTexture.NO_OVERLAY, pose, buffers, 0);
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(FocalManipulatorBlockEntity tile) {
        return true;
    }
}
