package com.darkifov.thaumcraft.item.simple;

import com.darkifov.thaumcraft.infusion.TC4RunicArmorHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Item class for the eight TC4 runic ring/amulet/girdle variants. */
public class TC4RunicBaubleItem extends Item {
    private final TC4BaubleItem.SlotType slotType;

    public TC4RunicBaubleItem(Properties properties, TC4BaubleItem.SlotType slotType) {
        super(properties.stacksTo(1));
        this.slotType = slotType;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(switch (slotType) {
            case AMULET -> "Amulet / necklace";
            case RING -> "Ring";
            case BELT -> "Belt";
        }).withStyle(ChatFormatting.DARK_AQUA));
        TC4RunicArmorHelper.appendTooltip(stack, tooltip);
    }
}
