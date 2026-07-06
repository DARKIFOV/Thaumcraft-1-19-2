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
            case AIR -> tooltip.add(Component.literal("TC4: faster movement and wider work range.").withStyle(ChatFormatting.GRAY));
            case FIRE -> tooltip.add(Component.literal("TC4: stronger combat and faster destructive work.").withStyle(ChatFormatting.GRAY));
            case WATER -> tooltip.add(Component.literal("TC4: careful harvesting and better item handling.").withStyle(ChatFormatting.GRAY));
            case EARTH -> tooltip.add(Component.literal("TC4: tougher body and larger carrying capacity.").withStyle(ChatFormatting.GRAY));
            case ORDER -> tooltip.add(Component.literal("TC4: precise sorting/filter work.").withStyle(ChatFormatting.GRAY));
            case ENTROPY -> tooltip.add(Component.literal("TC4: aggressive work with risky combat power.").withStyle(ChatFormatting.GRAY));
        }
    }
}
