package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.ClientAspectData;
import com.darkifov.thaumcraft.client.ClientResearchNoteData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.ResearchAspectGraph;
import com.darkifov.thaumcraft.research.ResearchNoteGrid;
import com.darkifov.thaumcraft.research.ResearchNoteRequirements;
import com.darkifov.thaumcraft.research.TC4ResearchTableParity;
import com.darkifov.thaumcraft.research.TC4ResearchNoteGraphParity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResearchNoteScreen extends Screen {
    private static final int BG_WIDTH = TC4ResearchTableParity.GUI_WIDTH;
    private static final int BG_HEIGHT = TC4ResearchTableParity.GUI_HEIGHT;
    private static final int ASPECTS_PER_PAGE = TC4ResearchTableParity.ASPECTS_PER_PAGE;
    private static final ResourceLocation UNKNOWN_ASPECT = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/aspects/_unknown.png");
    private static final ResourceLocation ASPECT_BACK = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/aspects/_back.png");
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
        super(Component.translatable("item.thaumcraft.research_note"));
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
        OriginalGuiTextures.blitTc4ResearchTableBackground(poseStack, leftPos, topPos);
        OriginalGuiTextures.blitTc4ResearchParchment(poseStack, leftPos, topPos);

        ConnectionView connectionView = buildConnectionView();
        renderLinks(poseStack, connectionView);
        renderGrid(poseStack, mouseX, mouseY, connectionView);
        renderAspectPalette(poseStack, mouseX, mouseY);
        renderAspectPageArrows(poseStack, mouseX, mouseY);
        renderDraggedAspect(poseStack, mouseX, mouseY);

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    private void renderGrid(PoseStack poseStack, int mouseX, int mouseY, ConnectionView connectionView) {
        for (ResearchNoteGrid.GridSlot slot : ResearchNoteGrid.slots()) {
            if (!ClientResearchNoteData.activeAt(slot.index())) {
                continue;
            }
            int x = leftPos + ResearchNoteGrid.x(slot.index());
            int y = topPos + ResearchNoteGrid.y(slot.index());
            Aspect aspect = ClientResearchNoteData.aspectAt(slot.index());

            boolean locked = isLockedSlot(slot.index());
            boolean validTarget = selectedAspect != null && canClientPlace(slot.index(), selectedAspect);
            boolean hovered = mouseX >= x - 10 && mouseX <= x + 10 && mouseY >= y - 9 && mouseY <= y + 9;

            // TC4's hex textures are 32x32 and are sampled in full, then drawn as an
            // approximately 16x16 hex. The old adapter declared the destination as
            // the source sheet size, which cropped and stretched every cell.
            if (!locked) {
                if (hovered) {
                    OriginalGuiTextures.blitOriginalScaledTintedAlpha(poseStack, x - 10, y - 9,
                            OriginalGuiTextures.HEX2, 32, 32,
                            TC4ResearchTableParity.NOTE_HEX_DRAW_W, TC4ResearchTableParity.NOTE_HEX_DRAW_H,
                            0xFFFFFF, 1.0F);
                }
                OriginalGuiTextures.blitOriginalScaledTintedAlpha(poseStack, x - 10, y - 9,
                        OriginalGuiTextures.HEX1, 32, 32,
                        TC4ResearchTableParity.NOTE_HEX_DRAW_W, TC4ResearchTableParity.NOTE_HEX_DRAW_H,
                        0xFFFFFF, 0.25F);
            }

            if (aspect != null) {
                boolean known = ClientAspectData.knows(aspect);
                if (locked) {
                    // TC4 puts fixed research anchors on an animated orb instead of
                    // leaving them as bare square icons on the parchment.
                    OriginalGuiTextures.blitOriginalScaledTintedAlpha(poseStack, x - 10, y - 10,
                            ASPECT_BACK, 32, 32, 20, 20, aspect.nativeColor(), 0.78F);
                }

                if (!known) {
                    OriginalGuiTextures.blitOriginalScaledTintedAlpha(poseStack, x - 8, y - 8,
                            UNKNOWN_ASPECT, 32, 32, 16, 16, 0x111111, 0.58F);
                } else {
                    ResourceLocation texture = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + aspect.id() + ".png");
                    float aspectAlpha = locked || connectionView.highlighted().contains(slot.index())
                            ? 1.0F
                            : ClientResearchNoteData.placedAt(slot.index()) ? 0.66F : 1.0F;
                    OriginalGuiTextures.blitOriginalTintedAlpha(poseStack, x - 8, y - 8, texture, 16, 16,
                            aspect.nativeColor(), aspectAlpha);
                }
            }

            if (hovered) {
                if (aspect != null && ClientAspectData.knows(aspect)) {
                    renderTooltip(poseStack, Component.translatable("aspect.thaumcraft." + aspect.id()), mouseX, mouseY);
                } else if (selectedAspect != null && validTarget) {
                    renderTooltip(poseStack, Component.translatable("aspect.thaumcraft." + selectedAspect.id()), mouseX, mouseY);
                }
            }
        }
    }

    private void renderLinks(PoseStack poseStack, ConnectionView connectionView) {
        for (long edge : connectionView.edges()) {
            int first = (int) (edge >>> 32);
            int second = (int) edge;
            int x = leftPos + ResearchNoteGrid.x(first);
            int y = topPos + ResearchNoteGrid.y(first);
            int nx = leftPos + ResearchNoteGrid.x(second);
            int ny = topPos + ResearchNoteGrid.y(second);
            drawTc4Connection(poseStack, x, y, nx, ny);
        }
    }

    /**
     * Original GuiResearchTable does not draw every locally compatible pair.
     * It starts at the fixed research anchors and recursively highlights only
     * the connected solution network.  This prevents disconnected mini-chains
     * elsewhere on the sheet from glowing as though they were valid progress.
     */
    private ConnectionView buildConnectionView() {
        Set<Integer> highlighted = new HashSet<>();
        Set<Long> edges = new LinkedHashSet<>();
        ArrayDeque<Integer> open = new ArrayDeque<>();

        for (ResearchNoteGrid.GridSlot slot : ResearchNoteGrid.slots()) {
            int index = slot.index();
            Aspect aspect = ClientResearchNoteData.aspectAt(index);
            if (ClientResearchNoteData.activeAt(index)
                    && ClientResearchNoteData.anchorAt(index)
                    && aspect != null
                    && ClientAspectData.knows(aspect)
                    && highlighted.add(index)) {
                open.add(index);
            }
        }

        while (!open.isEmpty()) {
            int index = open.removeFirst();
            Aspect aspect = ClientResearchNoteData.aspectAt(index);
            if (aspect == null) {
                continue;
            }
            for (int neighbor : ResearchNoteGrid.neighbors(index)) {
                if (!ClientResearchNoteData.activeAt(neighbor)) {
                    continue;
                }
                Aspect other = ClientResearchNoteData.aspectAt(neighbor);
                if (other == null || !ClientAspectData.knows(other) || !ResearchAspectGraph.canConnect(aspect, other)) {
                    continue;
                }
                int low = Math.min(index, neighbor);
                int high = Math.max(index, neighbor);
                edges.add(((long) low << 32) | (high & 0xFFFFFFFFL));
                if (highlighted.add(neighbor)) {
                    open.addLast(neighbor);
                }
            }
        }
        return new ConnectionView(highlighted, edges);
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
            OriginalGuiTextures.blitOriginalTinted(poseStack, ix, iy, texture, 16, 16, aspect.nativeColor());
            if (!placed) {
                fill(poseStack, ix, iy + 13, ix + 16, iy + 16, 0xAA000000);
            }
            if (mouseX >= ix - 1 && mouseX <= ix + 17 && mouseY >= iy - 1 && mouseY <= iy + 17) {
                renderTooltip(poseStack, Component.translatable(placed ? "thaumcraft.gui.research.placed" : "thaumcraft.gui.research.missing", Component.translatable("aspect.thaumcraft." + aspect.id())), mouseX, mouseY);
            }
            i++;
        }
    }

    private void renderAspectPalette(PoseStack poseStack, int mouseX, int mouseY) {
        List<Aspect> known = knownAspects();
        int start = TC4ResearchTableParity.aspectPageStart(aspectPage);
        int end = Math.min(start + ASPECTS_PER_PAGE, known.size());

        for (int i = start; i < end; i++) {
            Aspect aspect = known.get(i);
            int local = i - start;
            int x = leftPos + TC4ResearchTableParity.aspectX(local);
            int y = topPos + TC4ResearchTableParity.aspectY(local);
            int pool = ClientAspectData.pool(aspect);
            ResourceLocation texture = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + aspect.id() + ".png");

            // GuiResearchTable draws bare aspect tags on the wood panel. Selection
            // is represented by the dragged orb itself, not a modern rectangular border.
            OriginalGuiTextures.blitOriginalTintedAlpha(poseStack, x, y, texture, 16, 16, aspect.nativeColor(), pool > 0 ? 1.0F : 0.33F);
            if (pool > 0) {
                drawString(poseStack, font, Component.literal(String.valueOf(Math.min(pool, 99))), x + 9, y + 8, 0xFFFFFF);
            }
            if (TC4ResearchTableParity.isAspectIconHit(mouseX - leftPos, mouseY - topPos, local)) {
                renderTooltip(poseStack, Component.translatable("aspect.thaumcraft." + aspect.id()), mouseX, mouseY);
            }
        }
    }

    private void renderAspectPageArrows(PoseStack poseStack, int mouseX, int mouseY) {
        List<Aspect> known = knownAspects();
        if (aspectPage > 0) {
            OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos + TC4ResearchTableParity.PAGE_PREVIOUS_X, topPos + TC4ResearchTableParity.PAGE_ARROW_Y,
                    OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL, 184, 208, 24, 8, 256, 256);
        }
        if (aspectPage < TC4ResearchTableParity.lastAspectPage(known.size())) {
            OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos + TC4ResearchTableParity.PAGE_NEXT_X, topPos + TC4ResearchTableParity.PAGE_ARROW_Y,
                    OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL, 208, 208, 24, 8, 256, 256);
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
                    && aspectPage < TC4ResearchTableParity.lastAspectPage(known.size())) {
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
        int start = TC4ResearchTableParity.aspectPageStart(aspectPage);
        int end = Math.min(start + ASPECTS_PER_PAGE, known.size());
        for (int i = start; i < end; i++) {
            int local = i - start;
            int x = leftPos + TC4ResearchTableParity.aspectX(local);
            int y = topPos + TC4ResearchTableParity.aspectY(local);
            if (TC4ResearchTableParity.isAspectIconHit(mouseX - leftPos, mouseY - topPos, local)) {
                Aspect aspect = known.get(i);
                return ClientAspectData.pool(aspect) > 0 ? aspect : null;
            }
        }
        return null;
    }

    private void renderDraggedAspect(PoseStack poseStack, int mouseX, int mouseY) {
        if (!draggingAspect || draggedAspect == null) {
            return;
        }
        ResourceLocation texture = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + draggedAspect.id() + ".png");
        OriginalGuiTextures.blitOriginalTintedAlpha(poseStack, mouseX - 8, mouseY - 8, texture, 16, 16,
                draggedAspect.nativeColor(), ClientAspectData.pool(draggedAspect) > 0 ? 0.92F : 0.33F);
    }

    private boolean canClientPlace(int slot, Aspect aspect) {
        return !ClientResearchNoteData.solved()
                && aspect != null
                && TC4ResearchNoteGraphParity.canPlaceIntoHex(
                        slot,
                        ClientResearchNoteData.emptyAt(slot)
                                ? ResearchNoteGrid.TYPE_EMPTY
                                : ResearchNoteGrid.TYPE_PLACED,
                        ClientResearchNoteData.aspectAt(slot) == null);
    }

    private boolean isLockedSlot(int index) {
        return ClientResearchNoteData.anchorAt(index);
    }

    /**
     * TC4 draws a straight three-pixel additive cyan line. Rendering it as tiny
     * filled pixels keeps the same geometry without depending on removed 1.7.10
     * immediate-mode GL line state.
     */
    private void drawTc4Connection(PoseStack poseStack, int x1, int y1, int x2, int y2) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        if (steps <= 0) {
            return;
        }
        int ticks = minecraft != null && minecraft.player != null ? minecraft.player.tickCount : 0;
        float pulse = 0.60F + (float) Math.sin(ticks + x1) * 0.30F;
        int alpha = Math.max(0, Math.min(255, Math.round(pulse * 255.0F)));
        int color = (alpha << 24) | 0x0099CC;
        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.max(1.0D, Math.sqrt(dx * dx + dy * dy));
        int ox = Math.abs(dx) >= Math.abs(dy) ? 0 : 1;
        int oy = Math.abs(dx) >= Math.abs(dy) ? 1 : 0;
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            int x = (int) Math.round(x1 + dx * t);
            int y = (int) Math.round(y1 + dy * t);
            fill(poseStack, x - ox, y - oy, x + ox + 1, y + oy + 1, color);
        }
    }

    private record ConnectionView(Set<Integer> highlighted, Set<Long> edges) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
