package com.darkifov.thaumcraft.item.gear;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Armor material used by TC4's Boots of the Traveller.
 *
 * <p>The original SPECIAL material supplied one armor point for boots and an
 * enchantability of 25. ItemBootsTraveller then overrode its maximum damage to
 * 350, so the modern material returns that exact durability for the feet slot.</p>
 */
public enum TC4TravellerArmorMaterial implements ArmorMaterial {
    INSTANCE;

    @Override
    public int getDurabilityForSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.FEET ? 350 : 0;
    }

    @Override
    public int getDefenseForSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.FEET ? 1 : 0;
    }

    @Override
    public int getEnchantmentValue() {
        return 25;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_LEATHER;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }

    @Override
    public String getName() {
        return "thaumcraft:traveller";
    }

    @Override
    public float getToughness() {
        return 0.0F;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.0F;
    }
}
