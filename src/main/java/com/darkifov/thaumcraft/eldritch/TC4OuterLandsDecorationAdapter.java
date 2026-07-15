package com.darkifov.thaumcraft.eldritch;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.TC4LootBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Stage243-252: direct 1.19.2 bridge for TC4 GenCommon.processDecorations(world).
 *
 * <p>TC4 did not place most outer-lands crust, urns and crab vents immediately.
 * GenCommon.placeBlock queued coordinate entries in decoCommon, crabSpawner and
 * decoUrn, then processDecorations resolved exposed sides after the room shell
 * existed.  Earlier stages had eager placeholder decorations; this adapter
 * restores the original deferred contract without using 1.7.10 APIs.</p>
 */
public final class TC4OuterLandsDecorationAdapter {
    private static final List<BlockPos> DECO_COMMON = new ArrayList<>();
    private static final List<BlockPos> CRAB_SPAWNER = new ArrayList<>();
    private static final List<BlockPos> DECO_URN = new ArrayList<>();

    private TC4OuterLandsDecorationAdapter() {
    }

    public static void beginRoom() {
        DECO_COMMON.clear();
        CRAB_SPAWNER.clear();
        DECO_URN.clear();
    }

    public static void recordPlacement(ServerLevel level, BlockPos pos, int code, int feature, Direction dir) {
        BlockPos immutable = pos.immutable();
        switch (code) {
            case TC4OuterLandsGenCommonAdapter.CODE_CRAB_SPAWNER_A,
                    TC4OuterLandsGenCommonAdapter.CODE_ELDRITCH_LOCK,
                    TC4OuterLandsGenCommonAdapter.CODE_AIR -> removeAll(immutable);
            case TC4OuterLandsGenCommonAdapter.CODE_COSMETIC_SOLID -> recordCommonOrCrab(level, immutable, feature, false);
            case TC4OuterLandsGenCommonAdapter.CODE_COSMETIC_META_14 -> recordCommonOrCrab(level, immutable, feature, true);
            case TC4OuterLandsGenCommonAdapter.CODE_STAIRS_UP_A,
                    TC4OuterLandsGenCommonAdapter.CODE_STAIRS_UP_B -> {
                if (level.random.nextFloat() < 0.005F) {
                    addUnique(DECO_URN, immutable);
                }
            }
            default -> {
            }
        }
    }

    public static void processDecorations(ServerLevel level) {
        processUrns(level, level.random);
        processCommonCrust(level, level.random);
        processCrabSpawners(level);
        beginRoom();
    }

    /** Backward-compatible Stage223-232 entry point. */
    public static void processDecorations(ServerLevel level, BlockPos origin, int feature, RandomSource random) {
        processDecorations(level);
    }

    private static void recordCommonOrCrab(ServerLevel level, BlockPos pos, int feature, boolean crustCode) {
        if (level.getBlockState(pos).is(ThaumcraftMod.ELDRITCH_NOTHING.get())) {
            return;
        }
        if (crustCode) {
            if (level.random.nextInt(25) == 0) {
                return; // TC4 sometimes immediately made blockEldritch meta 4; handled by caller placement.
            }
            if (level.random.nextInt(25) == 0 && (feature == 7 || feature == 12)) {
                addUnique(CRAB_SPAWNER, pos);
            }
            return;
        }
        if (feature == 7 && level.random.nextInt(3) == 0) {
            return;
        }
        if (level.random.nextInt(25) == 0) {
            if (feature == 7) {
                addUnique(CRAB_SPAWNER, pos);
            } else {
                addUnique(DECO_COMMON, pos);
            }
        }
    }

    private static void processUrns(ServerLevel level, RandomSource random) {
        for (BlockPos pos : DECO_URN) {
            if (!level.isEmptyBlock(pos.above())) {
                continue;
            }
            level.setBlock(pos, ThaumcraftMod.OBSIDIAN_TILE.get().defaultBlockState(), 3);
            float rr = random.nextFloat();
            // Preserve TC4 line ordering: rr < 0.025 is swallowed by rr < 0.1, so md 2 is intentionally unreachable here.
            int meta = rr < 0.1F ? 1 : rr < 0.025F ? 2 : 0;
            level.setBlock(pos.above(), ThaumcraftMod.OUTER_LANDS_LOOT_URN.get().defaultBlockState().setValue(TC4LootBlock.VARIANT, meta), 3);
        }
    }

    private static void processCommonCrust(ServerLevel level, RandomSource random) {
        for (BlockPos pos : DECO_COMMON) {
            int exposed = countExposedSides(level, pos);
            if (exposed <= 0) {
                continue;
            }
            if (exposed != 1 && isBedrockShowing(level, pos)) {
                continue;
            }
            if (isAdjacentToEldritchStone(level, pos)) {
                continue;
            }
            int meta = random.nextInt(8) != 0 ? 5 : random.nextInt(3) != 0 ? 4 : 10;
            level.setBlock(pos, TC4EldritchBlockVariantAdapter.stateForMeta(meta), 3);
            if (meta == 4 && random.nextInt(12) == 0) {
                placeCrystalOnFirstAirSide(level, pos);
            }
        }
    }

    private static void processCrabSpawners(ServerLevel level) {
        for (BlockPos pos : CRAB_SPAWNER) {
            int exposed = countExposedSides(level, pos);
            if (exposed != 1 || isAdjacentToEldritchStone(level, pos)) {
                continue;
            }
            Direction facing = firstAirDirection(level, pos);
            BlockState state = ThaumcraftMod.ELDRITCH_CRAB_SPAWNER.get().defaultBlockState()
                    .setValue(BlockStateProperties.FACING, facing == null ? Direction.NORTH : facing);
            level.setBlock(pos, state, 3);
        }
    }

    private static void placeCrystalOnFirstAirSide(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos candidate = pos.relative(direction);
            if (level.isEmptyBlock(candidate)) {
                BlockState state = ThaumcraftMod.PERDITIO_CRYSTAL.get().defaultBlockState();
                level.setBlock(candidate, state, 3);
                return;
            }
        }
    }

    private static Direction firstAirDirection(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.isEmptyBlock(pos.relative(direction))) {
                return direction;
            }
        }
        return Direction.NORTH;
    }

    private static int countExposedSides(ServerLevel level, BlockPos pos) {
        int exposed = 0;
        for (Direction direction : Direction.values()) {
            BlockState state = level.getBlockState(pos.relative(direction));
            if (state.isAir() || state.is(ThaumcraftMod.ELDRITCH_NOTHING.get())) {
                exposed++;
            }
        }
        return exposed;
    }

    private static boolean isBedrockShowing(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockState front = level.getBlockState(pos.relative(direction));
            BlockState back = level.getBlockState(pos.relative(direction.getOpposite()));
            if (!front.isSolidRender(level, pos.relative(direction)) && (back.is(net.minecraft.world.level.block.Blocks.BEDROCK) || back.is(ThaumcraftMod.ELDRITCH_NOTHING.get()))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAdjacentToEldritchStone(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).is(ThaumcraftMod.ELDRITCH_STONE.get())) {
                return true;
            }
        }
        return false;
    }

    private static void removeAll(BlockPos pos) {
        DECO_COMMON.remove(pos);
        CRAB_SPAWNER.remove(pos);
        DECO_URN.remove(pos);
    }

    private static void addUnique(List<BlockPos> list, BlockPos pos) {
        if (!list.contains(pos)) {
            list.add(pos);
        }
    }
}
