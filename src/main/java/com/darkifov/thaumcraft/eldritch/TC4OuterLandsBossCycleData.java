package com.darkifov.thaumcraft.eldritch;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Stage273-282: SavedData equivalent for TC4 MapBossData.bossCount. */
public final class TC4OuterLandsBossCycleData extends SavedData {
    public static final String DATA_NAME = "thaumcraft_boss_cycle";
    private int bossCount;

    public static TC4OuterLandsBossCycleData load(CompoundTag tag) {
        TC4OuterLandsBossCycleData data = new TC4OuterLandsBossCycleData();
        data.bossCount = tag.getInt("bossCount");
        return data;
    }

    public static TC4OuterLandsBossCycleData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TC4OuterLandsBossCycleData::load, TC4OuterLandsBossCycleData::new, DATA_NAME);
    }

    public int advance(ServerLevel level) {
        bossCount += 1;
        if (level.random.nextFloat() < 0.25F) {
            bossCount += 1;
        }
        setDirty();
        return bossCount;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("bossCount", bossCount);
        return tag;
    }
}
