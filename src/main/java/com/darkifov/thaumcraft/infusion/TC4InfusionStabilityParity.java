package com.darkifov.thaumcraft.infusion;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stage723-742: source-of-truth bridge for TC4 infusion stability/symmetry checks.
 *
 * TC4 does not let stabilizers be arbitrary one-sided bonuses.  Stabilizing blocks
 * around the altar only really help when their mirrored partner exists around the
 * altar center, while unpaired decor contributes to instability.  The original
 * implementation did this inside TileInfusionMatrix/validLocation using world
 * coordinates; this Forge 1.19.2 adapter keeps the same data flow without adding
 * new blocks or recipes.
 */
public final class TC4InfusionStabilityParity {
    public static final int STABILIZER_SCAN_RADIUS = InfusionAltarStructure.STABILIZER_RADIUS;
    public static final int STABILIZER_SCAN_DOWN = -10;
    public static final int STABILIZER_SCAN_UP = 5;
    public static final int MAX_EFFECTIVE_STABILIZER_PAIRS = 4;
    public static final String NBT_RECIPE_STABILIZERS = "recipestabilizers";
    public static final String NBT_RECIPE_SYMMETRY = "recipesymmetry";
    public static final String NBT_RECIPE_STABILIZER_PAIRS = "recipestabilizerpairs";
    public static final String NBT_RECIPE_UNPAIRED_STABILIZERS = "recipeunpairedstabilizers";

    private TC4InfusionStabilityParity() {
    }

    public static StabilitySnapshot scan(Level level, BlockPos center) {
        Set<BlockPos> positions = new HashSet<>();
        for (BlockPos scan : BlockPos.betweenClosed(center.offset(-STABILIZER_SCAN_RADIUS, STABILIZER_SCAN_DOWN, -STABILIZER_SCAN_RADIUS), center.offset(STABILIZER_SCAN_RADIUS, STABILIZER_SCAN_UP, STABILIZER_SCAN_RADIUS))) {
            if (scan.equals(center)) {
                continue;
            }
            if (isOriginalStyleStabilizer(level.getBlockState(scan))) {
                positions.add(scan.immutable());
            }
        }

        int mirrored = 0;
        int unpaired = 0;
        Set<BlockPos> consumed = new HashSet<>();
        for (BlockPos pos : positions) {
            if (consumed.contains(pos)) {
                continue;
            }
            BlockPos mirror = mirrorAround(center, pos);
            if (positions.contains(mirror) && !mirror.equals(pos)) {
                mirrored++;
                consumed.add(pos);
                consumed.add(mirror);
            } else {
                unpaired++;
                consumed.add(pos);
            }
        }

        List<BlockPos> sorted = new ArrayList<>(positions);
        sorted.sort(Comparator.<BlockPos>comparingInt(pos -> pos.getY()).thenComparingInt(pos -> pos.getX()).thenComparingInt(pos -> pos.getZ()));
        return new StabilitySnapshot(sorted, mirrored, unpaired, Math.min(MAX_EFFECTIVE_STABILIZER_PAIRS, mirrored));
    }

    public static BlockPos mirrorAround(BlockPos center, BlockPos pos) {
        int dx = pos.getX() - center.getX();
        int dz = pos.getZ() - center.getZ();
        return new BlockPos(center.getX() - dx, pos.getY(), center.getZ() - dz);
    }

    public static boolean isOriginalStyleStabilizer(BlockState state) {
        Block block = state.getBlock();
        return block == Blocks.WITHER_SKELETON_SKULL
                || block == Blocks.SKELETON_SKULL
                || block == Blocks.ZOMBIE_HEAD
                || block == Blocks.CREEPER_HEAD
                || block == Blocks.DRAGON_HEAD
                || state.is(BlockTags.CANDLES)
                || state.is(ThaumcraftMod.MATRIX_STABILIZER.get())
                || state.is(ThaumcraftMod.NODE_STABILIZER.get())
                || state.is(ThaumcraftMod.ARCANE_STONE_BRICKS.get())
                || state.is(ThaumcraftMod.INFUSION_PILLAR.get());
    }

    public record StabilitySnapshot(List<BlockPos> positions, int mirroredPairs, int unpaired, int effectivePairs) {
        public String signature() {
            StringBuilder builder = new StringBuilder();
            for (BlockPos pos : positions) {
                if (builder.length() > 0) {
                    builder.append('|');
                }
                builder.append(pos.getX()).append(',').append(pos.getY()).append(',').append(pos.getZ());
            }
            return builder.toString();
        }
    }
}
