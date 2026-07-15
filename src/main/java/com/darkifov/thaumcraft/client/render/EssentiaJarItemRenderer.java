package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.block.EssentiaJarBlockItem;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Forge 1.19.2 BEWLR equivalent of TC4 ItemJarFilledRenderer. */
public final class EssentiaJarItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static EssentiaJarItemRenderer instance;
    private final EssentiaJarRenderer contentsRenderer = new EssentiaJarRenderer(null);

    private EssentiaJarItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static EssentiaJarItemRenderer instance() {
        if (instance == null) {
            instance = new EssentiaJarItemRenderer();
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }
        BlockState state = blockItem.getBlock().defaultBlockState();
        EssentiaJarBlockEntity jar = new EssentiaJarBlockEntity(BlockPos.ZERO, state);
        jar.load(EssentiaJarBlockItem.readJarData(stack));

        poseStack.pushPose();
        applyOriginalItemTransform(transformType, poseStack);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                state, poseStack, buffer, packedLight, packedOverlay);
        contentsRenderer.renderItemContents(jar, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void applyOriginalItemTransform(ItemTransforms.TransformType type, PoseStack poseStack) {
        // TC4 rendered the real jar block plus TileJarFillable. These transforms
        // retain the original centered GUI/ground/fixed presentation instead of
        // letting a flat/generated model decide the view.
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
            poseStack.translate(-0.50D, -0.50D, -0.50D);
        } else {
            poseStack.translate(0.44D, 0.30D, 0.34D);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(75.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(45.0F));
            poseStack.scale(0.58F, 0.58F, 0.58F);
            poseStack.translate(-0.50D, -0.50D, -0.50D);
        }
    }
}
