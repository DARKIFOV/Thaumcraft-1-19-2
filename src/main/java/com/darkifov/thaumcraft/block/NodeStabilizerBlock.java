package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class NodeStabilizerBlock extends Block {
    public static final int RANGE = 5;

    public NodeStabilizerBlock(Properties properties) {
        super(properties);
    }

    public static boolean hasStabilizerNearby(Level level, BlockPos nodePos) {
        for (BlockPos pos : BlockPos.betweenClosed(nodePos.offset(-RANGE, -RANGE, -RANGE), nodePos.offset(RANGE, RANGE, RANGE))) {
            if (level.getBlockState(pos).is(ThaumcraftMod.NODE_STABILIZER.get())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            int nodes = 0;

            for (BlockPos check : BlockPos.betweenClosed(pos.offset(-RANGE, -RANGE, -RANGE), pos.offset(RANGE, RANGE, RANGE))) {
                if (level.getBlockState(check).is(ThaumcraftMod.AURA_NODE.get())) {
                    nodes++;
                }
            }

            player.displayClientMessage(
                    Component.literal("Node Stabilizer active. Aura nodes in range: " + nodes).withStyle(ChatFormatting.AQUA),
                    false
            );
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
