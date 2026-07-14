package com.darkifov.thaumcraft.item.gear;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;

public class TC4ThaumiumShovelItem extends ShovelItem {
    public TC4ThaumiumShovelItem(Properties properties) {
        super(TC4ThaumiumToolTier.INSTANCE, 1.5F, -3.0F, properties.stacksTo(1));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.UNCOMMON;
    }
}
