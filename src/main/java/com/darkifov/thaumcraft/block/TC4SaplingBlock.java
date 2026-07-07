package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.world.TC4TreeGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

public class TC4SaplingBlock extends Block implements BonemealableBlock {
    public enum Kind {
        GREATWOOD,
        SILVERWOOD
    }

    private final Kind kind;

    public TC4SaplingBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(7) == 0) {
            grow(level, pos, random);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState soil = level.getBlockState(pos.below());
        return soil.is(Blocks.GRASS_BLOCK)
                || soil.is(Blocks.DIRT)
                || soil.is(Blocks.COARSE_DIRT)
                || soil.is(Blocks.PODZOL)
                || soil.is(Blocks.ROOTED_DIRT)
                || soil.is(Blocks.MOSS_BLOCK)
                || soil.is(Blocks.FARMLAND)
                || soil.is(BlockTags.DIRT);
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter level, BlockPos pos, BlockState state, boolean clientSide) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(net.minecraft.world.level.Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextFloat() < 0.45F;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        grow(level, pos, random);
    }

    private void grow(ServerLevel level, BlockPos pos, RandomSource random) {
        if (kind == Kind.SILVERWOOD) {
            TC4TreeGenerator.growSilverwood(level, pos, random, false);
        } else {
            TC4TreeGenerator.growGreatwood(level, pos, random, false);
        }
    }
}
