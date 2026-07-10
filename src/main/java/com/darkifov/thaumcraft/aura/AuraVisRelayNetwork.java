package com.darkifov.thaumcraft.aura;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.mojang.math.Vector3f;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * TC4-style energized node relay bridge for the 1.19.2 port.
 *
 * Original TC4 treats transduced/stabilized nodes as a vis source and then pushes that
 * power through relay hardware instead of forcing the player to click the node itself.
 * This runtime keeps that behavior data-driven and safe for Forge 1.19.2: only energized
 * primal vis can enter a wand, relay chains are distance-limited, and the node is still
 * drained as the source of truth.
 */
public final class AuraVisRelayNetwork {
    private static final Aspect[] PRIMARY = new Aspect[]{
            Aspect.AER,
            Aspect.TERRA,
            Aspect.IGNIS,
            Aspect.AQUA,
            Aspect.ORDO,
            Aspect.PERDITIO
    };

    private static final int PLAYER_RELAY_RADIUS = 8;
    private static final int MACHINE_NETWORK_RADIUS = 8;
    private static final int RELAY_CHAIN_LIMIT = 32;
    private static final int NODE_SCAN_RADIUS = 12;
    private static final int CENTIVIS_PER_NODE_POINT = 100;

    private AuraVisRelayNetwork() {
    }

    public static void tickPlayerRecharge(ServerLevel level, ServerPlayer player) {
        if (player.tickCount % 20 != 0) {
            return;
        }

        Optional<BlockPos> relay = findNearestRelay(level, player.blockPosition(), PLAYER_RELAY_RADIUS);
        if (relay.isEmpty()) {
            return;
        }

        ItemStack wand = findRechargeableWand(player);
        if (wand.isEmpty()) {
            return;
        }

        int moved = chargeWandFromRelay(wand, level, relay.get(), player, false);
        if (moved > 0) {
            player.displayClientMessage(Component.literal("Vis relay charges wand: +" + moved + " primal vis").withStyle(ChatFormatting.AQUA), true);
        }
    }

    public static int chargeWandFromRelay(ItemStack wandStack, ServerLevel level, BlockPos relayPos, Player player, boolean feedback) {
        if (!(wandStack.getItem() instanceof WandItem wandItem)) {
            return 0;
        }
        if (WandItem.hasInfiniteVis(wandStack)) {
            return 0;
        }

        Optional<AuraNodeBlockEntity> node = findConnectedEnergizedNode(level, relayPos);
        if (node.isEmpty()) {
            if (feedback) {
                player.displayClientMessage(Component.literal("No energized aura node is linked to this relay.").withStyle(ChatFormatting.GRAY), true);
            }
            return 0;
        }

        AuraNodeBlockEntity source = node.get();
        int movedTotal = 0;
        int perAspectDrain = source.stabilizerStrength() >= 2 ? 2 : 1;

        for (Aspect aspect : PRIMARY) {
            int current = WandItem.getVis(wandStack, aspect);
            int capacity = wandItem.stackVisCapacity(wandStack);
            if (current >= capacity) {
                continue;
            }
            int space = Math.max(0, (capacity - current + 99) / 100);
            int drained = source.drainToWand(aspect, Math.min(perAspectDrain, space));
            if (drained > 0) {
                WandItem.addVis(wandStack, aspect, drained);
                movedTotal += drained;
            }
        }

        if (movedTotal > 0) {
            playRelayFx(level, relayPos, source.getBlockPos(), player.blockPosition());
        } else if (feedback) {
            player.displayClientMessage(Component.literal("The relay is linked, but your wand is full or the energized node is depleted.").withStyle(ChatFormatting.GRAY), true);
        }

        return movedTotal;
    }

