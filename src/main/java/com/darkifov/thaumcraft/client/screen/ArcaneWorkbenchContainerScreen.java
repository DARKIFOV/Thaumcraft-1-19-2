package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectColor;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.client.ClientResearchData;
import com.darkifov.thaumcraft.client.arcane.ClientArcaneRecipePage;
import com.darkifov.thaumcraft.client.arcane.ClientArcaneRecipeRegistry;
import com.darkifov.thaumcraft.arcane.TC4ArcaneWorkbenchParity;
import com.darkifov.thaumcraft.menu.ArcaneWorkbenchMenu;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import java.util.ArrayList;
import java.util.List;
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
    public ArcaneWorkbenchContainerScreen(ArcaneWorkbenchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = TC4ArcaneWorkbenchParity.GUI_WIDTH;
        imageHeight = TC4ArcaneWorkbenchParity.GUI_HEIGHT;
        inventoryLabelY = TC4ArcaneWorkbenchParity.PLAYER_INV_Y;
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
        renderOriginalAspectHover(poseStack, mouseX, mouseY);
    }

    private void renderOriginalAspectCosts(PoseStack poseStack, float partialTick) {
        // Stage443-462: original GuiArcaneWorkbench shows the primal aspect cost
        // for the shaped grid as soon as the recipe is present.  Do not wait for
        // a non-empty output slot only; fall back to output lookup for server
        // preview desync cases, but keep all crafting logic server-side.
        ClientArcaneRecipePage recipe = recipeForVisibleGrid();
        if (recipe == null) {
            recipe = recipeForOutput();
        }
        if (recipe == null) {
            return;
        }

        Map<Aspect, Integer> costs = parseAspectCosts(recipe.visCost());
        if (costs.isEmpty()) {
            return;
        }

        ItemStack wand = menu.getSlot(ArcaneWorkbenchMenu.MENU_SLOT_WAND).getItem();
        for (int i = 0; i < TC4ArcaneWorkbenchParity.PRIMALS.length; i++) {
            Aspect aspect = TC4ArcaneWorkbenchParity.PRIMALS[i];
            int baseAmount = costs.getOrDefault(aspect, 0);
            if (baseAmount <= 0) {
                continue;
            }

            int amount = wand.getItem() instanceof WandItem ? WandItem.modifiedVisCost(wand, aspect, baseAmount) : baseAmount;
            boolean enough = !(wand.getItem() instanceof WandItem) || WandItem.getVis(wand, aspect) >= amount || WandItem.hasInfiniteVis(wand);
            int x = leftPos + TC4ArcaneWorkbenchParity.ASPECT_LOCS[i][0] - 8;
            int y = topPos + TC4ArcaneWorkbenchParity.ASPECT_LOCS[i][1] - 8;
            // Stage383-402: original GuiArcaneWorkbench displays primal aspect icons from
            // the TC4 aspect texture set, not modern flat color boxes.
            ResourceLocation texture = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + aspect.id() + ".png");
            OriginalGuiTextures.blitOriginal(poseStack, x, y, texture, 16, 16);
            if (!enough) {
                fill(poseStack, x, y, x + 16, y + 16, 0x88000000);
            }
            drawString(poseStack, font, Component.literal(String.valueOf(amount)), x + 11, y + 9, enough ? 0xFFFFFF : 0x9E5A3B);
        }
    }

    private void renderOriginalInsufficientVis(PoseStack poseStack) {
        // Stage683-702: original TC4 does not draw a modern text warning inside
        // GuiArcaneWorkbench.  The output slot may still preview the recipe while
        // the server-side Slot#mayPickup gate blocks taking it without enough vis.
    }


    private void renderOriginalAspectHover(PoseStack poseStack, int mouseX, int mouseY) {
        int localX = mouseX - leftPos;
        int localY = mouseY - topPos;
        Aspect hovered = TC4ArcaneWorkbenchParity.aspectAt(localX, localY);
        if (hovered == null) {
            return;
        }
        ClientArcaneRecipePage recipe = recipeForVisibleGrid();
        if (recipe == null) {
            recipe = recipeForOutput();
        }
        if (recipe == null) {
            return;
        }
        int baseAmount = parseAspectCosts(recipe.visCost()).getOrDefault(hovered, 0);
        if (baseAmount <= 0) {
            return;
        }
        ItemStack wand = menu.getSlot(ArcaneWorkbenchMenu.MENU_SLOT_WAND).getItem();
        int amount = wand.getItem() instanceof WandItem ? WandItem.modifiedVisCost(wand, hovered, baseAmount) : baseAmount;
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(hovered.displayName() + " " + amount).withStyle(ChatFormatting.LIGHT_PURPLE));
        if (wand.getItem() instanceof WandItem && !WandItem.hasInfiniteVis(wand)) {
            lines.add(Component.literal(String.valueOf(WandItem.getVis(wand, hovered))).withStyle(ChatFormatting.DARK_PURPLE));
        }
        renderComponentTooltip(poseStack, lines, mouseX, mouseY);
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
        for (Aspect aspect : TC4ArcaneWorkbenchParity.PRIMALS) {
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
