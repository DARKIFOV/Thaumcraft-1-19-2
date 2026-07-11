package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.NodeTransducerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stage132: TC4 Node Transducer port start.
 *
 * The original TileNodeConverter turns a stabilized aura node into an energized
 * node when powered. This block keeps that rule: it only transduces stabilized
 * nodes, and only while the transducer receives redstone power.
 */
public class NodeTransducerBlock extends BaseEntityBlock {

    public NodeTransducerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NodeTransducerBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                   BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, ThaumcraftMod.NODE_TRANSDUCER_BLOCK_ENTITY.get(),
                        NodeTransducerBlockEntity::clientTick)
                : null;
    }

    public static boolean isActiveTransducerNearby(Level level, BlockPos nodePos) {
        if (level == null || nodePos == null) {
            return false;
        }
        // Exact TC4 stack: converter directly above the node. A radius search
        // let one powered converter energize unrelated nodes through walls.
        BlockPos transducerPos = nodePos.above();
        return level.getBlockState(transducerPos).is(ThaumcraftMod.NODE_TRANSDUCER.get())
                && level.hasNeighborSignal(transducerPos);
    }

    public static int activeTransducerCount(Level level, BlockPos nodePos) {
        return isActiveTransducerNearby(level, nodePos) ? 1 : 0;
    }


}