    /**
     * 1.19.2 adapter for original VisNetHandler.drainVis used by machines such as
     * the Focal Manipulator. Original energized nodes and relays expose an
     * eight-block vis-network range and machine costs are measured in centivis.
     * The local aura-node storage is whole vis points, so one point equals 100 cv.
     */
    public static int drainMachineVis(ServerLevel level, BlockPos machinePos, Aspect aspect, int requestCentivis) {
        if (level == null || machinePos == null || aspect == null || !aspect.isPrimal()
                || requestCentivis < CENTIVIS_PER_NODE_POINT) {
            return 0;
        }

        int requestedPoints = Math.max(1, requestCentivis / CENTIVIS_PER_NODE_POINT);
        Optional<AuraNodeBlockEntity> source = findEnergizedNodeNearWithAspect(
                level, machinePos, MACHINE_NETWORK_RADIUS, aspect);
        BlockPos relayPos = null;

        if (source.isEmpty()) {
            Optional<BlockPos> relay = findNearestRelay(level, machinePos, MACHINE_NETWORK_RADIUS);
            if (relay.isPresent()) {
                Optional<AuraNodeBlockEntity> connected = findConnectedEnergizedNodeWithAspect(level, relay.get(), aspect);
                if (connected.isPresent()) {
                    source = connected;
                    relayPos = relay.get();
                }
            }
        }

        if (source.isEmpty()) return 0;
        AuraNodeBlockEntity node = source.get();
        int drainedPoints = node.drainToWand(aspect, requestedPoints);
        if (drainedPoints <= 0) return 0;

        node.markWandDrain(aspect, null);
        playMachineRelayFx(level, machinePos, relayPos, node.getBlockPos(), aspect);
        return Math.min(requestCentivis, drainedPoints * CENTIVIS_PER_NODE_POINT);
    }


    public static Optional<BlockPos> findNearestRelay(ServerLevel level, BlockPos origin, int radius) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;

