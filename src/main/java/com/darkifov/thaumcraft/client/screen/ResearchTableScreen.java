package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectColor;
import com.darkifov.thaumcraft.AspectCombinationRegistry;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.ClientAspectData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
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
    private static final int BG_WIDTH = 255;
    private static final int BG_HEIGHT = 255;
    private static final int ASPECTS_PER_PAGE = 25;

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
            int x = leftPos + 10 + (local % 5) * 18;
            int y = topPos + 40 + (local / 5) * 18;
            drawAspectIcon(poseStack, aspect, x, y, ClientAspectData.pool(aspect), aspect == first || aspect == second);
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                renderTooltip(poseStack, Component.literal(aspect.displayName()), mouseX, mouseY);
            }
        }
    }

    private void renderSelectedAspects(PoseStack poseStack, int mouseX, int mouseY) {
        drawSelectionSlot(poseStack, first, leftPos + 13, topPos + 139, mouseX, mouseY);
        drawSelectionSlot(poseStack, second, leftPos + 71, topPos + 139, mouseX, mouseY);

        boolean canCombine = first != null && second != null && previewResult != null;
        int u = canCombine ? 184 : 184;
        int v = canCombine ? 184 : 168;
        OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos + 35, topPos + 139,
                OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL, u, v, 24, 16, 255, 255);

        if (previewResult != null) {
            drawAspectIcon(poseStack, previewResult, leftPos + 45, topPos + 139, 0, false);
        }
        if (mouseX >= leftPos + 35 && mouseX < leftPos + 59 && mouseY >= topPos + 139 && mouseY < topPos + 155) {
            Component tooltip = previewResult == null
                    ? Component.literal("Select two TC4-discovered aspects")
                    : Component.literal(first.displayName() + " + " + second.displayName() + " = " + previewResult.displayName());
            renderTooltip(poseStack, tooltip, mouseX, mouseY);
        }
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
            OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos + 27, topPos + 121,
                    OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL, 184, 200, 16, 10, 255, 255);
        }
        if (hasNext) {
            OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos + 51, topPos + 121,
                    OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL, 200, 200, 16, 10, 255, 255);
        }
        if (hasPrevious && mouseX >= leftPos + 27 && mouseX < leftPos + 43 && mouseY >= topPos + 121 && mouseY < topPos + 131) {
            renderTooltip(poseStack, Component.literal("Previous aspects page"), mouseX, mouseY);
        } else if (hasNext && mouseX >= leftPos + 51 && mouseX < leftPos + 67 && mouseY >= topPos + 121 && mouseY < topPos + 131) {
            renderTooltip(poseStack, Component.literal("Next aspects page"), mouseX, mouseY);
        }
    }

    private void drawAspectIcon(PoseStack poseStack, Aspect aspect, int x, int y, int pool, boolean selected) {
        if (selected) {
            fill(poseStack, x - 2, y - 2, x + 18, y + 18, AspectColor.dim(aspect, 165, 0.42F));
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
        if (mouseX >= leftPos + 27 && mouseX < leftPos + 43 && mouseY >= topPos + 121 && mouseY < topPos + 131 && aspectPage > 0) {
            aspectPage--;
            return true;
        }
        if (mouseX >= leftPos + 51 && mouseX < leftPos + 67 && mouseY >= topPos + 121 && mouseY < topPos + 131
                && (aspectPage + 1) * ASPECTS_PER_PAGE < known.size()) {
            aspectPage++;
            return true;
        }

        if (mouseX >= leftPos + 35 && mouseX < leftPos + 59 && mouseY >= topPos + 139 && mouseY < topPos + 155) {
            if (first != null && second != null && previewResult != null) {
                ThaumcraftNetwork.requestCombineAspectsFromClient(first.id(), second.id());
            }
            return true;
        }

        int start = aspectPage * ASPECTS_PER_PAGE;
        int end = Math.min(start + ASPECTS_PER_PAGE, known.size());
        for (int i = start; i < end; i++) {
            int local = i - start;
            int x = leftPos + 10 + (local % 5) * 18;
            int y = topPos + 40 + (local / 5) * 18;
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
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
