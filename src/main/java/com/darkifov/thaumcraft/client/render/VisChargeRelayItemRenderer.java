package com.darkifov.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;

/** Inventory, hand, entity and frame renderer for the original relay OBJ. */
public final class VisChargeRelayItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static VisChargeRelayItemRenderer instance;

    private VisChargeRelayItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static VisChargeRelayItemRenderer instance() {
        if (instance == null) {
            instance = new VisChargeRelayItemRenderer();
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType,
                             PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
        poseStack.pushPose();
        applyTransform(transformType, poseStack);
        VisChargeRelayRenderer.renderStandalone(null, 0.0F, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    private static void applyTransform(ItemTransforms.TransformType type, PoseStack poseStack) {
        if (type == ItemTransforms.TransformType.GUI) {
            poseStack.translate(0.18D, 0.00D, 0.12D);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(24.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(215.0F));
            poseStack.scale(0.92F, 0.92F, 0.92F);
        } else if (type.firstPerson()) {
            poseStack.translate(0.08D, -0.08D, 0.04D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(30.0F));
            poseStack.scale(0.78F, 0.78F, 0.78F);
        } else if (type == ItemTransforms.TransformType.GROUND) {
            poseStack.translate(0.18D, -0.22D, 0.18D);
            poseStack.scale(0.60F, 0.60F, 0.60F);
        } else if (type == ItemTransforms.TransformType.FIXED) {
            poseStack.translate(0.10D, -0.02D, 0.10D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            poseStack.scale(0.76F, 0.76F, 0.76F);
        } else {
            poseStack.translate(0.08D, -0.08D, 0.04D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(30.0F));
            poseStack.scale(0.74F, 0.74F, 0.74F);
        }
    }
}
