package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.Locale;

/**
 * Stage169/170: strict 1.19.2 adapter for TC4 TileResearchTable.recalculateBonus.
 *
 * Original TC4 does not immediately add these points to the player's aspect pool.
 * It keeps them in TileResearchTable.bonusAspects, saves them in the tile NBT and
 * consumes them from the table when a placed aspect is not available in the player
 * pool. This class mirrors that ownership and the original source categories:
 * darkness -> entropy, height/air/crystals/infused ore categories -> primals,
 * bookshelf/brain-jar style blocks -> random aspect.
 */
public final class ResearchTableBonusRuntime {
    public static final int RECALCULATE_INTERVAL_TICKS = 600;
    public static final int SCAN_RADIUS = 8;

    private ResearchTableBonusRuntime() {
    }

    public static boolean recalculateInto(Level level, BlockPos tablePos, AspectList bonusAspects) {
        if (level == null || tablePos == null || bonusAspects == null || level.isClientSide) {
            return false;
        }
        int before = bonusAspects.totalAmount();

        if (!level.isDay() && level.getRawBrightness(tablePos.above(), 0) < 4 && !level.canSeeSky(tablePos.above())) {
            addChance(level, bonusAspects, Aspect.PERDITIO, 20, false);
        }

        int worldHeight = Math.max(1, level.getMaxBuildHeight());
        if (tablePos.getY() > worldHeight * 0.50F) {
            addChance(level, bonusAspects, Aspect.AER, 20, false);
        }
        if (tablePos.getY() > worldHeight * 0.66F) {
            addChance(level, bonusAspects, Aspect.AER, 20, false);
        }
        if (tablePos.getY() > worldHeight * 0.75F) {
            addChance(level, bonusAspects, Aspect.AER, 20, false);
        }

        BlockPos.MutableBlockPos scan = new BlockPos.MutableBlockPos();
        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                for (int dy = -SCAN_RADIUS; dy <= SCAN_RADIUS; dy++) {
                    int y = tablePos.getY() + dy;
                    if (y <= level.getMinBuildHeight() || y >= level.getMaxBuildHeight()) {
                        continue;
                    }
                    scan.set(tablePos.getX() + dx, y, tablePos.getZ() + dz);
                    BlockState state = level.getBlockState(scan);
                    applyNearbySource(level, state, bonusAspects);
                    if (applyRandomAspectSource(level, state, bonusAspects)) {
                        return bonusAspects.totalAmount() != before;
                    }
                }
            }
        }
        return bonusAspects.totalAmount() != before;
    }

    private static void applyNearbySource(Level level, BlockState state, AspectList bonusAspects) {
        if (state == null || state.isAir()) {
            return;
        }
        if (isCrystal(state, Aspect.AER)) {
            addChance(level, bonusAspects, Aspect.AER, 10, true);
        } else if (isCrystal(state, Aspect.IGNIS)) {
            addChance(level, bonusAspects, Aspect.IGNIS, 10, true);
        } else if (isCrystal(state, Aspect.AQUA)) {
            addChance(level, bonusAspects, Aspect.AQUA, 10, true);
        } else if (isCrystal(state, Aspect.TERRA)) {
            addChance(level, bonusAspects, Aspect.TERRA, 10, true);
        } else if (isCrystal(state, Aspect.ORDO)) {
            addChance(level, bonusAspects, Aspect.ORDO, 10, true);
        } else if (isCrystal(state, Aspect.PERDITIO)) {
            addChance(level, bonusAspects, Aspect.PERDITIO, 10, true);
        } else if (isFireSource(state)) {
            addChance(level, bonusAspects, Aspect.IGNIS, 20, true);
        } else if (isEarthSource(state)) {
            addChance(level, bonusAspects, Aspect.TERRA, 20, true);
        } else if (isWaterSource(state)) {
            addChance(level, bonusAspects, Aspect.AQUA, 15, true);
        } else if (isOrderSource(state)) {
            addChance(level, bonusAspects, Aspect.ORDO, 20, true);
        }
    }

    private static boolean applyRandomAspectSource(Level level, BlockState state, AspectList bonusAspects) {
        if (state == null || state.isAir()) {
            return false;
        }
        if (state.is(Blocks.BOOKSHELF) && level.getRandom().nextInt(300) == 0) {
            Aspect[] aspects = Aspect.values();
            bonusAspects.add(aspects[level.getRandom().nextInt(aspects.length)], 1);
            return true;
        }
        // Original TC4 also used brain-in-a-jar (blockJar meta 1) here. The exact
        // block is not fully ported yet, so Stage169 deliberately does not map a
        // normal essentia jar as a fake brain jar.
        return false;
    }

    private static void addChance(Level level, AspectList bonusAspects, Aspect aspect, int chance, boolean onlyIfAbsent) {
        if (aspect == null || chance <= 0) {
            return;
        }
        if (onlyIfAbsent && bonusAspects.get(aspect) >= 1) {
            return;
        }
        if (level.getRandom().nextInt(chance) == 0) {
            bonusAspects.add(aspect, 1);
        }
    }

    private static boolean isCrystal(BlockState state, Aspect aspect) {
        return switch (aspect) {
            case AER -> state.is(ThaumcraftMod.AER_CRYSTAL.get());
            case IGNIS -> state.is(ThaumcraftMod.IGNIS_CRYSTAL.get());
            case AQUA -> state.is(ThaumcraftMod.AQUA_CRYSTAL.get());
            case TERRA -> state.is(ThaumcraftMod.TERRA_CRYSTAL.get());
            case ORDO -> state.is(ThaumcraftMod.ORDO_CRYSTAL.get());
            case PERDITIO -> state.is(ThaumcraftMod.PERDITIO_CRYSTAL.get());
            default -> false;
        };
    }

    private static boolean isFireSource(BlockState state) {
        FluidState fluid = state.getFluidState();
        return state.is(Blocks.FIRE) || state.is(Blocks.LAVA) || fluid.is(net.minecraft.tags.FluidTags.LAVA);
    }

    private static boolean isWaterSource(BlockState state) {
        FluidState fluid = state.getFluidState();
        return state.is(Blocks.WATER) || state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE) || fluid.is(net.minecraft.tags.FluidTags.WATER);
    }

    private static boolean isEarthSource(BlockState state) {
        return state.is(BlockTags.DIRT)
                || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(BlockTags.BASE_STONE_NETHER)
                || state.is(Blocks.CLAY)
                || state.is(Blocks.SAND)
                || state.is(Blocks.RED_SAND)
                || state.is(Blocks.GRAVEL);
    }

    private static boolean isOrderSource(BlockState state) {
        return state.is(Blocks.GLASS)
                || state.is(Blocks.GLASS_PANE)
                || state.is(Blocks.REDSTONE_WIRE)
                || state.is(Blocks.REPEATER)
                || state.is(Blocks.COMPARATOR)
                || state.is(BlockTags.LEAVES);
    }

    public static String summary(AspectList aspects) {
        if (aspects == null || aspects.isEmpty()) {
            return "none";
        }
        StringBuilder out = new StringBuilder();
        for (java.util.Map.Entry<Aspect, Integer> entry : aspects.entries().entrySet()) {
            if (!out.isEmpty()) {
                out.append(' ');
            }
            out.append(entry.getKey().id().toUpperCase(Locale.ROOT)).append(':').append(entry.getValue());
        }
        return out.toString();
    }
}
