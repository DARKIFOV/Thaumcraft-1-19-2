package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.blockentity.ThaumatoriumBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

/** TC4 TileThaumatoriumRenderer floating remembered-formula output. */
public final class ThaumatoriumRenderer implements BlockEntityRenderer<ThaumatoriumBlockEntity> {
    private final ItemRenderer itemRenderer;

    public ThaumatoriumRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ThaumatoriumBlockEntity tile, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        long gameTime = tile.getLevel() == null ? 0L : tile.getLevel().getGameTime();
        ItemStack output = tile.displayedFormulaOutput(gameTime);
        if (output.isEmpty()) {
            return;
        }
        Direction facing = tile.facing();
        poseStack.pushPose();
        poseStack.translate(0.5D + facing.getStepX() / 1.99D,
                1.325D + facing.getStepY() / 1.99D,
                0.5D + facing.getStepZ() / 1.99D);
        switch (facing) {
            case EAST -> poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
            case WEST -> poseStack.mulPose(Vector3f.YP.rotationDegrees(270.0F));
            case NORTH -> poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            case UP -> poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            case DOWN -> poseStack.mulPose(Vector3f.XP.rotationDegrees(270.0F));
            default -> {
            }
        }
        poseStack.scale(0.75F, 0.75F, 0.75F);
        itemRenderer.renderStatic(output, ItemTransforms.TransformType.GROUND, packedLight,
                packedOverlay == 0 ? OverlayTexture.NO_OVERLAY : packedOverlay,
                poseStack, buffer, 0);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(ThaumatoriumBlockEntity tile) {
        return true;
    }
}
