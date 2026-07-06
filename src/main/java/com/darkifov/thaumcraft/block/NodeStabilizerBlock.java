package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class NodeStabilizerBlock extends Block {
    public static final int RANGE = 5;
    public static final int ADVANCED_RANGE = 8;

    public NodeStabilizerBlock(Properties properties) {
        super(properties);
    }

    public static boolean hasStabilizerNearby(Level level, BlockPos nodePos) {
        return stabilizerStrength(level, nodePos) > 0;
    }

    public static boolean hasAdvancedStabilizerNearby(Level level, BlockPos nodePos) {
        return stabilizerStrength(level, nodePos) >= 2;
    }

    public static int stabilizerStrength(Level level, BlockPos nodePos) {
        if (level == null) {
            return 0;
        }

        int strength = 0;
        for (BlockPos pos : BlockPos.betweenClosed(nodePos.offset(-ADVANCED_RANGE, -ADVANCED_RANGE, -ADVANCED_RANGE), nodePos.offset(ADVANCED_RANGE, ADVANCED_RANGE, ADVANCED_RANGE))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(ThaumcraftMod.ADVANCED_NODE_STABILIZER.get()) && pos.distSqr(nodePos) <= ADVANCED_RANGE * ADVANCED_RANGE) {
                strength = Math.max(strength, 2);
            } else if (state.is(ThaumcraftMod.NODE_STABILIZER.get()) && pos.distSqr(nodePos) <= RANGE * RANGE) {
                strength = Math.max(strength, 1);
            }
        }

        return strength;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            int nodes = 0;
            int stabilized = 0;
            int energized = 0;
            int radius = state.is(ThaumcraftMod.ADVANCED_NODE_STABILIZER.get()) ? ADVANCED_RANGE : RANGE;

            for (BlockPos check : BlockPos.betweenClosed(pos.offset(-radius, -radius, -radius), pos.offset(radius, radius, radius))) {
                if (level.getBlockEntity(check) instanceof AuraNodeBlockEntity node) {
                    nodes++;
                    if (node.isStabilized()) {
                        stabilized++;
                    }
                    if (node.isEnergized()) {
                        energized++;
                    }
                }
            }

            player.displayClientMessage(
                    Component.literal((state.is(ThaumcraftMod.ADVANCED_NODE_STABILIZER.get()) ? "Advanced " : "")
                                    + "Node Stabilizer active. Nodes: " + nodes
                                    + ", stabilized: " + stabilized
                                    + ", energized: " + energized)
                            .withStyle(ChatFormatting.AQUA),
                    false
            );
            level.playSound(null, pos, TC4Sounds.event("hhoff"), SoundSource.BLOCKS, 0.35F, 1.0F);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
