package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.client.render.model.TC4HungryChestModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

/** Exact modern BEWLR equivalent of TC4's BlockChestHungryRenderer inventory path. */
public final class HungryChestItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static HungryChestItemRenderer instance;
    private TC4HungryChestModel model;

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
        if (model == null) {
            model = new TC4HungryChestModel(
                    Minecraft.getInstance().getEntityModels().bakeLayer(TC4HungryChestModel.LAYER));
        }
        poseStack.pushPose();
        // BlockChestHungryRenderer: rotate Y 90 degrees, then translate all axes by -0.5.
        poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        HungryChestRenderer.renderModel(model, Direction.SOUTH, 0.0F,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }
}
