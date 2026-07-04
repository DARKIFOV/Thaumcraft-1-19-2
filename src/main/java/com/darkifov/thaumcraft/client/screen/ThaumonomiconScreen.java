package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.ClientResearchData;
import com.darkifov.thaumcraft.client.book.ThaumonomiconRecipePage;
import com.darkifov.thaumcraft.client.book.ThaumonomiconRecipeRegistry;
import com.darkifov.thaumcraft.research.ResearchEntry;
import com.darkifov.thaumcraft.research.ResearchRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ThaumonomiconScreen extends Screen {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/gui/thaumonomicon.png");

    private int researchPage = 0;
    private int recipePage = 0;
    private boolean recipeMode = false;

    private final int imageWidth = 256;
    private final int imageHeight = 256;
    private int leftPos;
    private int topPos;

    public ThaumonomiconScreen() {
        super(Component.literal("Thaumonomicon"));
    }

    @Override
    protected void init() {
        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;

        addRenderableWidget(new Button(leftPos + 24, topPos + 232, 54, 16, Component.literal("< Back"), button -> {
            if (recipeMode) {
                recipePage = Math.max(0, recipePage - 1);
            } else {
                researchPage = Math.max(0, researchPage - 1);
            }
        }));

        addRenderableWidget(new Button(leftPos + 178, topPos + 232, 54, 16, Component.literal("Next >"), button -> {
            if (recipeMode) {
                recipePage = Math.min(ThaumonomiconRecipeRegistry.size() - 1, recipePage + 1);
            } else {
                researchPage = Math.min(ResearchRegistry.size() - 1, researchPage + 1);
            }
        }));

        addRenderableWidget(new Button(leftPos + 92, topPos + 232, 70, 16, Component.literal("Recipes"), button -> {
            recipeMode = !recipeMode;
            button.setMessage(Component.literal(recipeMode ? "Research" : "Recipes"));
        }));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        drawCenteredString(poseStack, font, Component.literal("Thaumonomicon").withStyle(ChatFormatting.GOLD), width / 2, topPos + 16, 0xFFE8A54F);

        if (recipeMode) {
            renderRecipePage(poseStack);
        } else {
            renderResearchPage(poseStack);
        }

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    private void renderResearchPage(PoseStack poseStack) {
        ResearchEntry entry = ResearchRegistry.get(researchPage);
        boolean unlocked = ClientResearchData.hasResearch(entry.key());

        font.draw(poseStack, Component.literal("Research " + (researchPage + 1) + "/" + ResearchRegistry.size()).withStyle(ChatFormatting.DARK_PURPLE), leftPos + 38, topPos + 38, 0x5A2D75);

        int statusColor = unlocked ? 0x1F7A38 : 0x8A1F1F;
        String statusText = unlocked ? "UNLOCKED" : "LOCKED";
        font.draw(poseStack, Component.literal(statusText), leftPos + 158, topPos + 38, statusColor);

        font.draw(poseStack, Component.literal(entry.title()).withStyle(unlocked ? ChatFormatting.DARK_AQUA : ChatFormatting.DARK_RED), leftPos + 38, topPos + 58, unlocked ? 0x245A6E : 0x7A1F1F);

        if (unlocked) {
            font.drawWordWrap(Component.literal(entry.description()), leftPos + 38, topPos + 78, 170, 0x3D2B1F);
        } else {
            font.drawWordWrap(Component.literal("This research is locked. Use the Research Table and Research Points to unlock new knowledge."), leftPos + 38, topPos + 78, 170, 0x6A2A2A);
        }

        int y = topPos + 142;
        font.draw(poseStack, Component.literal("Requirements:").withStyle(ChatFormatting.DARK_PURPLE), leftPos + 38, y, 0x5A2D75);
        y += 12;

        if (entry.requirements().length == 0) {
            font.draw(poseStack, Component.literal("- none").withStyle(ChatFormatting.GRAY), leftPos + 46, y, 0x555555);
        } else {
            for (String req : entry.requirements()) {
                boolean reqUnlocked = ClientResearchData.hasResearch(req);
                int color = reqUnlocked ? 0x3D7A35 : 0x9A2B2B;
                font.draw(poseStack, Component.literal("- " + req + (reqUnlocked ? " ✓" : " ✗")), leftPos + 46, y, color);
                y += 11;
            }
        }

        font.draw(poseStack, Component.literal("Unlocked: " + ClientResearchData.research().size() + " | Warp: " + ClientResearchData.warp()).withStyle(ChatFormatting.DARK_GRAY), leftPos + 42, topPos + 210, 0x6E5A42);
    }

    private void renderRecipePage(PoseStack poseStack) {
        ThaumonomiconRecipePage page = ThaumonomiconRecipeRegistry.get(recipePage);

        if (page == null) {
            font.draw(poseStack, "No recipe pages.", leftPos + 38, topPos + 58, 0x6A2A2A);
            return;
        }

        boolean unlocked = ClientResearchData.hasResearch(page.research());

        font.draw(poseStack, Component.literal("Recipe " + (recipePage + 1) + "/" + ThaumonomiconRecipeRegistry.size()).withStyle(ChatFormatting.DARK_PURPLE), leftPos + 38, topPos + 38, 0x5A2D75);
        font.draw(poseStack, Component.literal(unlocked ? "AVAILABLE" : "LOCKED"), leftPos + 158, topPos + 38, unlocked ? 0x1F7A38 : 0x8A1F1F);

        font.draw(poseStack, Component.literal(page.title()).withStyle(unlocked ? ChatFormatting.DARK_AQUA : ChatFormatting.DARK_RED), leftPos + 38, topPos + 56, unlocked ? 0x245A6E : 0x7A1F1F);
        font.draw(poseStack, Component.literal(page.category()).withStyle(ChatFormatting.DARK_PURPLE), leftPos + 38, topPos + 70, 0x5A2D75);

        if (!unlocked) {
            font.drawWordWrap(Component.literal("Required research: " + page.research()), leftPos + 38, topPos + 90, 170, 0x8A1F1F);
            font.drawWordWrap(Component.literal("Unlock the research to view the full recipe details."), leftPos + 38, topPos + 114, 170, 0x6A2A2A);
            return;
        }

        int y = topPos + 88;
        font.draw(poseStack, Component.literal("Catalyst: " + page.catalyst()).withStyle(ChatFormatting.DARK_AQUA), leftPos + 38, y, 0x245A6E);
        y += 14;

        font.draw(poseStack, Component.literal("Ingredients:").withStyle(ChatFormatting.DARK_PURPLE), leftPos + 38, y, 0x5A2D75);
        y += 11;

        for (String ingredient : page.ingredients()) {
            font.draw(poseStack, Component.literal("- " + ingredient), leftPos + 46, y, 0x3D2B1F);
            y += 10;
            if (y > topPos + 158) {
                break;
            }
        }

        if (page.aspects().length > 0 && y < topPos + 174) {
            y += 4;
            font.draw(poseStack, Component.literal("Aspects:").withStyle(ChatFormatting.DARK_PURPLE), leftPos + 38, y, 0x5A2D75);
            y += 11;

            for (String aspect : page.aspects()) {
                font.draw(poseStack, Component.literal("- " + aspect), leftPos + 46, y, 0x3D2B1F);
                y += 10;
                if (y > topPos + 188) {
                    break;
                }
            }
        }

        font.draw(poseStack, Component.literal("Result: " + page.result()).withStyle(ChatFormatting.GOLD), leftPos + 38, topPos + 194, 0xA56A1D);
        font.drawWordWrap(Component.literal(page.note()), leftPos + 38, topPos + 208, 174, 0x5A4632);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
