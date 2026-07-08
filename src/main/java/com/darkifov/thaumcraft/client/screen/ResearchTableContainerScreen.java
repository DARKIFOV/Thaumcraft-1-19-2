package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectColor;
import com.darkifov.thaumcraft.AspectCombinationRegistry;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.ClientAspectData;
import com.darkifov.thaumcraft.block.ResearchNoteItem;
import com.darkifov.thaumcraft.block.ScribingToolsItem;
import com.darkifov.thaumcraft.blockentity.ResearchTableBlockEntity;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.menu.ResearchTableMenu;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.ResearchNoteState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Stage167: closer 1.19.2 adapter for TC4 GuiResearchTable.
 *
 * Original TC4 uses guiresearchtable2.png at 255x255 with table slots at
 * (14,10) and (70,10), aspect palette at (10,40), copy icon at (37,5),
 * and player inventory beginning at (48,175). The modern container screen keeps
 * those original coordinates while routing actions through Forge networking.
 */
public class ResearchTableContainerScreen extends AbstractContainerScreen<ResearchTableMenu> {
    private static final int BG_WIDTH = 255;
    private static final int BG_HEIGHT = 255;
    private static final int COPY_X = 37;
    private static final int COPY_Y = 5;
    private static final int ASPECTS_PER_PAGE = 25;

    private int aspectPage;
    private Aspect firstAspect;
    private Aspect secondAspect;
    private Aspect previewAspect;

    public ResearchTableContainerScreen(ResearchTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = BG_WIDTH;
        this.imageHeight = BG_HEIGHT;
        this.inventoryLabelX = 48;
        this.inventoryLabelY = 165;
        this.titleLabelX = 8;
        this.titleLabelY = 4;
    }

    @Override
    protected void init() {
        super.init();
        // Stage205 hard parity reset / Stage206: has no rebuild controls; no rebuild controls are added here. Copy remains bound to
        // the original icon region when RESEARCHDUPE and a completed note are present.
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        OriginalGuiTextures.blitOriginal(poseStack, leftPos, topPos, OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL, BG_WIDTH, BG_HEIGHT);


        ItemStack note = menu.tableStack(ResearchTableBlockEntity.SLOT_RESEARCH_NOTE);
        if (note.getItem() instanceof ResearchNoteItem && ResearchNoteState.solved(note) && minecraft != null && minecraft.player != null && PlayerThaumData.hasResearch(minecraft.player, "RESEARCHDUPE")) {
            // Original TC4 draws the copy icon at x+37,y+5 when RESEARCHDUPE and note complete are true.
            OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos + COPY_X, topPos + COPY_Y,
                    OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL, 232, 200, 24, 24, 256, 256);
        }

