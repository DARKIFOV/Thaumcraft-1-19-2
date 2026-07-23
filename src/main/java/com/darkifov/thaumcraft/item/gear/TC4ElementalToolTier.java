package com.darkifov.thaumcraft.item.gear;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/** TC4 4.2.3.5 elemental tool material: level 3, 1500 uses, speed 10, damage bonus 3, enchantability 18. */
public enum TC4ElementalToolTier implements Tier {
    INSTANCE;

    @Override
    public int getUses() {
        return 1500;
    }

    @Override
    public float getSpeed() {
        return 10.0F;
    }

    @Override
    public float getAttackDamageBonus() {
        return 3.0F;
    }

    @Override
    public int getLevel() {
        return 3;
    }

    @Override
    public int getEnchantmentValue() {
        return 18;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(ThaumcraftMod.THAUMIUM_INGOT.get());
    }
}
