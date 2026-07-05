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
import java.util.Locale;

public class ArcaneWorkbenchContainerScreen extends AbstractContainerScreen<ArcaneWorkbenchMenu> {
    private static final ResourceLocation ORIGINAL_TEXTURE =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/gui/arcane_workbench.png");

    private int page = 0;
    private EditBox searchBox;

    public ArcaneWorkbenchContainerScreen(ArcaneWorkbenchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 214;
        imageHeight = 214;
        inventoryLabelY = 121;
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
        font.draw(poseStack, "Ghost:", leftPos + 10, topPos + 104, 0x4A2A11);

        renderGhostStack(poseStack, recipe.catalystId(), leftPos + 50, topPos + 100);

        String[] ids = recipe.ingredientIds();

        for (int i = 0; i < Math.min(ids.length, 9); i++) {
            int x = leftPos + 92 + (i % 3) * 18;
            int y = topPos + 96 + (i / 3) * 18;
            renderGhostStack(poseStack, ids[i], x, y);
        }

        renderGhostStack(poseStack, recipe.resultId(), leftPos + 170, topPos + 100);
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
        renderTooltip(poseStack, mouseX, mouseY);

        if (searchBox != null) {
            searchBox.render(poseStack, mouseX, mouseY, partialTick);
        }

        ClientArcaneRecipePage recipe = currentRecipe();

        if (recipe != null && mouseY < topPos) {
            renderTooltip(poseStack,
                    Component.literal(recipe.title() + " | " + recipe.result()).withStyle(ChatFormatting.GOLD),
                    mouseX,
                    mouseY);
        }
    }
}
