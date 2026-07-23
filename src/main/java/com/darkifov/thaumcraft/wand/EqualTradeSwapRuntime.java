package com.darkifov.thaumcraft.wand;

import com.darkifov.thaumcraft.entity.FollowingItemEntity;

import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.ward.WardedBlockRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * ServerTickEventsFML.VirtualSwapper parity adapter.
 * Each level performs at most one successful swap per tick, preserving TC4's
 * gradual flood-fill and avoiding synchronous area replacement spikes.
 */
public final class EqualTradeSwapRuntime {
    private static final Map<ServerLevel, ArrayDeque<SwapTask>> QUEUES = new java.util.WeakHashMap<>();
    private static final Map<ServerLevel, Set<BlockPos>> QUEUED_POSITIONS = new java.util.WeakHashMap<>();

    private EqualTradeSwapRuntime() {
    }

    public static void enqueue(ServerLevel level, ServerPlayer player, int wandSlot,
                               BlockPos pos, BlockState sourceState, BlockState targetState,
                               ItemStack targetItem, int lifespan) {
        if (sourceState.equals(targetState) || targetItem.isEmpty()) return;
        Set<BlockPos> positions = QUEUED_POSITIONS.computeIfAbsent(level, ignored -> new HashSet<>());
        if (!positions.add(pos.immutable())) return;
        QUEUES.computeIfAbsent(level, ignored -> new ArrayDeque<>()).addLast(new SwapTask(
                player.getUUID(), wandSlot, pos.immutable(), sourceState, targetState,
                one(targetItem), Math.max(0, lifespan)));
    }

    public static void tick(ServerLevel level) {
        ArrayDeque<SwapTask> queue = QUEUES.get(level);
        if (queue == null || queue.isEmpty()) return;
        Set<BlockPos> positions = QUEUED_POSITIONS.computeIfAbsent(level, ignored -> new HashSet<>());

        // Original swaps one valid block per tick. Invalid/stale tasks are discarded
        // in the same tick until either a swap succeeds or the queue is exhausted.
        while (!queue.isEmpty()) {
            SwapTask task = queue.removeFirst();
            positions.remove(task.pos());
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(task.playerId());
            if (player == null || player.level != level) continue;
            ItemStack wand = resolveWand(player, task.wandSlot());
            if (!(wand.getItem() instanceof WandItem) || WandFocusRuntime.getFocus(wand) != WandFocusType.EQUAL_TRADE) continue;
            if (trySwap(level, player, wand, task, queue, positions)) break;
        }

        if (queue.isEmpty()) {
            QUEUES.remove(level);
            QUEUED_POSITIONS.remove(level);
        }
    }

    private static boolean trySwap(ServerLevel level, ServerPlayer player, ItemStack wand, SwapTask task,
                                   ArrayDeque<SwapTask> queue, Set<BlockPos> positions) {
        BlockState current = level.getBlockState(task.pos());
        if (!current.equals(task.sourceState()) || current.isAir() || current.hasBlockEntity()
                || current.getDestroySpeed(level, task.pos()) < 0.0F
                || WardedBlockRuntime.isWarded(level, task.pos())
                || !level.mayInteract(player, task.pos())
                || !player.mayUseItemAt(task.pos(), Direction.UP, wand)) {
            return false;
        }
        AspectList cost = WandFocusRuntime.focusVisCost(wand, WandFocusType.EQUAL_TRADE, level.random);
        if (!WandFocusRuntime.hasFocusVis(wand, player, WandFocusType.EQUAL_TRADE, cost)) return false;
        int itemSlot = player.getAbilities().instabuild ? Integer.MIN_VALUE : findItem(player, task.targetItem());
        if (!player.getAbilities().instabuild && itemSlot == Integer.MIN_VALUE) return false;

        BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(level, task.pos(), current, player);
        MinecraftForge.EVENT_BUS.post(breakEvent);
        if (breakEvent.isCanceled()) return false;

        boolean creative = player.getAbilities().instabuild;
        List<ItemStack> drops = creative
                ? List.of()
                : Block.getDrops(current, level, task.pos(), null, player, syntheticHarvestTool(wand));

        if (!creative) {
            player.getInventory().getItem(itemSlot).shrink(1);
        }
        if (!level.setBlock(task.pos(), task.targetState(), Block.UPDATE_ALL)) {
            if (!creative) refundTargetItem(player, itemSlot, task.targetItem());
            return false;
        }
        task.targetState().getBlock().setPlacedBy(level, task.pos(), task.targetState(), player, task.targetItem());
        if (!WandFocusRuntime.consumeFocusVis(wand, player, WandFocusType.EQUAL_TRADE, cost)) {
            level.setBlock(task.pos(), current, Block.UPDATE_ALL);
            if (!creative) refundTargetItem(player, itemSlot, task.targetItem());
            return false;
        }

        for (ItemStack drop : drops) {
            if (drop.isEmpty()) continue;
            ItemStack remainder = drop.copy();
            if (player.getInventory().add(remainder)) continue;
            FollowingItemEntity entity = new FollowingItemEntity(level,
                    task.pos().getX() + 0.5D, task.pos().getY() + 0.5D, task.pos().getZ() + 0.5D,
                    remainder, player, 5);
            entity.setDefaultPickUpDelay();
            level.addFreshEntity(entity);
        }
        level.levelEvent(null, 2001, task.pos(), Block.getId(current));
        WandFocusRuntime.sparkleBlock(level, task.pos(), 12632319); // TC4 PacketFXBlockSparkle

        if (task.lifespan() > 0) {
            enqueueOriginalNeighbours(level, task, queue, positions);
        }
        return true;
    }

