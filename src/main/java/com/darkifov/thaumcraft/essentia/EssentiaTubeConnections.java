package com.darkifov.thaumcraft.essentia;

import com.darkifov.thaumcraft.blockentity.AlembicBlockEntity;
import com.darkifov.thaumcraft.blockentity.AlchemicalCentrifugeBlockEntity;
import com.darkifov.thaumcraft.blockentity.AlchemicalFurnaceBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaReservoirBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaCrystalizerBlockEntity;
import com.darkifov.thaumcraft.blockentity.ThaumatoriumBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaTubeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.EnumMap;
import java.util.Map;

public final class EssentiaTubeConnections {
    private EssentiaTubeConnections() {
    }

    public static boolean canConnect(Level level, BlockPos pos, Direction direction) {
        if (level == null || pos == null || direction == null) {
            return false;
        }

        BlockPos targetPos = pos.relative(direction);
        Direction targetFace = direction.getOpposite();

        // TC4 TileThaumatorium exposes transport ports from both construct layers.
        // The upper layer has no separate block entity in 1.19.2, so resolve it back
        // to the lower controller before the ordinary neighbour lookup.
        ThaumatoriumBlockEntity thaumatorium = ThaumatoriumBlockEntity.resolveAt(level, targetPos);
        if (thaumatorium != null && thaumatorium.canConnectAt(targetPos, targetFace)) {
            return true;
        }

        BlockEntity neighbor = level.getBlockEntity(targetPos);
        if (neighbor instanceof AlchemicalCentrifugeBlockEntity centrifuge) {
            return centrifuge.canInputFrom(targetFace) || centrifuge.canOutputTo(targetFace);
        }
        if (neighbor instanceof AlchemicalFurnaceBlockEntity furnace && furnace.isAdvanced()) {
            return furnace.canAdvancedOutputTo(targetFace);
        }
        if (neighbor instanceof EssentiaCrystalizerBlockEntity crystalizer) {
            return crystalizer.canInputFrom(targetFace);
        }
        if (neighbor instanceof EssentiaJarBlockEntity) {
            // v11.62.9 strict TC4 TileJarFillable.isConnectable parity:
            // original jars connect/input/output only on ForgeDirection.UP.
            // From a tube this means the tube may connect only DOWN into the
            // jar sitting below it. Side/bottom tube arms are false positives.
            return isOriginalJarTopFace(direction.getOpposite());
        }
        if (neighbor instanceof EssentiaReservoirBlockEntity reservoir) {
            return reservoir.canAccessFrom(direction.getOpposite());
        }
        if (neighbor instanceof AlembicBlockEntity alembic) {
            return alembic.canOutputTo(direction.getOpposite());
        }
        return isTransportEndpoint(neighbor);
    }

    public static boolean isOriginalJarTopFace(Direction faceFromJar) {
        return faceFromJar == Direction.UP;
    }

    public static boolean isTransportEndpoint(BlockEntity entity) {
        return entity instanceof EssentiaTubeBlockEntity
                || entity instanceof EssentiaJarBlockEntity
                || entity instanceof EssentiaReservoirBlockEntity
                || entity instanceof ThaumatoriumBlockEntity
                || entity instanceof AlchemicalCentrifugeBlockEntity
                || (entity instanceof AlchemicalFurnaceBlockEntity furnace && furnace.isAdvanced())
                || entity instanceof EssentiaCrystalizerBlockEntity
                || entity instanceof AlembicBlockEntity;
    }

    public static Map<Direction, Boolean> scan(Level level, BlockPos pos) {
        Map<Direction, Boolean> result = new EnumMap<>(Direction.class);

        for (Direction direction : Direction.values()) {
            result.put(direction, canConnect(level, pos, direction));
        }

        return result;
    }

    public static int count(Level level, BlockPos pos) {
        int total = 0;

        for (boolean connected : scan(level, pos).values()) {
            if (connected) {
                total++;
            }
        }

        return total;
    }

    public static String summary(Level level, BlockPos pos) {
        StringBuilder builder = new StringBuilder();

        for (Direction direction : Direction.values()) {
            if (canConnect(level, pos, direction)) {
                if (!builder.isEmpty()) {
                    builder.append(", ");
                }

                builder.append(direction.getName());
            }
        }

        return builder.isEmpty() ? "none" : builder.toString();
    }
}
