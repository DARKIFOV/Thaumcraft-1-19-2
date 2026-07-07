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
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResearchNoteScreen extends Screen {
    private static final int BG_WIDTH = 256;
    private static final int BG_HEIGHT = 256;

    private int leftPos;
    private int topPos;
    private Aspect selectedAspect;
    private Aspect draggedAspect;
    private boolean draggingAspect;
    private String status = "Drag an aspect onto an empty active hex.";

    public ResearchNoteScreen() {
        super(Component.literal("Research Note"));
    }

    @Override
    protected void init() {
        leftPos = (width - BG_WIDTH) / 2;
        topPos = (height - BG_HEIGHT) / 2;

        addRenderableWidget(new Button(leftPos + BG_WIDTH - 74, topPos + BG_HEIGHT - 28, 56, 18,
                Component.literal("Solve"), button -> ThaumcraftNetwork.requestSolveResearchNoteFromClient()));

        addRenderableWidget(new Button(leftPos + 8, topPos + BG_HEIGHT - 28, 22, 18,
                Component.literal("×"), button -> onClose()));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        OriginalGuiTextures.blitOriginal(poseStack, leftPos, topPos, OriginalGuiTextures.RESEARCH_TABLE, BG_WIDTH, BG_HEIGHT);

        drawCenteredString(poseStack, font, Component.literal("Research Note"), leftPos + BG_WIDTH / 2, topPos + 8, 0x3F2612);
        drawString(poseStack, font, Component.literal("Target: " + (ClientResearchNoteData.target().isBlank() ? "available research" : ClientResearchNoteData.target())), leftPos + 18, topPos + 26, 0x3F2612);
        drawString(poseStack, font, Component.literal("Progress: " + ClientResearchNoteData.progress() + "%"), leftPos + 18, topPos + 38, 0x3F2612);
        drawString(poseStack, font, Component.literal("Gold hexes = original TC4 research anchors. Green = empty active hex."), leftPos + 18, topPos + 50, 0x5A3515);
        drawString(poseStack, font, Component.literal(ClientResearchNoteData.solved() ? "Solved" : status), leftPos + 18, topPos + 222, ClientResearchNoteData.solved() ? 0x287A28 : 0x5A3515);

        renderLinks(poseStack);
        renderPathHint(poseStack);
        renderGrid(poseStack, mouseX, mouseY);
        renderRequiredAspects(poseStack, mouseX, mouseY);
        renderAspectPalette(poseStack, mouseX, mouseY);
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
            boolean required = locked;
            boolean validTarget = selectedAspect != null && canClientPlace(slot.index(), selectedAspect);
            int slotColor = aspect == null ? 0xAA3F2612 : AspectColor.argb(aspect, required ? 245 : 210);
            int border = required ? 0xFFFFCC55 : validTarget ? 0xAA44AA44 : locked ? 0xAA6D4A22 : (aspect == null ? 0xAA3F2612 : AspectColor.dim(aspect, 185, 0.55F));
            fill(poseStack, x - 12, y - 12, x + 12, y + 12, border);
            fill(poseStack, x - 10, y - 10, x + 10, y + 10, slotColor);

            if (aspect != null) {
                ResourceLocation texture = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + aspect.id() + ".png");
                OriginalGuiTextures.blitOriginal(poseStack, x - 8, y - 8, texture, 16, 16);
            } else if (validTarget) {
                drawCenteredString(poseStack, font, Component.literal("+"), x, y - 4, 0xD8FFD8);
            }

            if (mouseX >= x - 10 && mouseX <= x + 10 && mouseY >= y - 10 && mouseY <= y + 10) {
                fill(poseStack, x - 12, y - 12, x + 12, y + 12, 0x33FFFFFF);
                String label = "Slot " + slot.index();
                if (aspect != null) {
                    label += " " + aspect.displayName() + (locked ? " (fixed)" : " - right-click to remove");
                } else if (selectedAspect != null) {
                    label += validTarget ? " accepts " + selectedAspect.displayName() : " is not an empty active TC4 hex";
                }
                renderTooltip(poseStack, Component.literal(label), mouseX, mouseY);
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
                drawLine(poseStack, x, y, nx, ny, lineColor);
            }
        }
    }

    private void renderPathHint(PoseStack poseStack) {
        Aspect start = null;
        Aspect end = null;
        for (Map.Entry<Integer, Integer> entry : ClientResearchNoteData.types().entrySet()) {
            if (entry.getValue() != ResearchNoteGrid.TYPE_RESEARCH_ANCHOR) {
                continue;
            }
            Aspect aspect = ClientResearchNoteData.aspectAt(entry.getKey());
            if (aspect == null) {
                continue;
            }
            if (start == null) {
                start = aspect;
            } else {
                end = aspect;
                break;
            }
        }
        List<Aspect> path = ResearchAspectGraph.shortestPath(start, end);

        if (path.size() <= 2) {
            return;
        }

        int x = leftPos + 176;
        int y = topPos + 138;
        drawString(poseStack, font, Component.literal("Path hint"), x, y - 10, 0x5A3515);

        for (int i = 1; i < Math.min(path.size() - 1, 5); i++) {
            Aspect aspect = path.get(i);
            ResourceLocation texture = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + aspect.id() + ".png");
            int ix = x + (i - 1) * 18;
            OriginalGuiTextures.blitOriginal(poseStack, ix, y, texture, 16, 16);
        }
    }

    private void renderRequiredAspects(PoseStack poseStack, int mouseX, int mouseY) {
        int x = leftPos + 176;
        int y = topPos + 58;
        Set<Aspect> requiredAspects = ResearchNoteRequirements.requiredFor(ClientResearchNoteData.target());
        drawString(poseStack, font, Component.literal("Required"), x, y - 12, 0x5A3515);
        int i = 0;
        for (Aspect aspect : requiredAspects) {
            int ix = x + (i % 3) * 20;
            int iy = y + (i / 3) * 20;
            boolean placed = ClientResearchNoteData.slots().containsValue(aspect.id());
            ResourceLocation texture = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + aspect.id() + ".png");
            fill(poseStack, ix - 2, iy - 2, ix + 18, iy + 18, placed ? 0xAA2F7A2F : 0xAA7A5A12);
            fill(poseStack, ix - 1, iy - 1, ix + 17, iy + 17, 0xAA1D140C);
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
        int startX = leftPos + 12;
        int startY = topPos + 58;
        int shown = 0;

        for (Aspect aspect : Aspect.values()) {
            if (!ClientAspectData.knows(aspect)) {
                continue;
            }

            int col = shown % 4;
            int row = shown / 4;

            if (row > 10) {
                break;
            }

            int x = startX + col * 20;
            int y = startY + row * 16;
            int pool = ClientAspectData.pool(aspect);
            ResourceLocation texture = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + aspect.id() + ".png");
            fill(poseStack, x - 2, y - 2, x + 18, y + 18, selectedAspect == aspect ? AspectColor.argb(aspect, 245) : AspectColor.dim(aspect, pool > 0 ? 170 : 85, 0.55F));
            fill(poseStack, x - 1, y - 1, x + 17, y + 17, pool > 0 ? 0xAA1D140C : 0xAA050505);
            OriginalGuiTextures.blitOriginal(poseStack, x, y, texture, 16, 16);

            if (pool <= 0) {
                fill(poseStack, x, y + 12, x + 16, y + 16, 0xAA000000);
            }

            if (mouseX >= x - 1 && mouseX <= x + 17 && mouseY >= y - 1 && mouseY <= y + 17) {
                renderTooltip(poseStack, Component.literal(aspect.displayName() + " pool:" + pool), mouseX, mouseY);
            }

            shown++;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            Aspect paletteAspect = paletteAspectAt(mouseX, mouseY);
            if (paletteAspect != null) {
                selectedAspect = paletteAspect;
                draggedAspect = paletteAspect;
                draggingAspect = true;
                status = "Dragging " + paletteAspect.displayName() + ". Release over a TC4 hex.";
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
                    status = "Clear request sent.";
                } else {
                    status = "Original research anchors cannot be cleared.";
                }
                return true;
            }

            if (selectedAspect == null) {
                status = "Select or drag an aspect first.";
                return true;
            }

            if (!canClientPlace(slot, selectedAspect)) {
                status = "That hex is not an empty active research note slot.";
                return true;
            }

            ThaumcraftNetwork.requestPlaceResearchNoteAspectFromClient(slot, selectedAspect.id());
            status = "Placement sent.";
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
                status = "Dropped outside the TC4 research hex grid.";
                return true;
            }
            int slot = hit.get().index();
            if (!canClientPlace(slot, releaseAspect)) {
                status = "That hex is fixed, missing, or already filled.";
                return true;
            }
            ThaumcraftNetwork.requestPlaceResearchNoteAspectFromClient(slot, releaseAspect.id());
            selectedAspect = releaseAspect;
            status = "Placement sent.";
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private Aspect paletteAspectAt(double mouseX, double mouseY) {
        int startX = leftPos + 12;
        int startY = topPos + 58;
        int shown = 0;

        for (Aspect aspect : Aspect.values()) {
            if (!ClientAspectData.knows(aspect)) {
                continue;
            }
            int col = shown % 4;
            int row = shown / 4;
            if (row > 10) {
                break;
            }
            int x = startX + col * 20;
            int y = startY + row * 16;
            if (mouseX >= x - 1 && mouseX <= x + 17 && mouseY >= y - 1 && mouseY <= y + 17) {
                return aspect;
            }
            shown++;
        }
        return null;
    }

    private void renderDraggedAspect(PoseStack poseStack, int mouseX, int mouseY) {
        if (!draggingAspect || draggedAspect == null) {
            return;
        }
        ResourceLocation texture = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + draggedAspect.id() + ".png");
        fill(poseStack, mouseX - 10, mouseY - 10, mouseX + 10, mouseY + 10, AspectColor.argb(draggedAspect, 190));
        OriginalGuiTextures.blitOriginal(poseStack, mouseX - 8, mouseY - 8, texture, 16, 16);
    }

    private boolean canClientPlace(int slot, Aspect aspect) {
        return aspect != null && ClientResearchNoteData.emptyAt(slot) && ClientResearchNoteData.aspectAt(slot) == null;
    }

    private boolean isLockedSlot(int index) {
        return ClientResearchNoteData.anchorAt(index);
    }

    private void drawLine(PoseStack poseStack, int x1, int y1, int x2, int y2, int color) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        if (steps <= 0) {
            fill(poseStack, x1, y1, x1 + 1, y1 + 1, color);
            return;
        }
        for (int i = 0; i <= steps; i++) {
            int x = x1 + (x2 - x1) * i / steps;
            int y = y1 + (y2 - y1) * i / steps;
            fill(poseStack, x, y, x + 1, y + 1, color);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
