package com.darkifov.thaumcraft.essentia;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.blockentity.AlembicBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaReservoirBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaTubeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Shared Stage503-562 helper for original TC4 tube-network consumers such as Thaumatorium.
 * Stage523-542 tightened this to obey tube side gates, one-way traversal, filters, valves
 * and TileTubeBuffer-like storage instead of treating all connected blocks as one flat tank.
 */
public final class TC4EssentiaNetworkRuntime {
    private static final int MAX_NETWORK = 96;

    private TC4EssentiaNetworkRuntime() {
    }

    public static int networkSize(Level level, BlockPos consumerPos) {
        return collectNetwork(level, consumerPos).size();
    }

    public static int available(Level level, BlockPos consumerPos, Aspect aspect) {
        if (level == null || consumerPos == null || aspect == null) {
            return 0;
        }
        int total = 0;
        for (ContainerRef ref : reachableContainers(level, consumerPos, aspect)) {
            total += ref.amount(aspect);
        }
        return total;
    }

    public static int drain(Level level, BlockPos consumerPos, Aspect aspect, int amount) {
        if (level == null || consumerPos == null || aspect == null || amount <= 0) {
            return 0;
        }
        int remaining = amount;
        for (ContainerRef ref : reachableContainers(level, consumerPos, aspect)) {
            if (remaining <= 0) {
                break;
            }
            remaining -= ref.remove(aspect, remaining);
        }
        return amount - remaining;
    }

    private static Set<BlockPos> collectNetwork(Level level, BlockPos consumerPos) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        if (level == null || consumerPos == null) {
            return visited;
        }
        for (Direction direction : Direction.values()) {
            BlockPos next = consumerPos.relative(direction);
            BlockEntity nextBe = level.getBlockEntity(next);
            if (nextBe instanceof EssentiaTubeBlockEntity tube
                    && EssentiaSuctionResolver.sideAllows(level, consumerPos, direction)
                    && tube.allowsOutputTo(direction.getOpposite())) {
                visited.add(next.immutable());
                queue.add(next.immutable());
            }
        }
        while (!queue.isEmpty() && visited.size() < MAX_NETWORK) {
            BlockPos current = queue.removeFirst();
            BlockEntity be = level.getBlockEntity(current);
            if (!(be instanceof EssentiaTubeBlockEntity tube)) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (visited.contains(next)
                        || !EssentiaSuctionResolver.sideAllows(level, current, direction)
                        || !tube.allowsInputFrom(direction)) {
                    continue;
                }
                BlockEntity nextBe = level.getBlockEntity(next);
                if (nextBe instanceof EssentiaTubeBlockEntity nextTube && nextTube.allowsOutputTo(direction.getOpposite())) {
                    visited.add(next.immutable());
                    queue.add(next.immutable());
                }
            }
        }
        return visited;
    }

    private static List<ContainerRef> reachableContainers(Level level, BlockPos consumerPos, Aspect aspect) {
        Set<ContainerRef> refs = new java.util.LinkedHashSet<>();
        Set<BlockPos> network = collectNetwork(level, consumerPos);
        for (BlockPos tubePos : network) {
            BlockEntity tubeBe = level.getBlockEntity(tubePos);
            if (!(tubeBe instanceof EssentiaTubeBlockEntity tube) || !tube.allowsAspectForTransfer(aspect)) {
                continue;
            }
            if (tube.subtype().storesBufferEssentia() && tube.bufferAmount() > 0 && tube.bufferAspect() == aspect) {
                refs.add(new BufferTubeRef(tube));
            }
            for (Direction direction : Direction.values()) {
                if (!EssentiaSuctionResolver.sideAllows(level, tubePos, direction) || !tube.allowsInputFrom(direction)) {
                    continue;
                }
                BlockPos adjacent = tubePos.relative(direction);
                BlockEntity be = level.getBlockEntity(adjacent);
                Direction sideFromContainer = direction.getOpposite();
                if (be instanceof EssentiaJarBlockEntity jar && jar.aspects().get(aspect) > 0) {
                    refs.add(new JarRef(jar));
                } else if (be instanceof EssentiaReservoirBlockEntity reservoir && reservoir.canAccessFrom(sideFromContainer) && reservoir.aspects().get(aspect) > 0) {
                    refs.add(new ReservoirRef(reservoir));
                } else if (be instanceof AlembicBlockEntity alembic && alembic.aspects().get(aspect) > 0) {
                    refs.add(new AlembicRef(alembic));
                }
            }
        }
        List<ContainerRef> ordered = new ArrayList<>(refs);
        ordered.sort(Comparator.comparingInt(ContainerRef::sourcePriority).reversed());
        return ordered;
    }

    private interface ContainerRef {
        int amount(Aspect aspect);
        int remove(Aspect aspect, int amount);
        int sourcePriority();
    }

    private record JarRef(EssentiaJarBlockEntity jar) implements ContainerRef {
        @Override
        public int amount(Aspect aspect) {
            return jar.aspects().get(aspect);
        }

        @Override
        public int remove(Aspect aspect, int amount) {
            int removed = Math.min(amount, jar.aspects().get(aspect));
            return removed > 0 && jar.takeFromContainerOriginal(aspect, removed) ? removed : 0;
        }

        @Override
        public int sourcePriority() {
            return EssentiaSuction.JAR_SOURCE_PRIORITY;
        }
    }

    private record ReservoirRef(EssentiaReservoirBlockEntity reservoir) implements ContainerRef {
        @Override
        public int amount(Aspect aspect) {
            return reservoir.aspects().get(aspect);
        }

        @Override
        public int remove(Aspect aspect, int amount) {
            return reservoir.removeEssentia(aspect, amount);
        }

        @Override
        public int sourcePriority() {
            return EssentiaSuction.RESERVOIR_SOURCE_PRIORITY;
        }
    }

    private record AlembicRef(AlembicBlockEntity alembic) implements ContainerRef {
        @Override
        public int amount(Aspect aspect) {
            return alembic.aspects().get(aspect);
        }

        @Override
        public int remove(Aspect aspect, int amount) {
            return alembic.removeEssentia(aspect, amount);
        }

        @Override
        public int sourcePriority() {
            return EssentiaSuction.ALEMBIC_SOURCE_PRIORITY;
        }
    }

    private record BufferTubeRef(EssentiaTubeBlockEntity tube) implements ContainerRef {
        @Override
        public int amount(Aspect aspect) {
            return tube.bufferAspect() == aspect ? tube.bufferAmount() : 0;
        }

        @Override
        public int remove(Aspect aspect, int amount) {
            return tube.drainBufferForNetwork(aspect, amount);
        }

        @Override
        public int sourcePriority() {
            return 32;
        }
    }
}
