package com.darkifov.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

/** Inventory/hand renderer for the original TC4 Hungry Chest model. */
public final class HungryChestItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static HungryChestItemRenderer instance;

    private HungryChestItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static HungryChestItemRenderer instance() {
        if (instance == null) {
            instance = new HungryChestItemRenderer();
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        applyTransform(transformType, poseStack);
        HungryChestRenderer.renderStandalone(Direction.SOUTH, 0.0F, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    private static void applyTransform(ItemTransforms.TransformType type, PoseStack poseStack) {
        if (type == ItemTransforms.TransformType.GUI) {
            poseStack.translate(0.18D, 0.02D, 0.0D);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(25.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(225.0F));
            poseStack.scale(0.78F, 0.78F, 0.78F);
        } else if (type.firstPerson()) {
            poseStack.translate(0.12D, -0.12D, 0.04D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(35.0F));
            poseStack.scale(0.66F, 0.66F, 0.66F);
        } else if (type == ItemTransforms.TransformType.GROUND) {
            poseStack.translate(0.18D, -0.32D, 0.18D);
            poseStack.scale(0.52F, 0.52F, 0.52F);
        } else if (type == ItemTransforms.TransformType.FIXED) {
            poseStack.translate(0.18D, -0.04D, 0.18D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            poseStack.scale(0.66F, 0.66F, 0.66F);
        } else {
            poseStack.translate(0.12D, -0.10D, 0.08D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(35.0F));
            poseStack.scale(0.62F, 0.62F, 0.62F);
        }
    }
}
