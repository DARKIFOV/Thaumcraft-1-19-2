package com.darkifov.thaumcraft.enchantment;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Modern registry implementation of TC4 {@code EnchantmentRepair}.
 *
 * <p>The original enchantment accepted damageable items implementing
 * {@code IRepairable}, had two levels and was incompatible with Unbreaking.
 * Forge 1.19.2 no longer has that marker interface, so the port uses the
 * data-driven {@code thaumcraft:repairable} item tag. The tag contains the
 * original TC4 repairable equipment families instead of every item in the
 * mod namespace.</p>
 */
public final class TC4RepairEnchantment extends Enchantment {
    public static final TagKey<Item> REPAIRABLE = TagKey.create(
            Registry.ITEM_REGISTRY,
            new ResourceLocation(ThaumcraftMod.MOD_ID, "repairable")
    );

    public TC4RepairEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.BREAKABLE, new EquipmentSlot[]{
                EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
                EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET
        });
    }

    @Override
    public int getMinCost(int level) {
        return 20 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        if (stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK)) {
            return true;
        }
        return stack.isDamageableItem() && stack.is(REPAIRABLE);
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return other != Enchantments.UNBREAKING && super.checkCompatibility(other);
    }
}
