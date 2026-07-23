package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.alchemy.AlchemyRecipe;
import com.darkifov.thaumcraft.blockentity.ThaumatoriumBlockEntity;
import com.darkifov.thaumcraft.menu.ThaumatoriumMenu;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Pixel-position parity adapter for TC4 GuiThaumatorium. */
public final class ThaumatoriumScreen extends AbstractContainerScreen<ThaumatoriumMenu> {
    private static final ResourceLocation GUI = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/gui/thaumcraft_core_original/gui_thaumatorium.png");
    private int recipeIndex;
    private int startAspect;

    public ThaumatoriumScreen(ThaumatoriumMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 72;
        titleLabelY = -1000;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos, topPos, GUI,
                0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderOriginalContent(poseStack, mouseX, mouseY);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int x = (int) mouseX - leftPos;
            int y = (int) mouseY - topPos;
            List<AlchemyRecipe> recipes = recipes();
            clampIndex(recipes);
            if (!recipes.isEmpty() && x >= 112 && x < 128 && y >= 16 && y < 32) {
                ThaumatoriumBlockEntity tile = tile();
                AlchemyRecipe recipe = recipes.get(recipeIndex);
                if (tile != null && (tile.isRemembered(recipe)
                        || tile.rememberedFormulaIds().size() < tile.maxRecipes())) {
                    ThaumcraftNetwork.requestThaumatoriumFormulaFromClient(menu.blockPos(), recipeIndex);
                }
                return true;
            }
            if (x >= 128 && x < 144 && y >= 16 && y < 24 && recipeIndex > 0) {
                recipeIndex--;
                startAspect = 0;
                return true;
            }
            if (x >= 128 && x < 144 && y >= 24 && y < 32 && recipeIndex + 1 < recipes.size()) {
                recipeIndex++;
                startAspect = 0;
                return true;
            }
            if (!recipes.isEmpty() && x >= 32 && x < 40 && y >= 40 && y < 56 && startAspect > 0) {
                startAspect--;
                return true;
            }
            if (!recipes.isEmpty() && x >= 136 && x < 144 && y >= 40 && y < 56
                    && startAspect < Math.max(0, recipes.get(recipeIndex).cost().size() - 6)) {
                startAspect++;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderOriginalContent(PoseStack poseStack, int mouseX, int mouseY) {
        ThaumatoriumBlockEntity tile = tile();
        List<AlchemyRecipe> recipes = recipes();
        clampIndex(recipes);
        if (tile == null || recipes.isEmpty()) {
            return;
        }
        AlchemyRecipe recipe = recipes.get(recipeIndex);
        ItemStack output = output(recipe);
        if (!output.isEmpty()) {
            boolean available = tile.isRemembered(recipe)
                    || tile.rememberedFormulaIds().size() < tile.maxRecipes();
            if (!available) {
                fill(poseStack, leftPos + 111, topPos + 15, leftPos + 129, topPos + 33, 0x88000000);
            }
            itemRenderer.renderAndDecorateItem(output, leftPos + 112, topPos + 16);
            itemRenderer.renderGuiItemDecorations(font, output, leftPos + 112, topPos + 16);
        }

        if (recipeIndex > 0) {
            OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos + 128, topPos + 16,
                    GUI, 192, 16, 16, 8, 256, 256);
        }
        if (recipeIndex + 1 < recipes.size()) {
            OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos + 128, topPos + 24,
                    GUI, 192, 24, 16, 8, 256, 256);
        }
        if (tile.isRemembered(recipe)) {
            OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos + 104, topPos + 8,
                    GUI, 176, 96, 48, 48, 256, 256);
            OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos + 88, topPos + 16,
                    GUI, 176, 56, 24, 24, 256, 256);
        }

        List<Map.Entry<Aspect, Integer>> aspects = new ArrayList<>(recipe.cost().entrySet());
        aspects.sort(Map.Entry.comparingByKey());
        startAspect = Math.min(startAspect, Math.max(0, aspects.size() - 6));
        for (int visible = 0; visible < 6 && startAspect + visible < aspects.size(); visible++) {
            Map.Entry<Aspect, Integer> entry = aspects.get(startAspect + visible);
            int x = leftPos + 40 + visible * 16;
            ResourceLocation texture = new ResourceLocation(ThaumcraftMod.MOD_ID,
                    "textures/aspects/" + entry.getKey().id() + ".png");
            OriginalGuiTextures.blitOriginalTinted(poseStack, x, topPos + 40,
                    texture, 16, 16, entry.getKey().nativeColor());
            drawString(poseStack, font, Component.literal(String.valueOf(entry.getValue())),
                    x + 10, topPos + 49, 0xFFFFFF);
            if (tile.isRemembered(recipe)) {
                int required = Math.max(1, entry.getValue());
                int width = Math.min(12, tile.storedEssentia(entry.getKey()) * 12 / required);
                fill(poseStack, x + 2, topPos + 58, x + 2 + width, topPos + 62,
                        0xFF000000 | entry.getKey().nativeColor());
            }
        }
        if (aspects.size() > 6 && startAspect > 0) {
            OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos + 32, topPos + 40,
                    GUI, 192, 32, 8, 16, 256, 256);
        }
        if (aspects.size() > 6 && startAspect < aspects.size() - 6) {
            OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos + 136, topPos + 40,
                    GUI, 200, 32, 8, 16, 256, 256);
        }

        if (tile.maxRecipes() > 1) {
            String memory = tile.rememberedFormulaIds().size() + "/" + tile.maxRecipes();
            poseStack.pushPose();
            poseStack.translate(leftPos + 136, topPos + 33, 0);
            poseStack.scale(0.5F, 0.5F, 1.0F);
            drawCenteredString(poseStack, font, memory, 0, 0, 0xFFFFFF);
            poseStack.popPose();
        }
        if (mouseX >= leftPos + 112 && mouseX < leftPos + 128
                && mouseY >= topPos + 16 && mouseY < topPos + 32 && !output.isEmpty()) {
            renderTooltip(poseStack, output, mouseX, mouseY);
        }
    }

    private List<AlchemyRecipe> recipes() {
        ThaumatoriumBlockEntity tile = tile();
        Minecraft minecraft = Minecraft.getInstance();
        return tile == null ? List.of() : tile.visibleFormulaCandidates(minecraft.player);
    }

    private void clampIndex(List<AlchemyRecipe> recipes) {
        recipeIndex = recipes.isEmpty() ? 0 : Math.max(0, Math.min(recipeIndex, recipes.size() - 1));
    }

    private ItemStack output(AlchemyRecipe recipe) {
        if (recipe == null || recipe.resultItemId() == null) return ItemStack.EMPTY;
        var item = ForgeRegistries.ITEMS.getValue(recipe.resultItemId());
        return item == null ? ItemStack.EMPTY : new ItemStack(item, recipe.resultCount());
    }

    private ThaumatoriumBlockEntity tile() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return null;
        BlockEntity be = minecraft.level.getBlockEntity(menu.blockPos());
        return be instanceof ThaumatoriumBlockEntity thaumatorium ? thaumatorium : null;
    }
}
