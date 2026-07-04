package com.darkifov.thaumcraft.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public class TransvectorBinderItem extends Item {
    private static final String TAG_X = "TargetX";
    private static final String TAG_Y = "TargetY";
    private static final String TAG_Z = "TargetZ";
    private static final String TAG_BOUND = "Bound";

    public TransvectorBinderItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();

        if (player == null || level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        setTarget(stack, pos);
        player.displayClientMessage(Component.literal("Transvector Binder привязан к блоку: " + pos.toShortString()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        player.displayClientMessage(Component.literal("Теперь ПКМ этим Binder по Transvector Interface — записать цель в интерфейс.").withStyle(ChatFormatting.GRAY), false);
        return InteractionResult.CONSUME;
    }

    public static boolean hasTarget(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(TAG_BOUND);
    }

    public static BlockPos getTarget(ItemStack stack) {
        CompoundTag tag = stack.getTag();

        if (tag == null || !tag.getBoolean(TAG_BOUND)) {
            return null;
        }

        return new BlockPos(tag.getInt(TAG_X), tag.getInt(TAG_Y), tag.getInt(TAG_Z));
    }

    public static void setTarget(ItemStack stack, BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(TAG_BOUND, true);
        tag.putInt(TAG_X, pos.getX());
        tag.putInt(TAG_Y, pos.getY());
        tag.putInt(TAG_Z, pos.getZ());
    }

    public static void clear(ItemStack stack) {
        stack.removeTagKey(TAG_BOUND);
        stack.removeTagKey(TAG_X);
        stack.removeTagKey(TAG_Y);
        stack.removeTagKey(TAG_Z);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        BlockPos target = getTarget(stack);

        if (target != null) {
            tooltip.add(Component.literal("Bound: " + target.toShortString()).withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.literal("ПКМ по Transvector Interface: записать цель.").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal("ПКМ по блоку: запомнить цель.").withStyle(ChatFormatting.GRAY));
        }
    }
}
