package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectCombinationRegistry;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.ResearchNoteItem;
import com.darkifov.thaumcraft.blockentity.ResearchTableBlockEntity;
import com.darkifov.thaumcraft.client.ClientAspectData;
import com.darkifov.thaumcraft.client.ClientResearchNoteData;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.menu.ResearchTableMenu;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.ResearchAspectGraph;
import com.darkifov.thaumcraft.research.ResearchNoteGrid;
import com.darkifov.thaumcraft.research.ResearchNoteState;
import com.darkifov.thaumcraft.research.TC4ResearchTableParity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Forge 1.19.2 adapter for the original TC4 GuiResearchTable.
 *
 * <p>v11.62.36 removes the rebuild-only second research-note window. The
 * parchment, hex puzzle, aspect palette, combination wells and inventory now
 * live in one 255x255 container exactly as they did in Thaumcraft 4.2.3.5.</p>
 */
public class ResearchTableContainerScreen extends AbstractContainerScreen<ResearchTableMenu> {
    private static final int BG_WIDTH = TC4ResearchTableParity.GUI_WIDTH;
    private static final int BG_HEIGHT = TC4ResearchTableParity.GUI_HEIGHT;
    private static final int ASPECTS_PER_PAGE = TC4ResearchTableParity.ASPECTS_PER_PAGE;
    private static final ResourceLocation UNKNOWN_ASPECT = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/aspects/_unknown.png");
    private static final ResourceLocation ASPECT_BACK = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/aspects/_back.png");

    private int aspectPage;
    private Aspect firstAspect;
    private Aspect secondAspect;
    private Aspect previewAspect;
    private Aspect draggedAspect;
    private boolean draggingAspect;
    private long combineFlashUntilNanos;
    private int lastNoteSignature = Integer.MIN_VALUE;
    private int noteSyncCooldown;

    public ResearchTableContainerScreen(ResearchTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = BG_WIDTH;
        imageHeight = BG_HEIGHT;
        inventoryLabelX = TC4ResearchTableParity.PLAYER_INVENTORY_X;
        inventoryLabelY = 165;
        titleLabelX = 8;
        titleLabelY = 4;
    }

    @Override
    protected void init() {
        super.init();
        // Original GuiResearchTable immediately reads the note in slot 1. The
        // packet only mirrors its NBT to the client; it does not open another GUI.
        requestNoteSync();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (noteSyncCooldown > 0) {
            noteSyncCooldown--;
        }
        int signature = noteSignature(currentNote());
        if (signature != lastNoteSignature && noteSyncCooldown <= 0) {
            lastNoteSignature = signature;
            requestNoteSync();
        }
    }

    private void requestNoteSync() {
        noteSyncCooldown = 5;
        ThaumcraftNetwork.requestResearchTableActionFromClient(
                menu.blockPos(), TC4ResearchTableParity.ACTION_SYNC_NOTE);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        OriginalGuiTextures.blitTc4ResearchTableBackground(poseStack, leftPos, topPos);
        sanitizeAspectSelections();

        ItemStack note = currentNote();
        if (hasResearchNote(note) && !ClientResearchNoteData.target().isBlank()) {
            OriginalGuiTextures.blitTc4ResearchParchment(poseStack, leftPos, topPos);
            ConnectionView connectionView = buildConnectionView();
            renderResearchLinks(poseStack, connectionView);
            renderResearchGrid(poseStack, mouseX, mouseY, connectionView);
        }

        if (hasResearchNote(note) && ResearchNoteState.solved(note)
                && minecraft != null && minecraft.player != null
                && PlayerThaumData.hasResearch(minecraft.player, "RESEARCHDUPE")) {
            OriginalGuiTextures.blitOriginalRegion(poseStack,
                    leftPos + TC4ResearchTableParity.COPY_ICON_X,
                    topPos + TC4ResearchTableParity.COPY_ICON_Y,
                    OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL,
                    232, 200, 24, 24, 256, 256);
        }

        renderAspectPaletteLikeTC4(poseStack);
        renderCombinationSlotsLikeTC4(poseStack);
        renderPageArrowsLikeTC4(poseStack);
    }

