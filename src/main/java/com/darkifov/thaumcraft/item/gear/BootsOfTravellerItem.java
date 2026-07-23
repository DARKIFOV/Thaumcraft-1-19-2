package com.darkifov.thaumcraft.item.gear;

import com.darkifov.thaumcraft.infusion.TC4RunicArmorHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Functional Forge 1.19.2 port of TC4 ItemBootsTraveller. */
public final class BootsOfTravellerItem extends ArmorItem {
    public BootsOfTravellerItem(Properties properties) {
        super(TC4TravellerArmorMaterial.INSTANCE, EquipmentSlot.FEET, properties.stacksTo(1));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.RARE;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "thaumcraft:textures/models/bootstraveler.png";
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        TC4RunicArmorHelper.appendTooltip(stack, tooltip);
    }
}
