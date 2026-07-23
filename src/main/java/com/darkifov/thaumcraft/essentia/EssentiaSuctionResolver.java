package com.darkifov.thaumcraft.essentia;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.EssentiaJarBlock;
import com.darkifov.thaumcraft.block.EssentiaValveBlock;
import com.darkifov.thaumcraft.blockentity.AlembicBlockEntity;
import com.darkifov.thaumcraft.blockentity.AlchemicalCentrifugeBlockEntity;
import com.darkifov.thaumcraft.blockentity.ThaumatoriumBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaReservoirBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaCrystalizerBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaTubeBlockEntity;
import com.darkifov.thaumcraft.blockentity.ArcaneBoreBaseBlockEntity;
import com.darkifov.thaumcraft.blockentity.TC4EssentiaLampBlockEntity;
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

        if (level.getBlockEntity(tubePos) instanceof EssentiaTubeBlockEntity tube && !tube.isSideOpen(direction)) {
            return false;
        }

        return EssentiaTubeConnections.canConnect(level, tubePos, direction);
    }

    public static int sourcePressure(Level level, BlockPos tubePos, Direction direction, Aspect aspect) {
        if (!sideAllows(level, tubePos, direction)) {
            return EssentiaSuction.SOURCE_NONE;
        }

        BlockEntity entity = level.getBlockEntity(tubePos.relative(direction));

        if (entity instanceof AlchemicalCentrifugeBlockEntity centrifuge
                && centrifuge.canOutputTo(direction.getOpposite())
                && centrifuge.outputType(direction.getOpposite()) == aspect) {
            return EssentiaSuction.ALEMBIC_SOURCE_PRIORITY;
        }

        if (entity instanceof AlembicBlockEntity alembic
                && alembic.canOutputTo(direction.getOpposite())
                && alembic.aspects().get(aspect) > 0) {
            return EssentiaSuction.ALEMBIC_SOURCE_PRIORITY;
        }

        if (entity instanceof EssentiaReservoirBlockEntity reservoir
                && reservoir.canAccessFrom(direction.getOpposite())
                && reservoir.aspects().get(aspect) > 0) {
            return EssentiaSuction.RESERVOIR_SOURCE_PRIORITY;
        }

        return EssentiaSuction.SOURCE_NONE;
    }

    public static int destinationSuction(Level level, BlockPos tubePos, Direction direction, Aspect aspect) {
        if (!sideAllows(level, tubePos, direction)) {
            return EssentiaSuction.SOURCE_NONE;
        }

        BlockPos destinationPos = tubePos.relative(direction);
        Direction destinationFace = direction.getOpposite();
        ThaumatoriumBlockEntity thaumatorium = ThaumatoriumBlockEntity.resolveAt(level, destinationPos);
        if (thaumatorium != null) {
            Aspect wanted = thaumatorium.suctionTypeAt(destinationPos, destinationFace);
            if (wanted != null && (aspect == null || wanted == aspect)) {
                return thaumatorium.suctionAmountAt(destinationPos, destinationFace);
            }
        }
        BlockEntity entity = level.getBlockEntity(destinationPos);
        if (entity instanceof TC4EssentiaLampBlockEntity lamp) {
            Aspect wanted = lamp.suctionType(destinationFace);
            if (wanted != null && (aspect == null || wanted == aspect)) {
                return lamp.suctionAmount(destinationFace);
            }
        }
        if (entity instanceof ArcaneBoreBaseBlockEntity boreBase
                && boreBase.canInputFrom(destinationFace)
                && (aspect == null || aspect == Aspect.PERDITIO)) {
            return boreBase.suctionAmount(destinationFace);
        }
        if (entity instanceof AlchemicalCentrifugeBlockEntity centrifuge
                && (aspect == null || !aspect.isPrimal())
                && centrifuge.canInputFrom(destinationFace)) {
            return centrifuge.suctionAmount(destinationFace);
        }
        if (entity instanceof EssentiaCrystalizerBlockEntity crystalizer
                && crystalizer.canInputFrom(destinationFace)) {
            return crystalizer.suctionAmount(destinationFace);
        }

        if (entity instanceof EssentiaJarBlockEntity jar
                && EssentiaTubeConnections.isOriginalJarTopFace(direction.getOpposite())
                && (aspect == null || jar.canAcceptAspect(aspect))) {
            boolean voidJar = level.getBlockState(destinationPos).is(ThaumcraftMod.VOID_ESSENTIA_JAR.get());

            // Stage204 exact TileJarFillable/TileJarFillableVoid suction edge cases.
            // v11.62.9: expose that suction only through the original jar UP face.
            // Normal jars stop pulling when full; void jars keep a baseline suction even while full.
            return jar.originalSuctionAmount(voidJar);
        }

        if (entity instanceof EssentiaReservoirBlockEntity reservoir
                && reservoir.canAccessFrom(direction.getOpposite())) {
            return reservoir.originalSuctionAmount(aspect);
        }

        return EssentiaSuction.SOURCE_NONE;
    }

    public static boolean isTubeLike(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }

        if (level.getBlockState(pos).is(ThaumcraftMod.ESSENTIA_VALVE.get())) {
            return level.getBlockEntity(pos) instanceof EssentiaTubeBlockEntity;
        }

        // Stage503-522: all original Tube subtypes (filter/restrict/oneway/buffer) are BE-backed
        // transport nodes, not just the normal tube registry id.
        return level.getBlockEntity(pos) instanceof EssentiaTubeBlockEntity;
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
