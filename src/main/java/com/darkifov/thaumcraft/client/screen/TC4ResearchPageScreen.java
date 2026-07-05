package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.research.ResearchEntry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * Stage118: TC4-style research entry pages now merge text pages and recipe
 * pages in the same order extracted from ConfigResearch.java. Recipe pages are
 * rendered as dedicated TC4 book cards instead of disappearing behind text-only
 * page indexing.
 */
public class TC4ResearchPageScreen extends Screen {
    private static final ResourceLocation BOOK = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/gui/thaumonomicon.png");
    private static final ResourceLocation OVERLAY = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/gui/gui_researchbook_overlay.png");
    private static final int BOOK_SIZE = 512;

    private final ThaumonomiconScreen parent;
    private final ResearchEntry entry;
    private int leftPos;
    private int topPos;
    private int pageIndex;

    public TC4ResearchPageScreen(ThaumonomiconScreen parent, ResearchEntry entry) {
        super(Component.literal(entry.title()));
        this.parent = parent;
        this.entry = entry;
    }

    @Override
    protected void init() {
        this.leftPos = (width - BOOK_SIZE) / 2;
        this.topPos = (height - BOOK_SIZE) / 2;
        clearWidgets();
        addRenderableWidget(new Button(leftPos + 28, topPos + BOOK_SIZE - 42, 58, 18, Component.literal("Back"), b -> minecraft.setScreen(parent)));
        addRenderableWidget(new Button(leftPos + 190, topPos + BOOK_SIZE - 42, 48, 18, Component.literal("<"), b -> pageIndex = Math.max(0, pageIndex - 2)));
        addRenderableWidget(new Button(leftPos + BOOK_SIZE - 238, topPos + BOOK_SIZE - 42, 48, 18, Component.literal(">"), b -> pageIndex = Math.min(maxFirstPage(), pageIndex + 2)));
    }

    private int totalPages() {
        if (entry.pageTypes().length > 0) {
            return Math.max(1, entry.pageTypes().length);
        }
        return Math.max(1, entry.pageTextKeys().length + entry.recipeKeys().length);
    }