    private void renderAspectPaletteLikeTC4(PoseStack poseStack) {
        List<Aspect> known = knownAspects();
        int start = TC4ResearchTableParity.aspectPageStart(aspectPage);
        int end = Math.min(start + ASPECTS_PER_PAGE, known.size());
        for (int i = start; i < end; i++) {
            Aspect aspect = known.get(i);
            int local = i - start;
            drawAspectIcon(poseStack, aspect,
                    leftPos + TC4ResearchTableParity.aspectX(local),
                    topPos + TC4ResearchTableParity.aspectY(local),
                    ClientAspectData.pool(aspect));
        }
    }

    private void renderCombinationSlotsLikeTC4(PoseStack poseStack) {
        drawAspectSlot(poseStack, firstAspect,
                leftPos + TC4ResearchTableParity.COMBINE_LEFT_X,
                topPos + TC4ResearchTableParity.COMBINE_Y);
        drawAspectSlot(poseStack, secondAspect,
                leftPos + TC4ResearchTableParity.COMBINE_RIGHT_X,
                topPos + TC4ResearchTableParity.COMBINE_Y);

        if (firstAspect == null || secondAspect == null) {
            return;
        }

        int x = leftPos + TC4ResearchTableParity.COMBINE_ARROW_X;
        int y = topPos + TC4ResearchTableParity.COMBINE_Y;
        OriginalGuiTextures.blitOriginalRegion(poseStack, x, y,
                OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL,
                184, 184, 32, 16, 256, 256);

        if (combineFlashUntilNanos >= System.nanoTime()) {
            OriginalGuiTextures.blitOriginalRegion(poseStack, x, y,
                    OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL,
                    184, 168, 32, 16, 256, 256);
        } else if (previewAspect != null) {
            drawAspectOrb(poseStack, previewAspect, x + 8, y - 1, 0.92F);
        }
    }

    private void renderPageArrowsLikeTC4(PoseStack poseStack) {
        List<Aspect> known = knownAspects();
        if (aspectPage > 0) {
            OriginalGuiTextures.blitOriginalRegion(poseStack,
                    leftPos + TC4ResearchTableParity.PAGE_PREVIOUS_X,
                    topPos + TC4ResearchTableParity.PAGE_ARROW_Y,
                    OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL,
                    184, 208, 24, 8, 256, 256);
        }
        if (aspectPage < TC4ResearchTableParity.lastAspectPage(known.size())) {
            OriginalGuiTextures.blitOriginalRegion(poseStack,
                    leftPos + TC4ResearchTableParity.PAGE_NEXT_X,
                    topPos + TC4ResearchTableParity.PAGE_ARROW_Y,
                    OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL,
                    208, 208, 24, 8, 256, 256);
        }
    }

