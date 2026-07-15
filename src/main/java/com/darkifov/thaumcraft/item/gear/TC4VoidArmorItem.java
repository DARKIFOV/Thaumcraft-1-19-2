package com.darkifov.thaumcraft.item.gear;

import com.darkifov.thaumcraft.infusion.TC4RunicArmorHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Functional 1.19.2 replacement for TC4 ItemVoidArmor. */
public class TC4VoidArmorItem extends ArmorItem {
    public TC4VoidArmorItem(EquipmentSlot slot, Properties properties) {
        super(TC4VoidArmorMaterial.INSTANCE, slot, properties.stacksTo(1));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.UNCOMMON;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return slot == EquipmentSlot.LEGS
                ? "thaumcraft:textures/models/void_2.png"
                : "thaumcraft:textures/models/void_1.png";
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        TC4VoidGearRuntime.repairTick(stack, level, entity);
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        TC4VoidGearRuntime.repairTick(stack, level, player);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        TC4VoidGearRuntime.appendTooltip(stack, tooltip, false);
        TC4RunicArmorHelper.appendTooltip(stack, tooltip);
    }
}
