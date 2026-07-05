package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class EssentiaCellItem extends Item {
    private static final String TAG_ASPECT = "Aspect";
    private static final String TAG_AMOUNT = "Amount";
    private static final String TAG_PARTITION_ASPECT = "PartitionAspect";

    private final int capacity;

    public EssentiaCellItem(Properties properties, int capacity) {
        super(properties);
        this.capacity = capacity;
    }

    public int capacity() {
        return capacity;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player == null || level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());

        if (!(blockEntity instanceof EssentiaJarBlockEntity jar)) {
            showStatus(player, stack);
            return InteractionResult.CONSUME;
        }

        if (player.isShiftKeyDown()) {
            int moved = transferToJar(stack, jar, 64);

            if (moved <= 0) {
                player.displayClientMessage(Component.literal("Цифровая ячейка не смогла выгрузить essentia в jar.").withStyle(ChatFormatting.RED), false);
            } else {
                player.displayClientMessage(Component.literal("Выгружено в jar: " + moved + " essentia.").withStyle(ChatFormatting.AQUA), false);
            }
        } else {
            int moved = transferFromJar(stack, jar, 64);

            if (moved <= 0) {
                player.displayClientMessage(Component.literal("Цифровая ячейка не смогла забрать essentia из jar.").withStyle(ChatFormatting.RED), false);
            } else {
                player.displayClientMessage(Component.literal("Загружено в ячейку: " + moved + " essentia.").withStyle(ChatFormatting.AQUA), false);
            }
        }

        return InteractionResult.CONSUME;
    }

    public static Aspect getAspect(ItemStack stack) {
        CompoundTag tag = stack.getTag();

        if (tag == null || !tag.contains(TAG_ASPECT)) {
            return null;
        }

        try {
            return Aspect.valueOf(tag.getString(TAG_ASPECT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public static int getAmount(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(TAG_AMOUNT);
    }

    public static Aspect getPartitionAspect(ItemStack stack) {
        CompoundTag tag = stack.getTag();

        if (tag == null || !tag.contains(TAG_PARTITION_ASPECT)) {
            return null;
        }

        try {
            return Aspect.valueOf(tag.getString(TAG_PARTITION_ASPECT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public static void setPartitionAspect(ItemStack stack, Aspect aspect) {
        if (aspect == null) {
            stack.removeTagKey(TAG_PARTITION_ASPECT);
            return;
        }

        stack.getOrCreateTag().putString(TAG_PARTITION_ASPECT, aspect.name());
    }

    public static void setEssentia(ItemStack stack, Aspect aspect, int amount) {
        if (amount <= 0 || aspect == null) {
            stack.removeTagKey(TAG_ASPECT);
            stack.removeTagKey(TAG_AMOUNT);
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_ASPECT, aspect.name());
        tag.putInt(TAG_AMOUNT, amount);
    }

    public static int transferFromJar(ItemStack cell, EssentiaJarBlockEntity jar, int maxAmount) {
        if (!(cell.getItem() instanceof EssentiaCellItem cellItem)) {
            return 0;
        }

        Aspect jarAspect = jar.aspects().firstAspect();

        if (jarAspect == null) {
            return 0;
        }

        Aspect partitionAspect = getPartitionAspect(cell);

        if (partitionAspect != null && partitionAspect != jarAspect) {
            return 0;
        }

        Aspect cellAspect = getAspect(cell);
        int current = getAmount(cell);

        if (cellAspect != null && cellAspect != jarAspect) {
            return 0;
        }

        int space = Math.max(0, cellItem.capacity - current);
        int toMove = Math.min(maxAmount, space);

        if (toMove <= 0) {
            return 0;
        }

        int removed = jar.aspects().removeUpTo(jarAspect, toMove);

        if (removed > 0) {
            setEssentia(cell, jarAspect, current + removed);
            jar.setChangedAndSync();
        }

        return removed;
    }

    public static int transferToJar(ItemStack cell, EssentiaJarBlockEntity jar, int maxAmount) {
        Aspect aspect = getAspect(cell);
        int current = getAmount(cell);

        if (aspect == null || current <= 0) {
            return 0;
        }

        int toMove = Math.min(maxAmount, current);
        int accepted = jar.acceptFromTube(aspect, toMove, false);

        if (accepted > 0) {
            setEssentia(cell, aspect, current - accepted);
            jar.setChangedAndSync();
        }

        return accepted;
    }

    public static Component status(ItemStack stack) {
        Aspect aspect = getAspect(stack);
        Aspect partition = getPartitionAspect(stack);
        int amount = getAmount(stack);
        int capacity = stack.getItem() instanceof EssentiaCellItem cellItem ? cellItem.capacity : 0;

        Component base;

        if (aspect == null || amount <= 0) {
            base = Component.literal("Digital Essentia Cell | empty | 0/" + capacity).withStyle(ChatFormatting.GRAY);
        } else {
            base = Component.literal("Digital Essentia Cell | ")
                    .append(Component.literal(aspect.displayName()).withStyle(aspect.color()))
                    .append(Component.literal(" " + amount + "/" + capacity));
        }

        if (partition != null) {
            return base.copy().append(Component.literal(" | locked: ")).append(Component.literal(partition.displayName()).withStyle(partition.color()));
        }

        return base;
    }

    private void showStatus(Player player, ItemStack stack) {
        player.displayClientMessage(status(stack), false);
        player.displayClientMessage(Component.literal("ПКМ по jar: загрузить. Shift+ПКМ по jar: выгрузить.").withStyle(ChatFormatting.GRAY), false);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(status(stack));
        tooltip.add(Component.literal("ПКМ по jar: загрузить essentia.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Shift+ПКМ по jar: выгрузить essentia.").withStyle(ChatFormatting.GRAY));

        Aspect partition = getPartitionAspect(stack);

        if (partition == null) {
            tooltip.add(Component.literal("Не привязана к аспекту.").withStyle(ChatFormatting.DARK_PURPLE));
        } else {
            tooltip.add(Component.literal("Partition lock: ").append(Component.literal(partition.displayName()).withStyle(partition.color())));
        }
    }
}
