package com.darkifov.thaumcraft.aura;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.darkifov.thaumcraft.blockentity.VisRelayBlockEntity;
import com.darkifov.thaumcraft.item.simple.TC4VisAmuletItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Loaded-chunk TC4 VisNetHandler adapter.
 *
 * Relay/source edges use the original eight-block range, wildcard/matching
 * attunement and line of sight. Energized-node output is consumed in exact
 * centivis-sized units from a transient pool that refills every server tick.
 */
public final class AuraVisRelayNetwork {
    public static final int NETWORK_RANGE = 8;
    private static final int NETWORK_RANGE_SQUARED = NETWORK_RANGE * NETWORK_RANGE;
    private static final int RELAY_GRAPH_LIMIT = 512;
    private static final double AMULET_RELAY_DISTANCE_SQUARED = 26.0D;

    private AuraVisRelayNetwork() {
    }

    public record RelayConnection(AuraNodeBlockEntity source, List<BlockPos> relayPath) {
        public RelayConnection {
            relayPath = List.copyOf(relayPath);
        }

        /** Next relay in the path, or the energized source when current is the last relay. */
        public BlockPos nextParent(BlockPos current) {
            int index = relayPath.indexOf(current);
            if (index < 0) return null;
            if (index + 1 < relayPath.size()) return relayPath.get(index + 1);
            return source.getBlockPos();
        }
    }

    /** Exact ItemAmuletVis relay recharge: up to five cv per primal aspect every five ticks. */
    public static int chargeAmuletFromNearestRelay(ServerLevel level, ServerPlayer player,
                                                    ItemStack stack, TC4VisAmuletItem amulet) {
        Optional<BlockPos> relayPos = findNearestRelayForPlayer(level, player);
        if (relayPos.isEmpty()) return 0;
        Optional<RelayConnection> connection = findConnection(level, relayPos.get(), relayAttunement(level, relayPos.get()));
        if (connection.isEmpty()) return 0;

        int movedTotal = 0;
        RelayConnection route = connection.get();
        for (Aspect aspect : primalAspects()) {
            int room = Math.max(0, amulet.capacity() - amulet.getVis(stack, aspect));
            int request = Math.min(5, room);
            if (request <= 0) continue;
            int drained = route.source().consumeEnergizedVis(aspect, request);
            if (drained <= 0) continue;
            amulet.addRealVis(stack, aspect, drained);
            triggerConsumeEffect(level, route, aspect, player);
            movedTotal += drained;
        }
        return movedTotal;
    }

    /** Exact TileMagicWorkbenchCharger/TileVisNode.consumeVis path from a known relay. */
    public static int drainFromRelay(ServerLevel level, BlockPos relayPos, Aspect aspect, int requestCentivis) {
        if (level == null || relayPos == null || aspect == null || !aspect.isPrimal() || requestCentivis <= 0) return 0;
        Optional<RelayConnection> connection = findConnection(level, relayPos, relayAttunement(level, relayPos));
        if (connection.isEmpty()) return 0;
        int drained = connection.get().source().consumeEnergizedVis(aspect, requestCentivis);
        if (drained > 0) triggerConsumeEffect(level, connection.get(), aspect, null);
        return drained;
    }

