package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Stage283-302 lightweight renderer bridge for TileEldritchCap/Lock/Crystal/Trap.
 * The original TC4 tile renderers are OBJ/GL based; this keeps active blockstate
 * rendering plus bob/rotation/glow hooks so the split block entities no longer
 * behave as purely static block placeholders.
 */
public class TC4EldritchTileRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    @SuppressWarnings("unused")
    private static final ResourceLocation VCRYSTAL = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/vcrystal.png");

    public TC4EldritchTileRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(T be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (be.getLevel() == null) return;
        poseStack.pushPose();
        double bob = Math.sin((be.getLevel().getGameTime() + partialTicks) * 0.08D) * 0.025D;
        poseStack.translate(0.5D, 0.5D + bob, 0.5D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees((be.getLevel().getGameTime() + partialTicks) * 0.8F));
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(be.getBlockState(), poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
