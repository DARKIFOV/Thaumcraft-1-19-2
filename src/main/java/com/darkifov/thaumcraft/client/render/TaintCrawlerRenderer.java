package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.TaintCrawlerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class TaintCrawlerRenderer extends EntityRenderer<TaintCrawlerEntity> {
    public TaintCrawlerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.32F;
    }

    @Override
    public void render(TaintCrawlerEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(-0.22D, 0.0D, -0.22D);
        poseStack.scale(0.44F, 0.42F, 0.44F);

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                ThaumcraftMod.TAINTED_SOIL.get().defaultBlockState(),
                poseStack,
                buffer,
                packedLight,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(TaintCrawlerEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
