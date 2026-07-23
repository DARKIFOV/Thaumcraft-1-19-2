package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.menu.ArcaneBoreMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Exact TC4 GuiArcaneBore dimensions, overlay and half-scale property labels. */
public final class ArcaneBoreScreen extends AbstractContainerScreen<ArcaneBoreMenu> {
    private static final ResourceLocation GUI = new ResourceLocation("thaumcraft", "textures/gui/gui_arcanebore.png");

    public ArcaneBoreScreen(ArcaneBoreMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 141;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, GUI);
        blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.pickaxeNearBroken()) blit(poseStack, leftPos + 74, topPos + 18, 184, 0, 16, 16);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        poseStack.pushPose();
        poseStack.translate(112.0D, 8.0D, 505.0D);
        poseStack.scale(0.5F, 0.5F, 1.0F);
        font.draw(poseStack, "Width: " + menu.width(), 0, 0, 0xFFFFFF);
        font.draw(poseStack, "Speed: +" + menu.speed(), 0, 10, 0xFFFFFF);
        font.draw(poseStack, "Other properties:", 0, 24, 0xFFFFFF);
        int offset = 0;
        if (menu.nativeClusters()) {
            font.draw(poseStack, "Native Clusters", 4, 34 + offset, 0xC0C0C0);
            offset += 9;
        }
        if (menu.fortune() > 0) {
            font.draw(poseStack, "Fortune " + menu.fortune(), 4, 34 + offset, 0xEEC64A);
            offset += 9;
        }
        if (menu.silkTouch()) font.draw(poseStack, "Silk Touch", 4, 34 + offset, 0x8080FF);
        poseStack.popPose();
    }

    @Override public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }
}