        renderAspectPaletteLikeTC4(poseStack);
        renderCombinationSlotsLikeTC4(poseStack);
        renderPageArrowsLikeTC4(poseStack);
    }


    private void renderAspectPaletteLikeTC4(PoseStack poseStack) {
        List<Aspect> known = knownAspects();
        int start = aspectPage * ASPECTS_PER_PAGE;
        int end = Math.min(start + ASPECTS_PER_PAGE, known.size());
        for (int i = start; i < end; i++) {
            Aspect aspect = known.get(i);
            int local = i - start;
            int x = leftPos + 10 + (local % 5) * 18;
            int y = topPos + 40 + (local / 5) * 18;
            drawAspectIcon(poseStack, aspect, x, y, ClientAspectData.pool(aspect), aspect == firstAspect || aspect == secondAspect);
        }
    }

    private void renderCombinationSlotsLikeTC4(PoseStack poseStack) {
        drawAspectSlot(poseStack, firstAspect, leftPos + 13, topPos + 139);
        drawAspectSlot(poseStack, secondAspect, leftPos + 71, topPos + 139);
        boolean canCombine = firstAspect != null && secondAspect != null && previewAspect != null;
        OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos + 35, topPos + 139,
                OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL, 184, canCombine ? 184 : 168, 24, 16, 255, 255);
        if (previewAspect != null) {
            drawAspectIcon(poseStack, previewAspect, leftPos + 45, topPos + 139, 0, false);
        }
    }

    private void renderPageArrowsLikeTC4(PoseStack poseStack) {
        List<Aspect> known = knownAspects();
        if (aspectPage > 0) {
            OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos + 27, topPos + 121,
                    OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL, 184, 200, 16, 10, 255, 255);
        }
        if ((aspectPage + 1) * ASPECTS_PER_PAGE < known.size()) {
            OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos + 51, topPos + 121,
                    OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL, 200, 200, 16, 10, 255, 255);
        }
    }

    private void drawAspectSlot(PoseStack poseStack, Aspect aspect, int x, int y) {
        if (aspect != null) {
            drawAspectIcon(poseStack, aspect, x, y, ClientAspectData.pool(aspect), true);
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
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTableFeedback(poseStack, mouseX, mouseY);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        // Original TC4 keeps this screen visual: no rebuild text labels are drawn
        // across guiresearchtable2.png.
    }


    private void renderAspectTooltips(PoseStack poseStack, int mouseX, int mouseY) {
        List<Aspect> known = knownAspects();
        int start = aspectPage * ASPECTS_PER_PAGE;
        int end = Math.min(start + ASPECTS_PER_PAGE, known.size());
        for (int i = start; i < end; i++) {
            int local = i - start;
            int x = leftPos + 10 + (local % 5) * 18;
            int y = topPos + 40 + (local / 5) * 18;
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                Aspect aspect = known.get(i);
                renderTooltip(poseStack, Component.literal(aspect.displayName()), mouseX, mouseY);
                return;
            }
        }
        if (mouseX >= leftPos + 35 && mouseX < leftPos + 59 && mouseY >= topPos + 139 && mouseY < topPos + 155) {
            renderTooltip(poseStack, previewAspect == null
                    ? Component.literal("Select two discovered TC4 aspects")
                    : Component.literal(firstAspect.displayName() + " + " + secondAspect.displayName() + " = " + previewAspect.displayName()), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
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
                if (firstAspect != null && secondAspect != null && previewAspect != null) {
                    ThaumcraftNetwork.requestCombineAspectsFromClient(firstAspect.id(), secondAspect.id());
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
                    selectAspectForCombination(known.get(i));
                    return true;
                }
            }
        }

        if (button == 0
                && mouseX >= leftPos + COPY_X && mouseX <= leftPos + COPY_X + 24
                && mouseY >= topPos + COPY_Y && mouseY <= topPos + COPY_Y + 24) {
            ItemStack note = menu.tableStack(ResearchTableBlockEntity.SLOT_RESEARCH_NOTE);
            if (note.getItem() instanceof ResearchNoteItem && ResearchNoteState.solved(note)
                    && minecraft != null && minecraft.player != null
                    && PlayerThaumData.hasResearch(minecraft.player, "RESEARCHDUPE")) {
                ThaumcraftNetwork.requestResearchTableActionFromClient(menu.blockPos(), 5);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }


    private void selectAspectForCombination(Aspect aspect) {
        if (aspect == null) {
            return;
        }
        if (firstAspect == null || secondAspect != null) {
            firstAspect = aspect;
            secondAspect = null;
        } else {
            secondAspect = aspect;
        }
        previewAspect = firstAspect != null && secondAspect != null
                ? AspectCombinationRegistry.combine(firstAspect, secondAspect).orElse(null)
                : null;
    }

    private void renderTableFeedback(PoseStack poseStack, int mouseX, int mouseY) {
        ItemStack tools = menu.tableStack(ResearchTableBlockEntity.SLOT_SCRIBING_TOOLS);
        ItemStack note = menu.tableStack(ResearchTableBlockEntity.SLOT_RESEARCH_NOTE);
        boolean hasNote = note.getItem() instanceof ResearchNoteItem;
        boolean hasInk = tools.getItem() instanceof ScribingToolsItem && ScribingToolsItem.hasInk(tools);

        if (hasNote && !hasInk) {
            renderTooltip(poseStack, Component.literal("No ink in Scribing Tools"), leftPos + 157, topPos + 84);
        }

        renderAspectTooltips(poseStack, mouseX, mouseY);

        if (mouseX >= leftPos + COPY_X && mouseX <= leftPos + COPY_X + 24
                && mouseY >= topPos + COPY_Y && mouseY <= topPos + COPY_Y + 24) {
            if (hasNote && ResearchNoteState.solved(note)) {
                renderTooltip(poseStack, Component.literal("Copy completed research note: paper + ink sac + original research aspects"), mouseX, mouseY);
            } else {
                renderTooltip(poseStack, Component.literal("Copy appears only for completed research notes"), mouseX, mouseY);
            }
        }
    }
}
