package com.darkifov.thaumcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Functional 1.19.2 replacement for TC4 {@code ItemSanityChecker}.
 *
 * <p>The original item stores no NBT and performs no server-side action. Its
 * gameplay contract is the client HUD shown while it is held in the main hand;
 * that renderer lives in {@code SanityCheckerOverlayEvents}.</p>
 */
public final class TC4SanityCheckerItem extends Item {
    public TC4SanityCheckerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.thaumcraft.sanity_checker")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
