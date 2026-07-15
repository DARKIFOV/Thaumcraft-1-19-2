package com.darkifov.thaumcraft.enchantment;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.registries.ForgeRegistries;

/** Modern registry implementation of TC4 {@code EnchantmentHaste}. */
public final class TC4HasteEnchantment extends Enchantment {
    private static final ResourceLocation HOVER_HARNESS =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "tc4_hoverharness");

    public TC4HasteEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_FEET,
                new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.CHEST});
    }

    @Override
    public int getMinCost(int level) {
        return 15 + (level - 1) * 9;
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        if (stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK)) {
            return true;
        }
        if (stack.getItem() instanceof ArmorItem armor && armor.getSlot() == EquipmentSlot.FEET) {
            return true;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return HOVER_HARNESS.equals(id);
    }
}