    /** 1.19.2 adapter for VisNetHandler.drainVis used by non-relay machines. */
    public static int drainMachineVis(ServerLevel level, BlockPos machinePos, Aspect aspect, int requestCentivis) {
        if (level == null || machinePos == null || aspect == null || !aspect.isPrimal() || requestCentivis <= 0) return 0;

        List<NetworkEntry> entries = new ArrayList<>();
        for (AuraNodeBlockEntity source : energizedSourcesWithin(level, machinePos, NETWORK_RANGE)) {
            entries.add(new NetworkEntry(distanceSquared(machineCenter(machinePos), center(source.getBlockPos())),
                    new RelayConnection(source, List.of())));
        }
        for (VisRelayBlockEntity relay : relayEntitiesWithin(level, machinePos, NETWORK_RANGE)) {
            findConnection(level, relay.getBlockPos(), relay.attunement()).ifPresent(connection ->
                    entries.add(new NetworkEntry(distanceSquared(machineCenter(machinePos), center(relay.getBlockPos())), connection)));
        }
        entries.sort(Comparator.comparingDouble(NetworkEntry::distance));

        int remaining = requestCentivis;
        int drainedTotal = 0;
        Set<BlockPos> usedSources = new HashSet<>();
        for (NetworkEntry entry : entries) {
            RelayConnection route = entry.connection();
            if (!usedSources.add(route.source().getBlockPos())) continue;
            int drained = route.source().consumeEnergizedVis(aspect, remaining);
            if (drained <= 0) continue;
            drainedTotal += drained;
            remaining -= drained;
            triggerConsumeEffect(level, route, aspect, null);
            if (remaining <= 0) break;
        }
        return drainedTotal;
    }

