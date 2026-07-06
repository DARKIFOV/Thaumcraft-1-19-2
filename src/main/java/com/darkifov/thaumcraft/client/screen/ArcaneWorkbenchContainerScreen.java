package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.ClientResearchData;
import com.darkifov.thaumcraft.client.arcane.ClientArcaneRecipePage;
import com.darkifov.thaumcraft.client.arcane.ClientArcaneRecipeRegistry;
import com.darkifov.thaumcraft.menu.ArcaneWorkbenchMenu;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Locale;

public class ArcaneWorkbenchContainerScreen extends AbstractContainerScreen<ArcaneWorkbenchMenu> {
    private static final ResourceLocation ORIGINAL_TEXTURE =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/gui/arcane_workbench.png");

    private int page = 0;
    private EditBox searchBox;

    public ArcaneWorkbenchContainerScreen(ArcaneWorkbenchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 234;
        imageHeight = 234;
        inventoryLabelY = 151;
    }

    @Override
    protected void init() {
        super.init();

        searchBox = new EditBox(font, leftPos + 8, topPos - 44, 198, 18, Component.literal("Search"));
        searchBox.setMaxLength(40);
        searchBox.setSuggestion("Search recipe...");
        addRenderableWidget(searchBox);

        addRenderableWidget(new Button(leftPos + 6, topPos - 24, 50, 18, Component.literal("<"), button -> {
            page = Math.max(0, page - 1);
        }));

        addRenderableWidget(new Button(leftPos + 158, topPos - 24, 50, 18, Component.literal(">"), button -> {
            page = Math.min(filteredRecipes().size() - 1, page + 1);
        }));

        addRenderableWidget(new Button(leftPos + 70, topPos - 24, 72, 18, Component.literal("Craft"), button -> {
            ClientArcaneRecipePage recipe = currentRecipe();

            if (recipe != null && ClientResearchData.hasResearch(recipe.research())) {
                ThaumcraftNetwork.requestArcaneMenuCraftFromClient(menu.blockPos(), new ResourceLocation(recipe.id()));
            }
        }));
    }

    private List<ClientArcaneRecipePage> filteredRecipes() {
        List<ClientArcaneRecipePage> all = ClientArcaneRecipeRegistry.pages();
        String query = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase(Locale.ROOT);

        if (query.isEmpty()) {
            return all;
        }

        List<ClientArcaneRecipePage> filtered = new ArrayList<>();

        for (ClientArcaneRecipePage page : all) {
            String haystack = (page.title() + " " + page.research() + " " + page.catalyst() + " " + page.result()).toLowerCase(Locale.ROOT);

            if (haystack.contains(query)) {
                filtered.add(page);
            }
        }

        return filtered;
    }

    private ClientArcaneRecipePage currentRecipe() {
        List<ClientArcaneRecipePage> filtered = filteredRecipes();

        if (filtered.isEmpty()) {
            return null;
        }

        if (page >= filtered.size()) {
            page = filtered.size() - 1;
        }

        if (page < 0) {
            page = 0;
        }

        return filtered.get(page);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        int x = leftPos + (imageWidth - 256) / 2;
        int y = topPos + (imageHeight - 256) / 2;
        OriginalGuiTextures.blitOriginal(poseStack, x, y, OriginalGuiTextures.ARCANE_WORKBENCH, 256, 256);
    }

    private void renderGhostItems(PoseStack poseStack, ClientArcaneRecipePage recipe) {
        boolean shaped = recipe.patternRows() != null && recipe.patternRows().length > 0;
        font.draw(poseStack, shaped ? "TC4 shaped layout:" : "TC4 ghost layout:", leftPos + 10, topPos + 126, 0x4A2A11);

        if (shaped) {
            renderPatternGhost(poseStack, recipe);
        } else {
            renderGhostStack(poseStack, recipe.catalystId(), leftPos + 16, topPos + 64);

            String[] ids = recipe.ingredientIds();

            for (int i = 0; i < Math.min(ids.length, 9); i++) {
                int x = leftPos + 40 + (i % 3) * 24;
                int y = topPos + 40 + (i / 3) * 24;
                renderGhostStack(poseStack, ids[i], x, y);
            }
        }

        renderGhostStack(poseStack, recipe.resultId(), leftPos + 160, topPos + 64);
    }

    private void renderPatternGhost(PoseStack poseStack, ClientArcaneRecipePage recipe) {
        String[] rows = recipe.patternRows();
        Map<Character, String> symbolMap = inferredClientPatternMap(recipe);

        for (int row = 0; row < Math.min(3, rows.length); row++) {
            String line = rows[row];
            for (int col = 0; col < Math.min(3, line.length()); col++) {
                char symbol = line.charAt(col);
                int x = leftPos + 40 + col * 24;
                int y = topPos + 40 + row * 24;

                if (symbol == ' ') {
                    drawSlot(poseStack, x - 1, y - 1);
                    continue;
                }

                String itemId = symbolMap.getOrDefault(symbol, recipe.catalystId());
                renderGhostStack(poseStack, itemId, x, y);
                font.draw(poseStack, String.valueOf(symbol), x + 12, y + 10, 0xFF2D1B0B);
            }
        }

        if (!recipe.catalystId().isBlank()) {
            font.draw(poseStack, "catalyst", leftPos + 10, topPos + 52, 0x4A2A11);
            renderGhostStack(poseStack, recipe.catalystId(), leftPos + 16, topPos + 64);
        }
    }

