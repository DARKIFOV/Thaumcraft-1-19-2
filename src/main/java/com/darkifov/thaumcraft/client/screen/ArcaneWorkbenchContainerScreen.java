package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectColor;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.client.ClientResearchData;
import com.darkifov.thaumcraft.client.arcane.ClientArcaneRecipePage;
import com.darkifov.thaumcraft.client.arcane.ClientArcaneRecipeRegistry;
import com.darkifov.thaumcraft.menu.ArcaneWorkbenchMenu;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Stage189 original TC4 GuiArcaneWorkbench adapter.
 *
 * Source of truth: thaumcraft.client.gui.GuiArcaneWorkbench.
 * There is no recipe browser, search field or client-side Craft button in TC4.
 * The server-side container previews the current 3x3 grid output and this screen
 * only renders gui_arcaneworkbench plus the six primal aspect costs around it.
 */
public class ArcaneWorkbenchContainerScreen extends AbstractContainerScreen<ArcaneWorkbenchMenu> {
    private static final int[][] ASPECT_LOCS = new int[][]{
            {72, 21},   // Air
            {24, 43},   // Earth
            {24, 102},  // Fire
            {72, 124},  // Water
            {120, 102}, // Order
            {120, 43}   // Entropy
    };
    private static final Aspect[] PRIMALS = new Aspect[]{Aspect.AER, Aspect.TERRA, Aspect.IGNIS, Aspect.AQUA, Aspect.ORDO, Aspect.PERDITIO};

