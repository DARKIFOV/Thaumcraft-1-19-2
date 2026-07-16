package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.render.model.TC4InfusionMatrixModel;
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

/** Eight-cube TC4 infusion matrix geometry and exact 64x64 ModelCube UV unwrap for item contexts. */
public final class InfusionMatrixItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/models/infuser.png");
    private static InfusionMatrixItemRenderer instance;
    private TC4InfusionMatrixModel model;

    private InfusionMatrixItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static InfusionMatrixItemRenderer instance() {
        if (instance == null) {
            instance = new InfusionMatrixItemRenderer();
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType,
                             PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
        if (model == null) {
            model = new TC4InfusionMatrixModel(
                    Minecraft.getInstance().getEntityModels().bakeLayer(TC4InfusionMatrixModel.LAYER));
        }
        poseStack.pushPose();
        applyTransform(transformType, poseStack);
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(35.0F));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(20.0F));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(25.0F));
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        for (int a = 0; a < 2; a++) {
            for (int b = 0; b < 2; b++) {
                for (int c = 0; c < 2; c++) {
                    renderPiece(poseStack, consumer, a, b, c, packedLight, packedOverlay);
                }
            }
        }
        poseStack.popPose();
    }

    private void renderPiece(PoseStack poseStack, VertexConsumer consumer,
                             int a, int b, int c, int light, int overlay) {
        poseStack.pushPose();
        poseStack.translate((a == 0 ? -1 : 1) * 0.25F,
                (b == 0 ? -1 : 1) * 0.25F,
                (c == 0 ? -1 : 1) * 0.25F);
        if (a > 0) poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
        if (b > 0) poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
        if (c > 0) poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
        poseStack.scale(0.45F, 0.45F, 0.45F);
        model.renderBase(poseStack, consumer, light, overlay);
        poseStack.popPose();
    }

    private static void applyTransform(ItemTransforms.TransformType type, PoseStack poseStack) {
        if (type == ItemTransforms.TransformType.GUI) {
            poseStack.scale(0.78F, 0.78F, 0.78F);
        } else if (type == ItemTransforms.TransformType.GROUND) {
            poseStack.translate(0.0D, -0.20D, 0.0D);
            poseStack.scale(0.52F, 0.52F, 0.52F);
        } else if (type == ItemTransforms.TransformType.FIXED) {
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            poseStack.scale(0.70F, 0.70F, 0.70F);
        } else if (type.firstPerson()) {
            poseStack.translate(0.0D, -0.10D, 0.0D);
            poseStack.scale(0.62F, 0.62F, 0.62F);
        } else {
            poseStack.translate(0.0D, -0.08D, 0.0D);
            poseStack.scale(0.60F, 0.60F, 0.60F);
        }
    }
}
