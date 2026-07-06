package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.golem.GolemMarkerMode;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

/** Portable TC4-style marker for golem home/input/output/guard/work positions. */
public class GolemTaskMarkerItem extends Item {
    public static final String TAG_MODE = "TC4GolemMarkerMode";
    public static final String TAG_HAS_POS = "TC4GolemMarkerHasPos";
    public static final String TAG_X = "TC4GolemMarkerX";
    public static final String TAG_Y = "TC4GolemMarkerY";
    public static final String TAG_Z = "TC4GolemMarkerZ";
    public static final String TAG_RADIUS = "TC4GolemMarkerRadius";
    public static final String TAG_PRIORITY = "TC4GolemMarkerPriority";

    public GolemTaskMarkerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            CompoundTag tag = stack.getOrCreateTag();
            if (player.isShiftKeyDown()) {
                GolemMarkerMode mode = GolemMarkerMode.byName(tag.getString(TAG_MODE)).next();
                tag.putString(TAG_MODE, mode.id());
                player.displayClientMessage(Component.literal("Marker mode: ").append(mode.displayName()), true);
            } else {
                int radius = nextRadius(tag.getInt(TAG_RADIUS));
                tag.putInt(TAG_RADIUS, radius);
                player.displayClientMessage(Component.literal("Marker work radius: " + radius).withStyle(ChatFormatting.YELLOW), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide || context.getPlayer() == null) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = context.getItemInHand();
        CompoundTag tag = stack.getOrCreateTag();
        if (context.getPlayer().isShiftKeyDown()) {
            GolemMarkerMode mode = GolemMarkerMode.byName(tag.getString(TAG_MODE)).next();
            tag.putString(TAG_MODE, mode.id());
            context.getPlayer().displayClientMessage(Component.literal("Marker mode: ").append(mode.displayName()), true);
            return InteractionResult.CONSUME;
        }

        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        tag.putBoolean(TAG_HAS_POS, true);
        tag.putInt(TAG_X, pos.getX());
        tag.putInt(TAG_Y, pos.getY());
        tag.putInt(TAG_Z, pos.getZ());
        if (!tag.contains(TAG_RADIUS)) {
            tag.putInt(TAG_RADIUS, 8);
        }
        if (!tag.contains(TAG_PRIORITY)) {
            tag.putInt(TAG_PRIORITY, 0);
        }
        GolemMarkerMode mode = GolemMarkerMode.byName(tag.getString(TAG_MODE));
        context.getPlayer().displayClientMessage(Component.literal("Stored ").append(mode.displayName()).append(Component.literal(" marker at " + pos.toShortString())).withStyle(ChatFormatting.GOLD), false);
        return InteractionResult.CONSUME;
    }

    public static GolemMarkerMode getMode(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? GolemMarkerMode.HOME : GolemMarkerMode.byName(tag.getString(TAG_MODE));
    }

    public static int getRadius(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        int radius = tag == null ? 8 : tag.getInt(TAG_RADIUS);
        return radius <= 0 ? 8 : Math.max(1, Math.min(32, radius));
    }

    public static int getPriority(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(TAG_PRIORITY);
    }

    private static int nextRadius(int current) {
        if (current < 4) {
            return 4;
        }
        if (current < 8) {
            return 8;
        }
        if (current < 16) {
            return 16;
        }
        return 4;
    }

    public static BlockPos getPosition(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.getBoolean(TAG_HAS_POS)) {
            return null;
        }
        return new BlockPos(tag.getInt(TAG_X), tag.getInt(TAG_Y), tag.getInt(TAG_Z));
    }

    public static void writePosition(CompoundTag target, GolemMarkerMode mode, BlockPos pos) {
        writePosition(target, mode, pos, 8, 0);
    }

    public static void writePosition(CompoundTag target, GolemMarkerMode mode, BlockPos pos, int radius, int priority) {
        if (target == null || pos == null) {
            return;
        }
        String prefix = markerPrefix(mode);
        target.putBoolean(prefix + "Has", true);
        target.putInt(prefix + "X", pos.getX());
        target.putInt(prefix + "Y", pos.getY());
        target.putInt(prefix + "Z", pos.getZ());
        target.putInt(prefix + "Radius", Math.max(1, Math.min(32, radius)));
        target.putInt(prefix + "Priority", Math.max(0, Math.min(9, priority)));
    }

    public static String markerPrefix(GolemMarkerMode mode) {
        return "TC4" + mode.id().substring(0, 1).toUpperCase() + mode.id().substring(1) + "Marker";
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        GolemMarkerMode mode = getMode(stack);
        tooltip.add(Component.literal("Mode: ").append(mode.displayName()));
        BlockPos pos = getPosition(stack);
        if (pos != null) {
            tooltip.add(Component.literal("Target: " + pos.toShortString()).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal("Right-click block to bind target.").withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.literal("Radius: " + getRadius(stack) + " / Priority: " + getPriority(stack)).withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("Shift + right-click cycles marker role.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Right-click air cycles work radius.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
