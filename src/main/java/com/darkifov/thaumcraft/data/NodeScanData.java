package com.darkifov.thaumcraft.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedHashSet;
import java.util.Set;

public final class NodeScanData {
    private static final String ROOT = "ThaumcraftLegacy";
    private static final String SCANNED_NODES = "ScannedAuraNodes";

    private NodeScanData() {}

    private static CompoundTag root(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT)) persistent.put(ROOT, new CompoundTag());
        return persistent.getCompound(ROOT);
    }

    /** Legacy coordinate key from rebuild versions before v11.64.25. */
    public static String key(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    public static boolean hasScanned(Player player, String nodeKey) {
        return contains(root(player).getList(SCANNED_NODES, 8), nodeKey);
    }

    public static boolean markScanned(Player player, String nodeKey) {
        if (nodeKey == null || nodeKey.isBlank() || hasScanned(player, nodeKey)) return false;
        ListTag list = root(player).getList(SCANNED_NODES, 8);
        list.add(StringTag.valueOf(nodeKey));
        root(player).put(SCANNED_NODES, list);
        return true;
    }

    public static boolean migrateLegacyPosition(Player player, BlockPos pos, String nodeKey) {
        if (player == null || pos == null || nodeKey == null || nodeKey.isBlank()) return false;
        ListTag list = root(player).getList(SCANNED_NODES, 8);
        String old = key(pos);
        boolean foundLegacy = false;
        for (int i = list.size() - 1; i >= 0; i--) {
            if (old.equals(list.getString(i))) { list.remove(i); foundLegacy = true; }
        }
        if (!foundLegacy) return false;
        if (!contains(list, nodeKey)) list.add(StringTag.valueOf(nodeKey));
        root(player).put(SCANNED_NODES, list);
        return true;
    }

    public static int importLegacyKeys(Player player, Iterable<String> keys) {
        if (keys == null) return 0;
        int added = 0;
        for (String value : keys) if (markScanned(player, value)) added++;
        return added;
    }

    /** Compatibility probes for old code; new authority is the stable node ID. */
    public static boolean hasScanned(Player player, BlockPos pos) {
        return pos != null && hasScanned(player, key(pos));
    }

    public static boolean markScanned(Player player, BlockPos pos) {
        return pos != null && markScanned(player, key(pos));
    }

    public static Set<String> getScannedNodeKeys(Player player) {
        Set<String> result = new LinkedHashSet<>();
        ListTag list = root(player).getList(SCANNED_NODES, 8);
        for (int i = 0; i < list.size(); i++) result.add(list.getString(i));
        return result;
    }

    private static boolean contains(ListTag list, String key) {
        if (key == null || key.isBlank()) return false;
        for (int i = 0; i < list.size(); i++) if (key.equals(list.getString(i))) return true;
        return false;
    }
}
