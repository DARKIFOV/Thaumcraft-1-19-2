package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.entity.FallingTaintEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;

/** Renders the carried taint state directly from the block atlas, matching TC4 RenderFallingTaint. */
public final class TC4FallingTaintRenderer extends EntityRenderer<FallingTaintEntity> {
    public TC4FallingTaintRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.5F;
    }

    @Override
    public void render(FallingTaintEntity entity, float yaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        BlockState state = entity.getBlockState();
        if (!state.isAir() && !entity.level.getBlockState(entity.blockPosition()).is(state.getBlock())) {
            poseStack.pushPose();
            poseStack.translate(-0.5D, 0.0D, -0.5D);
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                    state, poseStack, buffers, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
        super.render(entity, yaw, partialTicks, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FallingTaintEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
