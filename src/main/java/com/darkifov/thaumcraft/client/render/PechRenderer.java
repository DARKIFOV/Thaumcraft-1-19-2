package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.PechEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class PechRenderer extends EntityRenderer<PechEntity> {
    public PechRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.32F;
    }

    @Override
    public void render(PechEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.translate(-0.24D, 0.0D, -0.24D);
        poseStack.scale(0.48F, 0.72F, 0.48F);

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                ThaumcraftMod.EXTRAS_EARTH_BLOCK.get().defaultBlockState(),
                poseStack,
                buffer,
                packedLight,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.translate(0.0D, 0.8D, 0.0D);
        poseStack.scale(0.78F, 0.42F, 0.78F);

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                ThaumcraftMod.EXTRAS_LIGHT_BLOCK.get().defaultBlockState(),
                poseStack,
                buffer,
                packedLight,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(PechEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
