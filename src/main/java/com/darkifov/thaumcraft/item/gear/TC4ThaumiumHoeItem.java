package com.darkifov.thaumcraft.item.gear;

import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class TC4ThaumiumHoeItem extends HoeItem {
    public TC4ThaumiumHoeItem(Properties properties) {
        super(TC4ThaumiumToolTier.INSTANCE, -3, 0.0F, properties.stacksTo(1));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.UNCOMMON;
    }
}
