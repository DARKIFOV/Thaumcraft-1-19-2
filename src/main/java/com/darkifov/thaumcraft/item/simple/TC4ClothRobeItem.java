package com.darkifov.thaumcraft.item.simple;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.infusion.TC4RunicArmorHelper;
import com.darkifov.thaumcraft.wand.TC4VisDiscountGear;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/** Dyeable TC4 thaumaturge robes with the original per-piece vis discount. */
public class TC4ClothRobeItem extends ArmorItem implements DyeableLeatherItem, TC4VisDiscountGear {
    private final EquipmentSlot armorSlot;

    public TC4ClothRobeItem(EquipmentSlot slot, Properties properties) {
        super(TC4RobeArmorMaterial.SPECIAL, slot, properties.stacksTo(1));
        this.armorSlot = slot;
    }

    @Override
    public int getColor(ItemStack stack) {
        CompoundTag display = stack.getTagElement("display");
        return display != null && display.contains("color", Tag.TAG_ANY_NUMERIC)
                ? display.getInt("color")
                : 6961280;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        String layer = slot == EquipmentSlot.LEGS ? "robes_2" : "robes_1";
        return "thaumcraft:textures/models/" + layer + (type == null ? "" : "_overlay") + ".png";
    }

    @Override
    public int getVisDiscount(ItemStack stack, LivingEntity wearer, Aspect aspect) {
        return armorSlot == EquipmentSlot.FEET ? 1 : 2;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack ingredient) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(ingredient.getItem());
        return new ResourceLocation("thaumcraft", "tc4_cloth").equals(id)
                || super.isValidRepairItem(toRepair, ingredient);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Vis discount: " + getVisDiscount(stack, null, null) + "%")
                .withStyle(ChatFormatting.DARK_PURPLE));
        TC4RunicArmorHelper.appendTooltip(stack, tooltip);
    }
}