    private static void enqueueOriginalNeighbours(ServerLevel level, SwapTask task,
                                                  ArrayDeque<SwapTask> queue, Set<BlockPos> positions) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockPos next = task.pos().offset(dx, dy, dz).immutable();
                    BlockState neighbour = level.getBlockState(next);
                    // ServerTickEventsFML checks source block+metadata and exposure
                    // at queue-expansion time, after the current replacement exists.
                    if (!neighbour.equals(task.sourceState()) || !isExposed(level, next)) continue;
                    if (!positions.add(next)) continue;
                    queue.addLast(new SwapTask(task.playerId(), task.wandSlot(), next, task.sourceState(),
                            task.targetState(), task.targetItem().copy(), task.lifespan() - 1));
                }
            }
        }
    }

    private static void refundTargetItem(ServerPlayer player, int slot, ItemStack targetItem) {
        ItemStack current = player.getInventory().getItem(slot);
        if (current.isEmpty()) {
            player.getInventory().setItem(slot, one(targetItem));
            return;
        }
        if (ItemStack.isSameItemSameTags(current, targetItem) && current.getCount() < current.getMaxStackSize()) {
            current.grow(1);
            return;
        }
        ItemStack refund = one(targetItem);
        if (!player.getInventory().add(refund) && !refund.isEmpty()) {
            player.drop(refund, false);
        }
    }

    private static ItemStack one(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy;
    }

    private static ItemStack syntheticHarvestTool(ItemStack wand) {
        ItemStack tool = new ItemStack(Items.NETHERITE_PICKAXE);
        int treasure = WandFocusRuntime.focusUpgradeLevel(wand, FocusUpgradeType.TREASURE);
        if (WandFocusRuntime.focusHasUpgrade(wand, FocusUpgradeType.SILK_TOUCH)) {
            EnchantmentHelper.setEnchantments(Map.of(Enchantments.SILK_TOUCH, 1), tool);
        } else if (treasure > 0) {
            EnchantmentHelper.setEnchantments(Map.of(Enchantments.BLOCK_FORTUNE, treasure), tool);
        }
        return tool;
    }

    private static int findItem(ServerPlayer player, ItemStack wanted) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack present = player.getInventory().getItem(slot);
            if (!present.isEmpty() && ItemStack.isSameItemSameTags(present, wanted)) return slot;
        }
        return Integer.MIN_VALUE;
    }

    private static ItemStack resolveWand(ServerPlayer player, int slot) {
        return slot == -1 ? player.getOffhandItem() : player.getInventory().getItem(slot);
    }

    private static boolean isExposed(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos adjacent = pos.relative(direction);
            if (!level.getBlockState(adjacent).isSolidRender(level, adjacent)) return true;
        }
        return false;
    }

    private record SwapTask(UUID playerId, int wandSlot, BlockPos pos, BlockState sourceState,
                            BlockState targetState, ItemStack targetItem, int lifespan) {
    }
}
