package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.blockentity.ArcanePedestalBlockEntity;
import com.darkifov.thaumcraft.infusion.TC4InfusionAltarFullClosureParity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

/** Exact TC4 TilePedestalRenderer transform adapted to the 1.19 item renderer. */
public class ArcanePedestalRenderer implements BlockEntityRenderer<ArcanePedestalBlockEntity> {
    private final ItemRenderer itemRenderer;

    public ArcanePedestalRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ArcanePedestalBlockEntity pedestal, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack stack = pedestal.stored().copy();
        if (stack.isEmpty()) {
            return;
        }
        stack.setCount(1);

        float ticks = (pedestal.getLevel() == null ? 0.0F : pedestal.getLevel().getGameTime()) + partialTick;
        float bob = Mth.sin((ticks % 32767.0F) / TC4InfusionAltarFullClosureParity.PEDESTAL_BOB_DIVISOR)
                * TC4InfusionAltarFullClosureParity.PEDESTAL_BOB_AMPLITUDE;
        float scale = stack.getItem() instanceof BlockItem
                ? TC4InfusionAltarFullClosureParity.PEDESTAL_BLOCK_SCALE
                : TC4InfusionAltarFullClosureParity.PEDESTAL_ITEM_SCALE;

        poseStack.pushPose();
        poseStack.translate(0.5D, TC4InfusionAltarFullClosureParity.PEDESTAL_ITEM_Y + bob, 0.5D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(
                (ticks * TC4InfusionAltarFullClosureParity.PEDESTAL_ROTATION_DEGREES_PER_TICK) % 360.0F));
        poseStack.scale(scale, scale, scale);
        renderOne(stack, poseStack, buffer, packedLight, packedOverlay);

        // The original renderer draws a second, 180-degree copy when fancy
        // graphics are disabled so flat item sprites remain visible from both sides.
        if (!Minecraft.useFancyGraphics()) {
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            renderOne(stack, poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    private void renderOne(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer,
                           int packedLight, int packedOverlay) {
        itemRenderer.renderStatic(
                stack,
                ItemTransforms.TransformType.GROUND,
                packedLight,
                packedOverlay == 0 ? OverlayTexture.NO_OVERLAY : packedOverlay,
                poseStack,
                buffer,
                0
        );
    }
}
