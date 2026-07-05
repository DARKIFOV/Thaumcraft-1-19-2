package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class TaintedSoilBlock extends Block {
    public TaintedSoilBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);

        if (random.nextFloat() > 0.22F) {
            return;
        }

        BlockPos target = pos.offset(random.nextInt(5) - 2, random.nextInt(3) - 1, random.nextInt(5) - 2);
        BlockState targetState = level.getBlockState(target);

        if (targetState.is(Blocks.DIRT)
                || targetState.is(Blocks.GRASS_BLOCK)
                || targetState.is(Blocks.COARSE_DIRT)
                || targetState.is(Blocks.ROOTED_DIRT)
                || targetState.is(Blocks.MUD)) {
            level.setBlock(target, ThaumcraftMod.TAINTED_SOIL.get().defaultBlockState(), 3);
        }
    }
}
