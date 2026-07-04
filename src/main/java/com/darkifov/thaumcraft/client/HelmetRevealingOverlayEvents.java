package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

        if (player == null || minecraft.options.hideGui) {
            return;
        }

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);

        if (!helmet.is(ThaumcraftMod.HELMET_OF_REVEALING.get()) && !helmet.is(ThaumcraftMod.GOGGLES_OF_REVEALING.get())) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        int x = 8;
        int y = 8;
        int color = 0x80E8FFFF;

        minecraft.font.draw(poseStack, Component.literal(helmet.is(ThaumcraftMod.GOGGLES_OF_REVEALING.get()) ? "§bGoggles of Revealing" : "§bHelmet of Revealing"), x, y, color);
        minecraft.font.draw(poseStack, Component.literal("§7Research: " + PlayerThaumData.researchCount(player)), x, y + 10, 0x80FFFFFF);
        minecraft.font.draw(poseStack, Component.literal("§5Warp: " + PlayerThaumData.getWarp(player)), x, y + 20, 0x80D090FF);
    }
}
