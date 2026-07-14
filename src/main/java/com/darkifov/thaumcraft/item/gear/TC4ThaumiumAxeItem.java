package com.darkifov.thaumcraft.item.gear;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class TC4ThaumiumAxeItem extends AxeItem {
    public TC4ThaumiumAxeItem(Properties properties) {
        super(TC4ThaumiumToolTier.INSTANCE, 5.0F, -3.0F, properties.stacksTo(1));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.UNCOMMON;
    }
}
