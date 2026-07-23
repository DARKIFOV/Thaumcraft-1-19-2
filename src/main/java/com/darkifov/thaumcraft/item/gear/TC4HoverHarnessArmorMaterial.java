package com.darkifov.thaumcraft.item.gear;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

/** Exact functional material contract used by TC4's SPECIAL hover harness armor. */
public enum TC4HoverHarnessArmorMaterial implements ArmorMaterial {
    INSTANCE;

    @Override
    public int getDurabilityForSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.CHEST ? 400 : 0;
    }

    @Override
    public int getDefenseForSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.CHEST ? 3 : 0;
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
        return Ingredient.of(Items.GOLD_INGOT);
    }

    @Override
    public String getName() {
        return "thaumcraft:hover_harness";
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
