package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.client.render.model.TC4CentrifugeModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;

/** Full original six-part centrifuge geometry in GUI, hand, ground and frame contexts. */
public final class AlchemicalCentrifugeItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static AlchemicalCentrifugeItemRenderer instance;
    private TC4CentrifugeModel model;

    private AlchemicalCentrifugeItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static AlchemicalCentrifugeItemRenderer instance() {
        if (instance == null) {
            instance = new AlchemicalCentrifugeItemRenderer();
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType,
                             PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
        if (model == null) {
            model = new TC4CentrifugeModel(
                    Minecraft.getInstance().getEntityModels().bakeLayer(TC4CentrifugeModel.LAYER));
        }
        poseStack.pushPose();
        applyTransform(transformType, poseStack);
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale(0.92F, 0.92F, 0.92F);
        float rotation = Minecraft.getInstance().level == null
                ? (System.nanoTime() / 25_000_000L) % 360L
                : (Minecraft.getInstance().level.getGameTime() * 6.0F) % 360.0F;
        AlchemicalCentrifugeRenderer.renderStandalone(model, poseStack, buffer,
                packedLight, packedOverlay, rotation);
        poseStack.popPose();
    }

    private static void applyTransform(ItemTransforms.TransformType type, PoseStack poseStack) {
        if (type == ItemTransforms.TransformType.GUI) {
            poseStack.mulPose(Vector3f.XP.rotationDegrees(25.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(225.0F));
            poseStack.scale(0.86F, 0.86F, 0.86F);
        } else if (type == ItemTransforms.TransformType.GROUND) {
            poseStack.translate(0.0D, -0.18D, 0.0D);
            poseStack.scale(0.58F, 0.58F, 0.58F);
        } else if (type == ItemTransforms.TransformType.FIXED) {
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            poseStack.scale(0.72F, 0.72F, 0.72F);
        } else if (type.firstPerson()) {
            poseStack.translate(0.0D, -0.10D, 0.0D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(35.0F));
            poseStack.scale(0.64F, 0.64F, 0.64F);
        } else {
            poseStack.translate(0.0D, -0.08D, 0.0D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(35.0F));
            poseStack.scale(0.62F, 0.62F, 0.62F);
        }
    }
}
