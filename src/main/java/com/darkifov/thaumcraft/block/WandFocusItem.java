package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.AspectStack;
import com.darkifov.thaumcraft.wand.WandFocusType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class WandFocusItem extends Item {
    private final WandFocusType focusType;

    public WandFocusItem(Properties properties, WandFocusType focusType) {
        super(properties.stacksTo(1));
        this.focusType = focusType;
    }

    public WandFocusType focusType() {
        return focusType;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(focusType.displayName()).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("TC4 wand focus. Put in off-hand and right-click with a wand to equip.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Original vis cost:").withStyle(ChatFormatting.DARK_AQUA));
        for (AspectStack aspect : focusType.visCost().all()) {
            tooltip.add(Component.literal("- " + aspect.aspect().displayName() + " " + aspect.amount()).withStyle(style -> style.withColor(aspect.aspect().textColor())));
        }
    }
}
