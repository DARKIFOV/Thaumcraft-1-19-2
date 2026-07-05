package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public enum IchorToolTier implements Tier {
    INSTANCE;

    @Override
    public int getUses() {
        return 4096;
    }

    @Override
    public float getSpeed() {
        return 12.0F;
    }

    @Override
    public float getAttackDamageBonus() {
        return 5.0F;
    }

    @Override
    public int getLevel() {
        return 4;
    }

    @Override
    public int getEnchantmentValue() {
        return 28;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(ThaumcraftMod.ICHORIUM_INGOT.get());
    }
}
