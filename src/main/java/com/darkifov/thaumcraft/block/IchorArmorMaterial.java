package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public enum IchorArmorMaterial implements ArmorMaterial {
    INSTANCE;

    private static final int[] DURABILITY = new int[]{13, 15, 16, 11};
    private static final int[] DEFENSE = new int[]{4, 8, 10, 4};

    @Override
    public int getDurabilityForSlot(EquipmentSlot slot) {
        return DURABILITY[slot.getIndex()] * 48;
    }

    @Override
    public int getDefenseForSlot(EquipmentSlot slot) {
        return DEFENSE[slot.getIndex()];
    }

    @Override
    public int getEnchantmentValue() {
        return 28;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_GOLD;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(ThaumcraftMod.ICHORCLOTH.get());
    }

    @Override
    public String getName() {
        return "thaumcraft:ichorcloth";
    }

    @Override
    public float getToughness() {
        return 3.0F;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.08F;
    }
}
