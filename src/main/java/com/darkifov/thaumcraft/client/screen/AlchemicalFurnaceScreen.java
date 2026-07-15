package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.menu.AlchemicalFurnaceMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AlchemicalFurnaceScreen extends AbstractContainerScreen<AlchemicalFurnaceMenu> {
    private static final ResourceLocation GUI = new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

    public AlchemicalFurnaceScreen(AlchemicalFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int lit = menu.litProgress(13);
        if (lit > 0) {
            blit(poseStack, leftPos + 56, topPos + 36 + 12 - lit, 176, 12 - lit, 14, lit + 1);
        }
        int burn = menu.burnProgress(24);
        if (burn > 0) {
            blit(poseStack, leftPos + 79, topPos + 34, 176, 14, burn + 1, 16);
        }
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        font.draw(poseStack, title, 8, 6, 0x404040);
        font.draw(poseStack, playerInventoryTitle, 8, imageHeight - 96 + 2, 0x404040);
        String essentia = "Essentia: " + menu.storedEssentia() + "/" + menu.capacity();
        font.draw(poseStack, essentia, 79, 58, 0x404040);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }
}
