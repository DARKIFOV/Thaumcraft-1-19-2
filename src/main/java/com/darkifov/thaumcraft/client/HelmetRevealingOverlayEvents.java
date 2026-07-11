package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class HelmetRevealingOverlayEvents {
    private HelmetRevealingOverlayEvents() {
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        // TC4 keeps Thaumometer scan data on the held scanner glass.  The external
        // node HUD belongs to goggles/helmet revealers only; drawing both created
        // the duplicated ring-and-column interface reported in v11.62.31.
        if (player == null || minecraft.options.hideGui || minecraft.level == null
                || !TC4RevealerHudAdapter.hasIngamePopupRevealer(player)) {
            return;
        }

        AuraNodeBlockEntity node = TC4RevealerHudAdapter.targetedNode(minecraft);
        if (node == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        int x = minecraft.getWindow().getGuiScaledWidth() / 2 + 14;
        int y = minecraft.getWindow().getGuiScaledHeight() / 2 + 12;
        TC4RevealerHudAdapter.renderNodeHud(minecraft, poseStack, node, x, y);
    }
}
