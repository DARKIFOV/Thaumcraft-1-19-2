package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.menu.FocusPouchMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Stage186: GuiFocusPouch parity adapter: width=175, height=232, gui_focuspouch.png, blockSlot highlight at y=209. */
public class FocusPouchScreen extends AbstractContainerScreen<FocusPouchMenu> {
    private static final ResourceLocation ORIGINAL_TEXTURE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/gui/gui_focuspouch.png");
    private final int blockSlot;

    public FocusPouchScreen(FocusPouchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.blockSlot = inventory.selected;
        this.imageWidth = 175;
        this.imageHeight = 232;
        this.inventoryLabelY = 139;
        this.titleLabelY = 6;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ORIGINAL_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        // Original GuiFocusPouch overlays selected hotbar slot: drawTexturedModalRect(8 + blockSlot * 18, 209, 240, 0, 16, 16).
        RenderSystem.enableBlend();
        blit(poseStack, leftPos + 8 + blockSlot * 18, topPos + 209, 240, 0, 16, 16);
        RenderSystem.disableBlend();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }
}
