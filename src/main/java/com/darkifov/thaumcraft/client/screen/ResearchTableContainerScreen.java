package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.block.ResearchNoteItem;
import com.darkifov.thaumcraft.block.ScribingToolsItem;
import com.darkifov.thaumcraft.blockentity.ResearchTableBlockEntity;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.menu.ResearchTableMenu;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.ResearchNoteState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

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
        addRenderableWidget(new Button(leftPos + 101, topPos + 20, 42, 18,
                Component.literal("New"), button -> ThaumcraftNetwork.requestResearchTableActionFromClient(menu.blockPos(), 0)));
        addRenderableWidget(new Button(leftPos + 145, topPos + 20, 42, 18,
                Component.literal("Open"), button -> ThaumcraftNetwork.requestResearchTableActionFromClient(menu.blockPos(), 1)));
        addRenderableWidget(new Button(leftPos + COPY_X, topPos + COPY_Y, 24, 20,
                Component.literal("Copy"), button -> ThaumcraftNetwork.requestResearchTableActionFromClient(menu.blockPos(), 5)));
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        OriginalGuiTextures.blitOriginal(poseStack, leftPos, topPos, OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL, BG_WIDTH, BG_HEIGHT);

        // Stage167 visual parity markers: these sit exactly under the two original TC4 table slots.
        fill(poseStack, leftPos + 13, topPos + 9, leftPos + 32, topPos + 28, 0x553F2612);
        fill(poseStack, leftPos + 69, topPos + 9, leftPos + 88, topPos + 28, 0x553F2612);

        ItemStack note = menu.tableStack(ResearchTableBlockEntity.SLOT_RESEARCH_NOTE);
        if (note.getItem() instanceof ResearchNoteItem && ResearchNoteState.solved(note) && minecraft != null && minecraft.player != null && PlayerThaumData.hasResearch(minecraft.player, "RESEARCHDUPE")) {
            // Original TC4 draws the copy icon at x+37,y+5 when RESEARCHDUPE and note complete are true.
            OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos + COPY_X, topPos + COPY_Y,
                    OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL, 232, 200, 24, 24, 256, 256);
        }
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
        ItemStack tools = menu.tableStack(ResearchTableBlockEntity.SLOT_SCRIBING_TOOLS);
        int ink = tools.getItem() instanceof ScribingToolsItem ? ScribingToolsItem.inkLeft(tools) : 0;
        font.draw(poseStack, Component.literal("Ink " + ink), 100, 52, ink > 0 ? 0x145A6D : 0x7A2222);

        ItemStack note = menu.tableStack(ResearchTableBlockEntity.SLOT_RESEARCH_NOTE);
        if (note.getItem() instanceof ResearchNoteItem) {
            ResearchNoteState.initialize(note, ResearchNoteState.target(note));
            String target = ResearchNoteState.target(note).isBlank() ? "unbound" : ResearchNoteState.target(note);
            font.draw(poseStack, Component.literal(target), 100, 66, 0x3F2612);
            font.draw(poseStack, Component.literal("copies " + ResearchNoteState.copyCount(note)), 100, 78, 0x5A3515);
        }

        String bonus = bonusSummary();
        font.draw(poseStack, Component.literal("Bonus " + bonus), 100, 92, bonus.equals("none") ? 0x7A6A55 : 0x145A6D);
    }

    private String bonusSummary() {
        StringBuilder builder = new StringBuilder();
        for (java.util.Map.Entry<Aspect, Integer> entry : menu.tableBonusAspects().entries().entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(entry.getKey().id().substring(0, 1).toUpperCase()).append(':').append(entry.getValue());
        }
        return builder.isEmpty() ? "none" : builder.toString();
    }

    private void renderTableFeedback(PoseStack poseStack, int mouseX, int mouseY) {
        ItemStack tools = menu.tableStack(ResearchTableBlockEntity.SLOT_SCRIBING_TOOLS);
        ItemStack note = menu.tableStack(ResearchTableBlockEntity.SLOT_RESEARCH_NOTE);
        boolean hasNote = note.getItem() instanceof ResearchNoteItem;
        boolean hasInk = tools.getItem() instanceof ScribingToolsItem && ScribingToolsItem.hasInk(tools);

        if (hasNote && !hasInk) {
            renderTooltip(poseStack, Component.literal("No ink in Scribing Tools"), leftPos + 157, topPos + 84);
        }

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
