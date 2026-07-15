package com.darkifov.thaumcraft.item.gear;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/** TC4 4.2.3.5 Void tool material: level 4, 150 uses, speed 8, damage bonus 3, enchantability 10. */
public enum TC4VoidToolTier implements Tier {
    INSTANCE;

    @Override
    public int getUses() {
        return 150;
    }

    @Override
    public float getSpeed() {
        return 8.0F;
    }

    @Override
    public float getAttackDamageBonus() {
        return 3.0F;
    }

    @Override
    public int getLevel() {
        return 4;
    }

    @Override
    public int getEnchantmentValue() {
        return 10;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(ThaumcraftMod.VOID_METAL_INGOT.get());
    }
}
