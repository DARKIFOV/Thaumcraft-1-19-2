package com.darkifov.thaumcraft.item.simple;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.infusion.TC4RunicArmorHelper;
import com.darkifov.thaumcraft.runic.TC4WarpingGearAdapter;
import com.darkifov.thaumcraft.wand.TC4VisDiscountGear;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Shared cultist robe, plate and leader-plate implementation. */
public class TC4CultistArmorItem extends ArmorItem implements TC4VisDiscountGear {
    public enum Family { ROBE, PLATE, LEADER }

    private final Family family;

    public TC4CultistArmorItem(Family family, EquipmentSlot slot, Properties properties) {
        super(family == Family.LEADER ? TC4RobeArmorMaterial.FORTRESS : ArmorMaterials.IRON,
                slot, properties.stacksTo(1));
        this.family = family;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return switch (family) {
            case ROBE -> slot == EquipmentSlot.FEET
                    ? "thaumcraft:textures/models/cultistboots.png"
                    : "thaumcraft:textures/models/cultist_robe_armor.png";
            case PLATE -> "thaumcraft:textures/original/thaumcraft4/models/cultist_plate_armor.png";
            case LEADER -> "thaumcraft:textures/models/cultist_leader_armor.png";
        };
    }

    @Override
    public int getVisDiscount(ItemStack stack, LivingEntity wearer, Aspect aspect) {
        return family == Family.ROBE ? 1 : 0;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack ingredient) {
        return ingredient.is(Items.IRON_INGOT) || super.isValidRepairItem(toRepair, ingredient);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        int discount = getVisDiscount(stack, null, null);
        if (discount > 0) {
            tooltip.add(Component.literal("Vis discount: " + discount + "%").withStyle(ChatFormatting.DARK_PURPLE));
        }
        TC4RunicArmorHelper.appendTooltip(stack, tooltip);
        TC4WarpingGearAdapter.appendTooltip(stack, null, tooltip);
    }
}