    private Map<Character, String> inferredClientPatternMap(ClientArcaneRecipePage recipe) {
        Map<Character, String> map = new LinkedHashMap<>();
        List<Character> symbols = new ArrayList<>();
        String[] rows = recipe.patternRows();

        for (String row : rows) {
            for (int i = 0; i < row.length(); i++) {
                char symbol = row.charAt(i);
                if (symbol != ' ' && !symbols.contains(symbol)) {
                    symbols.add(symbol);
                }
            }
        }

        Character catalystSymbol = inferredClientCatalystSymbol(rows, symbols, recipe.ingredientIds().length);
        int ingredientIndex = 0;

        for (Character symbol : symbols) {
            if (catalystSymbol != null && symbol.equals(catalystSymbol)) {
                map.put(symbol, recipe.catalystId());
                continue;
            }
            String[] ingredients = recipe.ingredientIds();
            if (ingredients.length == 1) {
                map.put(symbol, ingredients[0]);
            } else if (ingredientIndex < ingredients.length) {
                map.put(symbol, ingredients[ingredientIndex++]);
            }
        }

        return map;
    }

    private Character inferredClientCatalystSymbol(String[] rows, List<Character> symbols, int ingredientCount) {
        if (symbols.isEmpty()) {
            return null;
        }
        if (ingredientCount == 0 && symbols.size() == 1) {
            return symbols.get(0);
        }
        if (symbols.size() == ingredientCount + 1 || (ingredientCount == 1 && symbols.size() == 2)) {
            if (rows.length > 1 && rows[1].length() > 1) {
                char center = rows[1].charAt(1);
                if (center != ' ' && countClientSymbol(rows, center) == 1) {
                    return center;
                }
            }
            Character rarest = null;
            int rarestCount = Integer.MAX_VALUE;
            for (Character symbol : symbols) {
                int count = countClientSymbol(rows, symbol);
                if (count < rarestCount) {
                    rarest = symbol;
                    rarestCount = count;
                }
            }
            return rarest;
        }
        return null;
    }

    private int countClientSymbol(String[] rows, char symbol) {
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

    private void renderGhostStack(PoseStack poseStack, String itemId, int x, int y) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));

        if (item == null) {
            fill(poseStack, x, y, x + 16, y + 16, 0x559A2B2B);
            font.draw(poseStack, "?", x + 5, y + 4, 0xFFFFFF);
            return;
        }

        itemRenderer.renderAndDecorateItem(new ItemStack(item), x, y);
        fill(poseStack, x, y, x + 16, y + 16, 0x55FFFFFF);
    }

    private void drawSlot(PoseStack poseStack, int x, int y) {
        fill(poseStack, x, y, x + 18, y + 18, 0xFF5C4631);
        fill(poseStack, x + 1, y + 1, x + 17, y + 17, 0xFFEEDDB9);
        fill(poseStack, x + 2, y + 2, x + 16, y + 16, 0x88FFFFFF);
    }

    private void renderRecipeInfo(PoseStack poseStack, ClientArcaneRecipePage recipe) {
        boolean unlocked = ClientResearchData.hasResearch(recipe.research());
        int ink = 0x3F2612;
        int warning = 0x8A2D1B;

        drawCenteredString(poseStack, font, Component.literal("Arcane Workbench"), leftPos + imageWidth / 2, topPos + 8, ink);
        drawString(poseStack, font, Component.literal(recipe.title()), leftPos + 10, topPos + 18, ink);
        drawString(poseStack, font, Component.literal("Research: " + (recipe.research().isBlank() ? "none" : recipe.research())), leftPos + 10, topPos + 30, unlocked ? 0x245A24 : warning);
        drawString(poseStack, font, Component.literal("Vis: " + recipe.visCost()), leftPos + 10, topPos + 42, 0x4A2A88);
        if (!recipe.tc4Kind().isBlank()) {
            drawString(poseStack, font, Component.literal(recipe.tc4Kind()), leftPos + 10, topPos + 54, 0x6D4A22);
        }
        drawString(poseStack, font, Component.literal((page + 1) + " / " + Math.max(1, filteredRecipes().size())), leftPos + 168, topPos + 18, ink);

        if (!unlocked) {
            drawCenteredString(poseStack, font, Component.literal("Research locked"), leftPos + 168, topPos + 86, warning);
        }

        if (!recipe.note().isBlank()) {
            drawString(poseStack, font, Component.literal(recipe.note()), leftPos + 10, topPos + 136, 0x5A3515);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchBox != null && searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            page = 0;
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchBox != null && searchBox.charTyped(codePoint, modifiers)) {
            page = 0;
            return true;
        }

        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void containerTick() {
        super.containerTick();

        if (searchBox != null) {
            searchBox.tick();
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        ClientArcaneRecipePage recipe = currentRecipe();

        if (recipe != null) {
            renderRecipeInfo(poseStack, recipe);
            renderGhostItems(poseStack, recipe);
        }

        renderTooltip(poseStack, mouseX, mouseY);

        if (searchBox != null) {
            searchBox.render(poseStack, mouseX, mouseY, partialTick);
        }

        if (recipe != null && mouseY < topPos) {
            renderTooltip(poseStack,
                    Component.literal(recipe.title() + " | " + recipe.result()).withStyle(ChatFormatting.GOLD),
                    mouseX,
                    mouseY);
        }
    }
}
