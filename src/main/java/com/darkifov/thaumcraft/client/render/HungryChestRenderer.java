package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.HungryChestBlock;
import com.darkifov.thaumcraft.blockentity.HungryChestBlockEntity;
import com.darkifov.thaumcraft.client.render.model.TC4HungryChestModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/** Exact TC4 Hungry Chest geometry, UV unwrap and lid animation. */
public final class HungryChestRenderer implements BlockEntityRenderer<HungryChestBlockEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/chesthungry.png");

    private final TC4HungryChestModel model;

    public HungryChestRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new TC4HungryChestModel(context.bakeLayer(TC4HungryChestModel.LAYER));
    }

    @Override
    public void render(HungryChestBlockEntity chest, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Direction facing = chest.getBlockState().hasProperty(HungryChestBlock.FACING)
                ? chest.getBlockState().getValue(HungryChestBlock.FACING)
                : Direction.SOUTH;
        renderModel(model, facing, chest.lidAngle(partialTick), poseStack, buffer, packedLight, packedOverlay);
    }

    public static void renderModel(TC4HungryChestModel model, Direction facing, float lidAngle,
                                   PoseStack poseStack, MultiBufferSource buffer,
                                   int packedLight, int packedOverlay) {
        // Identical easing to TileChestHungryRenderer / ModelChest in TC4 4.2.3.5.
        float open = 1.0F - lidAngle;
        open = 1.0F - open * open * open;
        model.setLidRotation(-(open * ((float) Math.PI / 2.0F)));

        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, 1.0D);
        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(rotationFor(facing)));
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.render(poseStack, consumer, packedLight,
                packedOverlay == 0 ? OverlayTexture.NO_OVERLAY : packedOverlay);
        poseStack.popPose();
    }

    private static float rotationFor(Direction facing) {
        return switch (facing) {
            case NORTH -> 180.0F;
            case WEST -> 90.0F;
            case EAST -> -90.0F;
            default -> 0.0F;
        };
    }
}
