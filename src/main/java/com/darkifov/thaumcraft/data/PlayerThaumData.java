package com.darkifov.thaumcraft.data;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public final class PlayerThaumData {
    private static final String ROOT = "ThaumcraftLegacy";
    /** Legacy Stage1-143 permanent warp field. Kept for save compatibility. */
    private static final String WARP = "Warp";
    private static final String WARP_STICKY = "WarpSticky";
    private static final String WARP_TEMPORARY = "WarpTemporary";
    private static final String WARP_COUNTER = "WarpCounter";
    private static final String RESEARCH = "Research";
    private static final String WARP_WARD_TICKS = "WarpWardTicks";
    private static final String WARP_EVENT_COOLDOWN = "WarpEventCooldown";
    private static final String ELDRITCH_ATTUNEMENT = "EldritchAttunement";
    private static final String PECH_FAVOR = "PechFavor";
    private static final String SCANNED_OBJECTS = "ScannedObjects";
    private static final String SCANNED_ENTITIES = "ScannedEntities";
    private static final String SCANNED_PHENOMENA = "ScannedPhenomena";
    private static final String SCANNED_ASPECTS = "ScannedAspects";
    private static final String OUTER_RETURN_POS = "OuterReturnPos";

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

    public static void setOuterLandsReturnPos(Player player, long position) {
        root(player).putLong(OUTER_RETURN_POS, position);
    }

    public static boolean hasOuterLandsReturnPos(Player player) {
        return root(player).contains(OUTER_RETURN_POS);
    }

    public static long getOuterLandsReturnPos(Player player) {
        return root(player).getLong(OUTER_RETURN_POS);
    }

    /**
     * TC4 total warp: permanent + sticky + temporary.  Old saves stored only
     * the permanent value in Warp, so that field remains the permanent bucket.
     */
    public static int getWarp(Player player) {
        return getWarpTotal(player);
    }

    public static int getWarpTotal(Player player) {
        return Math.max(0, getWarpPerm(player) + getWarpSticky(player) + getWarpTemporary(player));
    }

    /** TC4 actual warp excludes temporary warp. */
    public static int getActualWarp(Player player) {
        return Math.max(0, getWarpPerm(player) + getWarpSticky(player));
    }

    public static int getWarpPerm(Player player) {
        return root(player).getInt(WARP);
    }

    public static int getWarpSticky(Player player) {
        return root(player).getInt(WARP_STICKY);
    }

    public static int getWarpTemporary(Player player) {
        return root(player).getInt(WARP_TEMPORARY);
    }

    /** Legacy call site: treat generic warp gain as permanent, matching older stages. */
    public static void addWarp(Player player, int amount) {
        addWarpPermanent(player, amount);
    }

    public static void addWarpPermanent(Player player, int amount) {
        if (amount == 0) {
            return;
        }

        CompoundTag root = root(player);
        root.putInt(WARP, Math.max(0, root.getInt(WARP) + amount));
        resetWarpCounterToTotal(root);
    }

    public static void addWarpSticky(Player player, int amount) {
        if (amount == 0) {
            return;
        }

        CompoundTag root = root(player);
        root.putInt(WARP_STICKY, Math.max(0, root.getInt(WARP_STICKY) + amount));
        resetWarpCounterToTotal(root);
    }

    public static void addWarpTemporary(Player player, int amount) {
        if (amount == 0) {
            return;
        }

        CompoundTag root = root(player);
        root.putInt(WARP_TEMPORARY, Math.max(0, root.getInt(WARP_TEMPORARY) + amount));
        resetWarpCounterToTotal(root);
    }

    public static void removeWarp(Player player, int amount) {
        if (amount <= 0) {
            return;
        }

        int left = amount;
        CompoundTag root = root(player);
        int temp = root.getInt(WARP_TEMPORARY);
        int removeTemp = Math.min(temp, left);
        root.putInt(WARP_TEMPORARY, temp - removeTemp);
        left -= removeTemp;

        int sticky = root.getInt(WARP_STICKY);
        int removeSticky = Math.min(sticky, left);
        root.putInt(WARP_STICKY, sticky - removeSticky);
        left -= removeSticky;

        if (left > 0) {
            root.putInt(WARP, Math.max(0, root.getInt(WARP) - left));
        }
    }

    public static void decayTemporaryWarp(Player player, int amount) {
        if (amount <= 0) {
            return;
        }

        CompoundTag root = root(player);
        root.putInt(WARP_TEMPORARY, Math.max(0, root.getInt(WARP_TEMPORARY) - amount));
    }

    /**
     * TC4 WarpEvents directly decremented sticky warp for the rare cleansing
     * event without resetting the event counter. Public warp-grant/removal
     * APIs do reset the counter, so this path must remain separate.
     */
    public static void decayStickyWarpFromEvent(Player player, int amount) {
        if (amount <= 0) {
            return;
        }

        CompoundTag root = root(player);
        root.putInt(WARP_STICKY, Math.max(0, root.getInt(WARP_STICKY) - amount));
    }

    private static void resetWarpCounterToTotal(CompoundTag root) {
        int total = Math.max(0, root.getInt(WARP) + root.getInt(WARP_STICKY) + root.getInt(WARP_TEMPORARY));
        root.putInt(WARP_COUNTER, total);
    }

    public static int getWarpCounter(Player player) {
        return root(player).getInt(WARP_COUNTER);
    }

    public static void setWarpCounter(Player player, int counter) {
        root(player).putInt(WARP_COUNTER, Math.max(0, counter));
    }

    public static void addWarpCounter(Player player, int amount) {
        if (amount == 0) {
            return;
        }

        CompoundTag root = root(player);
        root.putInt(WARP_COUNTER, Math.max(0, root.getInt(WARP_COUNTER) + amount));
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

    /**
     * Consumes the pre-11.62.82 persistent ward timer so it can be migrated
     * once into the real synced MobEffect. Keeping both timers authoritative
     * made milk unable to remove Warp Ward, unlike TC4.
     */
    public static int takeLegacyWarpWardTicks(Player player) {
        CompoundTag root = root(player);
        if (!root.contains(WARP_WARD_TICKS)) {
            return 0;
        }
        int ticks = Math.max(0, root.getInt(WARP_WARD_TICKS));
        root.remove(WARP_WARD_TICKS);
        return ticks;
    }

    /** Legacy-save probe only. Runtime ward authority is the MobEffect. */
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

    public static boolean markScannedObject(Player player, String objectId) {
        return addString(root(player), SCANNED_OBJECTS, normalize(objectId));
    }

    public static boolean markScannedEntity(Player player, String entityId) {
        return addString(root(player), SCANNED_ENTITIES, normalize(entityId));
    }

    public static boolean markScannedPhenomenon(Player player, String phenomenonId) {
        return addString(root(player), SCANNED_PHENOMENA, normalize(phenomenonId));
    }

    public static int importScannedObjects(Player player, Iterable<String> values) {
        return importStrings(root(player), SCANNED_OBJECTS, values);
    }

    public static int importScannedEntities(Player player, Iterable<String> values) {
        return importStrings(root(player), SCANNED_ENTITIES, values);
    }

    public static int recordScannedAspects(Player player, AspectList aspects) {
        if (aspects == null || aspects.isEmpty()) {
            return 0;
        }

        int added = 0;
        CompoundTag root = root(player);

        for (Aspect aspect : aspects.entries().keySet()) {
            if (aspect != null && addString(root, SCANNED_ASPECTS, aspect.id())) {
                added++;
            }
        }

        return added;
    }

    public static boolean hasScannedObject(Player player, String objectId) {
        return containsString(root(player), SCANNED_OBJECTS, normalize(objectId));
    }

    public static boolean hasScannedEntity(Player player, String entityId) {
        return containsString(root(player), SCANNED_ENTITIES, normalize(entityId));
    }

    public static boolean hasScannedPhenomenon(Player player, String phenomenonId) {
        return containsString(root(player), SCANNED_PHENOMENA, normalize(phenomenonId));
    }

    public static Set<String> getScannedObjects(Player player) {
        return stringSet(root(player), SCANNED_OBJECTS);
    }

    public static Set<String> getScannedEntities(Player player) {
        return stringSet(root(player), SCANNED_ENTITIES);
    }

    public static Set<String> getScannedPhenomena(Player player) {
        return stringSet(root(player), SCANNED_PHENOMENA);
    }

    public static Set<String> getScannedAspectIds(Player player) {
        return stringSet(root(player), SCANNED_ASPECTS);
    }

    public static int getScanKnowledgeCount(Player player) {
        return getScannedObjects(player).size() + getScannedEntities(player).size() + getScannedPhenomena(player).size();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean addString(CompoundTag root, String key, String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        ListTag list = root.getList(key, 8);

        for (int i = 0; i < list.size(); i++) {
            if (list.getString(i).equals(value)) {
                root.put(key, list);
                return false;
            }
        }

        list.add(StringTag.valueOf(value));
        root.put(key, list);
        return true;
    }

    private static int importStrings(CompoundTag root, String key, Iterable<String> values) {
        if (values == null) return 0;
        int added = 0;
        for (String value : values) if (addString(root, key, normalize(value))) added++;
        return added;
    }

    private static boolean containsString(CompoundTag root, String key, String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        ListTag list = root.getList(key, 8);

        for (int i = 0; i < list.size(); i++) {
            if (list.getString(i).equals(value)) {
                return true;
            }
        }

        return false;
    }

    private static Set<String> stringSet(CompoundTag root, String key) {
        Set<String> result = new LinkedHashSet<>();
        ListTag list = root.getList(key, 8);

        for (int i = 0; i < list.size(); i++) {
            result.add(list.getString(i));
        }

        return result;
    }

}
