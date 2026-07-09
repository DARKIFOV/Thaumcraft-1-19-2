package com.darkifov.thaumcraft.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class ScribingToolsItem extends Item {
    public static final int MAX_INK = 100;
    public static final int TC4_INK_SAC_REFILL_AMOUNT = MAX_INK;
    public static final String TC4_ORIGINAL_REFILL_RECIPE_ID = "scribing_tools_refill_original_tc4_style";

    public ScribingToolsItem(Properties properties) {
        super(properties.durability(MAX_INK));
    }

    public static int inkLeft(ItemStack stack) {
        if (!(stack.getItem() instanceof ScribingToolsItem)) {
            return 0;
        }

        return Math.max(0, MAX_INK - stack.getDamageValue());
    }

    public static boolean hasInk(ItemStack stack) {
        return inkLeft(stack) > 0;
    }

    public static boolean consumeInk(ItemStack stack, int amount) {
        if (!(stack.getItem() instanceof ScribingToolsItem) || amount <= 0) {
            return false;
        }

        if (inkLeft(stack) < amount) {
            return false;
        }

        stack.setDamageValue(Math.min(MAX_INK, stack.getDamageValue() + amount));
        return true;
    }

    public static void refill(ItemStack stack) {
        if (stack.getItem() instanceof ScribingToolsItem) {
            stack.setDamageValue(0);
        }
    }

    /**
     * Stage623-642: original TC4 inkwell/scribing-tools refill is treated as a
     * crafting-table refill path, not as a new right-click mechanic.  The helper
     * is intentionally small so custom recipes/audits can validate the same
     * damage semantics: one ink sac restores the original inkwell to full ink.
     */
    public static boolean refillFromInkSac(ItemStack stack) {
        if (!(stack.getItem() instanceof ScribingToolsItem) || inkLeft(stack) >= MAX_INK) {
            return false;
        }
        stack.setDamageValue(0);
        return true;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.getDamageValue() > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * inkLeft(stack) / MAX_INK);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Ink: " + inkLeft(stack) + " / " + MAX_INK).withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.literal("TC4 refill: craft with an ink sac.").withStyle(ChatFormatting.GRAY));
    }
}
