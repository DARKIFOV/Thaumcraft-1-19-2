package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.menu.BottomlessPouchMenu;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BottomlessPouchScreen extends AbstractContainerScreen<BottomlessPouchMenu> {
    public BottomlessPouchScreen(BottomlessPouchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 168;
        inventoryLabelY = 76;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        fill(poseStack, leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xEE100F22);
        fill(poseStack, leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + imageHeight - 4, 0xFF2C1B16);
        fill(poseStack, leftPos + 6, topPos + 14, leftPos + imageWidth - 6, topPos + 74, 0x886B4524);
        font.draw(poseStack, Component.literal("Bottomless Pouch").withStyle(ChatFormatting.GOLD), leftPos + 8, topPos + 6, 0xF2DFB2);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }
}
