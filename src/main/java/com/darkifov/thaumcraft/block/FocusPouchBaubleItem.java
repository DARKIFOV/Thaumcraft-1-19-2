package com.darkifov.thaumcraft.block;

import net.minecraft.world.item.ItemStack;

/**
 * Stage186: ItemFocusPouchBauble parity marker.
 * Original TC4 registers itemFocusPouch as new ItemFocusPouchBauble(), a belt bauble.
 * Forge 1.19.2 has no Baubles API in the base port, so this class preserves the item class split
 * and exposes canEquip/canUnequip adapter hooks while retaining ItemFocusPouch storage/GUI behavior.
 */
public class FocusPouchBaubleItem extends FocusPouchItem {
    public FocusPouchBaubleItem(Properties properties) {
        super(properties);
    }

    public String getBaubleType(ItemStack itemstack) {
        return "BELT";
    }

    public boolean canEquip(ItemStack itemstack) {
        return true;
    }

    public boolean canUnequip(ItemStack itemstack) {
        return true;
    }
}
