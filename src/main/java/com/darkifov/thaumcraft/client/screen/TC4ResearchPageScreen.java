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
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.language.I18n;
import com.mojang.blaze3d.platform.Window;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stage118: TC4-style research entry pages now merge text pages and recipe
 * pages in the same order extracted from ConfigResearch.java. Recipe pages are
 * rendered as dedicated TC4 book cards instead of disappearing behind text-only
 * page indexing.
 */
public class TC4ResearchPageScreen extends Screen {
    private static final ResourceLocation BOOK = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/gui/thaumonomicon.png");
    // TC4 GuiResearchRecipe draws only the 256x181 upper-left region of the
    // 512 atlas, scaled to 130%. Drawing the complete 512 sheet caused the huge
    // clipped book and black bands shown in the v11.62.39 screenshots.
    private static final int BOOK_SOURCE_WIDTH = 256;
    private static final int BOOK_SOURCE_HEIGHT = 181;
    private static final int BOOK_DRAW_WIDTH = 333;
    private static final int BOOK_DRAW_HEIGHT = 235;
    private static final int PAGE_WIDTH = 120;
    private static final int PAGE_HEIGHT = 148;
    private static final int PAGE_TEXT_COLOR = 0x2D1B0B;
    private static final int PAGE_ACCENT_COLOR = 0x6D4A22;

    private final ThaumonomiconScreen parent;
    private final ResearchEntry entry;
    private int leftPos;
    private int topPos;
    private int pageIndex;
    private final List<BookHoverRegion> bookHoverRegions = new ArrayList<>();

    public TC4ResearchPageScreen(ThaumonomiconScreen parent, ResearchEntry entry) {
        super(researchNameComponent(entry));
        this.parent = parent;
        this.entry = entry;
    }

    private static Component researchNameComponent(ResearchEntry entry) {
        String key = "tc.research_name." + entry.key();
        return I18n.exists(key) ? Component.translatable(key) : Component.literal(entry.title());
    }

