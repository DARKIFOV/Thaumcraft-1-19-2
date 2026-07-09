package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.client.ClientResearchData;
import com.darkifov.thaumcraft.research.ResearchEntry;
import com.darkifov.thaumcraft.recipe.TC4RecipeRuntimeBridge;
import com.darkifov.thaumcraft.recipe.TC4RecipeRequirementIndex;
import com.darkifov.thaumcraft.porting.TC4ResearchItems;
import com.mojang.blaze3d.vertex.PoseStack;
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
        // Stage343-362: no modern Button widgets inside the book.
        // Click regions are handled manually below, matching the original TC4
        // page/back hotzones on top of the copied book texture.
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
        // Stage623-642: original Thaumonomicon pages do not print raw
        // ConfigResearch keys/categories in the header. Keep those values only in
        // NBT/audit data; the visible book remains TC4-style parchment.
        drawCenteredString(poseStack, font, Component.literal((pageIndex + 1) + "-" + Math.min(totalPages(), pageIndex + 2) + " / " + totalPages()), leftPos + BOOK_SIZE / 2, topPos + BOOK_SIZE - 58, 0x6D4A22);

        renderPage(poseStack, leftPos + 42, topPos + 70, pageIndex);
        renderPage(poseStack, leftPos + 286, topPos + 70, pageIndex + 1);
        renderAspects(poseStack, leftPos + 42, topPos + 424);
        renderOriginalNavigationHotspots(poseStack, mouseX, mouseY);

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
                    return new PageRef(type, recipeIndex < entry.recipeKeys().length ? entry.recipeKeys()[recipeIndex] : "RESEARCH:" + entry.key(), true);
                }
                return new PageRef(type, textIndex < entry.pageTextKeys().length ? entry.pageTextKeys()[textIndex] : "missing_text_" + textIndex, false);
            }
            if (recipe) recipeIndex++; else textIndex++;
        }
        return new PageRef("TEXT", "", false);
    }

    private boolean isRecipeType(String type) {
        String upper = type == null ? "" : type.toUpperCase();
        return upper.contains("CRAFT")
                || upper.contains("RECIPE")
                || upper.contains("INFUSION")
                || upper.contains("CRUCIBLE")
                || upper.contains("SMELT")
                || upper.contains("COMPOUND")
                || upper.contains("ITEMSTACK_PAGE");
    }

    private void renderTextPage(PoseStack poseStack, int x, int y, String type, String key) {
        String gate = gatedResearchKey(type);
        // Original TC4 book pages do not display raw adapter page type strings such as TEXT_RESEARCH_GATED.

        if (!gate.isBlank() && !ClientResearchData.hasResearch(gate)) {
            fill(poseStack, x, y, x + 190, y + 74, 0x221B0E08);
            drawCenteredString(poseStack, font, Component.literal("Locked"), x + 95, y + 24, 0x5A3515);
            return;
        }

        String text = TC4ResearchText.pageText(key);
        if (text.isBlank() || text.equals(key)) {
            text = "";
        }
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
        // Recipe page type is kept in data for gates/audits, but not rendered as a raw label in the book.

        String requiredResearch = TC4RecipeRequirementIndex.requiredResearchFor(recipeKey, entry.key());
        if (!requiredResearch.isBlank() && !ClientResearchData.hasResearch(requiredResearch)) {
            drawCenteredString(poseStack, font, Component.literal("Locked"), x + 95, y + 32, 0x5A3515);
            return;
        }

        if (type != null && type.toUpperCase().contains("ITEMSTACK_PAGE")) {
            renderResolvedItemIcon(poseStack, recipeKey, x + 87, y + 42);
            return;
        }

        TC4RecipeRuntimeBridge.OriginalRecipe recipe = TC4RecipeRuntimeBridge.byKey(recipeKey);
        if (recipe == null && recipeKey != null) {
            String researchKey = recipeKey.startsWith("RESEARCH:") ? recipeKey.substring("RESEARCH:".length()) : recipeKey;
            List<TC4RecipeRuntimeBridge.OriginalRecipe> byResearch = TC4RecipeRuntimeBridge.byResearch(researchKey);
            if (!byResearch.isEmpty()) {
                recipe = byResearch.get(0);
            }
        }

        if (recipe == null) {
            return;
        }

        // Stage623-642: visible Thaumonomicon recipe pages should look like
        // original TC4 book recipe cards, not like a debug dump of internal
        // ConfigRecipes keys/fields.  Keep the full key/research/catalyst data
        // in recipe objects for gates/audits, but draw only the item/aspect
        // layout in the book.
        renderResolvedItemIcon(poseStack, recipe.catalystExpression(), x + 54, y + 28);
        renderResolvedItemIcon(poseStack, recipe.resultExpression(), x + 118, y + 28);
        drawString(poseStack, font, "→", x + 92, y + 32, 0x6D4A22);
        renderRecipeVisuals(poseStack, x + 10, y + 52, recipe);
    }


    private void renderRecipeVisuals(PoseStack poseStack, int x, int y, TC4RecipeRuntimeBridge.OriginalRecipe recipe) {
        if (recipe.pattern().length > 0) {
            Map<Character, String> symbolMap = inferredOriginalPatternMap(recipe);
            for (int row = 0; row < Math.min(3, recipe.pattern().length); row++) {
                String line = recipe.pattern()[row];
                for (int col = 0; col < Math.min(3, line.length()); col++) {
                    int sx = x + col * 18;
                    int sy = y + row * 18;
                    char symbol = line.charAt(col);
                    if (symbol != ' ') {
                        String expression = symbolMap.get(symbol);
                        if (expression != null && !expression.isBlank()) {
                            renderResolvedItemIcon(poseStack, expression, sx, sy);
                        }
                        // Stage623-642: do not paint inferred pattern letters
                        // over item icons; those were adapter debugging marks.
                    }
                }
            }
        } else if (recipe.components().length > 0) {
            int limit = Math.min(8, recipe.components().length);
            for (int i = 0; i < limit; i++) {
                int sx = x + (i % 4) * 20;
                int sy = y + (i / 4) * 20;
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
        if (upper.contains("COMPOUND")) return "Compound TC4 Recipe";
        if (upper.contains("ITEMSTACK_PAGE")) return "Original ItemStack Page";
        return "Crafting Recipe";
    }

    private String pageTypeLabel(String type) {
        if (type == null || type.isBlank()) {
            return "TEXT";
        }
        int delimiter = type.indexOf(':');
        if (delimiter > 0) {
            return type.substring(0, delimiter);
        }
        return type;
    }

    private String gatedResearchKey(String type) {
        if (type == null) {
            return "";
        }
        String prefix = "TEXT_RESEARCH_GATED:";
        return type.startsWith(prefix) ? type.substring(prefix.length()) : "";
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
        // Stage643-662: warp remains preserved in ResearchEntry/NBT/requirements,
        // but the visible Thaumonomicon page no longer prints a raw adapter
        // "Warp: N" label over the copied TC4 book texture.
    }

    private void renderOriginalNavigationHotspots(PoseStack poseStack, int mouseX, int mouseY) {
        int navY = topPos + BOOK_SIZE - 44;
        int backX = leftPos + 30;
        int prevX = leftPos + 190;
        int nextX = leftPos + BOOK_SIZE - 238;
        int color = 0x5A3515;
        drawString(poseStack, font, "Back", backX + 10, navY + 6, color);
        drawCenteredString(poseStack, font, Component.literal("‹"), prevX + 24, navY + 6, pageIndex <= 0 ? 0xAA6D4A22 : color);
        drawCenteredString(poseStack, font, Component.literal("›"), nextX + 24, navY + 6, pageIndex >= maxFirstPage() ? 0xAA6D4A22 : color);
        if (inside(mouseX, mouseY, backX, navY, 58, 18)
                || inside(mouseX, mouseY, prevX, navY, 48, 18)
                || inside(mouseX, mouseY, nextX, navY, 48, 18)) {
            fill(poseStack, mouseX - 4, mouseY - 4, mouseX + 4, mouseY + 4, 0x55F0DDAA);
        }
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int navY = topPos + BOOK_SIZE - 44;
        if (inside(mouseX, mouseY, leftPos + 30, navY, 58, 18)) {
            minecraft.setScreen(parent);
            return true;
        }
        if (inside(mouseX, mouseY, leftPos + 190, navY, 48, 18)) {
            pageIndex = Math.max(0, pageIndex - 2);
            return true;
        }
        if (inside(mouseX, mouseY, leftPos + BOOK_SIZE - 238, navY, 48, 18)) {
            pageIndex = Math.min(maxFirstPage(), pageIndex + 2);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
