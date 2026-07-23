package com.darkifov.thaumcraft.item.simple;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Simple 1.19.2 replacement for TC4's blank Baubles crafting bases. */
public class TC4BaubleItem extends Item {
    public enum SlotType {
        AMULET("Amulet / necklace"),
        RING("Ring"),
        BELT("Belt");

        private final String display;

        SlotType(String display) {
            this.display = display;
        }
    }

    private final SlotType slotType;

    public TC4BaubleItem(Properties properties, SlotType slotType) {
        super(properties.stacksTo(1));
        this.slotType = slotType;
    }

    public SlotType slotType() {
        return slotType;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(slotType.display).withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.literal("TC4 Baubles crafting base").withStyle(ChatFormatting.GRAY));
    }
}
