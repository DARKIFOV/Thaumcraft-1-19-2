package com.darkifov.thaumcraft.taint;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.TaintBlock;
import com.darkifov.thaumcraft.block.TaintFibresBlock;
import com.darkifov.thaumcraft.config.ThaumcraftConfig;
import com.darkifov.thaumcraft.entity.FallingTaintEntity;
import com.darkifov.thaumcraft.entity.TaintSporeEntity;
import com.darkifov.thaumcraft.entity.TaintSporeSwarmerEntity;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Forge 1.19.2 port of TC4 BlockTaint/BlockTaintFibres ecology. */
public final class TaintSpreadRuntime {
    /** Original BlockTaint random X/Z offset bound: nextInt(3) - 1. */
    public static final int ORIGINAL_HORIZONTAL_OFFSET_BOUND = 3;
    /** Original BlockTaint random Y offset bound/shift: nextInt(5) - 3. */
    public static final int ORIGINAL_VERTICAL_OFFSET_BOUND = 5;
    public static final int ORIGINAL_VERTICAL_OFFSET_SHIFT = 3;
    /** Original Config.spawnTaintSpore gate: random.nextInt(ORIGINAL_SPORE_ROLL_BOUND) == 0. */
    public static final int ORIGINAL_SPORE_ROLL_BOUND = 200;
    private TaintSpreadRuntime() {}

    public static void randomTick(ServerLevel level, BlockPos pos, RandomSource random) {
        randomTick(level, pos, random, TaintBlock.Variant.SOIL);
    }