    public static Optional<RelayConnection> findConnection(ServerLevel level, BlockPos startRelay, byte attunement) {
        VisRelayBlockEntity start = relayEntity(level, startRelay);
        if (start == null) return Optional.empty();

        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Map<BlockPos, BlockPos> previous = new HashMap<>();
        Set<BlockPos> visited = new HashSet<>();
        BlockPos startPos = startRelay.immutable();
        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty() && visited.size() <= RELAY_GRAPH_LIMIT) {
            BlockPos current = queue.removeFirst();
            VisRelayBlockEntity currentRelay = relayEntity(level, current);
            if (currentRelay == null) continue;

            AuraNodeBlockEntity source = nearestVisibleSource(level, current);
            if (source != null) {
                return Optional.of(new RelayConnection(source, reconstructPath(startPos, current, previous)));
            }

            for (VisRelayBlockEntity nextRelay : relayEntitiesWithin(level, current, NETWORK_RANGE)) {
                BlockPos next = nextRelay.getBlockPos().immutable();
                if (next.equals(current) || visited.contains(next)) continue;
                if (!compatible(currentRelay.attunement(), nextRelay.attunement())) continue;
                if (!canSee(level, current, next)) continue;
                visited.add(next);
                previous.put(next, current);
                queue.addLast(next);
            }
        }
        return Optional.empty();
    }

    private static List<BlockPos> reconstructPath(BlockPos start, BlockPos end, Map<BlockPos, BlockPos> previous) {
        ArrayList<BlockPos> reversed = new ArrayList<>();
        BlockPos cursor = end;
        reversed.add(cursor);
        while (!cursor.equals(start)) {
            cursor = previous.get(cursor);
            if (cursor == null) return List.of(start);
            reversed.add(cursor);
        }
        java.util.Collections.reverse(reversed);
        return reversed;
    }

    public static Optional<BlockPos> findNearestRelayForPlayer(ServerLevel level, ServerPlayer player) {
        BlockPos origin = player.blockPosition();
        BlockPos best = null;
        double bestDistance = AMULET_RELAY_DISTANCE_SQUARED;
        for (VisRelayBlockEntity relay : relayEntitiesWithin(level, origin, 5)) {
            double distance = distanceSquared(player.position(), center(relay.getBlockPos()));
            if (distance < bestDistance) {
                bestDistance = distance;
                best = relay.getBlockPos().immutable();
            }
        }
        return Optional.ofNullable(best);
    }

    /** Compatibility helper retained for old callers/tests; exact player radius is handled above. */
    public static Optional<BlockPos> findNearestRelay(ServerLevel level, BlockPos origin, int radius) {
        return relayEntitiesWithin(level, origin, radius).stream()
                .min(Comparator.comparingDouble(relay -> distanceSquared(center(origin), center(relay.getBlockPos()))))
                .map(relay -> relay.getBlockPos().immutable());
    }

    private static AuraNodeBlockEntity nearestVisibleSource(ServerLevel level, BlockPos relayPos) {
        return energizedSourcesWithin(level, relayPos, NETWORK_RANGE).stream()
                .filter(source -> canSee(level, relayPos, source.getBlockPos()))
                .min(Comparator.comparingDouble(source -> distanceSquared(center(relayPos), center(source.getBlockPos()))))
                .orElse(null);
    }

    private static List<AuraNodeBlockEntity> energizedSourcesWithin(ServerLevel level, BlockPos origin, int radius) {
        List<AuraNodeBlockEntity> out = new ArrayList<>();
        for (BlockPos mutable : BlockPos.betweenClosed(origin.offset(-radius, -radius, -radius), origin.offset(radius, radius, radius))) {
            BlockPos pos = mutable.immutable();
            if (pos.equals(origin) || !level.hasChunkAt(pos)) continue;
            if (distanceSquared(center(origin), center(pos)) > radius * radius) continue;
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof AuraNodeBlockEntity node && node.isEnergized()) out.add(node);
        }
        return out;
    }

    private static List<VisRelayBlockEntity> relayEntitiesWithin(ServerLevel level, BlockPos origin, int radius) {
        List<VisRelayBlockEntity> out = new ArrayList<>();
        for (BlockPos mutable : BlockPos.betweenClosed(origin.offset(-radius, -radius, -radius), origin.offset(radius, radius, radius))) {
            BlockPos pos = mutable.immutable();
            if (!level.hasChunkAt(pos)) continue;
            if (distanceSquared(center(origin), center(pos)) > radius * radius) continue;
            VisRelayBlockEntity relay = relayEntity(level, pos);
            if (relay != null) out.add(relay);
        }
        return out;
    }

    private static VisRelayBlockEntity relayEntity(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) return null;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof VisRelayBlockEntity relay ? relay : null;
    }

    private static byte relayAttunement(ServerLevel level, BlockPos pos) {
        VisRelayBlockEntity relay = relayEntity(level, pos);
        return relay == null ? -1 : relay.attunement();
    }

    private static boolean compatible(byte first, byte second) {
        return first == -1 || second == -1 || first == second;
    }

    private static boolean canSee(ServerLevel level, BlockPos fromPos, BlockPos toPos) {
        Vec3 fromCenter = center(fromPos);
        Vec3 toCenter = center(toPos);
        Vec3 direction = toCenter.subtract(fromCenter);
        if (direction.lengthSqr() < 1.0E-6D) return true;
        Vec3 start = fromCenter.add(direction.normalize().scale(0.55D));
        BlockHitResult hit = level.clip(new ClipContext(start, toCenter,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)null));
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(toPos);
    }

    private static void triggerConsumeEffect(ServerLevel level, RelayConnection route, Aspect aspect, ServerPlayer player) {
        for (BlockPos relayPos : route.relayPath()) {
            VisRelayBlockEntity relay = relayEntity(level, relayPos);
            if (relay != null) {
                relay.triggerPulse(aspect);
                BlockPos next = route.nextParent(relayPos);
                if (next != null) relay.setParentPos(next);
            }
        }
        route.source().markWandDrain(aspect, player);
    }

    private static Aspect[] primalAspects() {
        return new Aspect[]{Aspect.AER, Aspect.TERRA, Aspect.IGNIS, Aspect.AQUA, Aspect.ORDO, Aspect.PERDITIO};
    }

    private static Vec3 center(BlockPos pos) {
        return Vec3.atCenterOf(pos);
    }

    private static Vec3 machineCenter(BlockPos pos) {
        return Vec3.atCenterOf(pos);
    }

    private static double distanceSquared(Vec3 first, Vec3 second) {
        return first.distanceToSqr(second);
    }

    private record NetworkEntry(double distance, RelayConnection connection) {
    }
}
