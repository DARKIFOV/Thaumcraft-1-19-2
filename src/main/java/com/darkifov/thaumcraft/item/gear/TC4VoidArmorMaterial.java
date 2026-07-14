package com.darkifov.thaumcraft.item.gear;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/** ArmorMaterial parity for TC4 Void armor. */
public enum TC4VoidArmorMaterial implements ArmorMaterial {
    INSTANCE;

    private static final int[] BASE_DURABILITY = new int[]{13, 15, 16, 11};
    private static final int[] DEFENSE = new int[]{3, 7, 6, 3};

    @Override
    public int getDurabilityForSlot(EquipmentSlot slot) {
        return BASE_DURABILITY[slot.getIndex()] * 10;
    }

    @Override
    public int getDefenseForSlot(EquipmentSlot slot) {
        return DEFENSE[slot.getIndex()];
    }

    @Override
    public int getEnchantmentValue() {
        return 10;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_IRON;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(ThaumcraftMod.VOID_METAL_INGOT.get());
    }

    @Override
    public String getName() {
        return "thaumcraft:void";
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
