package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.blockentity.NodeTransducerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;

/** Inventory/held renderer for the original TC4 Node Transducer mesh. */
public final class NodeTransducerItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static NodeTransducerItemRenderer instance;

    private NodeTransducerItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static NodeTransducerItemRenderer instance() {
        if (instance == null) {
            instance = new NodeTransducerItemRenderer();
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        applyItemTransform(transformType, poseStack);

        long animationTime = Minecraft.getInstance().level == null
                ? System.nanoTime() / 50_000_000L
                : Minecraft.getInstance().level.getGameTime();
        float fullExtension = NodeTransducerBlockEntity.MAX_EXTENSION_TICKS / 137.0F;
        NodeTransducerRenderer.renderStandalone(0, fullExtension, animationTime,
                poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    private static void applyItemTransform(ItemTransforms.TransformType type, PoseStack poseStack) {
        if (type == ItemTransforms.TransformType.GUI) {
            poseStack.translate(0.0D, 0.08D, 0.0D);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(24.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(225.0F));
            poseStack.scale(0.76F, 0.76F, 0.76F);
        } else if (type.firstPerson()) {
            poseStack.translate(0.0D, -0.05D, 0.0D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(35.0F));
            poseStack.scale(0.62F, 0.62F, 0.62F);
        } else if (type == ItemTransforms.TransformType.GROUND) {
            poseStack.translate(0.0D, -0.18D, 0.0D);
            poseStack.scale(0.48F, 0.48F, 0.48F);
        } else if (type == ItemTransforms.TransformType.FIXED) {
            poseStack.mulPose(Vector3f.XP.rotationDegrees(15.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(225.0F));
            poseStack.scale(0.62F, 0.62F, 0.62F);
        } else {
            poseStack.mulPose(Vector3f.YP.rotationDegrees(35.0F));
            poseStack.scale(0.60F, 0.60F, 0.60F);
        }
    }
}
