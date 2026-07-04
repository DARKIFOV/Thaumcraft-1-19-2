package com.darkifov.thaumcraft.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.entity.player.Player;

public final class NodeScanData {
    private static final String ROOT = "ThaumcraftLegacy";
    private static final String SCANNED_NODES = "ScannedAuraNodes";

    private NodeScanData() {
    }

    private static CompoundTag root(Player player) {
        CompoundTag persistent = player.getPersistentData();

        if (!persistent.contains(ROOT)) {
            persistent.put(ROOT, new CompoundTag());
        }

        return persistent.getCompound(ROOT);
    }

    public static String key(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    public static boolean hasScanned(Player player, BlockPos pos) {
        String key = key(pos);
        ListTag list = root(player).getList(SCANNED_NODES, 8);

        for (int i = 0; i < list.size(); i++) {
            if (list.getString(i).equals(key)) {
                return true;
            }
        }

        return false;
    }

    public static boolean markScanned(Player player, BlockPos pos) {
        if (hasScanned(player, pos)) {
            return false;
        }

        ListTag list = root(player).getList(SCANNED_NODES, 8);
        list.add(StringTag.valueOf(key(pos)));
        root(player).put(SCANNED_NODES, list);
        return true;
    }
}
