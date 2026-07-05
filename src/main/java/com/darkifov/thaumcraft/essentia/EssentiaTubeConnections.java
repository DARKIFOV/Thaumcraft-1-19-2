package com.darkifov.thaumcraft.essentia;

import com.darkifov.thaumcraft.blockentity.AlembicBlockEntity;
import com.darkifov.thaumcraft.blockentity.AlchemicalFurnaceBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
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

        BlockEntity neighbor = level.getBlockEntity(pos.relative(direction));
        return isTransportEndpoint(neighbor);
    }

    public static boolean isTransportEndpoint(BlockEntity entity) {
        return entity instanceof EssentiaTubeBlockEntity
                || entity instanceof EssentiaJarBlockEntity
                || entity instanceof AlembicBlockEntity
                || entity instanceof AlchemicalFurnaceBlockEntity;
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