    public ArcaneWorkbenchContainerScreen(ArcaneWorkbenchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 190;
        imageHeight = 234;
        inventoryLabelY = 151;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos, topPos, OriginalGuiTextures.ARCANE_WORKBENCH, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        // Original GuiArcaneWorkbench leaves the foreground layer empty.
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderOriginalAspectCosts(poseStack, partialTick);
        renderOriginalInsufficientVis(poseStack);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    private void renderOriginalAspectCosts(PoseStack poseStack, float partialTick) {
        ClientArcaneRecipePage recipe = recipeForOutput();
        if (recipe == null) {
            return;
        }

        Map<Aspect, Integer> costs = parseAspectCosts(recipe.visCost());
        if (costs.isEmpty()) {
            return;
        }

        ItemStack wand = menu.getSlot(ArcaneWorkbenchMenu.MENU_SLOT_WAND).getItem();
        for (int i = 0; i < PRIMALS.length; i++) {
            Aspect aspect = PRIMALS[i];
            int baseAmount = costs.getOrDefault(aspect, 0);
            if (baseAmount <= 0) {
                continue;
            }

            int amount = wand.getItem() instanceof WandItem ? WandItem.modifiedVisCost(wand, aspect, baseAmount) : baseAmount;
            boolean enough = !(wand.getItem() instanceof WandItem) || WandItem.getVis(wand, aspect) >= amount || WandItem.hasInfiniteVis(wand);
            int x = leftPos + ASPECT_LOCS[i][0] - 8;
            int y = topPos + ASPECT_LOCS[i][1] - 8;
            int alpha = enough ? 230 : 120;
            int color = enough ? AspectColor.argb(aspect, alpha) : AspectColor.dim(aspect, alpha, 0.45F);

            fill(poseStack, x, y, x + 16, y + 16, color);
            drawString(poseStack, font, Component.literal(String.valueOf(amount)), x + 11, y + 9, enough ? 0xFFFFFF : 0x9E5A3B);
        }
    }

    private void renderOriginalInsufficientVis(PoseStack poseStack) {
        ClientArcaneRecipePage recipe = recipeForVisibleGrid();
        if (recipe == null) {
            return;
        }
        if (!menu.getSlot(ArcaneWorkbenchMenu.MENU_SLOT_OUTPUT).getItem().isEmpty()) {
            return;
        }
        ItemStack wand = menu.getSlot(ArcaneWorkbenchMenu.MENU_SLOT_WAND).getItem();
        if (!(wand.getItem() instanceof WandItem)) {
            return;
        }
        Map<Aspect, Integer> costs = parseAspectCosts(recipe.visCost());
        if (costs.isEmpty()) {
            return;
        }
        for (Map.Entry<Aspect, Integer> entry : costs.entrySet()) {
            int needed = WandItem.modifiedVisCost(wand, entry.getKey(), entry.getValue());
            if (!WandItem.hasInfiniteVis(wand) && WandItem.getVis(wand, entry.getKey()) < needed) {
                drawCenteredString(poseStack, font, Component.literal("Insufficient vis").withStyle(ChatFormatting.RED), leftPos + 168, topPos + 46, 0xEE6E6E);
                return;
            }
        }
    }

    private ClientArcaneRecipePage recipeForOutput() {
        ItemStack output = menu.getSlot(ArcaneWorkbenchMenu.MENU_SLOT_OUTPUT).getItem();
        if (output.isEmpty()) {
            return null;
        }
        ResourceLocation outputId = ForgeRegistries.ITEMS.getKey(output.getItem());
        if (outputId == null) {
            return null;
        }
        for (ClientArcaneRecipePage page : ClientArcaneRecipeRegistry.pages()) {
            if (!ClientResearchData.hasResearch(page.research())) {
                continue;
            }
            if (outputId.toString().equals(page.resultId())) {
                return page;
            }
        }
        return null;
    }

    private ClientArcaneRecipePage recipeForVisibleGrid() {
        for (ClientArcaneRecipePage page : ClientArcaneRecipeRegistry.pages()) {
            if (!ClientResearchData.hasResearch(page.research())) {
                continue;
            }
            if (matchesVisibleGrid(page)) {
                return page;
            }
        }
        return null;
    }

    private boolean matchesVisibleGrid(ClientArcaneRecipePage page) {
        String[] rows = page.patternRows();
        if (rows == null || rows.length == 0) {
            return false;
        }
        Map<Character, String> key = inferredPatternMap(page);
        for (int row = 0; row < 3; row++) {
            String line = row < rows.length ? rows[row] : "";
            for (int col = 0; col < 3; col++) {
                char symbol = col < line.length() ? line.charAt(col) : ' ';
                Slot slot = menu.getSlot(ArcaneWorkbenchMenu.MENU_SLOT_GRID_START + row * 3 + col);
                ItemStack stack = slot.getItem();
                if (symbol == ' ') {
                    if (!stack.isEmpty()) return false;
                    continue;
                }
                String required = key.get(symbol);
                ResourceLocation actual = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (required == null || actual == null || !required.equals(actual.toString())) {
                    return false;
                }
            }
        }
        return true;
    }

    private Map<Character, String> inferredPatternMap(ClientArcaneRecipePage page) {
        Map<Character, String> result = new java.util.LinkedHashMap<>();
        java.util.List<Character> symbols = new java.util.ArrayList<>();
        for (String row : page.patternRows()) {
            for (int i = 0; i < row.length(); i++) {
                char symbol = row.charAt(i);
                if (symbol != ' ' && !symbols.contains(symbol)) {
                    symbols.add(symbol);
                }
            }
        }
        Character catalyst = inferCatalystSymbol(page.patternRows(), symbols, page.ingredientIds().length);
        int ingredientIndex = 0;
        for (Character symbol : symbols) {
            if (catalyst != null && catalyst.equals(symbol)) {
                result.put(symbol, page.catalystId());
            } else if (page.ingredientIds().length == 1) {
                result.put(symbol, page.ingredientIds()[0]);
            } else if (ingredientIndex < page.ingredientIds().length) {
                result.put(symbol, page.ingredientIds()[ingredientIndex++]);
            }
        }
        return result;
    }

    private Character inferCatalystSymbol(String[] rows, java.util.List<Character> symbols, int ingredientCount) {
        if (symbols.isEmpty()) return null;
        if (ingredientCount == 0 && symbols.size() == 1) return symbols.get(0);
        if (symbols.size() == ingredientCount + 1 || (ingredientCount == 1 && symbols.size() == 2)) {
            if (rows.length > 1 && rows[1].length() > 1) {
                char center = rows[1].charAt(1);
                if (center != ' ' && countSymbol(rows, center) == 1) return center;
            }
            Character rarest = null;
            int rarestCount = Integer.MAX_VALUE;
            for (Character symbol : symbols) {
                int count = countSymbol(rows, symbol);
                if (count < rarestCount) {
                    rarest = symbol;
                    rarestCount = count;
                }
            }
            return rarest;
        }
        return null;
    }

    private int countSymbol(String[] rows, char symbol) {
        int count = 0;
        for (String row : rows) {
            for (int i = 0; i < row.length(); i++) {
                if (row.charAt(i) == symbol) count++;
            }
        }
        return count;
    }

    private Map<Aspect, Integer> parseAspectCosts(String text) {
        Map<Aspect, Integer> costs = new EnumMap<>(Aspect.class);
        if (text == null || text.isBlank() || text.equals("none")) {
            return costs;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        for (Aspect aspect : PRIMALS) {
            String display = aspect.displayName().toLowerCase(Locale.ROOT);
            String id = aspect.id().toLowerCase(Locale.ROOT);
            int idx = lower.indexOf(display);
            if (idx < 0) idx = lower.indexOf(id);
            if (idx < 0) continue;
            String tail = lower.substring(idx + (lower.startsWith(display, idx) ? display.length() : id.length())).trim();
            StringBuilder digits = new StringBuilder();
            for (int i = 0; i < tail.length(); i++) {
                char ch = tail.charAt(i);
                if (Character.isDigit(ch)) digits.append(ch);
                else if (digits.length() > 0) break;
            }
            if (digits.length() > 0) {
                costs.put(aspect, Integer.parseInt(digits.toString()));
            }
        }
        return costs;
    }
}
