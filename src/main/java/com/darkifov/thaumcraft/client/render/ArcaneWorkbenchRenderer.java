package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.arcane.TC4ArcaneWorkbenchParity;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.blockentity.ArcaneWorkbenchBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;

/** Exact installed-wand overlay from TC4 TileArcaneWorkbenchRenderer. */
public final class ArcaneWorkbenchRenderer implements BlockEntityRenderer<ArcaneWorkbenchBlockEntity> {
    private final ItemRenderer itemRenderer;

    public ArcaneWorkbenchRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ArcaneWorkbenchBlockEntity table, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack wand = table.getItem(ArcaneWorkbenchBlockEntity.SLOT_WAND);
        if (!(wand.getItem() instanceof WandItem)) return;

        ItemStack rendered = wand.copy();
        rendered.setCount(1);
        poseStack.pushPose();
        poseStack.translate(TC4ArcaneWorkbenchParity.WAND_RENDER_X,
                TC4ArcaneWorkbenchParity.WAND_RENDER_Y,
                TC4ArcaneWorkbenchParity.WAND_RENDER_Z);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(TC4ArcaneWorkbenchParity.WAND_RENDER_X_ROTATION));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(TC4ArcaneWorkbenchParity.WAND_RENDER_Z_ROTATION));
        itemRenderer.renderStatic(rendered, ItemTransforms.TransformType.GROUND, packedLight,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, 0);
        poseStack.popPose();
    }
}
