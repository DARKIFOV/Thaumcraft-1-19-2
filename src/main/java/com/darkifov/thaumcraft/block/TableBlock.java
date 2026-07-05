package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class TableBlock extends Block {
    public TableBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (held.getItem() instanceof WandItem) {
            level.setBlock(pos, ThaumcraftMod.ARCANE_WORKBENCH.get().defaultBlockState(), 3);
            player.displayClientMessage(Component.literal("The table becomes an Arcane Workbench.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
            return InteractionResult.CONSUME;
        }

        if (held.is(ThaumcraftMod.SCRIBING_TOOLS.get())) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos other = pos.relative(direction);

                if (level.getBlockState(other).is(ThaumcraftMod.TABLE.get())) {
                    level.setBlock(pos, ThaumcraftMod.RESEARCH_TABLE.get().defaultBlockState(), 3);
                    level.removeBlock(other, false);

                    if (!player.getAbilities().instabuild) {
                        held.shrink(1);
                    }

                    player.displayClientMessage(Component.literal("The two tables become a Research Table.").withStyle(ChatFormatting.GOLD), false);
                    return InteractionResult.CONSUME;
                }
            }

            player.displayClientMessage(Component.literal("Place two Thaumcraft Tables side by side first.").withStyle(ChatFormatting.RED), false);
            return InteractionResult.CONSUME;
        }

        player.displayClientMessage(Component.literal("Use a wand for Arcane Workbench or Scribing Tools with two tables for Research Table.").withStyle(ChatFormatting.GRAY), false);
        return InteractionResult.CONSUME;
    }
}
