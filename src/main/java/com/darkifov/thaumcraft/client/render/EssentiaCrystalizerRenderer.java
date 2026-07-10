package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.EssentiaCrystalItem;
import com.darkifov.thaumcraft.blockentity.EssentiaCrystalizerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;

/** Modern renderer preserving the original visible spinning crystal core. */
public class EssentiaCrystalizerRenderer implements BlockEntityRenderer<EssentiaCrystalizerBlockEntity> {
    public EssentiaCrystalizerRenderer(BlockEntityRendererProvider.Context context) { }

    @Override
    public void render(EssentiaCrystalizerBlockEntity tile, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (tile.heldAspect() == null) return;
        ItemStack crystal = EssentiaCrystalItem.create(ThaumcraftMod.ESSENTIA_CRYSTAL.get(), tile.heldAspect());
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.55D, 0.5D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(tile.spin(partialTick)));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(35.0F));
        poseStack.scale(0.65F, 0.65F, 0.65F);
        Minecraft.getInstance().getItemRenderer().renderStatic(crystal, ItemTransforms.TransformType.FIXED,
                0x00F000F0, OverlayTexture.NO_OVERLAY, poseStack, buffers, 0);
        poseStack.popPose();
    }
}
