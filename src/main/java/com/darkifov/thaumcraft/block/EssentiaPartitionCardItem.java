package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class EssentiaPartitionCardItem extends Item {
    private static final String TAG_PARTITION_ASPECT = "PartitionAspect";

    public EssentiaPartitionCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack card = player.getItemInHand(hand);

        if (!level.isClientSide) {
            ItemStack otherHand = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);

            if (otherHand.getItem() instanceof EssentiaCellItem) {
                Aspect partition = getPartitionAspect(card);

                if (partition == null) {
                    player.displayClientMessage(Component.literal("Сначала выбери aspect на Partition Card.").withStyle(ChatFormatting.RED), false);
                } else {
                    EssentiaCellItem.setPartitionAspect(otherHand, partition);
                    player.displayClientMessage(Component.literal("Ячейка привязана к aspect: ")
                            .append(Component.literal(partition.displayName()).withStyle(partition.color())), false);
                }

                return InteractionResultHolder.success(card);
            }

            Aspect next = nextAspect(getPartitionAspect(card));
            setPartitionAspect(card, next);
            player.displayClientMessage(Component.literal("Partition Card настроена на: ")
                    .append(Component.literal(next.displayName()).withStyle(next.color())), false);
            player.displayClientMessage(Component.literal("Держи Digital Essentia Cell во второй руке и ПКМ картой, чтобы привязать ячейку.").withStyle(ChatFormatting.GRAY), false);
        }

        return InteractionResultHolder.success(card);
    }

    public static Aspect getPartitionAspect(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(TAG_PARTITION_ASPECT)) {
            return null;
        }

        try {
            return Aspect.valueOf(stack.getTag().getString(TAG_PARTITION_ASPECT));
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

    private Aspect nextAspect(Aspect current) {
        Aspect[] values = Aspect.values();

        if (current == null) {
            return values[0];
        }

        return values[(current.ordinal() + 1) % values.length];
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        Aspect aspect = getPartitionAspect(stack);

        if (aspect == null) {
            tooltip.add(Component.literal("ПКМ: выбрать аспект partition.").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal("Partition: ").append(Component.literal(aspect.displayName()).withStyle(aspect.color())));
            tooltip.add(Component.literal("ПКМ с ячейкой во второй руке: привязать ячейку.").withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
}
