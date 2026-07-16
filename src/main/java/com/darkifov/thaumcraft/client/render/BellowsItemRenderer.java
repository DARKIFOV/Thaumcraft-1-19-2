package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.render.model.TC4BellowsModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Full 3D item-context rendering of TC4's five-part bellows model and original UVs. */
public final class BellowsItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/models/bellows.png");
    private static BellowsItemRenderer instance;
    private TC4BellowsModel model;

    private BellowsItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static BellowsItemRenderer instance() {
        if (instance == null) {
            instance = new BellowsItemRenderer();
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType,
                             PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
        if (model == null) {
            model = new TC4BellowsModel(
                    Minecraft.getInstance().getEntityModels().bakeLayer(TC4BellowsModel.FRAME_LAYER),
                    Minecraft.getInstance().getEntityModels().bakeLayer(TC4BellowsModel.BAG_LAYER));
        }
        poseStack.pushPose();
        applyTransform(transformType, poseStack);
        poseStack.translate(0.5D, -0.5D, 0.5D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        poseStack.scale(0.62F, 0.62F, 0.62F);
        model.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY, 0.70F);
        poseStack.popPose();
    }

    private static void applyTransform(ItemTransforms.TransformType type, PoseStack poseStack) {
        if (type == ItemTransforms.TransformType.GUI) {
            poseStack.translate(0.02D, -0.03D, 0.0D);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(24.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(225.0F));
            poseStack.scale(0.72F, 0.72F, 0.72F);
        } else if (type == ItemTransforms.TransformType.GROUND) {
            poseStack.translate(0.10D, -0.28D, 0.10D);
            poseStack.scale(0.48F, 0.48F, 0.48F);
        } else if (type == ItemTransforms.TransformType.FIXED) {
            poseStack.translate(0.0D, -0.08D, 0.0D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            poseStack.scale(0.58F, 0.58F, 0.58F);
        } else if (type.firstPerson()) {
            poseStack.translate(0.0D, -0.15D, 0.0D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(35.0F));
            poseStack.scale(0.50F, 0.50F, 0.50F);
        } else {
            poseStack.translate(0.0D, -0.12D, 0.0D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(35.0F));
            poseStack.scale(0.48F, 0.48F, 0.48F);
        }
    }
}
