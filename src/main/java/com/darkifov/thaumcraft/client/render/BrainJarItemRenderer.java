package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.TC4BrainJarParity;
import com.darkifov.thaumcraft.client.render.model.TC4BrainJarBrineModel;
import com.darkifov.thaumcraft.client.render.model.TC4BrainJarModel;
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

/** Exact jar/brain/brine geometry in every modern item display context. */
public final class BrainJarItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation BRAIN_TEXTURE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/brain2.png");
    private static final ResourceLocation BRINE_TEXTURE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/jarbrine.png");
    private static BrainJarItemRenderer instance;
    private TC4BrainJarModel brainModel;
    private TC4BrainJarBrineModel brineModel;

    private BrainJarItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static BrainJarItemRenderer instance() {
        if (instance == null) {
            instance = new BrainJarItemRenderer();
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType,
                             PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
        if (brainModel == null) {
            brainModel = new TC4BrainJarModel(
                    Minecraft.getInstance().getEntityModels().bakeLayer(TC4BrainJarModel.LAYER));
            brineModel = new TC4BrainJarBrineModel(
                    Minecraft.getInstance().getEntityModels().bakeLayer(TC4BrainJarBrineModel.LAYER));
        }

        poseStack.pushPose();
        applyTransform(transformType, poseStack);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                ThaumcraftMod.BRAIN_JAR.get().defaultBlockState(), poseStack, buffer, packedLight, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.01D, 0.5D);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));

        poseStack.pushPose();
        poseStack.translate(0.0D,
                TC4BrainJarParity.BRAIN_Y_OFFSET + TC4BrainJarParity.BRAIN_BOB_BASE, 0.0D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
        poseStack.scale(TC4BrainJarParity.BRAIN_RENDER_SCALE,
                TC4BrainJarParity.BRAIN_RENDER_SCALE,
                TC4BrainJarParity.BRAIN_RENDER_SCALE);
        brainModel.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(BRAIN_TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        brineModel.render(poseStack, buffer.getBuffer(RenderType.entityTranslucent(BRINE_TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void applyTransform(ItemTransforms.TransformType type, PoseStack poseStack) {
        if (type == ItemTransforms.TransformType.GUI) {
            poseStack.translate(0.50D, 0.48D, 0.50D);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(30.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(225.0F));
            poseStack.scale(0.82F, 0.82F, 0.82F);
            poseStack.translate(-0.50D, -0.50D, -0.50D);
        } else if (type == ItemTransforms.TransformType.GROUND) {
            poseStack.translate(0.50D, 0.12D, 0.50D);
            poseStack.scale(0.55F, 0.55F, 0.55F);
            poseStack.translate(-0.50D, 0.0D, -0.50D);
        } else if (type == ItemTransforms.TransformType.FIXED) {
            poseStack.translate(0.50D, 0.50D, 0.50D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            poseStack.scale(0.80F, 0.80F, 0.80F);
            poseStack.translate(-0.50D, -0.50D, -0.50D);
        } else if (type.firstPerson()) {
            poseStack.translate(0.38D, 0.18D, 0.22D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(45.0F));
            poseStack.scale(0.62F, 0.62F, 0.62F);
        } else {
            poseStack.translate(0.38D, 0.16D, 0.22D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(45.0F));
            poseStack.scale(0.58F, 0.58F, 0.58F);
        }
    }
}
