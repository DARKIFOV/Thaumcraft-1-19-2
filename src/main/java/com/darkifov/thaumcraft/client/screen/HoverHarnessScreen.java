package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.menu.HoverHarnessMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Original TC4 GuiHoverHarness texture with its single essentia-jar slot. */
public final class HoverHarnessScreen extends AbstractContainerScreen<HoverHarnessMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/gui/guihoverharness.png");
    private final int selectedHotbarSlot;

    public HoverHarnessScreen(HoverHarnessMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        selectedHotbarSlot = inventory.selected;
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 72;
        titleLabelY = 6;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        RenderSystem.enableBlend();
        blit(poseStack, leftPos + 8 + selectedHotbarSlot * 18, topPos + 142, 240, 0, 16, 16);
        RenderSystem.disableBlend();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }
}
