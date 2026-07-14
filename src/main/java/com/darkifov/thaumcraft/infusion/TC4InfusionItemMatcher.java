package com.darkifov.thaumcraft.infusion;

import com.darkifov.thaumcraft.porting.TC4LegacyDuplicateItemMigrator;
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
        if (actualId == null || !canonical(actualId).equals(canonical(expectedId))) {
            // Old worlds and early rebuild recipes can still contain mirror ids
            // such as tc4_block_infusion_matrix. Treat those as the same TC4
            // object as the canonical 1.19.2 registry id instead of making an
            // otherwise correct infusion impossible to start.
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

    private static ResourceLocation canonical(ResourceLocation id) {
        if (id == null || !"thaumcraft".equals(id.getNamespace())) {
            return id;
        }
        String mapped = TC4LegacyDuplicateItemMigrator.mappings().get(id.getPath());
        return mapped == null ? id : new ResourceLocation("thaumcraft", mapped);
    }

    public static boolean catalystMatches(ItemStack stack, ResourceLocation expectedId, boolean wildcard, int expectedDamage, CompoundTag expectedTag) {
        if (wildcard) {
            return stack != null && !stack.isEmpty();
        }
        return matches(stack, expectedId, expectedDamage, expectedTag, true);
    }
}
