package com.darkifov.thaumcraft.recipe;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.EssentiaPhialItem;
import com.darkifov.thaumcraft.block.JarLabelItem;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Dynamic TC4 JarLabel0..47 + JarLabelNull parity recipe.
 *
 * blank label + filled 8-vis phial -> aspect-coded label + empty phial
 * aspect-coded label -> blank label
 */
public final class JarLabelRecipe extends CustomRecipe {
    public JarLabelRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        Scan scan = scan(container);
        return scan.validAssignment() || scan.validClear();
    }

    @Override
    public ItemStack assemble(CraftingContainer container) {
        Scan scan = scan(container);
        if (scan.validAssignment()) {
            return JarLabelItem.withAspect(scan.aspect());
        }
        if (scan.validClear()) {
            return new ItemStack(ThaumcraftMod.JAR_LABEL.get());
        }
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        Scan scan = scan(container);
        if (scan.validAssignment() && scan.phialSlot() >= 0) {
            remaining.set(scan.phialSlot(), new ItemStack(ThaumcraftMod.ESSENTIA_PHIAL.get()));
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ThaumcraftMod.JAR_LABEL_RECIPE.get();
    }

    private static Scan scan(CraftingContainer container) {
        ItemStack label = ItemStack.EMPTY;
        ItemStack phial = ItemStack.EMPTY;
        int phialSlot = -1;
        int occupied = 0;
        boolean invalid = false;

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) continue;
            occupied++;
            if (stack.is(ThaumcraftMod.JAR_LABEL.get()) && label.isEmpty()) {
                label = stack;
            } else if (stack.is(ThaumcraftMod.ESSENTIA_PHIAL.get()) && phial.isEmpty()) {
                phial = stack;
                phialSlot = slot;
            } else {
                invalid = true;
            }
        }

        Aspect labelAspect = JarLabelItem.getAspect(label);
        Aspect phialAspect = EssentiaPhialItem.getAspect(phial);
        int amount = EssentiaPhialItem.getAmount(phial);
        return new Scan(occupied, invalid, labelAspect, phialAspect, amount, phialSlot,
                !label.isEmpty(), !phial.isEmpty());
    }

    private record Scan(int occupied, boolean invalid, Aspect labelAspect, Aspect aspect,
                        int amount, int phialSlot, boolean hasLabel, boolean hasPhial) {
        boolean validAssignment() {
            return !invalid && occupied == 2 && hasLabel && hasPhial
                    && labelAspect == null && aspect != null && amount == 8;
        }

        boolean validClear() {
            return !invalid && occupied == 1 && hasLabel && !hasPhial && labelAspect != null;
        }
    }
}
