package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectColor;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.ClientAspectData;
import com.darkifov.thaumcraft.client.ClientResearchNoteData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.ResearchNoteGrid;
import com.darkifov.thaumcraft.research.ResearchAspectGraph;
import com.darkifov.thaumcraft.research.ResearchNoteRequirements;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ResearchNoteScreen extends Screen {
    private static final int BG_WIDTH = 256;
    private static final int BG_HEIGHT = 256;

    private int leftPos;
    private int topPos;
    private Aspect selectedAspect;
    private String status = "Select an aspect, then click a free node.";

    public ResearchNoteScreen() {
        super(Component.literal("Research Note"));
    }

    @Override
    protected void init() {
        leftPos = (width - BG_WIDTH) / 2;
        topPos = (height - BG_HEIGHT) / 2;

        addRenderableWidget(Button.builder(Component.literal("Solve"), button -> ThaumcraftNetwork.requestSolveResearchNoteFromClient())
                .bounds(leftPos + BG_WIDTH - 74, topPos + BG_HEIGHT - 28, 56, 18)
                .build());

        addRenderableWidget(Button.builder(Component.literal("×"), button -> onClose())
                .bounds(leftPos + 8, topPos + BG_HEIGHT - 28, 22, 18)
                .build());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        OriginalGuiTextures.blitOriginal(poseStack, leftPos, topPos, OriginalGuiTextures.RESEARCH_TABLE, BG_WIDTH, BG_HEIGHT);

        drawCenteredString(poseStack, font, Component.literal("Research Note"), leftPos + BG_WIDTH / 2, topPos + 8, 0x3F2612);
        drawString(poseStack, font, Component.literal("Target: " + (ClientResearchNoteData.target().isBlank() ? "available research" : ClientResearchNoteData.target())), leftPos + 18, topPos + 26, 0x3F2612);
        drawString(poseStack, font, Component.literal("Progress: " + ClientResearchNoteData.progress() + "%"), leftPos + 18, topPos + 38, 0x3F2612);
        drawString(poseStack, font, Component.literal("Required aspects are marked in gold."), leftPos + 18, topPos + 50, 0x5A3515);
        drawString(poseStack, font, Component.literal(ClientResearchNoteData.solved() ? "Solved" : status), leftPos + 18, topPos + 222, ClientResearchNoteData.solved() ? 0x287A28 : 0x5A3515);

        renderLinks(poseStack);
        renderGrid(poseStack, mouseX, mouseY);
        renderAspectPalette(poseStack, mouseX, mouseY);

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    private void renderGrid(PoseStack poseStack, int mouseX, int mouseY) {
        for (ResearchNoteGrid.GridSlot slot : ResearchNoteGrid.slots()) {
            int x = leftPos + ResearchNoteGrid.x(slot.index());
            int y = topPos + ResearchNoteGrid.y(slot.index());
            Aspect aspect = ClientResearchNoteData.aspectAt(slot.index());

            boolean required = aspect != null && ResearchNoteRequirements.requiredFor(ClientResearchNoteData.target()).contains(aspect);
            int slotColor = aspect == null ? 0xAA3F2612 : AspectColor.argb(aspect, required ? 245 : 210);
            fill(poseStack, x - 12, y - 12, x + 12, y + 12, required ? 0xFFFFCC55 : (aspect == null ? 0xAA3F2612 : AspectColor.dim(aspect, 185, 0.55F)));
            fill(poseStack, x - 10, y - 10, x + 10, y + 10, slotColor);

            if (aspect != null) {
                ResourceLocation texture = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + aspect.id() + ".png");
                OriginalGuiTextures.blitOriginal(poseStack, x - 8, y - 8, texture, 16, 16);
            }

            if (mouseX >= x - 10 && mouseX <= x + 10 && mouseY >= y - 10 && mouseY <= y + 10) {
                fill(poseStack, x - 12, y - 12, x + 12, y + 12, 0x33FFFFFF);
                renderTooltip(poseStack, Component.literal("Slot " + slot.index() + (aspect == null ? "" : " " + aspect.displayName())), mouseX, mouseY);
            }
        }
    }

    private void renderLinks(PoseStack poseStack) {
        for (ResearchNoteGrid.GridSlot slot : ResearchNoteGrid.slots()) {
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
                int lineColor = distance <= 1
                        ? AspectColor.mix(aspect, other, 220)
                        : distance <= 2 ? AspectColor.mix(aspect, other, 150) : 0xAA7A2222;
                fill(poseStack, Math.min(x, nx), Math.min(y, ny), Math.max(x, nx) + 1, Math.min(y, ny) + 1, lineColor);
            }
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
            ResourceLocation texture = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + aspect.id() + ".png");
            fill(poseStack, x - 2, y - 2, x + 18, y + 18, selectedAspect == aspect ? AspectColor.argb(aspect, 245) : AspectColor.dim(aspect, 170, 0.55F));
            fill(poseStack, x - 1, y - 1, x + 17, y + 17, 0xAA1D140C);
            OriginalGuiTextures.blitOriginal(poseStack, x, y, texture, 16, 16);

            if (mouseX >= x - 1 && mouseX <= x + 17 && mouseY >= y - 1 && mouseY <= y + 17) {
                renderTooltip(poseStack, Component.literal(aspect.displayName() + " pool:" + ClientAspectData.pool(aspect)), mouseX, mouseY);
            }

            shown++;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int startX = leftPos + 12;
        int startY = topPos + 58;
        int shown = 0;

        for (Aspect aspect : Aspect.values()) {
            if (!ClientAspectData.knows(aspect)) {
                continue;
            }

            int col = shown % 4;
            int row = shown / 4;
            int x = startX + col * 20;
            int y = startY + row * 16;

            if (mouseX >= x - 1 && mouseX <= x + 17 && mouseY >= y - 1 && mouseY <= y + 17) {
                selectedAspect = aspect;
                status = "Selected " + aspect.displayName() + ". Click a free node.";
                return true;
            }

            shown++;
        }

        for (ResearchNoteGrid.GridSlot slot : ResearchNoteGrid.slots()) {
            int x = leftPos + ResearchNoteGrid.x(slot.index());
            int y = topPos + ResearchNoteGrid.y(slot.index());

            if (mouseX >= x - 10 && mouseX <= x + 10 && mouseY >= y - 10 && mouseY <= y + 10) {
                if (selectedAspect == null) {
                    status = "Select an aspect first.";
                    return true;
                }

                ThaumcraftNetwork.requestPlaceResearchNoteAspectFromClient(slot.index(), selectedAspect.id());
                status = "Placement sent.";
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
