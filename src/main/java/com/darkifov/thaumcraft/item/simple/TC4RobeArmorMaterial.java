package com.darkifov.thaumcraft.item.simple;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/** Exact TC4 SPECIAL and FORTRESS armor-material stat lines. */
public enum TC4RobeArmorMaterial implements ArmorMaterial {
    SPECIAL(25, new int[] {1, 2, 3, 1}, 25, "special"),
    FORTRESS(40, new int[] {3, 6, 7, 3}, 25, "fortress");

    private final int durabilityMultiplier;
    private final int[] defense;
    private final int enchantability;
    private final String name;

    TC4RobeArmorMaterial(int durabilityMultiplier, int[] defense, int enchantability, String name) {
        this.durabilityMultiplier = durabilityMultiplier;
        this.defense = defense;
        this.enchantability = enchantability;
        this.name = name;
    }

    private static int baseDurability(EquipmentSlot slot) {
        return switch (slot) {
            case FEET -> 13;
            case LEGS -> 15;
            case CHEST -> 16;
            case HEAD -> 11;
            default -> 0;
        };
    }

    private static int armorIndex(EquipmentSlot slot) {
        return switch (slot) {
            case FEET -> 0;
            case LEGS -> 1;
            case CHEST -> 2;
            case HEAD -> 3;
            default -> 0;
        };
    }

    @Override
    public int getDurabilityForSlot(EquipmentSlot slot) {
        return baseDurability(slot) * durabilityMultiplier;
    }

    @Override
    public int getDefenseForSlot(EquipmentSlot slot) {
        return defense[armorIndex(slot)];
    }

    @Override
    public int getEnchantmentValue() {
        return enchantability;
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
        return "thaumcraft:" + name;
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
