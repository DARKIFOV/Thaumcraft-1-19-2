package com.darkifov.thaumcraft.item.simple;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Explicit passive alias for TC4 focus sprites that have no registered casting implementation. */
public class TC4LegacyFocusComponentItem extends Item {
    private final boolean orphanedReversalSprite;

    public TC4LegacyFocusComponentItem(Properties properties, boolean orphanedReversalSprite) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
        this.orphanedReversalSprite = orphanedReversalSprite;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(orphanedReversalSprite
                ? "Legacy reversal-focus sprite; no TC4 runtime registration exists"
                : "Blank wand-focus crafting component").withStyle(ChatFormatting.GRAY));
    }
}
