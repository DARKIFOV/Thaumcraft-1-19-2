package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ResearchTableScreen extends Screen {
    private static final int BG_WIDTH = 256;
    private static final int BG_HEIGHT = 256;
    private static final String[] ASPECTS = {"aer", "terra", "ignis", "aqua", "ordo", "perditio"};

    private int leftPos;
    private int topPos;
    private int selectedAspect = -1;
    private int theoryProgress = 0;

    public ResearchTableScreen() {
        super(Component.translatable("screen.thaumcraft.research_table"));
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - BG_WIDTH) / 2;
        this.topPos = (this.height - BG_HEIGHT) / 2;

        this.addRenderableWidget(Button.builder(Component.literal("←"), button -> onClose())
                .bounds(leftPos + 8, topPos + BG_HEIGHT - 26, 22, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("Ink"), button -> theoryProgress = Math.min(6, theoryProgress + 1))
                .bounds(leftPos + 198, topPos + 214, 42, 18)
                .build());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        OriginalGuiTextures.blitOriginal(poseStack, leftPos, topPos, OriginalGuiTextures.RESEARCH_TABLE, BG_WIDTH, BG_HEIGHT);
        OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos + 100, topPos + 30,
                OriginalGuiTextures.RESEARCH_TABLE_OVERLAY, 0, 0, 96, 96, 256, 256);

        int ink = 0x3F2612;
        drawCenteredString(poseStack, font, Component.literal("Research Notes"), leftPos + BG_WIDTH / 2, topPos + 8, ink);
        drawString(poseStack, font, Component.literal("Choose aspects to form a theory."), leftPos + 22, topPos + 142, ink);
        drawString(poseStack, font, Component.literal("Progress: " + theoryProgress + " / 6"), leftPos + 22, topPos + 156, ink);

        renderAspectWheel(poseStack, mouseX, mouseY);

        if (selectedAspect >= 0) {
            drawString(poseStack, font, Component.literal("Selected: " + ASPECTS[selectedAspect].toUpperCase()), leftPos + 22, topPos + 176, 0x5A3515);
        }

        if (theoryProgress >= 6) {
            drawString(poseStack, font, Component.literal("Theory complete - use the note."), leftPos + 22, topPos + 194, 0x336622);
        } else {
            drawString(poseStack, font, Component.literal("Needs Scribing Tools + Paper."), leftPos + 22, topPos + 194, 0x5A3515);\n            drawString(poseStack, font, Component.literal("Use Research Note to complete available node."), leftPos + 22, topPos + 208, 0x5A3515);
        }

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    private void renderAspectWheel(PoseStack poseStack, int mouseX, int mouseY) {
        int centerX = leftPos + 128;
        int centerY = topPos + 88;

        for (int i = 0; i < ASPECTS.length; i++) {
            double angle = Math.toRadians(-90 + i * 60);
            int x = centerX + (int) Math.round(Math.cos(angle) * 54) - 8;
            int y = centerY + (int) Math.round(Math.sin(angle) * 42) - 8;

            int color = selectedAspect == i ? 0xFFFFD76A : 0xAA3A2716;
            fill(poseStack, x - 2, y - 2, x + 18, y + 18, color);
            ResourceLocation texture = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/item/aspect_" + ASPECTS[i] + ".png");
            OriginalGuiTextures.blitOriginal(poseStack, x, y, texture, 16, 16);

            if (mouseX >= x - 2 && mouseX <= x + 18 && mouseY >= y - 2 && mouseY <= y + 18) {
                fill(poseStack, x - 3, y - 3, x + 19, y + 19, 0x33FFFFFF);
            }
        }

        // Original-like connection lines on parchment.
        int lineColor = theoryProgress >= 3 ? 0xAA7A5A28 : 0x66554444;
        fill(poseStack, centerX - 36, centerY - 1, centerX + 36, centerY, lineColor);
        fill(poseStack, centerX - 1, centerY - 28, centerX, centerY + 28, lineColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int centerX = leftPos + 128;
        int centerY = topPos + 88;

        for (int i = 0; i < ASPECTS.length; i++) {
            double angle = Math.toRadians(-90 + i * 60);
            int x = centerX + (int) Math.round(Math.cos(angle) * 54) - 8;
            int y = centerY + (int) Math.round(Math.sin(angle) * 42) - 8;

            if (mouseX >= x - 2 && mouseX <= x + 18 && mouseY >= y - 2 && mouseY <= y + 18) {
                selectedAspect = i;
                theoryProgress = Math.min(6, theoryProgress + 1);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
