package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Lightweight 1.19.2 equivalents of TC4's four warp post-processing shaders.
 * The original GLSL pipeline cannot be copied directly into the modern render
 * graph, but these synced-effect overlays preserve the visible state changes
 * until dedicated post-chain JSONs are fully ported.
 */
@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WarpEffectOverlayEvents {
    private WarpEffectOverlayEvents() {
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();

        if (player.hasEffect(ThaumcraftMod.BLURRED_VISION.get())) {
            GuiComponent.fill(poseStack, 0, 0, width, height, 0x2E7F7F7F);
            int drift = (player.tickCount / 3) % 9;
            GuiComponent.fill(poseStack, drift, 0, width, height, 0x147F6A8F);
        }

        if (player.hasEffect(ThaumcraftMod.UNNATURAL_HUNGER.get())) {
            GuiComponent.fill(poseStack, 0, 0, width, height, 0x183A4D20);
        }

        if (player.hasEffect(ThaumcraftMod.SUN_SCORNED.get())) {
            GuiComponent.fill(poseStack, 0, 0, width, height, 0x26FFE08A);
        }

        if (player.hasEffect(ThaumcraftMod.DEATH_GAZE.get())) {
            int border = Math.max(10, Math.min(width, height) / 11);
            GuiComponent.fill(poseStack, 0, 0, width, border, 0x4A280018);
            GuiComponent.fill(poseStack, 0, height - border, width, height, 0x4A280018);
            GuiComponent.fill(poseStack, 0, border, border, height - border, 0x4A280018);
            GuiComponent.fill(poseStack, width - border, border, width, height - border, 0x4A280018);
        }
    }
}
