package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/** TC4 Crimson Blade material: level 4, 200 uses, speed 8, +3.5 damage, enchantability 20. */
public enum TC4CrimsonToolTier implements Tier {
    INSTANCE;

    @Override public int getUses() { return 200; }
    @Override public float getSpeed() { return 8.0F; }
    @Override public float getAttackDamageBonus() { return 3.5F; }
    @Override public int getLevel() { return 4; }
    @Override public int getEnchantmentValue() { return 20; }
    @Override public Ingredient getRepairIngredient() {
        return Ingredient.of(ThaumcraftMod.TC4_RESEARCH_ITEMS.get("tc4_charm").get());
    }
}
