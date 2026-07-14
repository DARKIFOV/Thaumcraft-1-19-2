package com.darkifov.thaumcraft.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** TC4 bath salts expire after 200 ticks; the expiry conversion is handled by CommonEvents. */
public class BathSaltsItem extends Item {
    public static final int DISSOLVE_TICKS = 200;

    public BathSaltsItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getEntityLifespan(ItemStack itemStack, Level level) {
        return DISSOLVE_TICKS;
    }
}
