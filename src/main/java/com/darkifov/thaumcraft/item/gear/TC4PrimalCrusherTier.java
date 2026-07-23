package com.darkifov.thaumcraft.item.gear;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/** Original TC4 PRIMALVOID material used by the Primal Crusher. */
public enum TC4PrimalCrusherTier implements Tier {
    INSTANCE;

    @Override
    public int getUses() {
        return 500;
    }

    @Override
    public float getSpeed() {
        return 8.0F;
    }

    @Override
    public float getAttackDamageBonus() {
        return 4.0F;
    }

    @Override
    public int getLevel() {
        return 5;
    }

    @Override
    public int getEnchantmentValue() {
        return 20;
    }

    @Override
    public Ingredient getRepairIngredient() {
        // TC4 used itemResource meta 15 (the primal charm). The de-metadata'd
        // registry alias is resolved in PrimalCrusherItem#isValidRepairItem.
        return Ingredient.EMPTY;
    }
}
