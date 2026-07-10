package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.client.render.model.TC4ThaumGolemModel;
import com.darkifov.thaumcraft.entity.ThaumGolemEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;

/** Modern equivalent of RenderGolemBase.renderCarriedItems. */
public final class TC4GolemCarriedItemLayer extends RenderLayer<ThaumGolemEntity, TC4ThaumGolemModel> {
    public TC4GolemCarriedItemLayer(RenderLayerParent<ThaumGolemEntity, TC4ThaumGolemModel> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, ThaumGolemEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ItemStack carried = entity.getCarriedForDisplay();
        if (carried.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(-0.5D, 2.5D, -1.25D);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(335.0F));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(50.0F));
        Minecraft.getInstance().getItemRenderer().renderStatic(
                carried, ItemTransforms.TransformType.GROUND, packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, buffer, entity.getId());
        poseStack.popPose();
    }
}
