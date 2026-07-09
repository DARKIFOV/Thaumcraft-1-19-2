package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectCombinationRegistry;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.ClientAspectData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.TC4ResearchTableParity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * TC4 GuiResearchTable aspect-page adapter.
 * This removes rebuild/debug widgets and keeps the original guiresearchtable2 layout:
 * - aspect list page at 10,40, five columns;
 * - selected combination slots at 13,139 and 71,139;
 * - combine icon in the original center area;
 * - page arrows in the original lower-left strip.
 */
public class ResearchTableScreen extends Screen {
    private static final int BG_WIDTH = TC4ResearchTableParity.GUI_WIDTH;
    private static final int BG_HEIGHT = TC4ResearchTableParity.GUI_HEIGHT;
    private static final int ASPECTS_PER_PAGE = TC4ResearchTableParity.ASPECTS_PER_PAGE;

    private int leftPos;
    private int topPos;
    private int aspectPage;
    private Aspect first;
    private Aspect second;
    private Aspect previewResult;

    public ResearchTableScreen() {
        super(Component.translatable("screen.thaumcraft.research_table"));
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - BG_WIDTH) / 2;
        this.topPos = (this.height - BG_HEIGHT) / 2;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        OriginalGuiTextures.blitOriginal(poseStack, leftPos, topPos, OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL, BG_WIDTH, BG_HEIGHT);
        renderAspectPage(poseStack, mouseX, mouseY);
        renderSelectedAspects(poseStack, mouseX, mouseY);
        renderPageArrows(poseStack, mouseX, mouseY);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    private void renderAspectPage(PoseStack poseStack, int mouseX, int mouseY) {
        List<Aspect> known = knownAspects();
        int start = aspectPage * ASPECTS_PER_PAGE;
        int end = Math.min(start + ASPECTS_PER_PAGE, known.size());
        for (int i = start; i < end; i++) {
            Aspect aspect = known.get(i);
            int local = i - start;
            int x = leftPos + TC4ResearchTableParity.ASPECT_GRID_X + (local % TC4ResearchTableParity.ASPECT_GRID_COLUMNS) * TC4ResearchTableParity.ASPECT_GRID_STEP;
            int y = topPos + TC4ResearchTableParity.ASPECT_GRID_Y + (local / TC4ResearchTableParity.ASPECT_GRID_COLUMNS) * TC4ResearchTableParity.ASPECT_GRID_STEP;
            drawAspectIcon(poseStack, aspect, x, y, ClientAspectData.pool(aspect), aspect == first || aspect == second);
            if (TC4ResearchTableParity.isAspectIconHit(mouseX - leftPos, mouseY - topPos, local)) {
                renderTooltip(poseStack, Component.literal(aspect.displayName()), mouseX, mouseY);
            }
        }
    }

    private void renderSelectedAspects(PoseStack poseStack, int mouseX, int mouseY) {
        drawSelectionSlot(poseStack, first, leftPos + TC4ResearchTableParity.COMBINE_LEFT_X, topPos + TC4ResearchTableParity.COMBINE_Y, mouseX, mouseY);
        drawSelectionSlot(poseStack, second, leftPos + TC4ResearchTableParity.COMBINE_RIGHT_X, topPos + TC4ResearchTableParity.COMBINE_Y, mouseX, mouseY);

        boolean canCombine = first != null && second != null && previewResult != null;
        int u = canCombine ? 184 : 184;
        int v = canCombine ? 184 : 168;
        OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos + TC4ResearchTableParity.COMBINE_ARROW_X, topPos + TC4ResearchTableParity.COMBINE_Y,
                OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL, u, v, 24, 16, 255, 255);

