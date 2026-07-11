package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.block.NodeStabilizerItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;

/** Renders both node stabilizer items with the original TC4 OBJ mesh. */
public final class NodeStabilizerItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static NodeStabilizerItemRenderer instance;

    private NodeStabilizerItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static NodeStabilizerItemRenderer instance() {
        if (instance == null) {
            instance = new NodeStabilizerItemRenderer();
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean advanced = stack.getItem() instanceof NodeStabilizerItem stabilizer && stabilizer.isAdvanced();

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        applyItemTransform(transformType, poseStack);
        poseStack.translate(0.0D, -0.36D, 0.0D);

        // Keep the arms fully visible in inventory/hand, as TC4's block-item
        // preview displayed the complete machine instead of a closed black cube.
        long animationTime = Minecraft.getInstance().level == null
                ? System.nanoTime() / 50_000_000L
                : Minecraft.getInstance().level.getGameTime();
        NodeStabilizerRenderer.renderStandalone(advanced, 37.0F, animationTime,
                poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    private static void applyItemTransform(ItemTransforms.TransformType type, PoseStack poseStack) {
        if (type == ItemTransforms.TransformType.GUI) {
            poseStack.translate(0.0D, -0.05D, 0.0D);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(24.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(225.0F));
            poseStack.scale(0.72F, 0.72F, 0.72F);
        } else if (type.firstPerson()) {
            poseStack.translate(0.0D, -0.10D, 0.0D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(35.0F));
            poseStack.scale(0.60F, 0.60F, 0.60F);
        } else if (type == ItemTransforms.TransformType.GROUND) {
            poseStack.translate(0.0D, -0.24D, 0.0D);
            poseStack.scale(0.48F, 0.48F, 0.48F);
        } else if (type == ItemTransforms.TransformType.FIXED) {
            poseStack.mulPose(Vector3f.XP.rotationDegrees(15.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(225.0F));
            poseStack.scale(0.60F, 0.60F, 0.60F);
        } else {
            poseStack.mulPose(Vector3f.YP.rotationDegrees(35.0F));
            poseStack.scale(0.58F, 0.58F, 0.58F);
        }
    }
}
