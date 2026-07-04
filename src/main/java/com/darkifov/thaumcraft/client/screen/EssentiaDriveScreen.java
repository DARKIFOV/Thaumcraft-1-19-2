package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.menu.EssentiaDriveMenu;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class EssentiaDriveScreen extends AbstractContainerScreen<EssentiaDriveMenu> {
    public EssentiaDriveScreen(EssentiaDriveMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 200;
        inventoryLabelY = 106;
    }

    @Override
    protected void init() {
        super.init();

        addRenderableWidget(new Button(leftPos + 102, topPos + 48, 62, 18, Component.literal("Скан"), button -> {
            ThaumcraftNetwork.requestEssentiaDriveScan(menu.blockPos());
        }));
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        fill(poseStack, leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xEE100F22);
        fill(poseStack, leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + imageHeight - 4, 0xFF142236);
        fill(poseStack, leftPos + 10, topPos + 16, leftPos + 108, topPos + 62, 0x8820B8FF);
        fill(poseStack, leftPos + 116, topPos + 16, leftPos + 166, topPos + 44, 0x884B2E58);

        font.draw(poseStack, Component.literal("Essentia Drive").withStyle(ChatFormatting.AQUA), leftPos + 10, topPos + 7, 0xBFEFFF);
        font.draw(poseStack, "10 cell slots", leftPos + 16, topPos + 66, 0xE8D4A7);
        font.draw(poseStack, "4 upgrade slots", leftPos + 112, topPos + 66, 0xE8D4A7);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }
}
