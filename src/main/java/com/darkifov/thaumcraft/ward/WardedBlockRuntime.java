package com.darkifov.thaumcraft.ward;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.WardedBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Persistent TC4 warding wrapper/ownership runtime. */
public final class WardedBlockRuntime {
    private static final ThreadLocal<Boolean> INTERNAL_WARD_MUTATION = ThreadLocal.withInitial(() -> false);

    private WardedBlockRuntime() {
    }

    public static boolean isInternalWardMutation() {
        return INTERNAL_WARD_MUTATION.get();
    }

    public static boolean canWard(Level level, BlockPos pos, Player player, ItemStack wandStack) {
        BlockState state = level.getBlockState(pos);
        return !level.isClientSide
                && !state.isAir()
                && !state.is(ThaumcraftMod.WARDED_BLOCK.get())
                && !state.hasBlockEntity()
                && state.isSolidRender(level, pos)
                && level.mayInteract(player, pos)
                && player.mayUseItemAt(pos, net.minecraft.core.Direction.UP, wandStack);
    }

    public static boolean ward(Level level, BlockPos pos, Player player, ItemStack wandStack) {
        if (!canWard(level, pos, player, wandStack)) return false;
        BlockState remembered = level.getBlockState(pos);
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, remembered, player);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return false;
        if (!level.setBlock(pos, ThaumcraftMod.WARDED_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL)) return false;
        if (!(level.getBlockEntity(pos) instanceof WardedBlockEntity warded)) {
            level.setBlock(pos, remembered, Block.UPDATE_ALL);
            return false;
        }
        warded.initialize(remembered, player.getUUID());
        return true;
    }

    public static boolean unward(Level level, BlockPos pos, Player player) {
        if (level.isClientSide || !(level.getBlockEntity(pos) instanceof WardedBlockEntity warded)) return false;
        if (!warded.isOwner(player.getUUID()) && !player.getAbilities().instabuild) return false;
        if (!level.mayInteract(player, pos)) return false;

        BlockState wrapperState = level.getBlockState(pos);
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, wrapperState, player);
        INTERNAL_WARD_MUTATION.set(true);
        try {
            MinecraftForge.EVENT_BUS.post(event);
        } finally {
            INTERNAL_WARD_MUTATION.set(false);
        }
        if (event.isCanceled()) return false;

        BlockState restore = warded.rememberedState();
        boolean restored = level.setBlock(pos, restore, Block.UPDATE_ALL);
        if (restored && level instanceof ServerLevel server) {
            server.getChunkSource().getLightEngine().checkBlock(pos);
            server.scheduleTick(pos, restore.getBlock(), 2);
        }
        return restored;
    }

    /** Internal transaction rollback used only when vis consumption fails after placement. */
    public static boolean rollbackWard(Level level, BlockPos pos, Player player) {
        if (level.isClientSide || !(level.getBlockEntity(pos) instanceof WardedBlockEntity warded)
                || !warded.isOwner(player.getUUID())) {
            return false;
        }
        BlockState restore = warded.rememberedState();
        boolean restored = level.setBlock(pos, restore, Block.UPDATE_ALL);
        if (restored && level instanceof ServerLevel server) {
            server.getChunkSource().getLightEngine().checkBlock(pos);
            server.scheduleTick(pos, restore.getBlock(), 2);
        }
        return restored;
    }

    public static boolean isWarded(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(ThaumcraftMod.WARDED_BLOCK.get())
                && level.getBlockEntity(pos) instanceof WardedBlockEntity;
    }

    public static boolean isOwner(Level level, BlockPos pos, Player player) {
        return level.getBlockEntity(pos) instanceof WardedBlockEntity warded
                && (warded.isOwner(player.getUUID()) || player.getAbilities().instabuild);
    }

    /** Wards are immutable even to their owner until the focus removes them. */
    public static boolean mayEdit(Level level, BlockPos pos, Player player) {
        return !isWarded(level, pos);
    }

    public static boolean cancelIfProtected(Level level, BlockPos pos, Player player) {
        if (!isWarded(level, pos)) return false;
        boolean owner = isOwner(level, pos, player);
        player.displayClientMessage(Component.translatable(owner
                        ? "message.thaumcraft.warding.protected_owner"
                        : "message.thaumcraft.warding.protected_other")
                .withStyle(ChatFormatting.BLUE), true);
        return true;
    }
}
