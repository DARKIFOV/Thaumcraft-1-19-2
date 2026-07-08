package com.darkifov.thaumcraft.eldritch;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Stage223-232: tighter 1.19.2 mapping for TC4 lootbag potion / enchanted-book branches.
 */
public final class TC4LootPotionEnchantAdapter {
    private static final Potion[] COMMON_POTIONS = {
            Potions.HEALING, Potions.REGENERATION, Potions.SWIFTNESS, Potions.STRENGTH
    };
    private static final Potion[] UNCOMMON_POTIONS = {
            Potions.LONG_REGENERATION, Potions.STRONG_HEALING, Potions.LONG_SWIFTNESS, Potions.STRONG_STRENGTH
    };
    private static final Potion[] RARE_POTIONS = {
            Potions.STRONG_REGENERATION, Potions.LONG_FIRE_RESISTANCE, Potions.STRONG_HARMING, Potions.LONG_INVISIBILITY
    };
    private static final Enchantment[] ENCHANTS = {
            Enchantments.SHARPNESS, Enchantments.BLOCK_EFFICIENCY, Enchantments.UNBREAKING,
            Enchantments.ALL_DAMAGE_PROTECTION, Enchantments.MOB_LOOTING, Enchantments.POWER_ARROWS
    };

    private TC4LootPotionEnchantAdapter() {
    }

    public static ItemStack postProcessGeneratedLoot(ItemStack stack, int rarity, RandomSource random) {
        if (stack.isEmpty()) {
            return stack;
        }
        int md = Math.max(0, Math.min(2, rarity));
        if (stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION)) {
            Potion potion = randomPotion(md, random);
            PotionUtils.setPotion(stack, potion);
            CompoundTag tag = stack.getOrCreateTag();
            tag.putString("TC4PotionMetaFamily", md == 2 ? "rare" : md == 1 ? "uncommon" : "common");
            tag.putString("TC4OriginalBranch", "Utils.generateLoot:potion damage/meta");
        } else if (stack.is(Items.BOOK)) {
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            int base = 1 + md;
            Enchantment enchantment = ENCHANTS[random.nextInt(ENCHANTS.length)];
            int level = Math.max(1, Math.min(enchantment.getMaxLevel(), base + random.nextInt(2 + md)));
            EnchantedBookItem.addEnchantment(book, new EnchantmentInstance(enchantment, level));
            book.getOrCreateTag().putString("TC4OriginalBranch", "Utils.generateLoot:enchanted book");
            book.getOrCreateTag().putInt("TC4EnchantRarity", md);
            return book;
        }
        return stack;
    }

    private static Potion randomPotion(int rarity, RandomSource random) {
        Potion[] table = rarity >= 2 ? RARE_POTIONS : rarity == 1 ? UNCOMMON_POTIONS : COMMON_POTIONS;
        return table[random.nextInt(table.length)];
    }
}