    @Override
    protected void init() {
        // Content coordinates intentionally use the unscaled 256x181 pane,
        // exactly like TC4. The background itself is expanded around this pane.
        this.leftPos = (width - BOOK_SOURCE_WIDTH) / 2;
        this.topPos = (height - BOOK_SOURCE_HEIGHT) / 2;
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
        bookHoverRegions.clear();

        int backgroundX = (width - BOOK_DRAW_WIDTH) / 2;
        int backgroundY = (height - BOOK_DRAW_HEIGHT) / 2;
        OriginalGuiTextures.blitOriginalScaledRegion(poseStack, backgroundX, backgroundY, BOOK,
                0, 0, BOOK_SOURCE_WIDTH, BOOK_SOURCE_HEIGHT,
                BOOK_DRAW_WIDTH, BOOK_DRAW_HEIGHT, 512, 512);

        // Original spread: two 120px content columns, separated by 32px.
        if (pageIndex == 0) {
            drawCenteredString(poseStack, font, researchNameComponent(entry), leftPos + 56, topPos - 6, PAGE_TEXT_COLOR);
        }
        renderPage(poseStack, leftPos + 4, topPos + 14, pageIndex);
        renderPage(poseStack, leftPos + 156, topPos + 14, pageIndex + 1);
        renderAspects(poseStack, leftPos + 158, topPos + 158);

        drawCenteredString(poseStack, font,
                Component.literal((pageIndex + 1) + "-" + Math.min(totalPages(), pageIndex + 2) + " / " + totalPages()),
                leftPos + BOOK_SOURCE_WIDTH / 2, topPos + 192, PAGE_ACCENT_COLOR);
        renderOriginalNavigationHotspots(poseStack, mouseX, mouseY);
        renderBookHoverTooltip(poseStack, mouseX, mouseY);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    private void renderPage(PoseStack poseStack, int x, int y, int idx) {
        if (idx >= totalPages()) {
            return;
        }
        PageRef page = pageAt(idx);
        // v11.62.6: hard clip every physical page.  This prevents TC4 legacy
        // IMG tags, item cards and long translated strings from bleeding across
        // the spine/right page like the broken screenshots showed.
        withPageScissor(x - 5, y - 8, PAGE_WIDTH + 10, PAGE_HEIGHT + 14, () -> {
            if (page.recipe()) {
                renderRecipePage(poseStack, x, y, page.type(), page.value());
            } else {
                renderTextPage(poseStack, x, y, page.type(), page.value());
            }
        });
    }

    private void withPageScissor(int x, int y, int w, int h, Runnable draw) {
        Window window = Minecraft.getInstance().getWindow();
        double scale = window.getGuiScale();
        int sx = Math.max(0, (int)Math.floor(x * scale));
        int sy = Math.max(0, (int)Math.floor((window.getGuiScaledHeight() - (y + h)) * scale));
        int sw = Math.max(1, (int)Math.ceil(w * scale));
        int sh = Math.max(1, (int)Math.ceil(h * scale));
        RenderSystem.enableScissor(sx, sy, sw, sh);
        try {
            draw.run();
        } finally {
            RenderSystem.disableScissor();
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
            drawCenteredString(poseStack, font, Component.translatable("thaumcraft.gui.research.locked"), x + PAGE_WIDTH / 2, y + 24, 0x5A3515);
            return;
        }

        String raw = TC4ResearchText.rawPageText(key);
        if (raw.isBlank() || raw.equals(key)) {
            return;
        }
        // v11.62.4 Thaumonomicon reset marker retained; v11.62.5: one page is a hard clipping/layout box. v11.62.6: hard scissor + hover tooltips. TC4 <IMG>, <LINE>,
        // <BR> and paragraphs are rendered in order without spilling over the
        // parchment or drawing adapter placeholders such as [image].
        renderOriginalBookMarkup(poseStack, raw, x, y, PAGE_WIDTH, y + PAGE_HEIGHT);
    }

    private void renderOriginalBookMarkup(PoseStack poseStack, String raw, int x, int y, int pageWidth, int bottomY) {
        Pattern tokenPattern = Pattern.compile("(?is)<IMG>.*?</IMG>|<LINE\\s*/?>|<BR\\s*/?>");
        Matcher matcher = tokenPattern.matcher(raw);
        int cursorY = y;
        int last = 0;
        while (matcher.find()) {
            cursorY = renderOriginalBookTextBlock(poseStack, raw.substring(last, matcher.start()), x, cursorY, pageWidth, bottomY);
            String token = matcher.group();
            if (token.toUpperCase().startsWith("<IMG>")) {
                String payload = token.replaceAll("(?is)^<IMG>|</IMG>$", "");
                cursorY = renderOriginalBookImageTag(poseStack, payload, x, cursorY + 3, pageWidth, bottomY);
            } else if (token.toUpperCase().startsWith("<LINE")) {
                cursorY = renderOriginalBookSeparator(poseStack, x, cursorY + 4, pageWidth, bottomY);
            } else {
                cursorY += 8;
            }
            last = matcher.end();
            if (cursorY > bottomY) {
                return;
            }
        }
        renderOriginalBookTextBlock(poseStack, raw.substring(last), x, cursorY, pageWidth, bottomY);
    }

    private int renderOriginalBookTextBlock(PoseStack poseStack, String raw, int x, int y, int width, int bottomY) {
        String text = TC4ResearchText.cleanInline(raw);
        if (text.isBlank()) {
            return y;
        }
        for (String paragraph : text.split("\n", -1)) {
            if (paragraph.isBlank()) {
                y += 6;
                continue;
            }
            for (FormattedCharSequence line : splitText(paragraph, width)) {
                if (y + font.lineHeight > bottomY) {
                    drawString(poseStack, font, "…", x + width - 8, Math.max(topPos + 14, bottomY - font.lineHeight), PAGE_TEXT_COLOR);
                    return bottomY + 1;
                }
                font.draw(poseStack, line, x, y, PAGE_TEXT_COLOR);
                y += 9;
            }
            y += 2;
        }
        return y;
    }

    private int renderOriginalBookSeparator(PoseStack poseStack, int x, int y, int pageWidth, int bottomY) {
        if (y + 8 > bottomY) {
            return bottomY + 1;
        }
        int left = x + 12;
        int right = x + pageWidth - 12;
        fill(poseStack, left, y + 3, right, y + 4, 0x665A3515);
        fill(poseStack, left + 8, y + 5, right - 8, y + 6, 0x33A47A3C);
        return y + 10;
    }

    private int renderOriginalBookImageTag(PoseStack poseStack, String payload, int x, int y, int pageWidth, int bottomY) {
        OriginalImageSpec spec = OriginalImageSpec.parse(payload);
        if (spec == null || y > bottomY) {
            return y;
        }
        int drawWidth = Math.max(8, Math.round(spec.sourceWidth() * spec.scale()));
        int drawHeight = Math.max(8, Math.round(spec.sourceHeight() * spec.scale()));
        if (spec.oldItemTexture()) {
            drawWidth = 16;
            drawHeight = 16;
        }
        if (drawWidth > pageWidth - 8) {
            float scale = (pageWidth - 8) / (float) drawWidth;
            drawWidth = Math.max(8, Math.round(drawWidth * scale));
            drawHeight = Math.max(8, Math.round(drawHeight * scale));
        }
        if (y + drawHeight > bottomY) {
            int available = Math.max(8, bottomY - y);
            float scale = available / (float) drawHeight;
            drawWidth = Math.max(8, Math.round(drawWidth * scale));
            drawHeight = Math.max(8, available);
        }
        int drawX = x + (pageWidth - drawWidth) / 2;
        ResourceLocation texture = spec.resolvedTexture();
        OriginalGuiTextures.blitOriginalScaledRegion(
                poseStack,
                drawX,
                y,
                texture,
                spec.u(),
                spec.v(),
                spec.sourceWidth(),
                spec.sourceHeight(),
                drawWidth,
                drawHeight,
                spec.sheetWidth(),
                spec.sheetHeight()
        );
        return y + drawHeight + 6;
    }

    private void renderRecipePage(PoseStack poseStack, int x, int y, String type, String recipeKey) {
        // Recipe page type is kept in data for gates/audits, but not rendered as a raw label in the book.

        String requiredResearch = TC4RecipeRequirementIndex.requiredResearchFor(recipeKey, entry.key());
        if (!requiredResearch.isBlank() && !ClientResearchData.hasResearch(requiredResearch)) {
            drawCenteredString(poseStack, font, Component.translatable("thaumcraft.gui.research.locked"), x + PAGE_WIDTH / 2, y + 32, 0x5A3515);
            return;
        }

        if (type != null && type.toUpperCase().contains("ITEMSTACK_PAGE")) {
            renderItemStackBookPage(poseStack, recipeKey, x, y);
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
            renderMissingRecipeHint(poseStack, x, y);
            return;
        }

        // Stage623-642: visible Thaumonomicon recipe pages should look like
        // original TC4 book recipe cards, now split by crafting/crucible/infusion kind.
        switch (recipe.kind()) {
            case INFUSION, INFUSION_ENCHANTMENT -> renderInfusionBookPage(poseStack, x, y, recipe);
            case CRUCIBLE -> renderCrucibleBookPage(poseStack, x, y, recipe);
            case ARCANE_SHAPED, ARCANE_SHAPELESS, NORMAL_SHAPED, NORMAL_SHAPELESS, SMELTING -> renderCraftingBookPage(poseStack, x, y, recipe);
            default -> renderCompoundRecipePage(poseStack, x, y, recipe);
        }
    }

    private void renderItemStackBookPage(PoseStack poseStack, String expression, int x, int y) {
        renderBookSlot(poseStack, x + 76, y + 42, true);
        renderResolvedItemIcon(poseStack, expression, x + 78, y + 44);
    }

    private void renderMissingRecipeHint(PoseStack poseStack, int x, int y) {
        drawCenteredString(poseStack, font, Component.translatable("thaumcraft.gui.recipe.title"), x + PAGE_WIDTH / 2, y + 38, PAGE_ACCENT_COLOR);
        drawCenteredString(poseStack, font, Component.translatable("thaumcraft.gui.recipe.not_linked"), x + PAGE_WIDTH / 2, y + 52, PAGE_TEXT_COLOR);
    }

    private void renderCraftingBookPage(PoseStack poseStack, int x, int y, TC4RecipeRuntimeBridge.OriginalRecipe recipe) {
        int gridX = x + 18;
        int gridY = y + 38;
        renderRecipeFrameTitle(poseStack, x, y, recipe.kind().name().contains("ARCANE") ? I18n.get("thaumcraft.gui.recipe.arcane") : I18n.get("thaumcraft.gui.recipe.crafting"));
        Map<Character, String> symbolMap = inferredOriginalPatternMap(recipe);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int sx = gridX + col * 20;
                int sy = gridY + row * 20;
                renderBookSlot(poseStack, sx, sy, false);
                String expression = expressionForCraftingCell(recipe, symbolMap, row, col);
                if (!expression.isBlank()) {
                    renderResolvedItemIcon(poseStack, expression, sx + 1, sy + 1);
                }
                // Stage623-642: do not paint inferred pattern letters over item icons.
            }
        }
        drawString(poseStack, font, "→", x + 86, y + 61, PAGE_ACCENT_COLOR);
        renderBookSlot(poseStack, x + 111, y + 58, true);
        renderResolvedItemIcon(poseStack, recipe.resultExpression(), x + 113, y + 60);
        renderAspectCostIcons(poseStack, x + 18, y + 112, recipe);
    }

    private String expressionForCraftingCell(TC4RecipeRuntimeBridge.OriginalRecipe recipe, Map<Character, String> symbolMap, int row, int col) {
        if (recipe.pattern().length > row && recipe.pattern()[row].length() > col) {
            char symbol = recipe.pattern()[row].charAt(col);
            if (symbol != ' ') {
                String mapped = symbolMap.get(symbol);
                return mapped == null ? "" : mapped;
            }
            return "";
        }
        if (recipe.pattern().length == 0 && recipe.components().length > 0) {
            int idx = row * 3 + col;
            if (idx < recipe.components().length) {
                return recipe.components()[idx];
            }
        }
        return "";
    }

    private void renderCrucibleBookPage(PoseStack poseStack, int x, int y, TC4RecipeRuntimeBridge.OriginalRecipe recipe) {
        renderRecipeFrameTitle(poseStack, x, y, I18n.get("thaumcraft.gui.recipe.crucible"));
        renderBookSlot(poseStack, x + 22, y + 52, true);
        renderResolvedItemIcon(poseStack, recipe.catalystExpression(), x + 24, y + 54);
        drawString(poseStack, font, "+", x + 54, y + 58, PAGE_ACCENT_COLOR);
        renderAspectCostIcons(poseStack, x + 70, y + 42, recipe);
        drawString(poseStack, font, "→", x + 116, y + 58, PAGE_ACCENT_COLOR);
        renderBookSlot(poseStack, x + 139, y + 52, true);
        renderResolvedItemIcon(poseStack, recipe.resultExpression(), x + 141, y + 54);
        drawCenteredString(poseStack, font, Component.translatable("thaumcraft.gui.recipe.crucible_hint"), x + PAGE_WIDTH / 2, y + 116, PAGE_TEXT_COLOR);
    }

    private void renderInfusionBookPage(PoseStack poseStack, int x, int y, TC4RecipeRuntimeBridge.OriginalRecipe recipe) {
        renderRecipeFrameTitle(poseStack, x, y, I18n.get("thaumcraft.gui.recipe.infusion"));
        int centerX = x + 76;
        int centerY = y + 70;
        renderBookSlot(poseStack, centerX - 10, centerY - 10, true);
        renderResolvedItemIcon(poseStack, recipe.catalystExpression(), centerX - 8, centerY - 8);
        renderBookSlot(poseStack, x + 137, y + 45, true);
        renderResolvedItemIcon(poseStack, recipe.resultExpression(), x + 139, y + 47);
        drawString(poseStack, font, Component.literal("→"), x + 116, y + 55, PAGE_ACCENT_COLOR);
        renderInfusionComponentsRing(poseStack, centerX, centerY, recipe.components());
        renderAspectCostIcons(poseStack, x + 14, y + 134, recipe);
        if (recipe.instability() != null && !recipe.instability().isBlank()) {
            drawString(poseStack, font, Component.translatable("thaumcraft.gui.recipe.instability", recipe.instability()), x + 14, y + 119, PAGE_ACCENT_COLOR);
        }
    }

    private void renderInfusionComponentsRing(PoseStack poseStack, int cx, int cy, String[] components) {
        int limit = Math.min(8, components.length);
        double[] xs = {0, 31, 45, 31, 0, -31, -45, -31};
        double[] ys = {-34, -24, 0, 24, 34, 24, 0, -24};
        for (int i = 0; i < limit; i++) {
            int sx = cx + (int)xs[i] - 9;
            int sy = cy + (int)ys[i] - 9;
            renderBookSlot(poseStack, sx, sy, false);
            renderResolvedItemIcon(poseStack, components[i], sx + 1, sy + 1);
        }
    }

    private void renderCompoundRecipePage(PoseStack poseStack, int x, int y, TC4RecipeRuntimeBridge.OriginalRecipe recipe) {
        renderRecipeFrameTitle(poseStack, x, y, "Recipe");
        renderBookSlot(poseStack, x + 52, y + 52, true);
        renderResolvedItemIcon(poseStack, recipe.catalystExpression(), x + 54, y + 54);
        drawString(poseStack, font, "→", x + 86, y + 58, PAGE_ACCENT_COLOR);
        renderBookSlot(poseStack, x + 111, y + 52, true);
        renderResolvedItemIcon(poseStack, recipe.resultExpression(), x + 113, y + 54);
        renderRecipeVisuals(poseStack, x + 16, y + 92, recipe);
    }

    /**
     * Fallback visual card for compound ConfigResearch pages. Dedicated recipe
     * kinds use their own TC4 layouts; this path only draws resolved component
     * slots and aspect icons, never raw adapter/debug expressions.
     */
    private void renderRecipeVisuals(PoseStack poseStack, int x, int y, TC4RecipeRuntimeBridge.OriginalRecipe recipe) {
        int limit = Math.min(6, recipe.components().length);
        for (int i = 0; i < limit; i++) {
            int sx = x + (i % 3) * 28;
            int sy = y + (i / 3) * 26;
            renderBookSlot(poseStack, sx, sy, false);
            renderResolvedItemIcon(poseStack, recipe.components()[i], sx + 2, sy + 2);
        }
        if (recipe.aspectCosts().length > 0) {
            renderAspectCostIcons(poseStack, x + 92, y, recipe);
        }
    }

    private void renderRecipeFrameTitle(PoseStack poseStack, int x, int y, String title) {
        drawCenteredString(poseStack, font, Component.literal(title), x + PAGE_WIDTH / 2, y + 14, PAGE_ACCENT_COLOR);
        fill(poseStack, x + 18, y + 27, x + PAGE_WIDTH - 18, y + 28, 0x665A3515);
    }

    private void renderBookSlot(PoseStack poseStack, int x, int y, boolean highlight) {
        int outer = highlight ? 0x997B5524 : 0x774A2D12;
        int inner = highlight ? 0x33F0DDAA : 0x22F0DDAA;
        fill(poseStack, x, y, x + 20, y + 20, outer);
        fill(poseStack, x + 1, y + 1, x + 19, y + 19, 0x55D9BE82);
        fill(poseStack, x + 2, y + 2, x + 18, y + 18, inner);
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
                int sx = x + (i % 3) * 24;
                int sy = y + (i / 3) * 20;
                OriginalGuiTextures.blitOriginalTinted(poseStack, sx, sy, icon, 16, 16, aspect.nativeColor());
                String amount = parts.length > 1 ? parts[1].trim() : "";
                if (!amount.isBlank()) {
                    drawString(poseStack, font, amount, sx + 10, sy + 9, 0x2D1B0B);
                }
                registerAspectHover(sx, sy, aspect, amount);
            }
            i++;
        }
    }

    private void renderResolvedItemIcon(PoseStack poseStack, String expression, int x, int y) {
        TC4ResearchItems.resolveLegacyExpression(expression).ifPresent(entry -> {
            Item item = ForgeRegistries.ITEMS.getValue(entry.registryName());

            if (item != null) {
                ItemStack stack = new ItemStack(item);
                itemRenderer.renderAndDecorateItem(stack, x, y);
                registerItemHover(x, y, stack);
            }
        });
    }

    private void registerItemHover(int x, int y, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        bookHoverRegions.add(BookHoverRegion.item(x, y, 16, 16, stack));
    }

    private void registerAspectHover(int x, int y, Aspect aspect, String amount) {
        if (aspect == null) {
            return;
        }
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("aspect.thaumcraft." + aspect.id()).withStyle(style -> style.withColor(aspect.textColor())));
        if (amount != null && !amount.isBlank()) {
            lines.add(Component.translatable("thaumcraft.gui.recipe.cost", amount.trim()));
        }
        bookHoverRegions.add(BookHoverRegion.text(x, y, 16, 16, lines));
    }

    private void renderBookHoverTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        // v11.62.6 Thaumonomicon hover parity: recipe/item/aspect cards now behave
        // like real book content instead of static pasted pictures.
        for (int i = bookHoverRegions.size() - 1; i >= 0; i--) {
            BookHoverRegion region = bookHoverRegions.get(i);
            if (!inside(mouseX, mouseY, region.x(), region.y(), region.w(), region.h())) {
                continue;
            }
            if (!region.stack().isEmpty()) {
                renderTooltip(poseStack, region.stack(), mouseX, mouseY);
            } else if (!region.lines().isEmpty()) {
                renderComponentTooltip(poseStack, region.lines(), mouseX, mouseY);
            }
            return;
        }
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
        if (upper.contains("ARCANE")) return I18n.get("thaumcraft.gui.recipe.arcane");
        if (upper.contains("INFUSION")) return I18n.get("thaumcraft.gui.recipe.infusion");
        if (upper.contains("CRUCIBLE")) return I18n.get("thaumcraft.gui.recipe.crucible");
        if (upper.contains("SMELT")) return I18n.get("thaumcraft.gui.recipe.smelting");
        if (upper.contains("COMPOUND")) return I18n.get("thaumcraft.gui.recipe.compound");
        if (upper.contains("ITEMSTACK_PAGE")) return I18n.get("thaumcraft.gui.recipe.itemstack");
        return I18n.get("thaumcraft.gui.recipe.crafting");
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
            Aspect visualAspect = Aspect.byId(aspect.getKey().toLowerCase());
            ResourceLocation icon = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + aspect.getKey().toLowerCase() + ".png");
            int sx = x + i * 21;
            if (visualAspect != null) {
                OriginalGuiTextures.blitOriginalTinted(poseStack, sx, y, icon, 16, 16, visualAspect.nativeColor());
            } else {
                OriginalGuiTextures.blitOriginal(poseStack, sx, y, icon, 16, 16);
            }
            String amount = String.valueOf(aspect.getValue());
            drawString(poseStack, font, amount, sx + 11, y + 10, 0x2D1B0B);
            registerAspectHover(sx, y, Aspect.byId(aspect.getKey().toLowerCase()), amount);
            i++;
        }
        // Stage643-662: warp remains preserved in ResearchEntry/NBT/requirements,
        // but the visible Thaumonomicon page no longer prints a raw adapter
        // "Warp: N" label over the copied TC4 book texture.
    }

    private void renderOriginalNavigationHotspots(PoseStack poseStack, int mouseX, int mouseY) {
        int navY = topPos + 190;
        int backX = leftPos + 118;
        int prevX = leftPos - 17;
        int nextX = leftPos + 261;
        int color = 0x5A3515;
        drawString(poseStack, font, Component.translatable("thaumcraft.gui.back"), backX + 2, navY + 3, color);
        drawCenteredString(poseStack, font, Component.literal("‹"), prevX + 6, navY + 3,
                pageIndex <= 0 ? 0xAA6D4A22 : color);
        drawCenteredString(poseStack, font, Component.literal("›"), nextX + 6, navY + 3,
                pageIndex >= maxFirstPage() ? 0xAA6D4A22 : color);
        if (inside(mouseX, mouseY, backX, navY, 38, 14)
                || inside(mouseX, mouseY, prevX, navY, 14, 14)
                || inside(mouseX, mouseY, nextX, navY, 14, 14)) {
            fill(poseStack, mouseX - 3, mouseY - 3, mouseX + 3, mouseY + 3, 0x55F0DDAA);
        }
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int navY = topPos + 190;
        if (inside(mouseX, mouseY, leftPos + 118, navY, 38, 14)) {
            minecraft.setScreen(parent);
            return true;
        }
        if (inside(mouseX, mouseY, leftPos - 17, navY, 14, 14)) {
            pageIndex = Math.max(0, pageIndex - 2);
            return true;
        }
        if (inside(mouseX, mouseY, leftPos + 261, navY, 14, 14)) {
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

    private record OriginalImageSpec(ResourceLocation texture, int u, int v, int sourceWidth, int sourceHeight, float scale, int sheetWidth, int sheetHeight, boolean oldItemTexture) {
        static OriginalImageSpec parse(String payload) {
            if (payload == null || payload.isBlank()) {
                return null;
            }
            String[] parts = payload.trim().split(":");
            if (parts.length < 6) {
                return null;
            }
            try {
                String namespace = parts[0];
                String path = parts[1];
                int u = safeInt(parts, 2, 0);
                int v = safeInt(parts, 3, 0);
                int w = safeInt(parts, 4, 16);
                int h = safeInt(parts, 5, 16);
                float scale = safeFloat(parts, 6, 1.0F);
                boolean oldItem = path.startsWith("textures/items/");
                ResourceLocation texture = new ResourceLocation(namespace, remapLegacyTexturePath(path));
                if (oldItem) {
                    return new OriginalImageSpec(texture, 0, 0, 16, 16, 1.0F, 16, 16, true);
                }
                int sheet = path.contains("textures/misc/") ? 256 : 512;
                return new OriginalImageSpec(texture, u, v, Math.max(1, w), Math.max(1, h), Math.max(0.05F, scale), sheet, sheet, false);
            } catch (RuntimeException ignored) {
                return null;
            }
        }

        ResourceLocation resolvedTexture() {
            return texture;
        }

        private static String remapLegacyTexturePath(String path) {
            if (!path.startsWith("textures/items/")) {
                return path;
            }
            // TC4 1.7.10 used textures/items/*.png.  The rebuild carries those
            // sprites under textures/item/tc4/ so book IMG pages can show the
            // v11.62.4 compatibility marker: quicksilver_drop was the old remap target, now replaced by TC4 original sprites.
            // original icon art instead of a modern placeholder/remapped drop.
            String name = path.substring("textures/items/".length());
            return "textures/item/tc4/" + name;
        }

        private static int safeInt(String[] parts, int index, int fallback) {
            if (index >= parts.length) {
                return fallback;
            }
            try {
                return Integer.parseInt(parts[index].trim());
            } catch (NumberFormatException ex) {
                return fallback;
            }
        }

        private static float safeFloat(String[] parts, int index, float fallback) {
            if (index >= parts.length) {
                return fallback;
            }
            try {
                return Float.parseFloat(parts[index].trim());
            } catch (NumberFormatException ex) {
                return fallback;
            }
        }
    }

    private record BookHoverRegion(int x, int y, int w, int h, ItemStack stack, List<Component> lines) {
        static BookHoverRegion item(int x, int y, int w, int h, ItemStack stack) {
            return new BookHoverRegion(x, y, w, h, stack, List.of());
        }

        static BookHoverRegion text(int x, int y, int w, int h, List<Component> lines) {
            return new BookHoverRegion(x, y, w, h, ItemStack.EMPTY, lines);
        }
    }

    private record PageRef(String type, String value, boolean recipe) {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
