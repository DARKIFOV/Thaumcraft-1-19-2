package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.alchemy.AlchemyRecipe;
import com.darkifov.thaumcraft.blockentity.ThaumatoriumBlockEntity;
import com.darkifov.thaumcraft.menu.ThaumatoriumMenu;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
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

/**
 * Stage523-562 original GuiThaumatorium visual adapter.
 * Uses the TC4 gui_thaumatorium texture and displays catalyst, formula slots,
 * mnemonic matrix state and essentia costs without adding any modern buttons.
 */
public class ThaumatoriumScreen extends AbstractContainerScreen<ThaumatoriumMenu> {
    private static final ResourceLocation GUI = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/gui/thaumcraft_core_original/gui_thaumatorium.png");
    private static final int[][] FORMULA_SLOTS = new int[][]{{34, 22}, {58, 22}, {82, 22}, {106, 22}, {130, 22}};

    public ThaumatoriumScreen(ThaumatoriumMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 214;
        inventoryLabelY = 132;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos, topPos, GUI, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        // TC4 renders this GUI almost entirely as texture + icons. Keep text minimal and diagnostic only.
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderThaumatoriumContent(poseStack, mouseX, mouseY);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int localX = (int) mouseX - leftPos;
            int localY = (int) mouseY - topPos;
            for (int i = 0; i < FORMULA_SLOTS.length; i++) {
                int x = FORMULA_SLOTS[i][0];
                int y = FORMULA_SLOTS[i][1];
                if (localX >= x - 1 && localX < x + 18 && localY >= y - 1 && localY < y + 18) {
                    ThaumcraftNetwork.requestThaumatoriumFormulaFromClient(menu.blockPos(), i);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderThaumatoriumContent(PoseStack poseStack, int mouseX, int mouseY) {
        ThaumatoriumBlockEntity tile = tile();
        if (tile == null) {
            drawCenteredString(poseStack, font, Component.literal("Thaumatorium missing").withStyle(ChatFormatting.RED), leftPos + 88, topPos + 50, 0xEE6E6E);
            return;
        }

        ItemStack catalyst = tile.catalyst();
        if (!catalyst.isEmpty()) {
            itemRenderer.renderAndDecorateItem(catalyst, leftPos + 79, topPos + 54);
            itemRenderer.renderGuiItemDecorations(font, catalyst, leftPos + 79, topPos + 54);
        }

        List<AlchemyRecipe> candidates = new ArrayList<>(tile.visibleFormulaCandidates());
        for (int i = 0; i < FORMULA_SLOTS.length; i++) {
            int x = leftPos + FORMULA_SLOTS[i][0];
            int y = topPos + FORMULA_SLOTS[i][1];
            if (i < candidates.size()) {
                renderRecipeIcon(poseStack, candidates.get(i), x, y, tile.isSelectedFormula(candidates.get(i)));
            } else {
                fill(poseStack, x, y, x + 16, y + 16, 0x33000000);
            }
        }

        AlchemyRecipe active = tile.activeRecipe();
        if (active != null) {
            int x = leftPos + 28;
            int y = topPos + 82;
            for (Map.Entry<Aspect, Integer> entry : active.cost().entrySet()) {
                ResourceLocation texture = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + entry.getKey().id() + ".png");
                OriginalGuiTextures.blitOriginal(poseStack, x, y, texture, 16, 16);
                drawString(poseStack, font, Component.literal(String.valueOf(entry.getValue())), x + 11, y + 10, 0xFFFFFF);
                x += 24;
                if (x > leftPos + 140) {
                    x = leftPos + 28;
                    y += 18;
                }
            }
        }

        int progressWidth = Math.min(48, (int) (48.0F * tile.progress() / ThaumatoriumBlockEntity.ORIGINAL_CRAFT_INTERVAL_TICKS));
        fill(poseStack, leftPos + 64, topPos + 74, leftPos + 64 + progressWidth, topPos + 77, 0x99D6B25E);

        if (tile.hasMnemonicMatrix()) {
            drawString(poseStack, font, Component.literal("Mnemonic matrix").withStyle(ChatFormatting.GOLD), leftPos + 34, topPos + 112, 0xD6B25E);
        }
        if (!tile.lastMissing().isBlank()) {
            drawString(poseStack, font, Component.literal(tile.lastMissing()).withStyle(ChatFormatting.DARK_RED), leftPos + 34, topPos + 122, 0xAA5555);
        }
    }

    private void renderRecipeIcon(PoseStack poseStack, AlchemyRecipe recipe, int x, int y, boolean selected) {
        ItemStack stack = ItemStack.EMPTY;
        if (recipe != null && recipe.resultItemId() != null) {
            var item = ForgeRegistries.ITEMS.getValue(recipe.resultItemId());
            if (item != null) {
                stack = new ItemStack(item, recipe.resultCount());
            }
        }
        fill(poseStack, x - 1, y - 1, x + 17, y + 17, selected ? 0x88D6B25E : 0x44000000);
        if (!stack.isEmpty()) {
            itemRenderer.renderAndDecorateItem(stack, x, y);
        }
    }

    private ThaumatoriumBlockEntity tile() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }
        BlockEntity be = minecraft.level.getBlockEntity(menu.blockPos());
        return be instanceof ThaumatoriumBlockEntity thaumatorium ? thaumatorium : null;
    }
}
