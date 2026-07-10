package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.golem.GolemUpgradeType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class GolemUpgradeItem extends Item {
    private final GolemUpgradeType upgradeType;

    public GolemUpgradeItem(Properties properties, GolemUpgradeType upgradeType) {
        super(properties.stacksTo(16));
        this.upgradeType = upgradeType;
    }

    public GolemUpgradeType getUpgradeType() {
        return upgradeType;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Golem upgrade: ").append(upgradeType.displayName()));
        switch (upgradeType) {
            case AIR -> tooltip.add(Component.literal("TC4: +15% movement speed per installed rune.").withStyle(ChatFormatting.GRAY));
            case EARTH -> tooltip.add(Component.literal("TC4: more carry space and +1 strength; two runes stack.").withStyle(ChatFormatting.GRAY));
            case FIRE -> tooltip.add(Component.literal("TC4: melee attacks ignite targets; improves destructive jobs.").withStyle(ChatFormatting.GRAY));
            case WATER -> tooltip.add(Component.literal("TC4: larger work range and more accurate dart attacks.").withStyle(ChatFormatting.GRAY));
            case ORDER -> tooltip.add(Component.literal("TC4: coloured markers, precision targets and crop replanting.").withStyle(ChatFormatting.GRAY));
            case ENTROPY -> tooltip.add(Component.literal("TC4: advanced item matching and retaliatory combat effects.").withStyle(ChatFormatting.GRAY));
        }
    }
}