    public static void randomTick(ServerLevel level, BlockPos pos, RandomSource random, TaintBlock.Variant variant) {
        if (variant == TaintBlock.Variant.FLESH) return;
        taintBiomeSpread(level, pos, random);

        if (variant == TaintBlock.Variant.CRUST && tryToFall(level, pos, random)) return;

        BlockPos target = pos.offset(random.nextInt(ORIGINAL_HORIZONTAL_OFFSET_BOUND) - 1,
                random.nextInt(ORIGINAL_VERTICAL_OFFSET_BOUND) - ORIGINAL_VERTICAL_OFFSET_SHIFT,
                random.nextInt(ORIGINAL_HORIZONTAL_OFFSET_BOUND) - 1);
        if (isTaintedColumn(level, target)) {
            boolean fibresPlaced = spreadFibres(level, target, random, true);
            if (!fibresPlaced || variant == TaintBlock.Variant.CRUST) {
                int adjacent = getAdjacentTaint(level, target);
                BlockState targetState = level.getBlockState(target);
                if (adjacent >= 2 && isCrustTarget(targetState)) {
                    setTaint(level, target, ThaumcraftMod.TAINT_CRUST.get().defaultBlockState(), true);
                } else if (adjacent >= 3 && !targetState.isAir() && isSoilTarget(targetState)) {
                    setTaint(level, target, ThaumcraftMod.TAINT_SOIL.get().defaultBlockState(), true);
                } else if (variant == TaintBlock.Variant.CRUST) {
                    if (ThaumcraftConfig.SPAWN_TAINT_SPORES.get() && level.isEmptyBlock(pos.above())
                            && random.nextInt(ORIGINAL_SPORE_ROLL_BOUND) == 0) {
                        AABB search = new AABB(pos).inflate(16.0D);
                        if (level.getEntitiesOfClass(TaintSporeSwarmerEntity.class, search).isEmpty()) {
                            TaintSporeSwarmerEntity swarmer = ThaumcraftMod.TAINT_SWARMER.get().create(level);
                            if (swarmer != null) {
                                level.removeBlock(pos, false);
                                swarmer.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                                        random.nextFloat() * 360.0F, 0.0F);
                                level.addFreshEntity(swarmer);
                                level.playSound(null, pos, TC4Sounds.event("roots"), SoundSource.HOSTILE,
                                        0.1F, 0.9F + random.nextFloat() * 0.2F);
                            }
                        }
                    } else if (isDenseCrust(level, pos)) {
                        level.setBlock(pos, ThaumcraftMod.FLUX_GOO.get().defaultBlockState(), 3);
                    }
                }
            }
        } else if (variant == TaintBlock.Variant.CRUST && random.nextInt(20) == 0) {
            level.setBlock(pos, ThaumcraftMod.FLUX_GOO.get().defaultBlockState(), 3);
        } else if (variant == TaintBlock.Variant.SOIL && random.nextInt(10) == 0) {
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
        }
    }

    public static void randomTickFibres(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        taintBiomeSpread(level, pos, random);
        int age = state.getValue(TaintFibresBlock.AGE);
        if ((age == 0 && isOnlyAdjacentToTaint(level, pos)) || !isTaintedColumn(level, pos)) {
            level.removeBlock(pos, false);
            return;
        }

        BlockPos target = pos.offset(random.nextInt(ORIGINAL_HORIZONTAL_OFFSET_BOUND) - 1,
                random.nextInt(ORIGINAL_VERTICAL_OFFSET_BOUND) - ORIGINAL_VERTICAL_OFFSET_SHIFT,
                random.nextInt(ORIGINAL_HORIZONTAL_OFFSET_BOUND) - 1);
        if (!isTaintedColumn(level, target) || spreadFibres(level, target, random, true)) return;

        int adjacent = getAdjacentTaint(level, target);
        BlockState targetState = level.getBlockState(target);
        if (adjacent >= 2 && isCrustTarget(targetState)) {
            setTaint(level, target, ThaumcraftMod.TAINT_CRUST.get().defaultBlockState(), true);
        } else if (adjacent >= 3 && !targetState.isAir() && isSoilTarget(targetState)) {
            setTaint(level, target, ThaumcraftMod.TAINT_SOIL.get().defaultBlockState(), true);
        } else if (age == 3 && ThaumcraftConfig.SPAWN_TAINT_SPORES.get()
                && random.nextInt(10) == 0 && level.isEmptyBlock(pos.above())) {
            BlockState mature = TaintFibresBlock.withAttachments(level, pos,
                    state.setValue(TaintFibresBlock.AGE, 4));
            level.setBlock(pos, mature, 3);
            TaintSporeEntity spore = ThaumcraftMod.TAINT_SPORE.get().create(level);
            if (spore != null) {
                spore.moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 0.0F, 0.0F);
                level.addFreshEntity(spore);
            }
        } else if (age == 4 && level.getEntitiesOfClass(TaintSporeEntity.class,
                new net.minecraft.world.phys.AABB(pos.above())).isEmpty()) {
            level.setBlock(pos, state.setValue(TaintFibresBlock.AGE, 3), 3);
        }
    }

    public static boolean trySpreadNear(Level level, BlockPos center, RandomSource random, int radius, boolean playSound) {
        for (int tries = 0; tries < 12; tries++) {
            BlockPos target = center.offset(random.nextInt(radius * 2 + 1) - radius,
                    random.nextInt(5) - 3, random.nextInt(radius * 2 + 1) - radius);
            if (convert(level, target, playSound)) return true;
        }
        return false;
    }

    public static boolean convert(Level level, BlockPos target, boolean playSound) {
        if (level instanceof ServerLevel server) markTaintedColumn(server, target);
        BlockState state = level.getBlockState(target);
        if (isTaint(state) || state.is(ThaumcraftMod.FLUX_GOO.get()) || state.is(ThaumcraftMod.FLUX_GAS.get())) return false;
        if (spreadFibres(level, target, level.random, playSound)) return true;
        int adjacent = getAdjacentTaint(level, target);
        if (adjacent >= 2 && isCrustTarget(state)) {
            setTaint(level, target, ThaumcraftMod.TAINT_CRUST.get().defaultBlockState(), playSound);
            return true;
        }
        if (adjacent >= 3 && !state.isAir() && isSoilTarget(state)) {
            setTaint(level, target, ThaumcraftMod.TAINT_SOIL.get().defaultBlockState(), playSound);
            return true;
        }
        return false;
    }

    public static boolean spreadFibres(Level level, BlockPos target, RandomSource random, boolean playSound) {
        BlockState replaced = level.getBlockState(target);
        if ((!replaced.isAir() && !replaced.getMaterial().isReplaceable()) || replaced.getFluidState().isSource()
                || isOnlyAdjacentToTaint(level, target) || !hasAdjacentSolid(level, target)) return false;

        int age = 0;
        if (random.nextInt(10) == 0 && level.isEmptyBlock(target.above())
                && level.getBlockState(target.below()).isFaceSturdy(level, target.below(), Direction.UP)) {
            if (random.nextInt(10) < 9) age = 1;
            else age = random.nextInt(12) < 10 ? 2 : 3;
        }
        BlockState fibres = TaintFibresBlock.withAttachments(level, target,
                ThaumcraftMod.TAINT_FIBRES.get().defaultBlockState().setValue(TaintFibresBlock.AGE, age));
        if (age == 0 && !TaintFibresBlock.hasAttachment(fibres)) return false;
        level.setBlock(target, fibres, 3);
        if (playSound && !level.isClientSide) {
            level.playSound(null, target, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.1F,
                    0.9F + random.nextFloat() * 0.2F);
        }
        return true;
    }

    public static boolean isTaintable(BlockState state) {
        return isCrustTarget(state) || isSoilTarget(state) || state.getMaterial().isReplaceable();
    }

    public static boolean isTaint(BlockState state) {
        return state.is(ThaumcraftMod.TAINT_CRUST.get()) || state.is(ThaumcraftMod.TAINT_SOIL.get())
                || state.is(ThaumcraftMod.FLESH_BLOCK.get()) || state.is(ThaumcraftMod.TAINT_FIBRES.get())
                || state.is(ThaumcraftMod.TAINTED_SOIL.get());
    }

    public static boolean hasAdjacentTaint(LevelReader level, BlockPos pos) { return getAdjacentTaint(level, pos) > 0; }

    public static int getAdjacentTaint(LevelReader level, BlockPos pos) {
        int count = 0;
        for (Direction direction : Direction.values()) if (isTaint(level.getBlockState(pos.relative(direction)))) count++;
        return count;
    }

    /** Public entity/runtime contract used by spores and future taint mobs. */
    public static boolean isColumnTainted(ServerLevel level, BlockPos pos) {
        boolean biome = level.getBiome(pos).unwrapKey()
                .map(key -> key.location().getPath().contains("taint"))
                .orElse(false);
        return biome || TaintRegionSavedData.get(level).isTainted(pos.getX(), pos.getZ());
    }

    /** Backward-compatible name retained for existing spread code and migration patches. */
    public static boolean isTaintedColumn(ServerLevel level, BlockPos pos) {
        return isColumnTainted(level, pos);
    }

    public static void markTaintedColumn(ServerLevel level, BlockPos pos) {
        TaintRegionSavedData.get(level).markTainted(pos.getX(), pos.getZ());
    }

    private static void taintBiomeSpread(ServerLevel level, BlockPos pos, RandomSource random) {
        int rate = ThaumcraftConfig.TAINT_SPREAD_RATE.get();
        if (rate <= 0 || getAdjacentTaint(level, pos) < 2) return;
        BlockPos target = pos.offset(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
        if (!isTaintedColumn(level, target) && random.nextInt(rate * 5) == 0) markTaintedColumn(level, target);
    }

    private static boolean tryToFall(ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState state = level.getBlockState(pos);
        if (canTaintFallBelow(level, pos.below())) {
            level.addFreshEntity(new FallingTaintEntity(level, pos, state, pos));
            return true;
        }
        if (!level.isEmptyBlock(pos.above())) return false;
        Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        for (int depth = 0; depth < 4; depth++) {
            if (!level.isEmptyBlock(pos.relative(side).below(depth))
                    || !level.getBlockState(pos.below(depth)).is(ThaumcraftMod.TAINT_CRUST.get())) return false;
        }
        BlockPos lateral = pos.relative(side);
        if (canTaintFallBelow(level, lateral.below())) {
            // TC4 starts the visual entity in the neighbouring column but leaves
            // the source crust in place until EntityFallingTaint's first tick.
            level.addFreshEntity(new FallingTaintEntity(level, lateral, state, pos));
            return true;
        }
        return false;
    }

    /** Shared source-level falling contract used by the custom falling-taint entity. */
    public static boolean canTaintFallBelow(LevelReader level, BlockPos pos) {
        for (BlockPos check : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (level.getBlockState(check).is(BlockTags.LOGS)) return false;
        }
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.getMaterial().isReplaceable() || state.is(ThaumcraftMod.TAINT_FIBRES.get())
                || state.getFluidState().is(FluidTags.WATER);
    }

    private static boolean isDenseCrust(LevelReader level, BlockPos pos) {
        if (!level.getBlockState(pos.above()).is(ThaumcraftMod.TAINT_CRUST.get())) return false;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (!level.getBlockState(pos.relative(direction)).is(ThaumcraftMod.TAINT_CRUST.get())) return false;
        }
        return true;
    }

    private static boolean isOnlyAdjacentToTaint(LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(direction));
            if (!neighbor.isAir() && !isTaint(neighbor)) return false;
        }
        return true;
    }

    private static boolean hasAdjacentSolid(LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos check = pos.relative(direction);
            if (level.getBlockState(check).isFaceSturdy(level, check, direction.getOpposite())) return true;
        }
        return false;
    }

    private static boolean isCrustTarget(BlockState state) {
        return state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.is(Blocks.VINE)
                || state.is(Blocks.CAVE_VINES) || state.is(Blocks.CAVE_VINES_PLANT)
                || state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.MOSSY_COBBLESTONE);
    }

    private static boolean isSoilTarget(BlockState state) {
        return state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.ROOTED_DIRT) || state.is(Blocks.MUD) || state.is(Blocks.MYCELIUM)
                || state.is(Blocks.PODZOL) || state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE)
                || state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || state.is(Blocks.GRAVEL)
                || state.is(Blocks.CLAY);
    }

    private static void setTaint(Level level, BlockPos target, BlockState taint, boolean playSound) {
        level.setBlock(target, taint, 3);
        if (level instanceof ServerLevel server) markTaintedColumn(server, target);
        if (playSound && !level.isClientSide) {
            level.playSound(null, target, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.1F,
                    0.9F + level.random.nextFloat() * 0.2F);
        }
    }
}
