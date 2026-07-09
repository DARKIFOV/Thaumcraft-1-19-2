package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.InfusionMatrixBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class InfusionOverlayEvents {
    private InfusionOverlayEvents() {
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || minecraft.player == null || minecraft.hitResult == null) {
            return;
        }

        if (minecraft.hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos pos = ((BlockHitResult) minecraft.hitResult).getBlockPos();
        BlockEntity blockEntity = minecraft.level.getBlockEntity(pos);

        if (!(blockEntity instanceof InfusionMatrixBlockEntity matrix) || !matrix.crafting()) {
            return;
        }

        int duration = Math.max(1, matrix.duration());
        int progress = Math.max(0, matrix.progress());
        int pct = Math.min(100, progress * 100 / duration);
        int pendingEssentia = matrix.pendingEssentiaAmount();
        int pendingComponents = matrix.pendingComponentAmount();

        PoseStack poseStack = event.getPoseStack();
        Font font = minecraft.font;

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int x = screenWidth / 2 - 92;
        int y = 28;
        int w = 184;
        int h = 42;

        fill(poseStack, x, y, x + w, y + h, 0xAA160F20);
        fill(poseStack, x + 6, y + 20, x + w - 6, y + 27, 0xFF3A2A45);
        fill(poseStack, x + 6, y + 20, x + 6 + (w - 12) * pct / 100, y + 27, 0xFFB06CFF);

        String title = "Infusion: " + pct + "%";
        font.draw(poseStack, Component.literal(title), x + 8, y + 7, 0xFFE8D4FF);
        font.draw(poseStack, Component.literal("Essentia " + pendingEssentia + "  Items " + pendingComponents + "  Inst " + matrix.currentInstability()), x + 8, y + 31, 0xFFBFAFEF);

        if (matrix.activeRecipeId() != null) {
            font.draw(poseStack, Component.literal(matrix.activeRecipeId().toString()), x + 8, y + 44, 0xFFBFAFEF);
        }
    }

    private static void fill(PoseStack poseStack, int left, int top, int right, int bottom, int color) {
        net.minecraft.client.gui.GuiComponent.fill(poseStack, left, top, right, bottom, color);
    }
}
