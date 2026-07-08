package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Stage211: small client HUD mirror for PacketRunicCharge. */
@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RunicShieldOverlayEvents {
    private RunicShieldOverlayEvents() {
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        int max = RunicShieldClientState.max(minecraft.player.getId());
        if (max <= 0) {
            return;
        }
        int charge = Math.min(max, RunicShieldClientState.charge(minecraft.player.getId()));
        PoseStack poseStack = event.getPoseStack();
        Font font = minecraft.font;
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int x = screenWidth / 2 + 96;
        int y = screenHeight - 49;
        String text = "Runic " + charge + "/" + max;
        font.draw(poseStack, Component.literal(text), x, y, 0xFFE8D4FF);
    }
}
