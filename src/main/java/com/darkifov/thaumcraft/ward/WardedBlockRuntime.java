package com.darkifov.thaumcraft.ward;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stage130: strict-port stepping stone for Focus: Warding.
 *
 * TC4 has real warded blocks/tiles and ownership checks. The 1.19.2 port now
 * enforces ownership at runtime for break attempts so the focus is no longer a
 * tooltip-only placeholder. Persistence will be expanded when the full warded
 * block-state wrapper is ported.
 */
public final class WardedBlockRuntime {
    private static final Map<String, UUID> WARDS = new ConcurrentHashMap<>();

    private WardedBlockRuntime() {
    }

    public static boolean ward(Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return false;
        }
        WARDS.put(key(level, pos), player.getUUID());
        return true;
    }

    public static boolean unward(Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return false;
        }
        String key = key(level, pos);
        UUID owner = WARDS.get(key);
        if (owner == null || owner.equals(player.getUUID()) || player.getAbilities().instabuild) {
            WARDS.remove(key);
            return true;
        }
        return false;
    }

    public static boolean isWarded(Level level, BlockPos pos) {
        return WARDS.containsKey(key(level, pos));
    }

    public static boolean mayEdit(Level level, BlockPos pos, Player player) {
        UUID owner = WARDS.get(key(level, pos));
        return owner == null || owner.equals(player.getUUID()) || player.getAbilities().instabuild;
    }

    public static boolean cancelIfProtected(Level level, BlockPos pos, Player player) {
        if (mayEdit(level, pos, player)) {
            return false;
        }
        player.displayClientMessage(Component.literal("This block is warded by another thaumaturge.").withStyle(ChatFormatting.BLUE), true);
        return true;
    }

    private static String key(Level level, BlockPos pos) {
        ResourceLocation dimension = level.dimension().location();
        return dimension + ":" + pos.asLong();
    }
}
