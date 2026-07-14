package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.block.TC4BannerBlockItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

/** Inventory, first-person, dropped and frame renderer for NBT-coloured TC4 banners. */
public final class TC4BannerItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static TC4BannerItemRenderer instance;

    private TC4BannerItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static TC4BannerItemRenderer instance() {
        if (instance == null) {
            instance = new TC4BannerItemRenderer();
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType,
                             PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
        poseStack.pushPose();
        applyTransform(transformType, poseStack);
        TC4BannerRenderer.renderStandalone(
                TC4BannerBlockItem.getColor(stack),
                TC4BannerBlockItem.getAspect(stack),
                false, 0, BlockPos.ZERO, 0L, 0.0F,
                poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    private static void applyTransform(ItemTransforms.TransformType type, PoseStack poseStack) {
        if (type == ItemTransforms.TransformType.GUI) {
            poseStack.translate(0.18D, -0.38D, 0.05D);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(12.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(205.0F));
            poseStack.scale(0.46F, 0.46F, 0.46F);
        } else if (type.firstPerson()) {
            poseStack.translate(0.16D, -0.58D, 0.12D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(25.0F));
            poseStack.scale(0.42F, 0.42F, 0.42F);
        } else if (type == ItemTransforms.TransformType.GROUND) {
            poseStack.translate(0.22D, -0.50D, 0.22D);
            poseStack.scale(0.30F, 0.30F, 0.30F);
        } else if (type == ItemTransforms.TransformType.FIXED) {
            poseStack.translate(0.20D, -0.42D, 0.20D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            poseStack.scale(0.38F, 0.38F, 0.38F);
        } else {
            poseStack.translate(0.15D, -0.55D, 0.10D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(25.0F));
            poseStack.scale(0.40F, 0.40F, 0.40F);
        }
    }
}
