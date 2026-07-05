package com.darkifov.thaumcraft.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;

public final class PlayerThaumData {
    private static final String ROOT = "ThaumcraftLegacy";
    private static final String WARP = "Warp";
    private static final String RESEARCH = "Research";
    private static final String WARP_WARD_TICKS = "WarpWardTicks";
    private static final String WARP_EVENT_COOLDOWN = "WarpEventCooldown";
    private static final String ELDRITCH_ATTUNEMENT = "EldritchAttunement";
    private static final String PECH_FAVOR = "PechFavor";

    private PlayerThaumData() {
    }

    private static CompoundTag root(Player player) {
        CompoundTag persistent = player.getPersistentData();

        if (!persistent.contains(ROOT)) {
            persistent.put(ROOT, new CompoundTag());
        }

        return persistent.getCompound(ROOT);
    }

    public static void copyFrom(Player from, Player to) {
        CompoundTag fromRoot = root(from);
        to.getPersistentData().put(ROOT, fromRoot.copy());
    }

    public static int getWarp(Player player) {
        return root(player).getInt(WARP);
    }

    public static void addWarp(Player player, int amount) {
        if (amount <= 0) {
            return;
        }

        CompoundTag root = root(player);
        root.putInt(WARP, Math.max(0, root.getInt(WARP) + amount));
    }

    public static void removeWarp(Player player, int amount) {
        if (amount <= 0) {
            return;
        }

        CompoundTag root = root(player);
        root.putInt(WARP, Math.max(0, root.getInt(WARP) - amount));
    }

    public static int getWarpWardTicks(Player player) {
        return root(player).getInt(WARP_WARD_TICKS);
    }

    public static void addWarpWard(Player player, int ticks) {
        if (ticks <= 0) {
            return;
        }

        CompoundTag root = root(player);
        root.putInt(WARP_WARD_TICKS, Math.min(20 * 60 * 20, root.getInt(WARP_WARD_TICKS) + ticks));
    }

    public static void setWarpWardTicks(Player player, int ticks) {
        root(player).putInt(WARP_WARD_TICKS, Math.max(0, ticks));
    }

    public static boolean hasWarpWard(Player player) {
        return getWarpWardTicks(player) > 0;
    }

    public static int getWarpEventCooldown(Player player) {
        return root(player).getInt(WARP_EVENT_COOLDOWN);
    }

    public static void setWarpEventCooldown(Player player, int ticks) {
        root(player).putInt(WARP_EVENT_COOLDOWN, Math.max(0, ticks));
    }

    public static int getEldritchAttunement(Player player) {
        return root(player).getInt(ELDRITCH_ATTUNEMENT);
    }

    public static void addEldritchAttunement(Player player, int amount) {
        if (amount <= 0) {
            return;
        }

        CompoundTag root = root(player);
        root.putInt(ELDRITCH_ATTUNEMENT, Math.min(100, root.getInt(ELDRITCH_ATTUNEMENT) + amount));
    }

    public static int getPechFavor(Player player) {
        return root(player).getInt(PECH_FAVOR);
    }

    public static void addPechFavor(Player player, int amount) {
        CompoundTag root = root(player);
        root.putInt(PECH_FAVOR, Math.max(0, Math.min(100, root.getInt(PECH_FAVOR) + amount)));
    }

    public static boolean hasResearch(Player player, String key) {
        if (key == null || key.isEmpty()) {
            return true;
        }

        if ("FIRST_STEPS".equals(key)) {
            return true;
        }

        ListTag list = root(player).getList(RESEARCH, 8);

        for (int i = 0; i < list.size(); i++) {
            if (list.getString(i).equals(key)) {
                return true;
            }
        }

        return false;
    }

    public static boolean unlockResearch(Player player, String key) {
        if (key == null || key.isEmpty() || hasResearch(player, key)) {
            return false;
        }

        ListTag list = root(player).getList(RESEARCH, 8);
        list.add(StringTag.valueOf(key));
        root(player).put(RESEARCH, list);
        return true;
    }

    public static Set<String> getResearchSet(Player player) {
        Set<String> result = new HashSet<>();
        result.add("FIRST_STEPS");

        ListTag list = root(player).getList(RESEARCH, 8);

        for (int i = 0; i < list.size(); i++) {
            result.add(list.getString(i));
        }

        return result;
    }

    public static int researchCount(Player player) {
        return getResearchSet(player).size();
    }
}