        for (BlockPos mutable : BlockPos.betweenClosed(origin.offset(-radius, -radius, -radius), origin.offset(radius, radius, radius))) {
            BlockPos pos = mutable.immutable();
            if (!isRelay(level, pos)) {
                continue;
            }
            double distance = distanceSquared(pos, origin);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = pos;
            }
        }

        return Optional.ofNullable(best);
    }

    private static Optional<AuraNodeBlockEntity> findConnectedEnergizedNodeWithAspect(
            ServerLevel level, BlockPos relayStart, Aspect aspect) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(relayStart.immutable());
        visited.add(relayStart.immutable());

        while (!queue.isEmpty() && visited.size() <= RELAY_CHAIN_LIMIT) {
            BlockPos relay = queue.removeFirst();
            Optional<AuraNodeBlockEntity> nearbyNode = findEnergizedNodeNearWithAspect(
                    level, relay, NODE_SCAN_RADIUS, aspect);
            if (nearbyNode.isPresent()) return nearbyNode;

            for (Direction direction : Direction.values()) {
                BlockPos next = relay.relative(direction);
                if (visited.contains(next) || !isRelay(level, next)) continue;
                visited.add(next.immutable());
                queue.add(next.immutable());
            }
        }
        return Optional.empty();
    }

    public static Optional<AuraNodeBlockEntity> findConnectedEnergizedNode(ServerLevel level, BlockPos relayStart) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(relayStart.immutable());
        visited.add(relayStart.immutable());

        while (!queue.isEmpty() && visited.size() <= RELAY_CHAIN_LIMIT) {
            BlockPos relay = queue.removeFirst();

            Optional<AuraNodeBlockEntity> nearbyNode = findEnergizedNodeNear(level, relay, NODE_SCAN_RADIUS);
            if (nearbyNode.isPresent()) {
                return nearbyNode;
            }

            for (Direction direction : Direction.values()) {
                BlockPos next = relay.relative(direction);
                if (visited.contains(next) || !isRelay(level, next)) {
                    continue;
                }
                visited.add(next.immutable());
                queue.add(next.immutable());
            }
        }

        return Optional.empty();
    }

    private static Optional<AuraNodeBlockEntity> findEnergizedNodeNearWithAspect(
            ServerLevel level, BlockPos origin, int radius, Aspect aspect) {
        AuraNodeBlockEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos mutable : BlockPos.betweenClosed(
                origin.offset(-radius, -radius, -radius), origin.offset(radius, radius, radius))) {
            BlockPos pos = mutable.immutable();
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof AuraNodeBlockEntity node)
                    || !node.isEnergized() || node.aspects().get(aspect) <= 0) {
                continue;
            }
            double distance = distanceSquared(pos, origin);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = node;
            }
        }
        return Optional.ofNullable(best);
    }

    private static Optional<AuraNodeBlockEntity> findEnergizedNodeNear(ServerLevel level, BlockPos relayPos, int radius) {
        AuraNodeBlockEntity best = null;
        double bestDistance = Double.MAX_VALUE;

        for (BlockPos mutable : BlockPos.betweenClosed(relayPos.offset(-radius, -radius, -radius), relayPos.offset(radius, radius, radius))) {
            BlockPos pos = mutable.immutable();
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof AuraNodeBlockEntity node) || !node.isEnergized()) {
                continue;
            }
            double distance = distanceSquared(pos, relayPos);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = node;
            }
        }

        return Optional.ofNullable(best);
    }

    private static ItemStack findRechargeableWand(Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof WandItem && needsVis(main)) {
            return main;
        }

        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof WandItem && needsVis(offhand)) {
            return offhand;
        }

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof WandItem && needsVis(stack)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static boolean needsVis(ItemStack stack) {
        if (!(stack.getItem() instanceof WandItem wandItem) || WandItem.hasInfiniteVis(stack)) {
            return false;
        }
        int capacity = wandItem.stackVisCapacity(stack);
        for (Aspect aspect : PRIMARY) {
            if (WandItem.getVis(stack, aspect) < capacity) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRelay(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(ThaumcraftMod.VIS_RELAY.get());
    }

    private static double distanceSquared(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private static void playMachineRelayFx(ServerLevel level, BlockPos machinePos, BlockPos relayPos,
                                           BlockPos nodePos, Aspect aspect) {
        int color = aspect.nativeColor();
        Vector3f rgb = new Vector3f(
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F);
        level.playSound(null, machinePos, TC4Sounds.event("wand"), SoundSource.BLOCKS, 0.12F, 1.25F);
        level.sendParticles(new DustParticleOptions(rgb, 0.75F),
                machinePos.getX() + 0.5D, machinePos.getY() + 1.05D, machinePos.getZ() + 0.5D,
                3, 0.18D, 0.15D, 0.18D, 0.01D);
        if (relayPos != null) {
            level.sendParticles(new DustParticleOptions(rgb, 0.65F),
                    relayPos.getX() + 0.5D, relayPos.getY() + 0.55D, relayPos.getZ() + 0.5D,
                    2, 0.12D, 0.12D, 0.12D, 0.01D);
        }
        level.sendParticles(new DustParticleOptions(rgb, 0.65F),
                nodePos.getX() + 0.5D, nodePos.getY() + 0.5D, nodePos.getZ() + 0.5D,
                2, 0.16D, 0.16D, 0.16D, 0.01D);
    }

    private static void playRelayFx(ServerLevel level, BlockPos relayPos, BlockPos nodePos, BlockPos playerPos) {
        level.playSound(null, relayPos, TC4Sounds.event("wand"), SoundSource.BLOCKS, 0.25F, 1.35F);
        level.sendParticles(new DustParticleOptions(new Vector3f(0.35F, 0.65F, 1.0F), 0.9F),
                relayPos.getX() + 0.5D, relayPos.getY() + 0.55D, relayPos.getZ() + 0.5D,
                7, 0.20D, 0.20D, 0.20D, 0.015D);
        level.sendParticles(new DustParticleOptions(new Vector3f(0.75F, 0.45F, 1.0F), 0.8F),
                nodePos.getX() + 0.5D, nodePos.getY() + 0.5D, nodePos.getZ() + 0.5D,
                4, 0.28D, 0.28D, 0.28D, 0.01D);
        level.sendParticles(new DustParticleOptions(new Vector3f(0.9F, 0.85F, 0.35F), 0.7F),
                playerPos.getX() + 0.5D, playerPos.getY() + 1.2D, playerPos.getZ() + 0.5D,
                4, 0.28D, 0.45D, 0.28D, 0.01D);
    }
}
