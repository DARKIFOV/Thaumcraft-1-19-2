package com.darkifov.thaumcraft.block;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Stores a copied item stack as a TC4-style golem filter card. */
public class GolemFilterItem extends Item {
    public static final String TAG_FILTER = "TC4GolemFilter";
    public static final String TAG_ALLOW_LIST = "TC4GolemAllowList";

    public GolemFilterItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            CompoundTag tag = stack.getOrCreateTag();
            if (player.isShiftKeyDown()) {
                boolean allow = !tag.getBoolean(TAG_ALLOW_LIST);
                tag.putBoolean(TAG_ALLOW_LIST, allow);
                player.displayClientMessage(Component.literal("Golem filter mode: " + (allow ? "Allow list" : "Deny list")).withStyle(ChatFormatting.AQUA), true);
            } else {
                ItemStack other = hand == InteractionHand.MAIN_HAND ? player.getOffhandItem() : player.getMainHandItem();
                if (!other.isEmpty() && other.getItem() != this) {
                    ItemStack copy = other.copy();
                    copy.setCount(1);
                    CompoundTag filterTag = new CompoundTag();
                    copy.save(filterTag);
                    tag.put(TAG_FILTER, filterTag);
                    player.displayClientMessage(Component.literal("Golem filter learned: ").append(copy.getHoverName()).withStyle(ChatFormatting.GOLD), true);
                } else {
                    tag.remove(TAG_FILTER);
                    player.displayClientMessage(Component.literal("Golem filter cleared.").withStyle(ChatFormatting.GRAY), true);
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static ItemStack getFilterStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_FILTER)) {
            return ItemStack.EMPTY;
        }
        return ItemStack.of(tag.getCompound(TAG_FILTER));
    }

    public static boolean isAllowList(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(TAG_ALLOW_LIST);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        ItemStack filter = getFilterStack(stack);
        boolean allow = isAllowList(stack);
        tooltip.add(Component.literal(allow ? "Allow-list filter" : "Deny-list filter").withStyle(allow ? ChatFormatting.GREEN : ChatFormatting.RED));
        if (!filter.isEmpty()) {
            tooltip.add(Component.literal("Item: ").append(filter.getHoverName()).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal("Right-click with item in other hand to bind.").withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.literal("Shift + right-click toggles allow/deny mode.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
