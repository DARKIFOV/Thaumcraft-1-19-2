package com.darkifov.thaumcraft.item.gear;

import com.darkifov.thaumcraft.infusion.TC4RunicArmorHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Functional Forge 1.19.2 port of TC4 ItemGirdleHover. */
public final class HoverGirdleItem extends Item {
    public HoverGirdleItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.RARE;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.thaumcraft.hover_girdle"));
        TC4RunicArmorHelper.appendTooltip(stack, tooltip);
    }
}
