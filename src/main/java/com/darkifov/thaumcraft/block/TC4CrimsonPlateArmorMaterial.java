package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Stage221: 1.19.2 ArmorMaterial bridge for TC4 crimson/cultist plate armor.
 * Original anchor: ConfigItems.itemChestCultistPlate is dropped by EntityEldritchCrab
 * when its helm bit breaks below half health.
 */
public enum TC4CrimsonPlateArmorMaterial implements ArmorMaterial {
    INSTANCE;

    private static final int[] DURABILITY = new int[]{13, 15, 16, 11};
    private static final int[] DEFENSE = new int[]{2, 6, 5, 2};

    @Override
    public int getDurabilityForSlot(EquipmentSlot slot) {
        return DURABILITY[slot.getIndex()] * 30;
    }

    @Override
    public int getDefenseForSlot(EquipmentSlot slot) {
        return DEFENSE[slot.getIndex()];
    }

    @Override
    public int getEnchantmentValue() {
        return 18;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_IRON;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(ThaumcraftMod.THAUMIUM_INGOT.get());
    }

    @Override
    public String getName() {
        return "thaumcraft:tc4_crimson_plate";
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
