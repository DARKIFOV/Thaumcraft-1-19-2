package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.client.ClientResearchData;
import com.darkifov.thaumcraft.client.arcane.ClientArcaneRecipePage;
import com.darkifov.thaumcraft.client.arcane.ClientArcaneRecipeRegistry;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ArcaneWorkbenchScreen extends Screen {
    private int page = 0;

    public ArcaneWorkbenchScreen() {
        super(Component.literal("Arcane Workbench"));
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int bottom = height / 2 + 82;

        addRenderableWidget(new Button(centerX - 110, bottom, 72, 20, Component.literal("< Back"), button -> {
            page = Math.max(0, page - 1);
        }));

        addRenderableWidget(new Button(centerX + 38, bottom, 72, 20, Component.literal("Next >"), button -> {
            page = Math.min(ClientArcaneRecipeRegistry.size() - 1, page + 1);
        }));

        addRenderableWidget(new Button(centerX - 44, bottom + 26, 88, 20, Component.literal("Craft"), button -> {
            ClientArcaneRecipePage recipe = ClientArcaneRecipeRegistry.get(page);

            if (recipe != null) {
                ThaumcraftNetwork.requestArcaneCraftFromClient(new ResourceLocation(recipe.id()));
            }
        }));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);

        int panelX = width / 2 - 136;
        int panelY = height / 2 - 106;

        fill(poseStack, panelX, panelY, panelX + 272, panelY + 212, 0xDD1C1423);
        fill(poseStack, panelX + 5, panelY + 5, panelX + 267, panelY + 207, 0xDDE9D6AF);

        ClientArcaneRecipePage recipe = ClientArcaneRecipeRegistry.get(page);

        drawCenteredString(poseStack, font, Component.literal("Arcane Workbench").withStyle(ChatFormatting.GOLD), width / 2, panelY + 12, 0xE8A54F);

        if (recipe == null) {
            font.draw(poseStack, "No arcane recipes.", panelX + 20, panelY + 44, 0x7A1F1F);
            super.render(poseStack, mouseX, mouseY, partialTick);
            return;
        }

        boolean unlocked = ClientResearchData.hasResearch(recipe.research());

        font.draw(poseStack, "Recipe " + (page + 1) + "/" + ClientArcaneRecipeRegistry.size(), panelX + 18, panelY + 34, 0x4A2A11);
        font.draw(poseStack, unlocked ? "AVAILABLE" : "LOCKED", panelX + 188, panelY + 34, unlocked ? 0x247A2E : 0x9A2222);

        font.draw(poseStack, Component.literal(recipe.title()).withStyle(unlocked ? ChatFormatting.DARK_AQUA : ChatFormatting.DARK_RED), panelX + 18, panelY + 54, unlocked ? 0x245A6E : 0x7A1F1F);
        font.draw(poseStack, "Research: " + recipe.research(), panelX + 18, panelY + 70, 0x5A2D75);

        if (!unlocked) {
            font.drawWordWrap(Component.literal("This recipe is locked. Unlock the required research in the Research Table."), panelX + 18, panelY + 94, 230, 0x7A1F1F);
            super.render(poseStack, mouseX, mouseY, partialTick);
            return;
        }

        int y = panelY + 92;
        font.draw(poseStack, Component.literal("Catalyst slot: " + recipe.catalyst()).withStyle(ChatFormatting.DARK_AQUA), panelX + 18, y, 0x245A6E);
        y += 13;

        font.draw(poseStack, Component.literal("Wand slot / vis cost: " + recipe.visCost()).withStyle(ChatFormatting.DARK_PURPLE), panelX + 18, y, 0x5A2D75);
        y += 15;

        font.draw(poseStack, "Ingredients:", panelX + 18, y, 0x4A2A11);
        y += 12;

        for (String ingredient : recipe.ingredients()) {
            font.draw(poseStack, "- " + ingredient, panelX + 28, y, 0x3D2B1F);
            y += 10;
            if (y > panelY + 162) {
                break;
            }
        }

        font.draw(poseStack, Component.literal("Output preview: " + recipe.result()).withStyle(ChatFormatting.GOLD), panelX + 18, panelY + 174, 0xA56A1D);
        font.drawWordWrap(Component.literal(recipe.note()), panelX + 18, panelY + 188, 232, 0x5A4632);

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
