package com.darkifov.thaumcraft.taint;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stage144 taint spread bridge.  It ports the important TC4 BlockTaint behavior:
 * tainted blocks tick, look around nearby soil/grass/stone, convert susceptible
 * blocks, and occasionally collapse into flux goo if they cannot keep spreading.
 */
public final class TaintSpreadRuntime {
    private TaintSpreadRuntime() {
    }

    public static void randomTick(ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) < 28) {
            trySpreadNear(level, pos, random, 2, true);
        }

        if (random.nextInt(180) == 0 && level.isEmptyBlock(pos.above())) {
            level.setBlock(pos.above(), ThaumcraftMod.FLUX_GAS.get().defaultBlockState(), 3);
        }

        if (random.nextInt(260) == 0 && level.getBlockState(pos.below()).is(ThaumcraftMod.TAINTED_SOIL.get())) {
            level.setBlock(pos, ThaumcraftMod.FLUX_GOO.get().defaultBlockState(), 3);
        }
    }

    public static boolean trySpreadNear(Level level, BlockPos center, RandomSource random, int radius, boolean playSound) {
        for (int tries = 0; tries < 12; tries++) {
            BlockPos target = center.offset(
                    random.nextInt(radius * 2 + 1) - radius,
                    random.nextInt(3) - 1,
                    random.nextInt(radius * 2 + 1) - radius
            );

            if (convert(level, target, playSound)) {
                return true;
            }
        }

        return false;
    }

    public static boolean convert(Level level, BlockPos target, boolean playSound) {
        BlockState state = level.getBlockState(target);

        if (state.is(ThaumcraftMod.TAINTED_SOIL.get()) || state.is(ThaumcraftMod.FLUX_GOO.get())) {
            return false;
        }

        if (isTaintable(state)) {
            level.setBlock(target, ThaumcraftMod.TAINTED_SOIL.get().defaultBlockState(), 3);

            if (playSound && !level.isClientSide) {
                level.playSound(null, target, SoundEvents.HONEY_BLOCK_BREAK, SoundSource.BLOCKS, 0.35F, 0.55F + level.random.nextFloat() * 0.25F);
            }

            return true;
        }

        if (state.isAir() && hasTaintedNeighbor(level, target)) {
            level.setBlock(target, ThaumcraftMod.FLUX_GAS.get().defaultBlockState(), 3);
            return true;
        }

        return false;
    }

    public static boolean isTaintable(BlockState state) {
        return state.is(Blocks.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.MUD)
                || state.is(Blocks.MYCELIUM)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.STONE)
                || state.is(Blocks.COBBLESTONE)
                || state.is(Blocks.MOSSY_COBBLESTONE)
                || state.is(Blocks.SAND)
                || state.is(Blocks.RED_SAND)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.CLAY);
    }

    private static boolean hasTaintedNeighbor(Level level, BlockPos pos) {
        return level.getBlockState(pos.north()).is(ThaumcraftMod.TAINTED_SOIL.get())
                || level.getBlockState(pos.south()).is(ThaumcraftMod.TAINTED_SOIL.get())
                || level.getBlockState(pos.east()).is(ThaumcraftMod.TAINTED_SOIL.get())
                || level.getBlockState(pos.west()).is(ThaumcraftMod.TAINTED_SOIL.get())
                || level.getBlockState(pos.below()).is(ThaumcraftMod.TAINTED_SOIL.get());
    }
}
