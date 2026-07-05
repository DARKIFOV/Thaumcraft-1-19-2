package com.darkifov.thaumcraft.essentia;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.EssentiaJarBlock;
import com.darkifov.thaumcraft.block.EssentiaValveBlock;
import com.darkifov.thaumcraft.blockentity.AlembicBlockEntity;
import com.darkifov.thaumcraft.blockentity.AlchemicalFurnaceBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaTubeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class EssentiaSuctionResolver {
    private EssentiaSuctionResolver() {
    }

    public static boolean sideAllows(Level level, BlockPos tubePos, Direction direction) {
        if (level == null || tubePos == null || direction == null) {
            return false;
        }

        if (level.getBlockState(tubePos).is(ThaumcraftMod.ESSENTIA_VALVE.get()) && !EssentiaValveBlock.isOpen(level, tubePos)) {
            return false;
        }

        return EssentiaTubeConnections.canConnect(level, tubePos, direction);
    }

    public static int sourcePressure(Level level, BlockPos tubePos, Direction direction, Aspect aspect) {
        if (!sideAllows(level, tubePos, direction)) {
            return EssentiaSuction.SOURCE_NONE;
        }

        BlockEntity entity = level.getBlockEntity(tubePos.relative(direction));

        if (entity instanceof AlembicBlockEntity alembic && alembic.aspects().get(aspect) > 0) {
            return EssentiaSuction.ALEMBIC_SOURCE_PRIORITY;
        }

        if (entity instanceof AlchemicalFurnaceBlockEntity furnace && furnace.aspects().get(aspect) > 0) {
            return EssentiaSuction.FURNACE_SOURCE_PRIORITY;
        }

        return EssentiaSuction.SOURCE_NONE;
    }

    public static int destinationSuction(Level level, BlockPos tubePos, Direction direction, Aspect aspect) {
        if (!sideAllows(level, tubePos, direction)) {
            return EssentiaSuction.SOURCE_NONE;
        }

        BlockPos destinationPos = tubePos.relative(direction);
        BlockEntity entity = level.getBlockEntity(destinationPos);

        if (!(entity instanceof EssentiaJarBlockEntity jar) || !jar.canAcceptAspect(aspect)) {
            return EssentiaSuction.SOURCE_NONE;
        }

        boolean voidJar = level.getBlockState(destinationPos).is(ThaumcraftMod.VOID_ESSENTIA_JAR.get());
        boolean filteredJar = level.getBlockState(destinationPos).is(ThaumcraftMod.FILTERED_ESSENTIA_JAR.get());

        if (!voidJar && jar.aspects().totalAmount() >= EssentiaJarBlock.CAPACITY) {
            return EssentiaSuction.SOURCE_NONE;
        }

        int base = voidJar ? EssentiaSuction.JAR_VOID : filteredJar ? EssentiaSuction.JAR_FILTERED : EssentiaSuction.JAR_NORMAL;

        if (jar.filterAspect() == aspect) {
            base += 8;
        }

        return base;
    }

    public static boolean isTubeLike(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }

        if (level.getBlockState(pos).is(ThaumcraftMod.ESSENTIA_TUBE.get())) {
            return level.getBlockEntity(pos) instanceof EssentiaTubeBlockEntity;
        }

        if (level.getBlockState(pos).is(ThaumcraftMod.ESSENTIA_VALVE.get())) {
            return EssentiaValveBlock.isOpen(level, pos) && level.getBlockEntity(pos) instanceof EssentiaTubeBlockEntity;
        }

        return false;
    }

    public static int competingDestinations(Level level, Iterable<BlockPos> network, Aspect aspect, BlockPos winningDestination) {
        int conflicts = 0;

        for (BlockPos tubePos : network) {
            for (Direction direction : Direction.values()) {
                BlockPos candidate = tubePos.relative(direction);

                if (winningDestination != null && winningDestination.equals(candidate)) {
                    continue;
                }

                if (destinationSuction(level, tubePos, direction, aspect) > EssentiaSuction.SOURCE_NONE) {
                    conflicts++;
                }
            }
        }

        return conflicts;
    }
}
