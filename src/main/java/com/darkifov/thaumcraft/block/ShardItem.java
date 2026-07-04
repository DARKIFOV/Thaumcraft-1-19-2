package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class ShardItem extends Item {
    private final Aspect aspect;
    private final boolean balanced;

    public ShardItem(Properties properties, Aspect aspect, boolean balanced) {
        super(properties);
        this.aspect = aspect;
        this.balanced = balanced;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            if (balanced) {
                PlayerThaumData.unlockResearch(player, "BALANCED_SHARD");
                player.displayClientMessage(Component.literal("Balanced shard resonates with all primal aspects.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
            } else {
                PlayerThaumData.unlockResearch(player, "SHARD_" + aspect.name());
                player.displayClientMessage(Component.literal("Shard resonance: ").append(Component.literal(aspect.displayName()).withStyle(aspect.color())), false);
            }
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        if (balanced) {
            tooltip.add(Component.literal("Balanced shard: all primal aspects.").withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            tooltip.add(Component.literal("Primal shard: ").append(Component.literal(aspect.displayName()).withStyle(aspect.color())));
        }

        tooltip.add(Component.literal("Right-click: record resonance insight.").withStyle(ChatFormatting.GRAY));
    }
}