    private void renderResearchGrid(PoseStack poseStack, int mouseX, int mouseY,
                                    ConnectionView connectionView) {
        for (ResearchNoteGrid.GridSlot slot : ResearchNoteGrid.slots()) {
            int index = slot.index();
            if (!ClientResearchNoteData.activeAt(index)) {
                continue;
            }

            int x = leftPos + ResearchNoteGrid.x(index);
            int y = topPos + ResearchNoteGrid.y(index);
            Aspect aspect = ClientResearchNoteData.aspectAt(index);
            boolean locked = ClientResearchNoteData.anchorAt(index);
            boolean hovered = isHexHovered(mouseX, mouseY, x, y);

            if (!locked && !ClientResearchNoteData.solved()) {
                if (hovered) {
                    OriginalGuiTextures.blitOriginalScaledTintedAlpha(poseStack, x - 10, y - 9,
                            OriginalGuiTextures.HEX2, 32, 32,
                            TC4ResearchTableParity.NOTE_HEX_DRAW_W,
                            TC4ResearchTableParity.NOTE_HEX_DRAW_H,
                            0xFFFFFF, 1.0F);
                }
                OriginalGuiTextures.blitOriginalScaledTintedAlpha(poseStack, x - 10, y - 9,
                        OriginalGuiTextures.HEX1, 32, 32,
                        TC4ResearchTableParity.NOTE_HEX_DRAW_W,
                        TC4ResearchTableParity.NOTE_HEX_DRAW_H,
                        0xFFFFFF, 0.25F);
            }

            if (aspect == null) {
                continue;
            }

            if (locked) {
                OriginalGuiTextures.blitOriginalScaledTintedAlpha(poseStack, x - 10, y - 10,
                        ASPECT_BACK, 32, 32, 20, 20,
                        aspect.nativeColor(), 0.78F);
            }

            if (!ClientAspectData.knows(aspect)) {
                OriginalGuiTextures.blitOriginalScaledTintedAlpha(poseStack, x - 8, y - 8,
                        UNKNOWN_ASPECT, 32, 32, 16, 16,
                        0x111111, 0.58F);
            } else {
                ResourceLocation texture = aspectTexture(aspect);
                float alpha = locked || connectionView.highlighted().contains(index)
                        ? 1.0F
                        : ClientResearchNoteData.placedAt(index) ? 0.66F : 1.0F;
                OriginalGuiTextures.blitOriginalTintedAlpha(poseStack, x - 8, y - 8,
                        texture, 16, 16, aspect.nativeColor(), alpha);
            }
        }
    }

