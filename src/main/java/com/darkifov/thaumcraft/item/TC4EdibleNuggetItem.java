package com.darkifov.thaumcraft.item;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** TC4 ItemNuggetEdible parity: 1 hunger, 0.3 saturation modifier, 10-tick use time. */
public class TC4EdibleNuggetItem extends Item {
    private static final FoodProperties FOOD = new FoodProperties.Builder()
            .nutrition(1).saturationMod(0.3F).build();

    public TC4EdibleNuggetItem(Properties properties) {
        super(properties.food(FOOD));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 10;
    }
}
