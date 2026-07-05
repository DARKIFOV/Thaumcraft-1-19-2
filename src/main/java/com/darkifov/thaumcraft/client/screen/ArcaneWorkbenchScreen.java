package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ArcaneWorkbenchScreen extends Screen {
    private static final int BG_WIDTH = 256;
    private static final int BG_HEIGHT = 256;
    private static final String[] PRIMAL = {"aer", "terra", "ignis", "aqua", "ordo", "perditio"};

    private int leftPos;
    private int topPos;
    private int vis = 50;

    public ArcaneWorkbenchScreen() {
        super(Component.translatable("screen.thaumcraft.arcane_workbench"));
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - BG_WIDTH) / 2;
        this.topPos = (this.height - BG_HEIGHT) / 2;

        this.addRenderableWidget(new Button(leftPos + BG_WIDTH - 26, topPos + 6, 18, 18,
                Component.literal("×"), button -> onClose()));
        this.addRenderableWidget(new Button(leftPos + 190, topPos + 212, 46, 18,
                Component.literal("+vis"), button -> vis = Math.min(100, vis + 10)));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        OriginalGuiTextures.blitOriginal(poseStack, leftPos, topPos, OriginalGuiTextures.ARCANE_WORKBENCH, BG_WIDTH, BG_HEIGHT);

        int ink = 0x3F2612;
        drawCenteredString(poseStack, font, title, leftPos + BG_WIDTH / 2, topPos + 6, ink);

        renderOriginalLikeCraftingOverlay(poseStack);
        renderVisAndPrimalCosts(poseStack);

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    private void renderOriginalLikeCraftingOverlay(PoseStack poseStack) {
        int gridX = leftPos + 48;
        int gridY = topPos + 54;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int x = gridX + col * 22;
                int y = gridY + row * 22;
                fill(poseStack, x, y, x + 18, y + 18, 0x55331D0E);
            }
        }

        fill(poseStack, leftPos + 136, topPos + 76, leftPos + 154, topPos + 94, 0x55331D0E);
        drawString(poseStack, font, Component.literal("→"), leftPos + 160, topPos + 80, 0x3F2612);
        fill(poseStack, leftPos + 180, topPos + 76, leftPos + 198, topPos + 94, 0x55331D0E);
    }

    private void renderVisAndPrimalCosts(PoseStack poseStack) {
        int barX = leftPos + 36;
        int barY = topPos + 156;

        fill(poseStack, barX, barY, barX + 100, barY + 8, 0x8820100A);
        fill(poseStack, barX, barY, barX + vis, barY + 8, 0xAA6FD6FF);
        drawString(poseStack, font, Component.literal("Vis: " + vis + " / 100"), barX, barY + 12, 0x3F2612);
        drawString(poseStack, font, Component.literal("Bridge-backed costs"), barX, barY + 24, 0x5A3515);

        for (int i = 0; i < PRIMAL.length; i++) {
            int x = leftPos + 38 + i * 30;
            int y = topPos + 188;
            ResourceLocation texture = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/item/aspect_" + PRIMAL[i] + ".png");
            OriginalGuiTextures.blitOriginal(poseStack, x, y, texture, 16, 16);
            drawString(poseStack, font, Component.literal("1"), x + 18, y + 5, 0x3F2612);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
