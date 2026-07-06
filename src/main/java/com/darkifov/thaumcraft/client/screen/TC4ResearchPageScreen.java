package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.research.ResearchEntry;
import com.darkifov.thaumcraft.recipe.TC4RecipeRuntimeBridge;
import com.darkifov.thaumcraft.porting.TC4ResearchItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

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
        drawCenteredString(poseStack, font, Component.literal((pageIndex + 1) + "-" + Math.min(totalPages(), pageIndex + 2) + " / " + totalPages()), leftPos + BOOK_SIZE / 2, topPos + BOOK_SIZE - 58, 0x6D4A22);

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
        y += 16;
        fill(poseStack, x, y, x + 190, y + 150, 0x22A06D2B);
        fill(poseStack, x + 6, y + 6, x + 184, y + 144, 0x22F5E0B8);

        TC4RecipeRuntimeBridge.OriginalRecipe recipe = TC4RecipeRuntimeBridge.byKey(recipeKey);
        if (recipe == null && recipeKey != null) {
            List<TC4RecipeRuntimeBridge.OriginalRecipe> byResearch = TC4RecipeRuntimeBridge.byResearch(recipeKey);
            if (!byResearch.isEmpty()) {
                recipe = byResearch.get(0);
            }
        }

        if (recipe == null) {
            drawCenteredString(poseStack, font, Component.literal(recipeTitle(type)), x + 95, y + 14, 0x2D1B0B);
            drawCenteredString(poseStack, font, Component.literal(recipeKey == null || recipeKey.isBlank() ? "missing recipe key" : recipeKey), x + 95, y + 36, 0x5A3515);
            drawCenteredString(poseStack, font, Component.literal("No matching TC4 ConfigRecipes entry yet"), x + 95, y + 60, 0x8A2D1B);
            return;
        }

        int cursor = y + 10;
        drawCenteredString(poseStack, font, Component.literal(recipe.key()), x + 95, cursor, 0x2D1B0B);
        renderResolvedItemIcon(poseStack, recipe.catalystExpression(), x + 54, y + 28);
        renderResolvedItemIcon(poseStack, recipe.resultExpression(), x + 118, y + 28);
        drawString(poseStack, font, "→", x + 92, y + 32, 0x6D4A22);
        renderRecipeVisuals(poseStack, x + 10, y + 52, recipe);
        cursor += 94;
        cursor = drawRecipeField(poseStack, x + 10, cursor, "Type", recipe.typeLabel(), 168);
        cursor = drawRecipeField(poseStack, x + 10, cursor, "Research", recipe.research().isBlank() ? entry.key() : recipe.research(), 168);
        cursor = drawRecipeField(poseStack, x + 10, cursor, "Result", compactExpressionWithTc4Item(recipe.resultExpression()), 168);
        if (!recipe.catalystExpression().isBlank()) {
            cursor = drawRecipeField(poseStack, x + 10, cursor, "Catalyst", compactExpressionWithTc4Item(recipe.catalystExpression()), 168);
        }
        if (!recipe.instability().isBlank()) {
            cursor = drawRecipeField(poseStack, x + 10, cursor, "Instability", recipe.instability(), 168);
        }
        if (!recipe.patternText().isBlank()) {
            cursor = drawRecipeField(poseStack, x + 10, cursor, "Pattern", recipe.patternText(), 168);
        }
        if (!recipe.aspectText().equals("none")) {
            cursor = drawRecipeField(poseStack, x + 10, cursor, "Aspects", recipe.aspectText(), 168);
        }
        if (!recipe.componentText().equals("none")) {
            drawRecipeField(poseStack, x + 10, cursor, "Items", compactExpressionWithTc4Item(recipe.componentText()), 168);
        }
    }


    private void renderRecipeVisuals(PoseStack poseStack, int x, int y, TC4RecipeRuntimeBridge.OriginalRecipe recipe) {
        if (recipe.pattern().length > 0) {
            Map<Character, String> symbolMap = inferredOriginalPatternMap(recipe);
            for (int row = 0; row < Math.min(3, recipe.pattern().length); row++) {
                String line = recipe.pattern()[row];
                for (int col = 0; col < Math.min(3, line.length()); col++) {
                    int sx = x + col * 18;
                    int sy = y + row * 18;
                    fill(poseStack, sx - 1, sy - 1, sx + 17, sy + 17, 0x553F2612);
                    char symbol = line.charAt(col);
                    if (symbol != ' ') {
                        String expression = symbolMap.get(symbol);
                        if (expression != null && !expression.isBlank()) {
                            renderResolvedItemIcon(poseStack, expression, sx, sy);
                        }
                        drawString(poseStack, font, String.valueOf(symbol), sx + 10, sy + 8, 0x2D1B0B);
                    }
                }
            }
        } else if (recipe.components().length > 0) {
            int limit = Math.min(8, recipe.components().length);
            for (int i = 0; i < limit; i++) {
                int sx = x + (i % 4) * 20;
                int sy = y + (i / 4) * 20;
                fill(poseStack, sx - 1, sy - 1, sx + 17, sy + 17, 0x553F2612);
                renderResolvedItemIcon(poseStack, recipe.components()[i], sx, sy);
            }
        }
        renderAspectCostIcons(poseStack, x + 96, y, recipe);
    }

    private Map<Character, String> inferredOriginalPatternMap(TC4RecipeRuntimeBridge.OriginalRecipe recipe) {
        Map<Character, String> map = new LinkedHashMap<>();
        List<Character> symbols = new ArrayList<>();
        for (String row : recipe.pattern()) {
            for (int i = 0; i < row.length(); i++) {
                char symbol = row.charAt(i);
                if (symbol != ' ' && !symbols.contains(symbol)) {
                    symbols.add(symbol);
                }
            }
        }

        Character catalystSymbol = inferredOriginalCatalystSymbol(recipe.pattern(), symbols, recipe.components().length);
        int componentIndex = 0;
        for (Character symbol : symbols) {
            if (catalystSymbol != null && symbol.equals(catalystSymbol)) {
                map.put(symbol, recipe.catalystExpression());
                continue;
            }
            if (recipe.components().length == 1) {
                map.put(symbol, recipe.components()[0]);
            } else if (componentIndex < recipe.components().length) {
                map.put(symbol, recipe.components()[componentIndex++]);
            }
        }
        return map;
    }

    private Character inferredOriginalCatalystSymbol(String[] rows, List<Character> symbols, int componentCount) {
        if (symbols.isEmpty()) {
            return null;
        }
        if (componentCount == 0 && symbols.size() == 1) {
            return symbols.get(0);
        }
        if (symbols.size() == componentCount + 1 || (componentCount == 1 && symbols.size() == 2)) {
            if (rows.length > 1 && rows[1].length() > 1) {
                char center = rows[1].charAt(1);
                if (center != ' ' && countOriginalSymbol(rows, center) == 1) {
                    return center;
                }
            }
            Character rarest = null;
            int rarestCount = Integer.MAX_VALUE;
            for (Character symbol : symbols) {
                int count = countOriginalSymbol(rows, symbol);
                if (count < rarestCount) {
                    rarest = symbol;
                    rarestCount = count;
                }
            }
            return rarest;
        }
        return null;
    }

    private int countOriginalSymbol(String[] rows, char symbol) {
        int count = 0;
        for (String row : rows) {
            for (int i = 0; i < row.length(); i++) {
                if (row.charAt(i) == symbol) {
                    count++;
                }
            }
        }
        return count;
    }

    private void renderAspectCostIcons(PoseStack poseStack, int x, int y, TC4RecipeRuntimeBridge.OriginalRecipe recipe) {
        int i = 0;
        for (String cost : recipe.aspectCosts()) {
            if (i >= 6) {
                break;
            }
            String[] parts = cost.split(":");
            Aspect aspect = parts.length > 0 ? Aspect.byId(parts[0].trim()) : null;
            if (aspect != null) {
                ResourceLocation icon = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + aspect.id() + ".png");
                int sx = x + (i % 3) * 26;
                int sy = y + (i / 3) * 24;
                OriginalGuiTextures.blitOriginal(poseStack, sx, sy, icon, 16, 16);
                if (parts.length > 1) {
                    drawString(poseStack, font, parts[1].trim(), sx + 10, sy + 11, 0x2D1B0B);
                }
            }
            i++;
        }
    }

    private void renderResolvedItemIcon(PoseStack poseStack, String expression, int x, int y) {
        TC4ResearchItems.resolveLegacyExpression(expression).ifPresent(entry -> {
            Item item = ForgeRegistries.ITEMS.getValue(entry.registryName());

            if (item != null) {
                itemRenderer.renderAndDecorateItem(new ItemStack(item), x, y);
                fill(poseStack, x, y, x + 16, y + 16, 0x33FFFFFF);
            }
        });
    }

    private int drawRecipeField(PoseStack poseStack, int x, int y, String label, String value, int width) {
        drawString(poseStack, font, label + ":", x, y, 0x6D4A22);
        y += 10;
        int lines = 0;
        for (FormattedCharSequence line : splitText(value, width)) {
            font.draw(poseStack, line, x + 4, y, 0x2D1B0B);
            y += 9;
            lines++;
            if (lines >= 3) {
                drawString(poseStack, font, "…", x + width - 8, y - 9, 0x2D1B0B);
                break;
            }
        }
        return y + 2;
    }

    private String compactExpressionWithTc4Item(String expression) {
        String compact = compactExpression(expression);
        String resolved = TC4ResearchItems.resolveLegacyExpressionLabel(expression);
        if (!resolved.isBlank()) {
            return compact + " -> " + resolved;
        }
        return compact;
    }

    private String compactExpression(String expression) {
        if (expression == null || expression.isBlank()) {
            return "none";
        }
        String compact = expression
                .replace("new ItemStack", "ItemStack")
                .replace("ConfigItems.", "")
                .replace("ConfigBlocks.", "")
                .replace("Items.", "")
                .replace("Blocks.", "")
                .replace("Character.valueOf", "char");
        return compact.length() > 360 ? compact.substring(0, 360) + " …" : compact;
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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta < 0) {
            pageIndex = Math.min(maxFirstPage(), pageIndex + 2);
        } else if (delta > 0) {
            pageIndex = Math.max(0, pageIndex - 2);
        }

        return true;
    }

    private record PageRef(String type, String value, boolean recipe) {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
