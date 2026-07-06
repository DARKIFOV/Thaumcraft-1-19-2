package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.taint.TaintSpreadRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TaintedSoilBlock extends Block {
    public TaintedSoilBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        TaintSpreadRuntime.randomTick(level, pos, random);
    }
}
