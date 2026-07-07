package com.darkifov.thaumcraft.taint;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.TaintBlock;
import com.darkifov.thaumcraft.block.TaintFibresBlock;
import com.darkifov.thaumcraft.entity.TaintCrawlerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Stage145 TC4 taint spread bridge.
 *
 * Port source: TC4 BlockTaint and BlockTaintFibres. 1.7.10 stored the taint
 * variants in metadata; 1.19.2 uses explicit blocks/properties. The runtime
 * keeps the original ideas: taint blocks random tick, fibres overlay/grow on
 * nearby blocks, adjacent taint count decides stronger conversion, isolated
 * fibres die, and dense crust can collapse into flux goo.
 */
public final class TaintSpreadRuntime {
    private TaintSpreadRuntime() {
    }

    public static void randomTick(ServerLevel level, BlockPos pos, RandomSource random) {
        randomTick(level, pos, random, TaintBlock.Variant.SOIL);
    }

    public static void randomTick(ServerLevel level, BlockPos pos, RandomSource random, TaintBlock.Variant variant) {
        if (variant == TaintBlock.Variant.FLESH) {
            return;
        }

        if (random.nextInt(100) < (variant == TaintBlock.Variant.CRUST ? 34 : 28)) {
            trySpreadNear(level, pos, random, 2, true);
        }

        if (variant == TaintBlock.Variant.CRUST && random.nextInt(260) == 0 && level.isEmptyBlock(pos.above())) {
            level.setBlock(pos, ThaumcraftMod.FLUX_GOO.get().defaultBlockState(), 3);
            return;
        }

        if (variant == TaintBlock.Variant.SOIL && random.nextInt(220) == 0 && getAdjacentTaint(level, pos) <= 1) {
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
            return;
        }

        if (random.nextInt(180) == 0 && level.isEmptyBlock(pos.above()) && hasAdjacentTaint(level, pos.above())) {
            spreadFibres(level, pos.above(), random, true);
        }

        if (random.nextInt(260) == 0 && isTaint(level.getBlockState(pos.below()))) {
            level.setBlock(pos, ThaumcraftMod.FLUX_GOO.get().defaultBlockState(), 3);
        }
    }

    public static void randomTickFibres(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        int adjacent = getAdjacentTaint(level, pos);
        if (adjacent <= 0) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }

        if (random.nextInt(100) < 38) {
            trySpreadNear(level, pos, random, 2, true);
        }

        int age = state.getValue(TaintFibresBlock.AGE);
        if (age > 0 && age < 4 && random.nextInt(4) == 0) {
            level.setBlock(pos, state.setValue(TaintFibresBlock.AGE, Math.min(4, age + 1)), 3);
            return;
        }

