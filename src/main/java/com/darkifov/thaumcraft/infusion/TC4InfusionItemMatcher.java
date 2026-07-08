package com.darkifov.thaumcraft.infusion;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Stage209 adapter for TC4 InfusionRecipe.areItemStacksEqual.
 *
 * TC4 1.7.10 compares ItemStack tags first, supports OreDictionary/fuzzy
 * equivalents, and treats damage 32767 as a wildcard.  The 1.19.2 port cannot
 * represent every pre-flattening metadata variant, but this matcher preserves
 * the semantics available in modern ItemStack data: item id, optional damage,
 * optional NBT, and the wildcard damage sentinel.
 */
public final class TC4InfusionItemMatcher {
    public static final int WILDCARD_DAMAGE = 32767;
    public static final int ANY_DAMAGE = -1;

    private TC4InfusionItemMatcher() {
    }

    public static boolean matches(ItemStack stack, ResourceLocation expectedId) {
        return matches(stack, expectedId, ANY_DAMAGE, null, false);
    }

    public static boolean matches(ItemStack stack, ResourceLocation expectedId, int expectedDamage, CompoundTag expectedTag, boolean fuzzy) {
        if (stack == null || stack.isEmpty() || expectedId == null) {
            return false;
        }

        ResourceLocation actualId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (actualId == null || !actualId.equals(expectedId)) {
            // TC4's OreDictionary fuzzy path is represented by modern tags in
            // recipe data.  Current materialized TC4 recipes mostly resolve to
            // concrete ids, so this branch intentionally stays conservative.
            return false;
        }

        if (expectedTag != null && !expectedTag.isEmpty()) {
            CompoundTag actualTag = stack.getTag();
            if (actualTag == null || !actualTag.equals(expectedTag)) {
                return false;
            }
        }

        if (expectedDamage != ANY_DAMAGE && expectedDamage != WILDCARD_DAMAGE) {
            return stack.getDamageValue() == expectedDamage;
        }

        return true;
    }

    public static boolean catalystMatches(ItemStack stack, ResourceLocation expectedId, boolean wildcard, int expectedDamage, CompoundTag expectedTag) {
        if (wildcard) {
            return stack != null && !stack.isEmpty();
        }
        return matches(stack, expectedId, expectedDamage, expectedTag, true);
    }
}
