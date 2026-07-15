package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.render.model.TC4TravelingTrunkModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Forge BEWLR counterpart of TC4 ItemTrunkSpawnerRenderer. */
public final class TravelingTrunkItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/models/trunk.png");
    private static TravelingTrunkItemRenderer INSTANCE;
    private TC4TravelingTrunkModel model;

    private TravelingTrunkItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static TravelingTrunkItemRenderer instance() {
        if (INSTANCE == null) INSTANCE = new TravelingTrunkItemRenderer();
        return INSTANCE;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType type, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (model == null) model = new TC4TravelingTrunkModel(Minecraft.getInstance().getEntityModels().bakeLayer(TC4TravelingTrunkModel.LAYER));
        model.setupItem();
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        if (type == ItemTransforms.TransformType.GUI) {
            poseStack.translate(0.0D, 0.12D, 0.0D);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(25.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(225.0F));
            poseStack.scale(0.72F, -0.72F, -0.72F);
        } else if (type.firstPerson()) {
            poseStack.translate(-0.25D, -0.50D, -0.25D);
            poseStack.scale(0.70F, -0.70F, -0.70F);
        } else if (type == ItemTransforms.TransformType.GROUND) {
            poseStack.translate(0.0D, -0.18D, 0.0D);
            poseStack.scale(0.48F, -0.48F, -0.48F);
        } else {
            poseStack.translate(0.15D, -0.30D, 0.0D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            poseStack.scale(0.60F, -0.60F, -0.60F);
        }
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        poseStack.popPose();
    }
}
