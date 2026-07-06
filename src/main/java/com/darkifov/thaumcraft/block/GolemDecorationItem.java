package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.golem.GolemDecorationType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** TC4-style golem decoration item. Right-click a golem or combine with a core. */
public class GolemDecorationItem extends Item {
    private final GolemDecorationType decorationType;

    public GolemDecorationItem(Properties properties, GolemDecorationType decorationType) {
        super(properties.stacksTo(16));
        this.decorationType = decorationType;
    }

    public GolemDecorationType getDecorationType() {
        return decorationType;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Golem decoration: ").append(decorationType.displayName()));
        tooltip.add(Component.literal("Right-click a golem or combine with a golem core.").withStyle(ChatFormatting.GRAY));
    }
}
