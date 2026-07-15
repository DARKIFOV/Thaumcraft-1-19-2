package com.darkifov.thaumcraft.item.gear;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;

public class TC4ThaumiumPickaxeItem extends PickaxeItem {
    public TC4ThaumiumPickaxeItem(Properties properties) {
        super(TC4ThaumiumToolTier.INSTANCE, 1, -2.8F, properties.stacksTo(1));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.UNCOMMON;
    }
}
