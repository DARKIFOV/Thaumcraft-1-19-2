package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.client.ClientResearchData;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.ResearchEntry;
import com.darkifov.thaumcraft.research.ResearchRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ThaumonomiconScreen extends Screen {
    private static final int BG_WIDTH = 512;
    private static final int BG_HEIGHT = 512;

    private int leftPos;
    private int topPos;
    private OriginalResearchCategory category = OriginalResearchCategory.BASICS;
    private ResearchEntry selected;

    public ThaumonomiconScreen() {
        super(Component.translatable("item.thaumcraft.thaumonomicon"));
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - BG_WIDTH) / 2;
        this.topPos = (this.height - BG_HEIGHT) / 2;
        this.clearWidgets();

        int tabX = leftPos + 18;
        int tabY = topPos + 38;

        for (OriginalResearchCategory value : OriginalResearchCategory.values()) {
            int y = tabY + value.ordinal() * 23;
            this.addRenderableWidget(Button.builder(Component.literal(value.title().substring(0, 1)), button -> {
                category = value;
                selected = null;
            }).bounds(tabX, y, 18, 18).build());
        }

        this.addRenderableWidget(Button.builder(Component.literal("Complete Selected"), button ->
                        ThaumcraftNetwork.requestCompleteSelectedResearchFromClient())
                .bounds(leftPos + BG_WIDTH - 154, topPos + BG_HEIGHT - 30, 126, 18)
                .build());

        this.addRenderableWidget(Button.builder(Component.literal("×"), button -> onClose())
                .bounds(leftPos + BG_WIDTH - 26, topPos + 8, 18, 18)
                .build());
    }

    private Set<String> unlockedResearch() {
        Set<String> synced = new HashSet<>(ClientResearchData.research());
        synced.add("FIRST_STEPS");
        return synced;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        OriginalGuiTextures.blitOriginal(poseStack, leftPos, topPos, OriginalGuiTextures.THAUMONOMICON, BG_WIDTH, BG_HEIGHT);

        int ink = 0x2D1B0B;
        drawCenteredString(poseStack, font, Component.translatable("item.thaumcraft.thaumonomicon"), leftPos + BG_WIDTH / 2, topPos + 15, ink);

        renderCategoryTabs(poseStack);
        renderResearchTree(poseStack, mouseX, mouseY);
        renderSelectedPage(poseStack);

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    private void renderCategoryTabs(PoseStack poseStack) {
        int x = leftPos + 40;
        int y = topPos + 41;

        for (OriginalResearchCategory value : OriginalResearchCategory.values()) {
            int rowY = y + value.ordinal() * 23;
            int color = value == category ? 0xFFD7AA46 : 0xFF6B5634;
            fill(poseStack, leftPos + 18, rowY - 3, leftPos + 36, rowY + 15, color);
            drawString(poseStack, font, value.title(), x, rowY, value == category ? 0x2D1B0B : 0x5A3A1A);
        }
    }

    private void renderResearchTree(PoseStack poseStack, int mouseX, int mouseY) {
        Set<String> unlocked = unlockedResearch();
        List<ResearchEntry> entries = OriginalResearchLayout.entriesFor(category);

        int treeLeft = leftPos + 40;
        int treeTop = topPos + 185;

        drawString(poseStack, font, category.title(), treeLeft, treeTop - 16, 0x2D1B0B);

        // Draw dependency lines first.
        for (int i = 0; i < entries.size(); i++) {
            ResearchEntry entry = entries.get(i);
            int x1 = treeLeft + OriginalResearchLayout.xFor(i) + 8;
            int y1 = treeTop + OriginalResearchLayout.yFor(i) + 8;

            for (String requirement : entry.requirements()) {
                for (int j = 0; j < entries.size(); j++) {
                    if (entries.get(j).key().equals(requirement)) {
                        int x2 = treeLeft + OriginalResearchLayout.xFor(j) + 8;
                        int y2 = treeTop + OriginalResearchLayout.yFor(j) + 8;
                        int lineColor = unlocked.contains(requirement) ? 0xAA7A5A28 : 0x66554444;
                        drawLine(poseStack, x1, y1, x2, y2, lineColor);
                    }
                }
            }
        }

        for (int i = 0; i < entries.size(); i++) {
            ResearchEntry entry = entries.get(i);
            int x = treeLeft + OriginalResearchLayout.xFor(i);
            int y = treeTop + OriginalResearchLayout.yFor(i);

            boolean isUnlocked = OriginalResearchLayout.unlocked(unlocked, entry);
            boolean isAvailable = OriginalResearchLayout.available(unlocked, entry);
            boolean isSelected = selected != null && selected.key().equals(entry.key());

            int fillColor = isUnlocked ? 0xFFC9A45A : isAvailable ? 0xFF7D6A42 : 0xFF2B2520;
            int borderColor = isSelected ? 0xFFFFE08A : isAvailable ? 0xFFB08A4D : 0xFF5A4A3A;

            fill(poseStack, x - 2, y - 2, x + 30, y + 30, borderColor);
            fill(poseStack, x, y, x + 28, y + 28, fillColor);
            fill(poseStack, x + 7, y + 7, x + 21, y + 21, isUnlocked ? 0xFF3E2412 : 0xFF15100B);

            drawString(poseStack, font, OriginalResearchLayout.shortTitle(entry.title()), x - 6, y + 32, isAvailable ? 0x2D1B0B : 0x6A5A4A);

            if (mouseX >= x - 2 && mouseX <= x + 30 && mouseY >= y - 2 && mouseY <= y + 30) {
                fill(poseStack, x - 3, y - 3, x + 31, y + 31, 0x33FFFFFF);
            }
        }
    }

    private void renderSelectedPage(PoseStack poseStack) {
        int x = leftPos + 292;
        int y = topPos + 58;
        int ink = 0x2D1B0B;

        ResearchEntry page = selected;
        if (page == null) {
            List<ResearchEntry> entries = OriginalResearchLayout.entriesFor(category);
            if (!entries.isEmpty()) {
                page = entries.get(0);
            }
        }

        if (page == null) {
            return;
        }

        Set<String> unlocked = unlockedResearch();
        boolean isUnlocked = OriginalResearchLayout.unlocked(unlocked, page);
        boolean isAvailable = OriginalResearchLayout.available(unlocked, page);

        drawString(poseStack, font, page.title(), x, y, ink);
        drawString(poseStack, font, isUnlocked ? "Research complete" : isAvailable ? "Research available" : "Research locked", x, y + 14,
                isUnlocked ? 0x336622 : isAvailable ? 0x7A5A22 : 0x7A2222);

        drawString(poseStack, font, OriginalResearchLayout.wrap(page.description(), 34), x, y + 38, ink);

        int reqY = y + 74;
        drawString(poseStack, font, "Parents:", x, reqY, ink);
        if (page.requirements().length == 0) {
            drawString(poseStack, font, "-", x + 48, reqY, ink);
        } else {
            for (int i = 0; i < page.requirements().length && i < 7; i++) {
                String requirement = page.requirements()[i];
                int color = unlocked.contains(requirement) ? 0x336622 : 0x7A2222;
                drawString(poseStack, font, requirement, x + 8, reqY + 14 + i * 12, color);
            }
        }

        drawString(poseStack, font, "Research Point or Research Note", x, topPos + 408, 0x5A3515);
        drawString(poseStack, font, "to unlock selected valid node.", x, topPos + 420, 0x5A3515);
        if (selected != null) {
            drawString(poseStack, font, "Selected for Research Note: " + selected.key(), x, topPos + 436, 0x7A3B9A);
        }
    }

    private void drawLine(PoseStack poseStack, int x1, int y1, int x2, int y2, int color) {
        int midX = x1 + (x2 - x1) / 2;
        fill(poseStack, Math.min(x1, midX), y1, Math.max(x1, midX) + 1, y1 + 1, color);
        fill(poseStack, midX, Math.min(y1, y2), midX + 1, Math.max(y1, y2) + 1, color);
        fill(poseStack, Math.min(midX, x2), y2, Math.max(midX, x2) + 1, y2 + 1, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<ResearchEntry> entries = OriginalResearchLayout.entriesFor(category);
        int treeLeft = leftPos + 40;
        int treeTop = topPos + 185;

        for (int i = 0; i < entries.size(); i++) {
            int x = treeLeft + OriginalResearchLayout.xFor(i);
            int y = treeTop + OriginalResearchLayout.yFor(i);

            if (mouseX >= x - 2 && mouseX <= x + 30 && mouseY >= y - 2 && mouseY <= y + 30) {
                selected = entries.get(i);
                OriginalClientResearchSelection.set(selected.key());
                ThaumcraftNetwork.requestSelectResearchFromClient(selected.key());
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