    private void renderResearchLinks(PoseStack poseStack, ConnectionView connectionView) {
        for (long edge : connectionView.edges()) {
            int first = (int) (edge >>> 32);
            int second = (int) edge;
            drawTc4Connection(poseStack,
                    leftPos + ResearchNoteGrid.x(first),
                    topPos + ResearchNoteGrid.y(first),
                    leftPos + ResearchNoteGrid.x(second),
                    topPos + ResearchNoteGrid.y(second));
        }
    }

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
                if (other == null || !ClientAspectData.knows(other)
                        || !ResearchAspectGraph.canConnect(aspect, other)) {
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

    private void drawAspectSlot(PoseStack poseStack, Aspect aspect, int x, int y) {
        if (aspect != null) {
            OriginalGuiTextures.blitOriginalTinted(poseStack, x, y,
                    aspectTexture(aspect), 16, 16, aspect.nativeColor());
        }
    }

    private void drawAspectIcon(PoseStack poseStack, Aspect aspect, int x, int y, int pool) {
        int bonus = menu.tableBonusAmount(aspect);
        int available = Math.max(0, pool) + Math.max(0, bonus);
        OriginalGuiTextures.blitOriginalTintedAlpha(poseStack, x, y,
                aspectTexture(aspect), 16, 16, aspect.nativeColor(),
                available > 0 ? 1.0F : 0.33F);
        if (pool > 0) {
            drawString(poseStack, font, Component.literal(String.valueOf(Math.min(pool, 99))),
                    x + 9, y + 8, 0xFFFFFF);
        }
        if (bonus > 0) {
            drawString(poseStack, font, Component.literal("+" + Math.min(bonus, 99)),
                    x - 2, y + 8, 0xFFE79A);
        }
    }

    private void drawAspectOrb(PoseStack poseStack, Aspect aspect, int x, int y, float alpha) {
        OriginalGuiTextures.blitOriginalScaledTintedAlpha(poseStack, x, y,
                ASPECT_BACK, 32, 32, 18, 18, aspect.nativeColor(), alpha * 0.78F);
        OriginalGuiTextures.blitOriginalTintedAlpha(poseStack, x + 1, y + 1,
                aspectTexture(aspect), 16, 16, aspect.nativeColor(), alpha);
    }

    private static ResourceLocation aspectTexture(Aspect aspect) {
        return new ResourceLocation(ThaumcraftMod.MOD_ID,
                "textures/aspects/" + aspect.id() + ".png");
    }

    private int availableAspect(Aspect aspect) {
        return aspect == null ? 0
                : ClientAspectData.pool(aspect) + menu.tableBonusAmount(aspect);
    }

    private void sanitizeAspectSelections() {
        if (firstAspect != null && availableAspect(firstAspect) <= 0) {
            firstAspect = null;
        }
        if (secondAspect != null && availableAspect(secondAspect) <= 0) {
            secondAspect = null;
        }
        previewAspect = firstAspect != null && secondAspect != null
                ? AspectCombinationRegistry.combine(firstAspect, secondAspect).orElse(null)
                : null;
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
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTableFeedback(poseStack, mouseX, mouseY);
        renderDraggedAspect(poseStack, mouseX, mouseY);
        if (!draggingAspect) {
            renderTooltip(poseStack, mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        // TC4 draws no modern title or inventory labels over this texture.
    }

    private void renderTableFeedback(PoseStack poseStack, int mouseX, int mouseY) {
        if (renderResearchHexTooltip(poseStack, mouseX, mouseY)) {
            return;
        }
        renderAspectTooltips(poseStack, mouseX, mouseY);

        ItemStack note = currentNote();
        if (hasResearchNote(note) && !menu.hasInkedTools()
                && mouseX >= leftPos + 94 && mouseX < leftPos + 244
                && mouseY >= topPos + 8 && mouseY < topPos + 158) {
            List<Component> lines = List.of(
                    Component.translatable("tile.researchtable.noink.0"),
                    Component.translatable("tile.researchtable.noink.1"));
            renderComponentTooltip(poseStack, lines, mouseX, mouseY);
        }
    }

    private boolean renderResearchHexTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        if (!hasResearchNote(currentNote()) || ClientResearchNoteData.target().isBlank()) {
            return false;
        }
        java.util.Optional<ResearchNoteGrid.GridSlot> hit = ResearchNoteGrid.hitTest(
                mouseX - leftPos, mouseY - topPos, ClientResearchNoteData.radius());
        if (hit.isEmpty() || !ClientResearchNoteData.activeAt(hit.get().index())) {
            return false;
        }
        Aspect aspect = ClientResearchNoteData.aspectAt(hit.get().index());
        if (aspect != null && ClientAspectData.knows(aspect)) {
            renderTooltip(poseStack,
                    Component.translatable("aspect.thaumcraft." + aspect.id()),
                    mouseX, mouseY);
            return true;
        }
        if (draggingAspect && draggedAspect != null
                && canClientPlace(hit.get().index(), draggedAspect)) {
            renderTooltip(poseStack,
                    Component.translatable("aspect.thaumcraft." + draggedAspect.id()),
                    mouseX, mouseY);
            return true;
        }
        return false;
    }

    private void renderAspectTooltips(PoseStack poseStack, int mouseX, int mouseY) {
        List<Aspect> known = knownAspects();
        int start = TC4ResearchTableParity.aspectPageStart(aspectPage);
        int end = Math.min(start + ASPECTS_PER_PAGE, known.size());
        for (int i = start; i < end; i++) {
            int local = i - start;
            int x = leftPos + TC4ResearchTableParity.aspectX(local);
            int y = topPos + TC4ResearchTableParity.aspectY(local);
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                Aspect aspect = known.get(i);
                List<Component> lines = new ArrayList<>();
                lines.add(Component.translatable("aspect.thaumcraft." + aspect.id())
                        .withStyle(style -> style.withColor(aspect.textColor())));
                lines.add(Component.translatable("thaumcraft.aspect.description." + aspect.id()));
                lines.add(Component.translatable("thaumcraft.gui.aspect.pool",
                        ClientAspectData.pool(aspect)));
                int bonus = menu.tableBonusAmount(aspect);
                if (bonus > 0) {
                    lines.add(Component.translatable("thaumcraft.gui.aspect.bonus", bonus));
                }
                if (hasResearchExpertise() && !aspect.isPrimal()) {
                    Aspect[] components = AspectCombinationRegistry.decompose(aspect).orElse(null);
                    if (components != null) {
                        lines.add(Component.translatable("thaumcraft.gui.research.expertise_components",
                                Component.translatable("aspect.thaumcraft." + components[0].id())
                                        .withStyle(style -> style.withColor(components[0].textColor())),
                                Component.translatable("aspect.thaumcraft." + components[1].id())
                                        .withStyle(style -> style.withColor(components[1].textColor()))));
                        if (hasResearchMastery()) {
                            lines.add(Component.translatable("thaumcraft.gui.research.mastery_shift"));
                        }
                    }
                }
                renderComponentTooltip(poseStack, lines, mouseX, mouseY);
                return;
            }
        }

        if (TC4ResearchTableParity.isCombineArrowHit(mouseX - leftPos, mouseY - topPos)) {
            Component tooltip;
            if (firstAspect == null || secondAspect == null) {
                tooltip = Component.translatable("thaumcraft.message.research.select_two");
            } else if (previewAspect == null) {
                tooltip = Component.translatable("thaumcraft.message.research.no_combination");
            } else {
                tooltip = Component.translatable("aspect.thaumcraft." + firstAspect.id())
                        .append(Component.literal(" + "))
                        .append(Component.translatable("aspect.thaumcraft." + secondAspect.id()))
                        .append(Component.literal(" = "))
                        .append(Component.translatable("aspect.thaumcraft." + previewAspect.id()));
            }
            renderTooltip(poseStack, tooltip, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double localX = mouseX - leftPos;
        double localY = mouseY - topPos;
        ItemStack note = currentNote();

        if (button == 1 && TC4ResearchTableParity.isNoteSlotHit(localX, localY)) {
            if (!hasResearchNote(note)) {
                ThaumcraftNetwork.requestResearchTableActionFromClient(
                        menu.blockPos(), TC4ResearchTableParity.ACTION_CREATE_NOTE);
            } else if (ResearchNoteState.solved(note) && hasShiftDown()) {
                ThaumcraftNetwork.requestResearchTableActionFromClient(
                        menu.blockPos(), TC4ResearchTableParity.ACTION_COMPLETE_SOLVED_NOTE);
            } else {
                ThaumcraftNetwork.requestResearchTableActionFromClient(
                        menu.blockPos(), TC4ResearchTableParity.ACTION_SYNC_NOTE);
            }
            return true;
        }

        if (button == 1 && hasResearchNote(note)) {
            java.util.Optional<ResearchNoteGrid.GridSlot> hit = ResearchNoteGrid.hitTest(
                    (int) Math.round(localX), (int) Math.round(localY),
                    ClientResearchNoteData.radius());
            if (hit.isPresent() && ClientResearchNoteData.activeAt(hit.get().index())) {
                int slot = hit.get().index();
                if (ClientResearchNoteData.placedAt(slot)
                        && ClientResearchNoteData.aspectAt(slot) != null) {
                    ThaumcraftNetwork.requestClearResearchNoteSlotFromClient(slot);
                }
                return true;
            }
        }

        if (button == 0 && TC4ResearchTableParity.isCopyIconHit(localX, localY)
                && hasResearchNote(note) && ResearchNoteState.solved(note)
                && minecraft != null && minecraft.player != null
                && PlayerThaumData.hasResearch(minecraft.player, "RESEARCHDUPE")) {
            ThaumcraftNetwork.requestResearchTableActionFromClient(
                    menu.blockPos(), TC4ResearchTableParity.ACTION_COPY_COMPLETED_NOTE);
            return true;
        }

        if (button == 0) {
            // GuiResearchTable 4.2.3.5 clears either selected component when
            // its 16x16 well is clicked. The earlier rebuild trapped aspects
            // in the wells until another drag replaced them.
            if (firstAspect != null && inside(localX, localY,
                    TC4ResearchTableParity.COMBINE_LEFT_X,
                    TC4ResearchTableParity.COMBINE_Y, 16, 16)) {
                firstAspect = null;
                updateCombinationPreview();
                return true;
            }
            if (secondAspect != null && inside(localX, localY,
                    TC4ResearchTableParity.COMBINE_RIGHT_X,
                    TC4ResearchTableParity.COMBINE_Y, 16, 16)) {
                secondAspect = null;
                updateCombinationPreview();
                return true;
            }

            List<Aspect> known = knownAspects();
            if (TC4ResearchTableParity.isPreviousAspectPageHit(localX, localY)
                    && aspectPage > 0) {
                aspectPage--;
                return true;
            }
            if (TC4ResearchTableParity.isNextAspectPageHit(localX, localY)
                    && aspectPage < TC4ResearchTableParity.lastAspectPage(known.size())) {
                aspectPage++;
                return true;
            }
            if (TC4ResearchTableParity.isCombineArrowHit(localX, localY)) {
                if (firstAspect != null && secondAspect != null) {
                    combineFlashUntilNanos = System.nanoTime() + 200_000_000L;
                    ThaumcraftNetwork.requestCombineAspectsFromClient(
                            firstAspect.id(), secondAspect.id());
                }
                return true;
            }

            Aspect knownPaletteAspect = knownPaletteAspectAt(mouseX, mouseY);
            if (knownPaletteAspect != null && hasShiftDown() && hasResearchMastery()
                    && !knownPaletteAspect.isPrimal()) {
                Aspect[] components = AspectCombinationRegistry.decompose(knownPaletteAspect).orElse(null);
                if (components != null) {
                    combineFlashUntilNanos = System.nanoTime() + 200_000_000L;
                    ThaumcraftNetwork.requestCombineAspectsFromClient(
                            components[0].id(), components[1].id());
                }
                return true;
            }

            Aspect paletteAspect = knownPaletteAspect != null && availableAspect(knownPaletteAspect) > 0
                    ? knownPaletteAspect : null;
            if (paletteAspect != null) {
                draggedAspect = paletteAspect;
                draggingAspect = true;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button,
                                double dragX, double dragY) {
        if (button == 0 && draggingAspect) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingAspect) {
            Aspect released = draggedAspect;
            draggedAspect = null;
            draggingAspect = false;
            if (released == null) {
                return true;
            }

            double localX = mouseX - leftPos;
            double localY = mouseY - topPos;
            if (inside(localX, localY,
                    TC4ResearchTableParity.COMBINE_LEFT_X,
                    TC4ResearchTableParity.COMBINE_Y, 18, 18)) {
                firstAspect = released;
                updateCombinationPreview();
                return true;
            }
            if (inside(localX, localY,
                    TC4ResearchTableParity.COMBINE_RIGHT_X,
                    TC4ResearchTableParity.COMBINE_Y, 18, 18)) {
                secondAspect = released;
                updateCombinationPreview();
                return true;
            }

            java.util.Optional<ResearchNoteGrid.GridSlot> hit = ResearchNoteGrid.hitTest(
                    (int) Math.round(localX), (int) Math.round(localY),
                    ClientResearchNoteData.radius());
            if (hit.isPresent() && ClientResearchNoteData.activeAt(hit.get().index())
                    && canClientPlace(hit.get().index(), released)) {
                ThaumcraftNetwork.requestPlaceResearchNoteAspectFromClient(
                        hit.get().index(), released.id());
                return true;
            }

            // TC4 treats dropping the dragged icon back onto the same palette
            // icon as a quick way to fill the first free combination well.
            Aspect paletteAspect = paletteAspectAt(mouseX, mouseY);
            if (paletteAspect == released) {
                selectAspectForCombination(released);
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void selectAspectForCombination(Aspect aspect) {
        if (aspect == null) {
            return;
        }
        if (firstAspect == null) {
            firstAspect = aspect;
        } else if (secondAspect == null) {
            secondAspect = aspect;
        } else {
            firstAspect = aspect;
            secondAspect = null;
        }
        updateCombinationPreview();
    }

    private void updateCombinationPreview() {
        previewAspect = firstAspect != null && secondAspect != null
                ? AspectCombinationRegistry.combine(firstAspect, secondAspect).orElse(null)
                : null;
    }

    private Aspect knownPaletteAspectAt(double mouseX, double mouseY) {
        List<Aspect> known = knownAspects();
        int start = TC4ResearchTableParity.aspectPageStart(aspectPage);
        int end = Math.min(start + ASPECTS_PER_PAGE, known.size());
        for (int i = start; i < end; i++) {
            int local = i - start;
            if (TC4ResearchTableParity.isAspectIconHit(
                    mouseX - leftPos, mouseY - topPos, local)) {
                return known.get(i);
            }
        }
        return null;
    }

    private Aspect paletteAspectAt(double mouseX, double mouseY) {
        Aspect aspect = knownPaletteAspectAt(mouseX, mouseY);
        return aspect != null && availableAspect(aspect) > 0 ? aspect : null;
    }

    private void renderDraggedAspect(PoseStack poseStack, int mouseX, int mouseY) {
        if (draggingAspect && draggedAspect != null) {
            drawAspectOrb(poseStack, draggedAspect, mouseX - 9, mouseY - 9, 0.92F);
        }
    }

    private boolean canClientPlace(int slot, Aspect aspect) {
        return hasResearchNote(currentNote())
                && !ClientResearchNoteData.solved()
                && aspect != null
                && availableAspect(aspect) > 0
                && ClientResearchNoteData.emptyAt(slot)
                && ClientResearchNoteData.aspectAt(slot) == null
                && touchesCompatibleClientNeighbor(slot, aspect);
    }

    private boolean touchesCompatibleClientNeighbor(int slot, Aspect aspect) {
        for (int neighbor : ResearchNoteGrid.neighbors(slot)) {
            Aspect other = ClientResearchNoteData.aspectAt(neighbor);
            if (other != null && ResearchAspectGraph.canConnect(aspect, other)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasResearchExpertise() {
        return minecraft != null && minecraft.player != null
                && (PlayerThaumData.hasResearch(minecraft.player, "RESEARCHER1")
                || PlayerThaumData.hasResearch(minecraft.player, "RESEARCHER2"));
    }

    private boolean hasResearchMastery() {
        return minecraft != null && minecraft.player != null
                && PlayerThaumData.hasResearch(minecraft.player, "RESEARCHER2");
    }

    private ItemStack currentNote() {
        return menu.tableStack(ResearchTableBlockEntity.SLOT_RESEARCH_NOTE);
    }

    private static boolean hasResearchNote(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ResearchNoteItem;
    }

    private static int noteSignature(ItemStack stack) {
        if (!hasResearchNote(stack)) {
            return 0;
        }
        int result = 31 + stack.getCount();
        if (stack.hasTag()) {
            result = 31 * result + stack.getTag().hashCode();
        }
        return result;
    }

    private static boolean isHexHovered(double mouseX, double mouseY, int x, int y) {
        double dx = mouseX - x;
        double dy = mouseY - y;
        return dx * dx + dy * dy <= TC4ResearchTableParity.NOTE_HEX_HIT_RADIUS_SQ;
    }

    private static boolean inside(double mouseX, double mouseY,
                                  int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width
                && mouseY >= y && mouseY < y + height;
    }

    /** Three-pixel additive-looking cyan line used by the original puzzle. */
    private void drawTc4Connection(PoseStack poseStack, int x1, int y1, int x2, int y2) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        if (steps <= 0) {
            return;
        }
        int ticks = minecraft != null && minecraft.player != null
                ? minecraft.player.tickCount : 0;
        float pulse = 0.60F + (float) Math.sin(ticks + x1) * 0.30F;
        int alpha = Math.max(0, Math.min(255, Math.round(pulse * 255.0F)));
        int color = (alpha << 24) | 0x0099CC;
        double dx = x2 - x1;
        double dy = y2 - y1;
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
}
