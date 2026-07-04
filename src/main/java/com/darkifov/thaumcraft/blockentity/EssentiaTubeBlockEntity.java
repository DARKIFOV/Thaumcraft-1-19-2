package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.config.ThaumcraftConfig;
import com.darkifov.thaumcraft.block.EssentiaJarBlock;
import com.darkifov.thaumcraft.block.EssentiaValveBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public class EssentiaTubeBlockEntity extends BlockEntity {
    public static final int MAX_NETWORK = 48;
    public static final int TRANSFER_AMOUNT = 1;

    public static final int SUCTION_NORMAL_JAR = 32;
    public static final int SUCTION_FILTERED_JAR = 48;
    public static final int SUCTION_VOID_JAR = 64;

    public EssentiaTubeBlockEntity(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        super(ThaumcraftMod.ESSENTIA_TUBE_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, net.minecraft.world.level.block.state.BlockState state, EssentiaTubeBlockEntity tube) {
        if (level.getGameTime() % Math.max(1, ThaumcraftConfig.ESSENTIA_TUBE_TRANSFER_INTERVAL_TICKS.get()) != 0L) {
            return;
        }

        tube.tryMoveEssentia();
    }

    public int networkSize() {
        if (level == null) {
            return 0;
        }

        return collectTubeNetwork(level, worldPosition).size();
    }

    private void tryMoveEssentia() {
        if (level == null || level.isClientSide) {
            return;
        }

        Set<BlockPos> network = collectTubeNetwork(level, worldPosition);
        AlchemicalFurnaceBlockEntity source = findSourceFurnace(level, network);

        if (source == null || source.aspects().isEmpty()) {
            return;
        }

        Aspect aspect = source.firstAspect();

        if (aspect == null) {
            return;
        }

        Destination destination = findBestDestinationJar(level, network, aspect);

        if (destination == null) {
            return;
        }

        int removed = source.removeUpTo(aspect, TRANSFER_AMOUNT);

        if (removed <= 0) {
            return;
        }

        int accepted = destination.jar.acceptFromTube(aspect, removed, destination.voidJar);

        if (accepted <= 0) {
            source.aspects().add(aspect, removed);
            source.setChangedAndSync();
            return;
        }

        if (level instanceof ServerLevel serverLevel) {
            BlockPos to = destination.jar.getBlockPos();
            serverLevel.sendParticles(ParticleTypes.PORTAL,
                    worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 0.5D,
                    worldPosition.getZ() + 0.5D,
                    5,
                    0.2D,
                    0.2D,
                    0.2D,
                    0.02D);
            serverLevel.sendParticles(destination.voidJar ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.WITCH,
                    to.getX() + 0.5D,
                    to.getY() + 0.95D,
                    to.getZ() + 0.5D,
                    3,
                    0.15D,
                    0.15D,
                    0.15D,
                    0.01D);
        }
    }

    private Set<BlockPos> collectTubeNetwork(Level level, BlockPos start) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start.immutable());
        visited.add(start.immutable());

        while (!queue.isEmpty() && visited.size() < ThaumcraftConfig.ESSENTIA_TUBE_MAX_NETWORK.get()) {
            BlockPos current = queue.removeFirst();

            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);

                if (visited.contains(next)) {
                    continue;
                }

                if (isOpenTubeLike(level, next)) {
                    visited.add(next.immutable());
                    queue.add(next.immutable());
                }
            }
        }

        return visited;
    }

    private boolean isOpenTubeLike(Level level, BlockPos pos) {
        if (level.getBlockState(pos).is(ThaumcraftMod.ESSENTIA_TUBE.get())) {
            return true;
        }

        if (level.getBlockState(pos).is(ThaumcraftMod.ESSENTIA_VALVE.get())) {
            return EssentiaValveBlock.isOpen(level, pos);
        }

        return false;
    }

    private AlchemicalFurnaceBlockEntity findSourceFurnace(Level level, Set<BlockPos> network) {
        for (BlockPos tubePos : network) {
            for (Direction direction : Direction.values()) {
                BlockEntity blockEntity = level.getBlockEntity(tubePos.relative(direction));

                if (blockEntity instanceof AlchemicalFurnaceBlockEntity furnace && !furnace.aspects().isEmpty()) {
                    return furnace;
                }
            }
        }

        return null;
    }

    private Destination findBestDestinationJar(Level level, Set<BlockPos> network, Aspect aspect) {
        Destination best = null;

        for (BlockPos tubePos : network) {
            for (Direction direction : Direction.values()) {
                BlockPos jarPos = tubePos.relative(direction);
                BlockEntity blockEntity = level.getBlockEntity(jarPos);

                if (!(blockEntity instanceof EssentiaJarBlockEntity jar) || !jar.canAcceptAspect(aspect)) {
                    continue;
                }

                boolean voidJar = level.getBlockState(jarPos).is(ThaumcraftMod.VOID_ESSENTIA_JAR.get());
                boolean filteredJar = level.getBlockState(jarPos).is(ThaumcraftMod.FILTERED_ESSENTIA_JAR.get());

                if (!voidJar && jar.aspects().totalAmount() >= EssentiaJarBlock.CAPACITY) {
                    continue;
                }

                int suction = voidJar ? SUCTION_VOID_JAR : filteredJar ? SUCTION_FILTERED_JAR : SUCTION_NORMAL_JAR;

                if (best == null || suction > best.suction) {
                    best = new Destination(jar, suction, voidJar);
                }
            }
        }

        return best;
    }

    private record Destination(EssentiaJarBlockEntity jar, int suction, boolean voidJar) {
    }
}