        if (age >= 3 && random.nextInt(age == 4 ? 10 : 30) == 0 && level.isEmptyBlock(pos.above())) {
            maybeSpawnCrawler(level, pos);
        }
    }

    public static boolean trySpreadNear(Level level, BlockPos center, RandomSource random, int radius, boolean playSound) {
        for (int tries = 0; tries < 12; tries++) {
            BlockPos target = center.offset(
                    random.nextInt(radius * 2 + 1) - radius,
                    random.nextInt(5) - 3,
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

        if (isTaint(state) || state.is(ThaumcraftMod.FLUX_GOO.get()) || state.is(ThaumcraftMod.FLUX_GAS.get())) {
            return false;
        }

        if (spreadFibres(level, target, level.random, playSound)) {
            return true;
        }

        int adjacent = getAdjacentTaint(level, target);
        if (isCrustTarget(state) && adjacent >= 2) {
            setTaint(level, target, ThaumcraftMod.TAINT_CRUST.get().defaultBlockState(), playSound);
            return true;
        }

        if (isSoilTarget(state) && adjacent >= 2) {
            setTaint(level, target, ThaumcraftMod.TAINT_SOIL.get().defaultBlockState(), playSound);
            return true;
        }

        if (state.isAir() && adjacent > 0) {
            spreadFibres(level, target, level.random, playSound);
            return true;
        }

        return false;
    }

    public static boolean spreadFibres(Level level, BlockPos target, RandomSource random, boolean playSound) {
        BlockState state = level.getBlockState(target);
        if ((!state.isAir() && !isSoftPlant(state)) || isOnlyAdjacentToTaint(level, target)) {
            return false;
        }
        if (!hasAdjacentSolid(level, target) || getAdjacentTaint(level, target) <= 0) {
            return false;
        }

        int age = 0;
        if (level.isEmptyBlock(target.above()) && level.getBlockState(target.below()).isFaceSturdy(level, target.below(), Direction.UP)) {
            int roll = random.nextInt(120);
            age = roll < 90 ? 1 : roll < 112 ? 2 : 3;
        }
        level.setBlock(target, ThaumcraftMod.TAINT_FIBRES.get().defaultBlockState().setValue(TaintFibresBlock.AGE, age), 3);
        if (playSound && !level.isClientSide) {
            level.playSound(null, target, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.18F, 0.8F + random.nextFloat() * 0.2F);
        }
        return true;
    }

    public static boolean isTaintable(BlockState state) {
        return isCrustTarget(state) || isSoilTarget(state) || isSoftPlant(state);
    }

    public static boolean isTaint(BlockState state) {
        return state.is(ThaumcraftMod.TAINT_CRUST.get())
                || state.is(ThaumcraftMod.TAINT_SOIL.get())
                || state.is(ThaumcraftMod.FLESH_BLOCK.get())
                || state.is(ThaumcraftMod.TAINT_FIBRES.get())
                || state.is(ThaumcraftMod.TAINTED_SOIL.get());
    }

    public static boolean hasAdjacentTaint(LevelReader level, BlockPos pos) {
        return isTaint(level.getBlockState(pos.north()))
                || isTaint(level.getBlockState(pos.south()))
                || isTaint(level.getBlockState(pos.east()))
                || isTaint(level.getBlockState(pos.west()))
                || isTaint(level.getBlockState(pos.above()))
                || isTaint(level.getBlockState(pos.below()));
    }

    public static int getAdjacentTaint(LevelReader level, BlockPos pos) {
        int count = 0;
        for (Direction direction : Direction.values()) {
            if (isTaint(level.getBlockState(pos.relative(direction)))) {
                count++;
            }
        }
        return count;
    }

    private static boolean isOnlyAdjacentToTaint(Level level, BlockPos pos) {
        boolean sawTaint = false;
        for (Direction direction : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(direction));
            if (isTaint(neighbor)) {
                sawTaint = true;
            } else if (!neighbor.isAir()) {
                return false;
            }
        }
        return sawTaint;
    }

    private static boolean hasAdjacentSolid(LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos check = pos.relative(direction);
            BlockState state = level.getBlockState(check);
            if (!state.isAir() && state.isFaceSturdy(level, check, direction.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isCrustTarget(BlockState state) {
        return state.is(BlockTags.LOGS)
                || state.is(BlockTags.LEAVES)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.MOSSY_COBBLESTONE)
                || state.is(Blocks.VINE)
                || state.is(Blocks.CAVE_VINES)
                || state.is(Blocks.CAVE_VINES_PLANT);
    }

    private static boolean isSoilTarget(BlockState state) {
        return state.is(Blocks.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.MUD)
                || state.is(Blocks.MYCELIUM)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.STONE)
                || state.is(Blocks.COBBLESTONE)
                || state.is(Blocks.SAND)
                || state.is(Blocks.RED_SAND)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.CLAY);
    }

    private static boolean isSoftPlant(BlockState state) {
        return state.is(Blocks.GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN)
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.SAPLINGS)
                || state.is(BlockTags.LEAVES);
    }

    private static void setTaint(Level level, BlockPos target, BlockState taint, boolean playSound) {
        level.setBlock(target, taint, 3);
        if (playSound && !level.isClientSide) {
            level.playSound(null, target, SoundEvents.HONEY_BLOCK_BREAK, SoundSource.BLOCKS, 0.35F, 0.55F + level.random.nextFloat() * 0.25F);
        }
    }

    private static void maybeSpawnCrawler(ServerLevel level, BlockPos pos) {
        if (!level.getEntitiesOfClass(Entity.class, new AABB(pos).inflate(12.0D), entity -> entity instanceof TaintCrawlerEntity).isEmpty()) {
            return;
        }
        TaintCrawlerEntity crawler = ThaumcraftMod.TAINT_CRAWLER.get().create(level);
        if (crawler != null) {
            crawler.moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 0.0F, 0.0F);
            level.addFreshEntity(crawler);
            level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.HOSTILE, 0.35F, 0.65F + level.random.nextFloat() * 0.2F);
        }
    }
}