        if (previewResult != null) {
            drawAspectIcon(poseStack, previewResult, leftPos + TC4ResearchTableParity.COMBINE_ARROW_X + 10, topPos + TC4ResearchTableParity.COMBINE_Y, 0, false);
        }
        // Original TC4 does not render adapter instruction text over the combine arrow.
    }

    private void drawSelectionSlot(PoseStack poseStack, Aspect aspect, int x, int y, int mouseX, int mouseY) {
        if (aspect != null) {
            drawAspectIcon(poseStack, aspect, x, y, ClientAspectData.pool(aspect), true);
        }
        if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16 && aspect != null) {
            renderTooltip(poseStack, Component.literal(aspect.displayName()), mouseX, mouseY);
        }
    }

    private void renderPageArrows(PoseStack poseStack, int mouseX, int mouseY) {
        boolean hasPrevious = aspectPage > 0;
        boolean hasNext = (aspectPage + 1) * ASPECTS_PER_PAGE < knownAspects().size();
        if (hasPrevious) {
            OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos + TC4ResearchTableParity.PAGE_PREVIOUS_X, topPos + TC4ResearchTableParity.PAGE_ARROW_Y,
                    OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL, 184, 200, 16, 10, 255, 255);
        }
        if (hasNext) {
            OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos + TC4ResearchTableParity.PAGE_NEXT_X, topPos + TC4ResearchTableParity.PAGE_ARROW_Y,
                    OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL, 200, 200, 16, 10, 255, 255);
        }
        // Arrow sprites are the navigation affordance; no modern explanatory tooltip.
    }

    private void drawAspectIcon(PoseStack poseStack, Aspect aspect, int x, int y, int pool, boolean selected) {
        if (selected) {
            fill(poseStack, x - 2, y - 2, x + 18, y - 1, 0xFFC08A32);
            fill(poseStack, x - 2, y + 17, x + 18, y + 18, 0xFFC08A32);
            fill(poseStack, x - 2, y - 2, x - 1, y + 18, 0xFFC08A32);
            fill(poseStack, x + 17, y - 2, x + 18, y + 18, 0xFFC08A32);
        }
        ResourceLocation texture = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + aspect.id() + ".png");
        OriginalGuiTextures.blitOriginal(poseStack, x, y, texture, 16, 16);
        if (pool > 0) {
            drawString(poseStack, font, Component.literal(String.valueOf(Math.min(pool, 99))), x + 9, y + 8, 0xFFFFFF);
        }
    }

    private List<Aspect> knownAspects() {
        List<Aspect> known = new ArrayList<>();
        for (Aspect aspect : Aspect.values()) {
            if (ClientAspectData.knows(aspect)) {
                known.add(aspect);
            }
        }
        return known;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        List<Aspect> known = knownAspects();
        if (TC4ResearchTableParity.isPreviousAspectPageHit(mouseX - leftPos, mouseY - topPos) && aspectPage > 0) {
            aspectPage--;
            return true;
        }
        if (TC4ResearchTableParity.isNextAspectPageHit(mouseX - leftPos, mouseY - topPos)
                && (aspectPage + 1) * ASPECTS_PER_PAGE < known.size()) {
            aspectPage++;
            return true;
        }

        if (TC4ResearchTableParity.isCombineArrowHit(mouseX - leftPos, mouseY - topPos)) {
            if (first != null && second != null && previewResult != null) {
                ThaumcraftNetwork.requestCombineAspectsFromClient(first.id(), second.id());
            }
            return true;
        }

        int start = aspectPage * ASPECTS_PER_PAGE;
        int end = Math.min(start + ASPECTS_PER_PAGE, known.size());
        for (int i = start; i < end; i++) {
            int local = i - start;
            int x = leftPos + TC4ResearchTableParity.ASPECT_GRID_X + (local % TC4ResearchTableParity.ASPECT_GRID_COLUMNS) * TC4ResearchTableParity.ASPECT_GRID_STEP;
            int y = topPos + TC4ResearchTableParity.ASPECT_GRID_Y + (local / TC4ResearchTableParity.ASPECT_GRID_COLUMNS) * TC4ResearchTableParity.ASPECT_GRID_STEP;
            if (TC4ResearchTableParity.isAspectIconHit(mouseX - leftPos, mouseY - topPos, local)) {
                selectAspect(known.get(i));
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void selectAspect(Aspect aspect) {
        if (aspect == null) {
            return;
        }
        if (first == null || (first != null && second != null)) {
            first = aspect;
            second = null;
        } else {
            second = aspect;
        }
        previewResult = first != null && second != null
                ? AspectCombinationRegistry.combine(first, second).orElse(null)
                : null;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
