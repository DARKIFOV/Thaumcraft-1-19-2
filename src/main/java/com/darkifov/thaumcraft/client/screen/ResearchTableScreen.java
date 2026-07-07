package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectColor;
import com.darkifov.thaumcraft.AspectCombinationRegistry;
import com.darkifov.thaumcraft.client.ClientAspectData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ResearchTableScreen extends Screen {
    private static final int BG_WIDTH = 256;
    private static final int BG_HEIGHT = 256;

    private int leftPos;
    private int topPos;
    private Aspect first;
    private Aspect second;
    private Aspect previewResult;
    private String serverStatus = "Select two known aspects.";

    public ResearchTableScreen() {
        super(Component.translatable("screen.thaumcraft.research_table"));
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - BG_WIDTH) / 2;
        this.topPos = (this.height - BG_HEIGHT) / 2;

        this.addRenderableWidget(new Button(leftPos + 8, topPos + BG_HEIGHT - 26, 22, 20,
                Component.literal("←"), button -> onClose()));

        this.addRenderableWidget(new Button(leftPos + 194, topPos + 214, 46, 18,
                Component.literal("Clear"), button -> {
                    first = null;
                    second = null;
                    previewResult = null;
                }));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        OriginalGuiTextures.blitOriginal(poseStack, leftPos, topPos, OriginalGuiTextures.RESEARCH_TABLE, BG_WIDTH, BG_HEIGHT);
        OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos + 100, topPos + 26,
                OriginalGuiTextures.RESEARCH_TABLE_OVERLAY, 0, 0, 96, 96, 256, 256);

        int ink = 0x3F2612;
        drawCenteredString(poseStack, font, Component.literal("Research Table"), leftPos + BG_WIDTH / 2, topPos + 8, ink);
        drawString(poseStack, font, Component.literal("TC4 aspect base: " + Aspect.values().length + " aspects"), leftPos + 18, topPos + 142, ink);
        drawString(poseStack, font, Component.literal("Known: " + ClientAspectData.knownCount() + " / " + Aspect.values().length), leftPos + 18, topPos + 154, ink);
        drawString(poseStack, font, Component.literal("Combinations: " + AspectCombinationRegistry.count()), leftPos + 18, topPos + 166, ink);
        drawString(poseStack, font, Component.literal("Slot 0: Scribing Tools  Slot 1: Research Note"), leftPos + 18, topPos + 232, 0x5A3515);

        renderAspectGrid(poseStack, mouseX, mouseY);
        renderSelected(poseStack);

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    private void renderAspectGrid(PoseStack poseStack, int mouseX, int mouseY) {
        Aspect[] aspects = Aspect.values();

        for (int i = 0; i < aspects.length; i++) {
            Aspect aspect = aspects[i];
            int col = i % 12;
            int row = i / 12;
            int x = leftPos + 20 + col * 18;
            int y = topPos + 34 + row * 18;

            boolean known = ClientAspectData.knows(aspect);
            int pool = ClientAspectData.pool(aspect);
            int color = aspect == first || aspect == second
                    ? AspectColor.argb(aspect, 238)
                    : known ? AspectColor.dim(aspect, aspect.isPrimal() ? 190 : 170, 0.72F) : 0xAA090807;
            fill(poseStack, x - 2, y - 2, x + 18, y + 18, color);
            fill(poseStack, x - 1, y - 1, x + 17, y + 17, known ? 0xAA1D140C : 0xAA050505);

            ResourceLocation texture = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + aspect.id() + ".png");
            OriginalGuiTextures.blitOriginal(poseStack, x, y, texture, 16, 16);

            if (!known) {
                fill(poseStack, x, y, x + 16, y + 16, 0x99000000);
                drawCenteredString(poseStack, font, Component.literal("?"), x + 8, y + 4, 0xFFD6C28A);
            }

            if (pool > 0) {
                drawString(poseStack, font, Component.literal(String.valueOf(Math.min(pool, 99))), x + 9, y + 8, 0xFFFFFF);
            }

            if (mouseX >= x - 1 && mouseX <= x + 17 && mouseY >= y - 1 && mouseY <= y + 17) {
                fill(poseStack, x - 2, y - 2, x + 18, y + 18, 0x33FFFFFF);
                renderTooltip(poseStack, Component.literal((known ? aspect.displayName() : "Unknown") + " [" + aspect.id() + "] pool:" + pool), mouseX, mouseY);
            }
        }
    }

    private void renderSelected(PoseStack poseStack) {
        int x = leftPos + 22;
        int y = topPos + 178;

        drawString(poseStack, font, Component.literal("First: " + label(first)), x, y, first == null ? 0x3F2612 : AspectColor.argb(first));
        drawString(poseStack, font, Component.literal("Second: " + label(second)), x, y + 12, second == null ? 0x3F2612 : AspectColor.argb(second));

        if (previewResult != null) {
            drawString(poseStack, font, Component.literal("Result: " + previewResult.displayName()), x, y + 28, AspectColor.argb(previewResult));
        } else if (first != null && second != null) {
            drawString(poseStack, font, Component.literal("No valid compound"), x, y + 28, 0x7A2222);
        } else {
            drawString(poseStack, font, Component.literal("Use paper + scribing tools on the table."), x, y + 28, 0x5A3515);
        }
        drawString(poseStack, font, Component.literal(serverStatus), x, y + 44, 0x5A3515);
    }

    private String label(Aspect aspect) {
        return aspect == null ? "-" : aspect.displayName();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Aspect[] aspects = Aspect.values();

        for (int i = 0; i < aspects.length; i++) {
            Aspect aspect = aspects[i];
            int col = i % 12;
            int row = i / 12;
            int x = leftPos + 20 + col * 18;
            int y = topPos + 34 + row * 18;

            if (mouseX >= x - 1 && mouseX <= x + 17 && mouseY >= y - 1 && mouseY <= y + 17) {
                if (!ClientAspectData.knows(aspect)) {
                    serverStatus = "That aspect is not discovered yet.";
                    return true;
                }

                if (first == null || (first != null && second != null)) {
                    first = aspect;
                    second = null;
                    serverStatus = "Select the second aspect.";
                } else {
                    second = aspect;
                }

                previewResult = first != null && second != null
                        ? AspectCombinationRegistry.combine(first, second).orElse(null)
                        : null;

                if (first != null && second != null) {
                    if (previewResult != null) {
                        ThaumcraftNetwork.requestCombineAspectsFromClient(first.id(), second.id());
                        serverStatus = "Combination sent to research table.";
                    } else {
                        serverStatus = "No valid compound for those aspects.";
                    }
                }

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
