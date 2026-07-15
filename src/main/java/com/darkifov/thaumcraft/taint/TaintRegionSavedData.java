package com.darkifov.thaumcraft.taint;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

/**
 * Persistent 1.19.2 bridge for TC4's spreading Taint biome columns.
 *
 * <p>TC4 changed the biome id of individual x/z columns at runtime. Modern
 * biome containers are quart-resolution palettes and mutating them safely
 * requires client chunk resends. Until that visual biome layer is completed,
 * the port stores the same gameplay boundary as a persistent column set. Taint
 * blocks and spores consult this data, while the objective report continues to
 * mark biome colour/weather parity as runtime-unverified.</p>
 */
public final class TaintRegionSavedData extends SavedData {
    private static final String DATA_NAME = "thaumcraft_taint_columns";
    private static final String COLUMNS_TAG = "Columns";

    private final Set<Long> taintedColumns = new HashSet<>();

    public static TaintRegionSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                TaintRegionSavedData::load,
                TaintRegionSavedData::new,
                DATA_NAME
        );
    }

    private static TaintRegionSavedData load(CompoundTag tag) {
        TaintRegionSavedData data = new TaintRegionSavedData();
        for (long packed : tag.getLongArray(COLUMNS_TAG)) {
            data.taintedColumns.add(packed);
        }
        return data;
    }

    public boolean isTainted(int x, int z) {
        return taintedColumns.contains(pack(x, z));
    }

    public boolean markTainted(int x, int z) {
        boolean changed = taintedColumns.add(pack(x, z));
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public int size() {
        return taintedColumns.size();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        long[] columns = new long[taintedColumns.size()];
        int index = 0;
        for (long packed : taintedColumns) {
            columns[index++] = packed;
        }
        tag.putLongArray(COLUMNS_TAG, columns);
        return tag;
    }

    private static long pack(int x, int z) {
        return ((long) x & 0xFFFFFFFFL) | (((long) z & 0xFFFFFFFFL) << 32);
    }
}
