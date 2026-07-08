package com.darkifov.thaumcraft.eldritch;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.TaintFibresBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stage263-272: 1.19.2-safe adapter for the tail of TC4 GenPassage.generateRoom.
 *
 * <p>The original passage generator had feature-specific mutations after the
 * doorway/wall pass: feature 11 trapped blocks, feature 12 organic flesh growth,
 * feature 13 taint fibres/biome tainting and feature 14 vishroom + MindSpider
 * spawner.  Earlier mega stages kept the structural room shell but skipped most
 * of these endings.  This class keeps those feature ids explicit and auditable
 * without depending on 1.7.10 Block metadata or TileEntity APIs.</p>
 */
public final class TC4OuterLandsPassageFeatureAdapter {
    public static final int FEATURE_TRAPPED_PASSAGE = 11;
    public static final int FEATURE_FLESHY_PASSAGE = 12;
    public static final int FEATURE_TAINTED_PASSAGE = 13;
    public static final int FEATURE_SPIDER_PASSAGE = 14;

    private TC4OuterLandsPassageFeatureAdapter() {
    }

    public static void apply(ServerLevel level, BlockPos origin, TC4OuterLandsGenCommonAdapter.Cell cell) {
        if (cell == null) {
            return;
        }
        switch (cell.feature()) {
            case FEATURE_TRAPPED_PASSAGE -> reinforceTrappedPassage(level, origin, cell);
            case FEATURE_FLESHY_PASSAGE -> growFleshPocket(level, origin, cell);
            case FEATURE_TAINTED_PASSAGE -> seedTaintPocket(level, origin);
            case FEATURE_SPIDER_PASSAGE -> placeVishroomAndMindSpiderSpawner(level, origin);
            default -> {
            }
        }
    }

    private static void reinforceTrappedPassage(ServerLevel level, BlockPos origin, TC4OuterLandsGenCommonAdapter.Cell cell) {
        // GenPassage used code 20 while closing walls for feature 11.  The wall
        // code path is probabilistic and direction-sensitive; this pass mirrors
        // the same intent by sprinkling a few dedicated TileEldritchTrap
        // equivalents on exposed wall caps after the shell exists.
        for (int w = -4; w <= 4; w++) {
            for (int j = -4; j <= 4; j++) {
                if (level.random.nextInt(5) == 0) {
                    BlockPos pos = origin.offset(8 + w, 3 + level.random.nextInt(5), 8 + j);
                    if (isOuterShell(level, pos)) {
                        level.setBlock(pos, ThaumcraftMod.ELDRITCH_TRAP.get().defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private static void growFleshPocket(ServerLevel level, BlockPos origin, TC4OuterLandsGenCommonAdapter.Cell cell) {
        for (int w = -4; w <= 4; w++) {
            for (int h = -4; h < 5; h++) {
                for (int j = -4; j <= 4; j++) {
                    BlockPos pos = origin.offset(8 + w, 4 + h, 8 + j);
                    BlockState state = level.getBlockState(pos);
                    if ((state.isAir() || state.is(ThaumcraftMod.OBSIDIAN_TILE.get()) || state.is(Blocks.STONE_BRICK_STAIRS))
                            && level.random.nextBoolean()) {
                        TC4OuterLandsGenCommonAdapter.placeBlock(level, pos,
                                TC4OuterLandsGenCommonAdapter.CODE_COSMETIC_META_14,
                                Direction.NORTH, cell);
                    }
                }
            }
        }
    }

    private static void seedTaintPocket(ServerLevel level, BlockPos origin) {
        for (int w = -4; w <= 4; w++) {
            for (int h = -3; h <= 3; h++) {
                for (int j = -4; j <= 4; j++) {
                    BlockPos pos = origin.offset(8 + w, 4 + h, 8 + j);
                    if (level.isEmptyBlock(pos) && adjacentSolid(level, pos) && level.random.nextInt(3) != 0) {
                        int age = level.random.nextInt(4) == 0 ? 1 : 0;
                        level.setBlock(pos, ThaumcraftMod.TAINT_FIBRES.get().defaultBlockState().setValue(TaintFibresBlock.AGE, age), 3);
                    }
                }
            }
        }
    }

    private static void placeVishroomAndMindSpiderSpawner(ServerLevel level, BlockPos origin) {
        for (int w = -3; w <= 3; w++) {
            for (int h = -3; h <= 3; h++) {
                for (int j = -3; j <= 3; j++) {
                    BlockPos pos = origin.offset(8 + w, 4 + h, 8 + j);
                    if (level.isEmptyBlock(pos) && level.random.nextFloat() < 0.35F && canPlaceMushroomEquivalent(level, pos)) {
                        level.setBlock(pos, Blocks.RED_MUSHROOM.defaultBlockState(), 3);
                    }
                }
            }
        }
        BlockPos spawnerPos = origin.offset(8, 4, 8);
        level.setBlock(spawnerPos, Blocks.SPAWNER.defaultBlockState(), 3);
        if (level.getBlockEntity(spawnerPos) instanceof SpawnerBlockEntity spawner) {
            spawner.getSpawner().setEntityId(ThaumcraftMod.MIND_SPIDER.get());
            spawner.getPersistentData().putString("TC4Original", "Thaumcraft.MindSpider");
            spawner.setChanged();
        }
    }

    private static boolean isOuterShell(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(ThaumcraftMod.OBSIDIAN_TILE.get()) || state.is(ThaumcraftMod.ELDRITCH_STONE.get()) || state.is(ThaumcraftMod.ELDRITCH_TRAP.get());
    }

    private static boolean adjacentSolid(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos side = pos.relative(direction);
            if (level.getBlockState(side).isFaceSturdy(level, side, direction.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    private static boolean canPlaceMushroomEquivalent(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }
}
