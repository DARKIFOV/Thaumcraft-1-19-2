package com.darkifov.thaumcraft.item.gear;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;

public class TC4ThaumiumSwordItem extends SwordItem {
    public TC4ThaumiumSwordItem(Properties properties) {
        super(TC4ThaumiumToolTier.INSTANCE, 3, -2.4F, properties.stacksTo(1));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.UNCOMMON;
    }
}
