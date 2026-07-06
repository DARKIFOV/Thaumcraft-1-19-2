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

/**
 * Stage132: TC4 Node Transducer port start.
 *
 * The original TileNodeConverter turns a stabilized aura node into an energized
 * node when powered. This block keeps that rule: it only transduces stabilized
 * nodes, and only while the transducer receives redstone power.
 */
public class NodeTransducerBlock extends Block {
    public static final int RANGE = 3;

    public NodeTransducerBlock(Properties properties) {
        super(properties);
    }

    public static boolean isActiveTransducerNearby(Level level, BlockPos nodePos) {
        if (level == null) {
            return false;
        }

        for (BlockPos pos : BlockPos.betweenClosed(nodePos.offset(-RANGE, -RANGE, -RANGE), nodePos.offset(RANGE, RANGE, RANGE))) {
            if (level.getBlockState(pos).is(ThaumcraftMod.NODE_TRANSDUCER.get()) && level.hasNeighborSignal(pos)) {
                return true;
            }
        }

        return false;
    }

    public static int activeTransducerCount(Level level, BlockPos nodePos) {
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(nodePos.offset(-RANGE, -RANGE, -RANGE), nodePos.offset(RANGE, RANGE, RANGE))) {
            if (level.getBlockState(pos).is(ThaumcraftMod.NODE_TRANSDUCER.get()) && level.hasNeighborSignal(pos)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            int nodes = 0;
            int stabilizedNodes = 0;
            int energizedNodes = 0;

            for (BlockPos check : BlockPos.betweenClosed(pos.offset(-RANGE, -RANGE, -RANGE), pos.offset(RANGE, RANGE, RANGE))) {
                if (level.getBlockEntity(check) instanceof AuraNodeBlockEntity node) {
                    nodes++;
                    if (node.isStabilized()) {
                        stabilizedNodes++;
                    }
                    if (node.isEnergized()) {
                        energizedNodes++;
                    }
                }
            }

            boolean powered = level.hasNeighborSignal(pos);
            player.displayClientMessage(
                    Component.literal("Node Transducer " + (powered ? "powered" : "idle")
                                    + ". Nodes: " + nodes
                                    + ", stabilized: " + stabilizedNodes
                                    + ", energized: " + energizedNodes)
                            .withStyle(powered ? ChatFormatting.AQUA : ChatFormatting.GRAY),
                    false
            );
            level.playSound(null, pos, TC4Sounds.event(powered ? "jacobs" : "wand"), SoundSource.BLOCKS, 0.35F, powered ? 1.2F : 0.8F);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
