package com.darkifov.thaumcraft.item.gear;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/** TC4 4.2.3.5 Thaumium tool material: level 3, 400 uses, speed 7, damage bonus 2, enchantability 22. */
public enum TC4ThaumiumToolTier implements Tier {
    INSTANCE;

    @Override
    public int getUses() {
        return 400;
    }

    @Override
    public float getSpeed() {
        return 7.0F;
    }

    @Override
    public float getAttackDamageBonus() {
        return 2.0F;
    }

    @Override
    public int getLevel() {
        return 3;
    }

    @Override
    public int getEnchantmentValue() {
        return 22;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(ThaumcraftMod.THAUMIUM_INGOT.get());
    }
}
