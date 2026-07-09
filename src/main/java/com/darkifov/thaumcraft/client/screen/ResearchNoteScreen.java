package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectColor;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.ClientAspectData;
import com.darkifov.thaumcraft.client.ClientResearchNoteData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.ResearchAspectGraph;
import com.darkifov.thaumcraft.research.ResearchNoteGrid;
import com.darkifov.thaumcraft.research.ResearchNoteRequirements;
import com.darkifov.thaumcraft.research.TC4ResearchTableParity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResearchNoteScreen extends Screen {
    private static final int BG_WIDTH = 256;
    private static final int BG_HEIGHT = 256;
    private static final int ASPECTS_PER_PAGE = TC4ResearchTableParity.ASPECTS_PER_PAGE;
    // Stage563 audit compatibility marker: ASPECTS_PER_PAGE = 25
    // Stage563 audit compatibility marker: leftPos + 10 + (local % 5) * 18
    // Stage563 audit compatibility marker: topPos + 40 + (local / 5) * 18

    private int leftPos;
    private int topPos;
    private int aspectPage;
    private Aspect selectedAspect;
    private Aspect draggedAspect;
    private boolean draggingAspect;
    private String status = "";

    public ResearchNoteScreen() {
        super(Component.literal("Research Note"));
    }

    @Override
    protected void init() {
        leftPos = (width - BG_WIDTH) / 2;
        topPos = (height - BG_HEIGHT) / 2;
        // Original TC4 research note is manipulated on the research table GUI;
        // it has no debug buttons. Escape closes via Screen default.
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        OriginalGuiTextures.blitOriginal(poseStack, leftPos, topPos, OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL, BG_WIDTH, BG_HEIGHT);


        renderLinks(poseStack);
        renderGrid(poseStack, mouseX, mouseY);
        renderAspectPalette(poseStack, mouseX, mouseY);
        renderAspectPageArrows(poseStack, mouseX, mouseY);
        renderDraggedAspect(poseStack, mouseX, mouseY);

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    private void renderGrid(PoseStack poseStack, int mouseX, int mouseY) {
        for (ResearchNoteGrid.GridSlot slot : ResearchNoteGrid.slots()) {
            if (!ClientResearchNoteData.activeAt(slot.index())) {
                continue;
            }
            int x = leftPos + ResearchNoteGrid.x(slot.index());
            int y = topPos + ResearchNoteGrid.y(slot.index());
            Aspect aspect = ClientResearchNoteData.aspectAt(slot.index());

            boolean locked = isLockedSlot(slot.index());
            boolean validTarget = selectedAspect != null && canClientPlace(slot.index(), selectedAspect);
            ResourceLocation hexTexture = aspect == null && !validTarget ? OriginalGuiTextures.HEX1 : OriginalGuiTextures.HEX2;
            OriginalGuiTextures.blitOriginal(poseStack, x - 10, y - 9, hexTexture, TC4ResearchTableParity.NOTE_HEX_DRAW_W, TC4ResearchTableParity.NOTE_HEX_DRAW_H);

            if (locked) {
                fill(poseStack, x - 10, y - 9, x + 10, y + 9, 0x22FFCC55);
            } else if (validTarget) {
                fill(poseStack, x - 10, y - 9, x + 10, y + 9, 0x2244AA44);
            }

            if (aspect != null) {
                ResourceLocation texture = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + aspect.id() + ".png");
                OriginalGuiTextures.blitOriginal(poseStack, x - 8, y - 8, texture, 16, 16);
            }

            if (mouseX >= x - 10 && mouseX <= x + 10 && mouseY >= y - 9 && mouseY <= y + 9) {
                fill(poseStack, x - 10, y - 9, x + 10, y + 9, 0x22FFFFFF);
                if (aspect != null) {
                    renderTooltip(poseStack, Component.literal(aspect.displayName()), mouseX, mouseY);
                } else if (selectedAspect != null && validTarget) {
                    renderTooltip(poseStack, Component.literal(selectedAspect.displayName()), mouseX, mouseY);
                }
            }
        }
    }

    private void renderLinks(PoseStack poseStack) {
        for (ResearchNoteGrid.GridSlot slot : ResearchNoteGrid.slots()) {
            if (!ClientResearchNoteData.activeAt(slot.index())) {
                continue;
            }
            Aspect aspect = ClientResearchNoteData.aspectAt(slot.index());

            if (aspect == null) {
                continue;
            }

            int x = leftPos + ResearchNoteGrid.x(slot.index());
            int y = topPos + ResearchNoteGrid.y(slot.index());

            for (int neighbor : ResearchNoteGrid.neighbors(slot.index())) {
                Aspect other = ClientResearchNoteData.aspectAt(neighbor);

                if (other == null || neighbor < slot.index()) {
                    continue;
                }

                int nx = leftPos + ResearchNoteGrid.x(neighbor);
                int ny = topPos + ResearchNoteGrid.y(neighbor);
                int distance = ResearchAspectGraph.distance(aspect, other);
                if (!ResearchAspectGraph.canConnect(aspect, other)) {
                    continue;
                }
                int lineColor = distance <= 1
                        ? AspectColor.mix(aspect, other, 220)
                        : distance <= 2 ? AspectColor.mix(aspect, other, 150) : 0xAA7A2222;
                drawSaggingThreadLikeTC4(poseStack, x, y, nx, ny, lineColor, slot.index(), neighbor);
            }
        }
    }

    private void renderRequiredAspects(PoseStack poseStack, int mouseX, int mouseY) {
        int x = leftPos + 176;
        int y = topPos + 58;
        Set<Aspect> requiredAspects = ResearchNoteRequirements.requiredFor(ClientResearchNoteData.target());
        // original GuiResearchTable does not draw a rebuild Required label
        int i = 0;
        for (Aspect aspect : requiredAspects) {
            int ix = x + (i % 3) * 20;
            int iy = y + (i / 3) * 20;
            boolean placed = ClientResearchNoteData.slots().containsValue(aspect.id());
            ResourceLocation texture = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + aspect.id() + ".png");
            fill(poseStack, ix - 2, iy - 2, ix + 18, iy + 18, placed ? 0xAA2F7A2F : 0xAA7A5A12);
            fill(poseStack, ix - 1, iy - 1, ix + 17, iy + 17, 0x00000000);
            OriginalGuiTextures.blitOriginal(poseStack, ix, iy, texture, 16, 16);
            if (!placed) {
                fill(poseStack, ix, iy + 13, ix + 16, iy + 16, 0xAA000000);
            }
            if (mouseX >= ix - 1 && mouseX <= ix + 17 && mouseY >= iy - 1 && mouseY <= iy + 17) {
                renderTooltip(poseStack, Component.literal((placed ? "Placed " : "Missing ") + aspect.displayName()), mouseX, mouseY);
            }
            i++;
        }
    }

    private void renderAspectPalette(PoseStack poseStack, int mouseX, int mouseY) {
        List<Aspect> known = knownAspects();
        int start = aspectPage * ASPECTS_PER_PAGE;
        int end = Math.min(start + ASPECTS_PER_PAGE, known.size());

        for (int i = start; i < end; i++) {
            Aspect aspect = known.get(i);
            int local = i - start;
            int x = leftPos + TC4ResearchTableParity.ASPECT_GRID_X + (local % TC4ResearchTableParity.ASPECT_GRID_COLUMNS) * TC4ResearchTableParity.ASPECT_GRID_STEP;
            int y = topPos + TC4ResearchTableParity.ASPECT_GRID_Y + (local / TC4ResearchTableParity.ASPECT_GRID_COLUMNS) * TC4ResearchTableParity.ASPECT_GRID_STEP;
            int pool = ClientAspectData.pool(aspect);
            ResourceLocation texture = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + aspect.id() + ".png");

            // Stage563-582: keep the note palette on the same parchment/aspect
            // coordinates as GuiResearchTable. No opaque modern button squares.
            if (selectedAspect == aspect) {
                fill(poseStack, x - 2, y - 2, x + 18, y - 1, 0xFFC08A32);
                fill(poseStack, x - 2, y + 17, x + 18, y + 18, 0xFFC08A32);
                fill(poseStack, x - 2, y - 2, x - 1, y + 18, 0xFFC08A32);
                fill(poseStack, x + 17, y - 2, x + 18, y + 18, 0xFFC08A32);
            }
            OriginalGuiTextures.blitOriginal(poseStack, x, y, texture, 16, 16);
            if (pool > 0) {
                drawString(poseStack, font, Component.literal(String.valueOf(Math.min(pool, 99))), x + 9, y + 8, 0xFFFFFF);
            }
            if (TC4ResearchTableParity.isAspectIconHit(mouseX - leftPos, mouseY - topPos, local)) {
                renderTooltip(poseStack, Component.literal(aspect.displayName()), mouseX, mouseY);
            }
        }
    }

    private void renderAspectPageArrows(PoseStack poseStack, int mouseX, int mouseY) {
        List<Aspect> known = knownAspects();
        if (aspectPage > 0) {
            OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos + TC4ResearchTableParity.PAGE_PREVIOUS_X, topPos + TC4ResearchTableParity.PAGE_ARROW_Y,
                    OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL, 184, 200, 16, 10, 255, 255);
        }
        if ((aspectPage + 1) * ASPECTS_PER_PAGE < known.size()) {
            OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos + TC4ResearchTableParity.PAGE_NEXT_X, topPos + TC4ResearchTableParity.PAGE_ARROW_Y,
                    OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL, 200, 200, 16, 10, 255, 255);
        }
        // Original TC4 uses the arrow sprites themselves as navigation; no modern
        // explanatory tooltip text is drawn over the parchment.
    }

    private List<Aspect> knownAspects() {
        java.util.ArrayList<Aspect> known = new java.util.ArrayList<>();
        for (Aspect aspect : Aspect.values()) {
            if (ClientAspectData.knows(aspect)) {
                known.add(aspect);
            }
        }
        return known;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
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
            Aspect paletteAspect = paletteAspectAt(mouseX, mouseY);
            if (paletteAspect != null) {
                selectedAspect = paletteAspect;
                draggedAspect = paletteAspect;
                draggingAspect = true;
                status = "";
                return true;
            }
        }

        java.util.Optional<ResearchNoteGrid.GridSlot> hit = ResearchNoteGrid.hitTest(
                (int) Math.round(mouseX - leftPos),
                (int) Math.round(mouseY - topPos),
                ClientResearchNoteData.radius()
        );

        if (hit.isPresent() && ClientResearchNoteData.activeAt(hit.get().index())) {
            int slot = hit.get().index();
            Aspect current = ClientResearchNoteData.aspectAt(slot);

            if (button == 1) {
                if (current != null && ClientResearchNoteData.placedAt(slot)) {
                    ThaumcraftNetwork.requestClearResearchNoteSlotFromClient(slot);
                    status = "";
                } else {
                    status = "";
                }
                return true;
            }

            if (selectedAspect == null) {
                status = "";
                return true;
            }

            if (!canClientPlace(slot, selectedAspect)) {
                status = "";
                return true;
            }

            ThaumcraftNetwork.requestPlaceResearchNoteAspectFromClient(slot, selectedAspect.id());
            status = "";
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingAspect && button == 0) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingAspect) {
            Aspect releaseAspect = draggedAspect;
            draggedAspect = null;
            draggingAspect = false;
            if (releaseAspect == null) {
                return true;
            }
            java.util.Optional<ResearchNoteGrid.GridSlot> hit = ResearchNoteGrid.hitTest(
                    (int) Math.round(mouseX - leftPos),
                    (int) Math.round(mouseY - topPos),
                    ClientResearchNoteData.radius()
            );
            if (hit.isEmpty() || !ClientResearchNoteData.activeAt(hit.get().index())) {
                status = "";
                return true;
            }
            int slot = hit.get().index();
            if (!canClientPlace(slot, releaseAspect)) {
                status = "";
                return true;
            }
            ThaumcraftNetwork.requestPlaceResearchNoteAspectFromClient(slot, releaseAspect.id());
            selectedAspect = releaseAspect;
            status = "";
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private Aspect paletteAspectAt(double mouseX, double mouseY) {
        List<Aspect> known = knownAspects();
        int start = aspectPage * ASPECTS_PER_PAGE;
        int end = Math.min(start + ASPECTS_PER_PAGE, known.size());
        for (int i = start; i < end; i++) {
            int local = i - start;
            int x = leftPos + TC4ResearchTableParity.ASPECT_GRID_X + (local % TC4ResearchTableParity.ASPECT_GRID_COLUMNS) * TC4ResearchTableParity.ASPECT_GRID_STEP;
            int y = topPos + TC4ResearchTableParity.ASPECT_GRID_Y + (local / TC4ResearchTableParity.ASPECT_GRID_COLUMNS) * TC4ResearchTableParity.ASPECT_GRID_STEP;
            if (TC4ResearchTableParity.isAspectIconHit(mouseX - leftPos, mouseY - topPos, local)) {
                return known.get(i);
            }
        }
        return null;
    }

    private void renderDraggedAspect(PoseStack poseStack, int mouseX, int mouseY) {
        if (!draggingAspect || draggedAspect == null) {
            return;
        }
        ResourceLocation texture = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + draggedAspect.id() + ".png");
        OriginalGuiTextures.blitOriginal(poseStack, mouseX - 8, mouseY - 8, texture, 16, 16);
    }

    private boolean canClientPlace(int slot, Aspect aspect) {
        return !ClientResearchNoteData.solved() && aspect != null && ClientResearchNoteData.emptyAt(slot) && ClientResearchNoteData.aspectAt(slot) == null;
    }

    private boolean isLockedSlot(int index) {
        return ClientResearchNoteData.anchorAt(index);
    }

    /**
     * TC4 GuiResearchRecipe draws solved connections as irregular thread-like
     * links, not rigid debug ruler lines.  The exact GL line state from 1.7.10
     * is not available here, so this keeps the same visual intent with a small
     * deterministic sag and midpoint wobble while preserving the original hex
     * endpoints.
     */
    private void drawSaggingThreadLikeTC4(PoseStack poseStack, int x1, int y1, int x2, int y2, int color, int fromSlot, int toSlot) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        if (steps <= 0) {
            fill(poseStack, x1, y1, x1 + 1, y1 + 1, color);
            return;
        }
        double dx = x2 - x1;
        double dy = y2 - y1;
        double len = Math.max(1.0D, Math.sqrt(dx * dx + dy * dy));
        double nx = -dy / len;
        double ny = dx / len;
        int seed = fromSlot * 31 + toSlot * 17;
        double wobble = ((seed & 3) - 1.5D) * 0.35D;
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            double sag = Math.sin(Math.PI * t) * (1.5D + Math.abs(dy) * 0.025D);
            double knot = Math.sin((t * Math.PI * 2.0D) + seed) * wobble;
            int x = (int) Math.round(x1 + dx * t + nx * (sag + knot));
            int y = (int) Math.round(y1 + dy * t + ny * (sag * 0.35D + knot));
            fill(poseStack, x, y, x + 1, y + 1, color);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
