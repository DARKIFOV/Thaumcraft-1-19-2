package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.BellowsBlock;
import com.darkifov.thaumcraft.blockentity.BellowsBlockEntity;
import com.darkifov.thaumcraft.client.render.model.TC4BellowsModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/** Exact TC4 TileBellowsRenderer orientation and inflation animation. */
public final class BellowsRenderer implements BlockEntityRenderer<BellowsBlockEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/models/bellows.png");
    private final TC4BellowsModel model;

    public BellowsRenderer(BlockEntityRendererProvider.Context context) {
        model = new TC4BellowsModel(context.bakeLayer(TC4BellowsModel.FRAME_LAYER),
                context.bakeLayer(TC4BellowsModel.BAG_LAYER));
    }

    @Override
    public void render(BellowsBlockEntity bellows, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Direction facing = bellows.getBlockState().hasProperty(BellowsBlock.FACING)
                ? bellows.getBlockState().getValue(BellowsBlock.FACING)
                : Direction.NORTH;

        poseStack.pushPose();
        poseStack.translate(0.5D, -0.5D, 0.5D);
        applyOrientation(poseStack, facing);
        model.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),
                packedLight, packedOverlay == 0 ? OverlayTexture.NO_OVERLAY : packedOverlay,
                bellows.inflation());
        poseStack.popPose();
    }

    private static void applyOrientation(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case DOWN -> {
                poseStack.translate(0.0D, 1.0D, -1.0D);
                poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            }
            case UP -> {
                poseStack.translate(0.0D, 1.0D, 1.0D);
                poseStack.mulPose(Vector3f.XP.rotationDegrees(270.0F));
            }
            case NORTH -> poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            case WEST -> poseStack.mulPose(Vector3f.YP.rotationDegrees(270.0F));
            case EAST -> poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
            case SOUTH -> {
                // Original renderer's neutral orientation.
            }
        }
    }
}
