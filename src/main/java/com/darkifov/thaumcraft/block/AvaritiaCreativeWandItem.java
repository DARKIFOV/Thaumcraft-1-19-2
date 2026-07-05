package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.wand.WandCapType;
import com.darkifov.thaumcraft.wand.WandRodType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class AvaritiaCreativeWandItem extends WandItem {
    public AvaritiaCreativeWandItem(Properties properties) {
        super(properties, INFINITE_VIS_DISPLAY, WandRodType.CREATIVE, WandCapType.INFINITY);
    }

    @Override
    public boolean isInfiniteVis(ItemStack stack) {
        return true;
    }

    @Override
    public int stackVisCapacity(ItemStack stack) {
        return INFINITE_VIS_DISPLAY;
    }

    @Override
    public float stackVisCostModifier(ItemStack stack) {
        return 0.0F;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Avaritia Addon Extension").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.literal("Infinite primal vis").withStyle(ChatFormatting.LIGHT_PURPLE));

        StringBuilder builder = new StringBuilder();
        for (Aspect aspect : Aspect.values()) {
            if (!builder.isEmpty()) {
                builder.append(", ");
            }
            builder.append(aspect.displayName()).append(" ∞");
        }

        tooltip.add(Component.literal(builder.toString()).withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