    private int maxFirstPage() {
        int pages = totalPages();
        return Math.max(0, pages - 1 - (pages % 2 == 0 ? 1 : 0));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        OriginalGuiTextures.blitOriginal(poseStack, leftPos, topPos, BOOK, BOOK_SIZE, BOOK_SIZE);
        OriginalGuiTextures.blitOriginal(poseStack, leftPos, topPos, OVERLAY, BOOK_SIZE, BOOK_SIZE);

        drawCenteredString(poseStack, font, entry.title(), leftPos + BOOK_SIZE / 2, topPos + 22, 0x2D1B0B);
        drawString(poseStack, font, entry.key(), leftPos + 38, topPos + 44, 0x6D4A22);
        drawString(poseStack, font, entry.category(), leftPos + BOOK_SIZE - 130, topPos + 44, 0x6D4A22);

        renderPage(poseStack, leftPos + 42, topPos + 70, pageIndex);
        renderPage(poseStack, leftPos + 286, topPos + 70, pageIndex + 1);
        renderAspects(poseStack, leftPos + 42, topPos + 424);

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    private void renderPage(PoseStack poseStack, int x, int y, int idx) {
        if (idx >= totalPages()) {
            return;
        }
        PageRef page = pageAt(idx);
        if (page.recipe()) {
            renderRecipePage(poseStack, x, y, page.type(), page.value());
        } else {
            renderTextPage(poseStack, x, y, page.type(), page.value());
        }
    }

    private PageRef pageAt(int idx) {
        int textIndex = 0;
        int recipeIndex = 0;
        if (entry.pageTypes().length == 0) {
            if (idx < entry.pageTextKeys().length) return new PageRef("TEXT", entry.pageTextKeys()[idx], false);
            int recipe = idx - entry.pageTextKeys().length;
            return new PageRef("RECIPE", recipe < entry.recipeKeys().length ? entry.recipeKeys()[recipe] : "", true);
        }
        for (int i = 0; i <= idx && i < entry.pageTypes().length; i++) {
            String type = entry.pageTypes()[i];
            boolean recipe = isRecipeType(type);
            if (i == idx) {
                if (recipe) {
                    return new PageRef(type, recipeIndex < entry.recipeKeys().length ? entry.recipeKeys()[recipeIndex] : "missing_recipe_" + recipeIndex, true);
                }
                return new PageRef(type, textIndex < entry.pageTextKeys().length ? entry.pageTextKeys()[textIndex] : "missing_text_" + textIndex, false);
            }
            if (recipe) recipeIndex++; else textIndex++;
        }
        return new PageRef("TEXT", "", false);
    }

    private boolean isRecipeType(String type) {
        String upper = type == null ? "" : type.toUpperCase();
        return upper.contains("CRAFT") || upper.contains("RECIPE") || upper.contains("INFUSION") || upper.contains("CRUCIBLE") || upper.contains("SMELT");
    }

    private void renderTextPage(PoseStack poseStack, int x, int y, String type, String key) {
        String text = TC4ResearchText.pageText(key);
        if (text.isBlank() || text.equals(key)) {
            text = "TC4 page key: " + key;
        }
        drawString(poseStack, font, type, x, y, 0x7A4E1A);
        y += 14;
        for (FormattedCharSequence line : splitText(text, 190)) {
            font.draw(poseStack, line, x, y, 0x2D1B0B);
            y += 10;
            if (y > topPos + 390) {
                drawString(poseStack, font, "…", x + 178, y - 10, 0x2D1B0B);
                break;
            }
        }
    }

    private void renderRecipePage(PoseStack poseStack, int x, int y, String type, String recipeKey) {
        drawString(poseStack, font, type, x, y, 0x7A4E1A);
        y += 18;
        fill(poseStack, x, y, x + 190, y + 112, 0x22A06D2B);
        fill(poseStack, x + 6, y + 6, x + 184, y + 106, 0x22F5E0B8);
        drawCenteredString(poseStack, font, Component.literal(recipeTitle(type)), x + 95, y + 13, 0x2D1B0B);
        drawCenteredString(poseStack, font, Component.literal(recipeKey == null || recipeKey.isBlank() ? "missing recipe key" : recipeKey), x + 95, y + 36, 0x5A3515);
        drawCenteredString(poseStack, font, Component.literal("Original TC4 recipe page"), x + 95, y + 58, 0x6D4A22);
        drawCenteredString(poseStack, font, Component.literal("ConfigRecipes runtime port required"), x + 95, y + 74, 0x8A2D1B);
    }

    private String recipeTitle(String type) {
        String upper = type == null ? "" : type.toUpperCase();
        if (upper.contains("ARCANE")) return "Arcane Workbench Recipe";
        if (upper.contains("INFUSION")) return "Infusion Recipe";
        if (upper.contains("CRUCIBLE")) return "Crucible Recipe";
        if (upper.contains("SMELT")) return "Smelting Recipe";
        return "Crafting Recipe";
    }

    private List<FormattedCharSequence> splitText(String text, int width) {
        List<FormattedCharSequence> result = new ArrayList<>();
        for (String paragraph : text.split("\\n")) {
            if (paragraph.isBlank()) {
                result.add(Component.literal(" ").getVisualOrderText());
            } else {
                result.addAll(font.split(Component.literal(paragraph), width));
            }
        }
        return result;
    }

    private void renderAspects(PoseStack poseStack, int x, int y) {
        int i = 0;
        for (var aspect : entry.aspects().entrySet()) {
            if (i >= 10) break;
            ResourceLocation icon = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + aspect.getKey().toLowerCase() + ".png");
            OriginalGuiTextures.blitOriginal(poseStack, x + i * 21, y, icon, 16, 16);
            drawString(poseStack, font, String.valueOf(aspect.getValue()), x + i * 21 + 11, y + 10, 0x2D1B0B);
            i++;
        }
        if (entry.warp() > 0) {
            drawString(poseStack, font, "Warp: " + entry.warp(), x + 230, y + 4, 0x5A1A66);
        }
    }

    private record PageRef(String type, String value, boolean recipe) {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
