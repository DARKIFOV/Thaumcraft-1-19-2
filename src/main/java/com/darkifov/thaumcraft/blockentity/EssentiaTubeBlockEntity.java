package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.essentia.EssentiaTubeConnections;
import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectColor;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.EssentiaJarBlock;
import com.darkifov.thaumcraft.block.EssentiaValveBlock;
import com.darkifov.thaumcraft.config.ThaumcraftConfig;
import com.darkifov.thaumcraft.essentia.EssentiaSuction;
import com.darkifov.thaumcraft.essentia.EssentiaBackflowResult;
import com.darkifov.thaumcraft.essentia.EssentiaSuctionResolver;
import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public class EssentiaTubeBlockEntity extends BlockEntity {
    // Directional tube pass: traversal is prepared to respect EssentiaTubeConnections side checks.
    private boolean connectedTransportNeighbor(BlockPos origin, net.minecraft.core.Direction direction) {
        return level != null && EssentiaSuctionResolver.sideAllows(level, origin, direction);
    }

    public static final int MAX_NETWORK = 48;
    public static final int TRANSFER_AMOUNT = 1;

    public static final int SUCTION_NORMAL_JAR = EssentiaSuction.JAR_NORMAL;
    public static final int SUCTION_FILTERED_JAR = EssentiaSuction.JAR_FILTERED;
    public static final int SUCTION_VOID_JAR = EssentiaSuction.JAR_VOID;

    private int lastNetworkSize;
    private int lastSourceCount;
    private int lastDestinationCount;
    private String lastMovedAspect = "";
    private int lastConflictCount;
    private int lastWinningSuction;
    private int lastSourcePressure;
    private boolean lastBackflowBlocked;

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

    public int lastNetworkSize() {
        return lastNetworkSize;
    }

    public int lastSourceCount() {
        return lastSourceCount;
    }

    public int lastDestinationCount() {
        return lastDestinationCount;
    }

    public String lastMovedAspect() {
        return lastMovedAspect;
    }

    public String connectedSidesDiagnostic(Level level, BlockPos pos) {
        return "Connected sides: " + EssentiaTubeConnections.summary(level, pos);
    }

    public int lastConflictCount() {
        return lastConflictCount;
    }

    public int lastWinningSuction() {
        return lastWinningSuction;
    }

    public int lastSourcePressure() {
        return lastSourcePressure;
    }

    public boolean lastBackflowBlocked() {
        return lastBackflowBlocked;
    }

    private void tryMoveEssentia() {
        if (level == null || level.isClientSide) {
            return;
        }

        Set<BlockPos> network = collectTubeNetwork(level, worldPosition);
        lastNetworkSize = network.size();

        Source source = findBestSource(level, network);
        lastSourceCount = countSources(level, network);

        if (source == null || source.aspect() == null) {
            lastMovedAspect = "";
            lastWinningSuction = 0;
            lastSourcePressure = 0;
            lastConflictCount = 0;
            lastBackflowBlocked = false;
            return;
        }

        Destination destination = findBestDestinationJar(level, network, source.aspect());
        lastDestinationCount = countDestinations(level, network, source.aspect());

        if (destination == null || destination.suction() <= EssentiaSuction.SOURCE_NONE) {
            lastMovedAspect = "";
            lastWinningSuction = 0;
            lastSourcePressure = source.priority();
            lastConflictCount = 0;
            lastBackflowBlocked = true;
            return;
        }

        lastWinningSuction = destination.suction();
        lastSourcePressure = source.priority();
        lastConflictCount = EssentiaSuctionResolver.competingDestinations(level, network, source.aspect(), destination.jar().getBlockPos());
        lastBackflowBlocked = destination.suction() <= source.priority();

        EssentiaBackflowResult backflowResult = new EssentiaBackflowResult(
                source.aspect(),
                worldPosition,
                Direction.NORTH,
                destination.jar().getBlockPos(),
                Direction.SOUTH,
                source.priority(),
                destination.suction(),
                lastConflictCount,
                lastBackflowBlocked
        );

        if (!backflowResult.canMove()) {
            lastMovedAspect = "";
            return;
        }

        int removed = source.remove(TRANSFER_AMOUNT);

        if (removed <= 0) {
            lastMovedAspect = "";
            return;
        }

        int accepted = destination.jar().acceptFromTube(source.aspect(), removed, destination.voidJar());

        if (accepted <= 0) {
            source.restore(removed);
            lastMovedAspect = "";
            return;
        }

        lastMovedAspect = source.aspect().id();
        renderTransferParticles(source.aspect(), destination.jar().getBlockPos(), destination.voidJar());
    }

    private void renderTransferParticles(Aspect aspect, BlockPos destination, boolean voidJar) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int rgb = AspectColor.rgb(aspect);
        float r = ((rgb >> 16) & 255) / 255.0F;
        float g = ((rgb >> 8) & 255) / 255.0F;
        float b = (rgb & 255) / 255.0F;

        serverLevel.sendParticles(new DustParticleOptions(new Vector3f(r, g, b), 0.8F),
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D,
                3,
                0.18D,
                0.18D,
                0.18D,
                0.01D);
        serverLevel.sendParticles(voidJar ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.WITCH,
                destination.getX() + 0.5D,
                destination.getY() + 0.95D,
                destination.getZ() + 0.5D,
                2,
                0.12D,
                0.12D,
                0.12D,
                0.01D);
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

                if (connectedTransportNeighbor(current, direction) && isOpenTubeLike(level, next)) {
                    visited.add(next.immutable());
                    queue.add(next.immutable());
                }
            }
        }

        return visited;
    }

    private boolean isOpenTubeLike(Level level, BlockPos pos) {
        return EssentiaSuctionResolver.isTubeLike(level, pos);
    }

    private Source findBestSource(Level level, Set<BlockPos> network) {
        Source best = null;

        for (BlockPos tubePos : network) {
            for (Direction direction : Direction.values()) {
                if (!EssentiaSuctionResolver.sideAllows(level, tubePos, direction)) {
                    continue;
                }

                BlockPos adjacent = tubePos.relative(direction);
                BlockEntity blockEntity = level.getBlockEntity(adjacent);
                Source source = sourceFrom(blockEntity);

                if (source == null) {
                    continue;
                }

                if (best == null || source.priority() > best.priority()) {
                    best = source;
                }
            }
        }

        return best;
    }

    private Source sourceFrom(BlockEntity blockEntity) {
        if (blockEntity instanceof AlembicBlockEntity alembic) {
            Aspect aspect = alembic.aspects().firstAspect();

            if (aspect != null) {
                return new AlembicSource(alembic, aspect);
            }
        }

        if (blockEntity instanceof AlchemicalFurnaceBlockEntity furnace) {
            Aspect aspect = furnace.firstAspect();

            if (aspect != null) {
                return new FurnaceSource(furnace, aspect);
            }
        }

        return null;
    }

    private int countSources(Level level, Set<BlockPos> network) {
        int count = 0;

        for (BlockPos tubePos : network) {
            for (Direction direction : Direction.values()) {
                if (EssentiaSuctionResolver.sideAllows(level, tubePos, direction)
                        && sourceFrom(level.getBlockEntity(tubePos.relative(direction))) != null) {
                    count++;
                }
            }
        }

        return count;
    }

    private Destination findBestDestinationJar(Level level, Set<BlockPos> network, Aspect aspect) {
        Destination best = null;

        for (BlockPos tubePos : network) {
            for (Direction direction : Direction.values()) {
                if (!EssentiaSuctionResolver.sideAllows(level, tubePos, direction)) {
                    continue;
                }

                BlockPos jarPos = tubePos.relative(direction);
                BlockEntity blockEntity = level.getBlockEntity(jarPos);

                if (!(blockEntity instanceof EssentiaJarBlockEntity jar) || !jar.canAcceptAspect(aspect)) {
                    continue;
                }

                boolean voidJar = level.getBlockState(jarPos).is(ThaumcraftMod.VOID_ESSENTIA_JAR.get());

                int suction = EssentiaSuctionResolver.destinationSuction(level, tubePos, direction, aspect);

                if (suction <= EssentiaSuction.SOURCE_NONE) {
                    continue;
                }

                if (best == null || suction > best.suction()) {
                    best = new Destination(jar, suction, voidJar);
                }
            }
        }

        return best;
    }

    private int countDestinations(Level level, Set<BlockPos> network, Aspect aspect) {
        int count = 0;

        for (BlockPos tubePos : network) {
            for (Direction direction : Direction.values()) {
                if (EssentiaSuctionResolver.destinationSuction(level, tubePos, direction, aspect) > EssentiaSuction.SOURCE_NONE) {
                    count++;
                }
            }
        }

        return count;
    }

    private interface Source {
        Aspect aspect();

        int priority();

        int remove(int amount);

        void restore(int amount);
    }

    private record AlembicSource(AlembicBlockEntity alembic, Aspect aspect) implements Source {
        @Override
        public int priority() {
            return EssentiaSuction.ALEMBIC_SOURCE_PRIORITY;
        }

        @Override
        public int remove(int amount) {
            return alembic.removeEssentia(aspect, amount);
        }

        @Override
        public void restore(int amount) {
            alembic.addEssentia(aspect, amount);
        }
    }

    private record FurnaceSource(AlchemicalFurnaceBlockEntity furnace, Aspect aspect) implements Source {
        @Override
        public int priority() {
            return EssentiaSuction.FURNACE_SOURCE_PRIORITY;
        }

        @Override
        public int remove(int amount) {
            return furnace.removeUpTo(aspect, amount);
        }

        @Override
        public void restore(int amount) {
            furnace.aspects().add(aspect, amount);
            furnace.setChangedAndSync();
        }
    }

    private record Destination(EssentiaJarBlockEntity jar, int suction, boolean voidJar) {
    }
}
