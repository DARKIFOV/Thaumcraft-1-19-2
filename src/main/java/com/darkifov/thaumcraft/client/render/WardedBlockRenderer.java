package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.blockentity.WardedBlockEntity;
import com.darkifov.thaumcraft.wand.WandFocusRuntime;
import com.darkifov.thaumcraft.wand.WandFocusType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Renders the remembered block and the owner-only ward outline while a Warding
 * focus is held, preserving the important visual rule from TC4 BlockWarded.
 */
public final class WardedBlockRenderer implements BlockEntityRenderer<WardedBlockEntity> {
    public WardedBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(WardedBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (blockEntity.getLevel() == null) return;
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getBlockRenderer().renderSingleBlock(
                blockEntity.rememberedState(), poseStack, buffers, packedLight, OverlayTexture.NO_OVERLAY);

        Player player = minecraft.player;
        if (player == null || !blockEntity.isOwner(player.getUUID()) || !holdsWardingFocus(player)) return;
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(poseStack, lines,
                new AABB(0.002D, 0.002D, 0.002D, 0.998D, 0.998D, 0.998D),
                1.0F, 0.82F, 0.18F, 0.82F);
    }

    private static boolean holdsWardingFocus(Player player) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        return WandFocusRuntime.getFocus(main) == WandFocusType.WARDING
                || WandFocusRuntime.getFocus(off) == WandFocusType.WARDING;
    }

    @Override
    public boolean shouldRenderOffScreen(WardedBlockEntity blockEntity) {
        return true;
    }
}
