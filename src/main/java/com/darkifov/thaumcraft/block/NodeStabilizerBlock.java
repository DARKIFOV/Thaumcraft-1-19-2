package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.NodeStabilizerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class NodeStabilizerBlock extends BaseEntityBlock {
    public NodeStabilizerBlock(Properties properties) {
        super(properties);
    }

    public static boolean hasStabilizerNearby(Level level, BlockPos nodePos) {
        return stabilizerStrength(level, nodePos) > 0;
    }

    public static boolean hasAdvancedStabilizerNearby(Level level, BlockPos nodePos) {
        return stabilizerStrength(level, nodePos) >= 2;
    }

    /**
     * Exact TC4 placement rule: the node must be directly above the stabilizer.
     * The earlier radius scan let a single block stabilize every node in a large
     * cube and made the rendered machine unrelated to the node it affected.
     */
    public static int stabilizerStrength(Level level, BlockPos nodePos) {
        if (level == null || nodePos == null) {
            return 0;
        }
        BlockPos stabilizerPos = nodePos.below();
        if (level.hasNeighborSignal(stabilizerPos)) {
            return 0;
        }
        BlockState state = level.getBlockState(stabilizerPos);
        if (state.is(ThaumcraftMod.ADVANCED_NODE_STABILIZER.get())) {
            return 2;
        }
        return state.is(ThaumcraftMod.NODE_STABILIZER.get()) ? 1 : 0;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NodeStabilizerBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, ThaumcraftMod.NODE_STABILIZER_BLOCK_ENTITY.get(), NodeStabilizerBlockEntity::clientTick)
                : null;
    }


}
