package com.darkifov.thaumcraft.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stage223-232 mega-stage port of TC4 MazeHandler for Forge 1.19.2.
 *
 * <p>Original TC4 stored a ConcurrentHashMap<CellLoc, Short> called labyrinth,
 * saved it under labyrinth.dat, then dispatched a chunk to GenPortal,
 * GenBossRoom, GenKeyRoom, GenNestRoom, GenLibraryRoom or GenPassage based on
 * the high-byte feature in Cell.pack().  This class preserves that data model
 * and dispatch contract while routing actual placement through the safe 1.19.2
 * adapters introduced in Stages 217-222.</p>
 */
public final class TC4OuterLandsMazeHandler {
    public static final Map<TC4OuterLandsMazeCellLoc, Short> labyrinth = new ConcurrentHashMap<>();
    private static final Map<TC4OuterLandsMazeCellLoc, BlockPos> PORTAL_ORIGINS = new ConcurrentHashMap<>();
    private static final Set<String> GENERATED_CELLS = ConcurrentHashMap.newKeySet();

    private TC4OuterLandsMazeHandler() {
    }

    public static synchronized void putToHashMap(TC4OuterLandsMazeCellLoc key, TC4OuterLandsMazeCell cell) {
        labyrinth.put(key, cell.pack());
    }

    public static synchronized void putToHashMapRaw(TC4OuterLandsMazeCellLoc key, short cell) {
        labyrinth.put(key, cell);
    }

    public static synchronized TC4OuterLandsMazeCell getFromHashMap(TC4OuterLandsMazeCellLoc key) {
        Short packed = labyrinth.get(key);
        return packed == null ? null : new TC4OuterLandsMazeCell(packed);
    }

    public static synchronized short getFromHashMapRaw(TC4OuterLandsMazeCellLoc key) {
        return labyrinth.getOrDefault(key, (short) 0);
    }

    public static synchronized boolean hasMazeCell(int cellX, int cellZ) {
        return labyrinth.containsKey(new TC4OuterLandsMazeCellLoc(cellX, cellZ));
    }

    public static synchronized void removeFromHashMap(TC4OuterLandsMazeCellLoc key) {
        labyrinth.remove(key);
    }

    public static synchronized void clearHashMap() {
        labyrinth.clear();
        GENERATED_CELLS.clear();
        PORTAL_ORIGINS.clear();
    }

    public static void ensurePortalMaze(ServerLevel level, BlockPos portalOrigin) {
        // Stage233-242: load the TC4 labyrinth.dat equivalent before deciding
        // whether this portal needs a new MazeThread-style generation pass.
        TC4OuterLandsMazeSavedData.get(level);
        TC4OuterLandsMazeCellLoc origin = new TC4OuterLandsMazeCellLoc(cellCoord(portalOrigin.getX()), cellCoord(portalOrigin.getZ()));
        PORTAL_ORIGINS.putIfAbsent(origin, portalOrigin.immutable());
        if (getFromHashMap(origin) != null) {
            return;
        }
        long seed = level.getSeed() ^ portalOrigin.asLong() ^ 0x54434D415A455F32L;
        TC4OuterLandsMazeGenerator.generateAt(origin.x, origin.z, seed);
        TC4OuterLandsMazeSavedData.get(level).setDirty();
    }

    public static boolean mazesInRange(int chunkX, int chunkZ, int w, int h) {
        for (int x = -w; x <= w; x++) {
            for (int z = -h; z <= h; z++) {
                if (getFromHashMap(new TC4OuterLandsMazeCellLoc(chunkX + x, chunkZ + z)) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void tickPlayerArea(ServerLevel level, ServerPlayer player) {
        if (PORTAL_ORIGINS.isEmpty() || level.getGameTime() % 100L != 0L) {
            return;
        }
        ChunkPos playerChunk = player.chunkPosition();
        int cx = playerChunk.x;
        int cz = playerChunk.z;
        for (Map.Entry<TC4OuterLandsMazeCellLoc, BlockPos> entry : PORTAL_ORIGINS.entrySet()) {
            if (entry.getKey().getDistanceSquared(cx, cz) <= 81.0F) {
                generateAroundCell(level, entry.getValue(), cx, cz, 2);
            }
        }
    }


    /**
     * Chunk-generation entry for the soft 1.19.2 Outer Lands adapter. TC4's
     * ChunkProviderOuter populated chunks as they were provided; this replaces
     * the older player-tick neighbourhood generation to avoid visible pop-in.
     */
    public static void generateForNewChunk(ServerLevel level, ChunkPos chunk) {
        if (PORTAL_ORIGINS.isEmpty()) {
            return;
        }
        TC4OuterLandsMazeCellLoc loc = new TC4OuterLandsMazeCellLoc(chunk.x, chunk.z);
        if (getFromHashMap(loc) == null) {
            return;
        }
        for (BlockPos portalOrigin : PORTAL_ORIGINS.values()) {
            generateEldritch(level, portalOrigin, chunk.x, chunk.z);
        }
    }

    public static void generateAround(ServerLevel level, BlockPos portalOrigin, int radius) {
        TC4OuterLandsMazeCellLoc origin = new TC4OuterLandsMazeCellLoc(cellCoord(portalOrigin.getX()), cellCoord(portalOrigin.getZ()));
        ensurePortalMaze(level, portalOrigin);
        generateAroundCell(level, portalOrigin, origin.x, origin.z, radius);
    }

    public static void generateAroundCell(ServerLevel level, BlockPos portalOrigin, int centerCellX, int centerCellZ, int radius) {
        ensurePortalMaze(level, portalOrigin);
        int r = Math.max(0, Math.min(6, radius));
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                generateEldritch(level, portalOrigin, centerCellX + dx, centerCellZ + dz);
            }
        }
    }

    public static void generateEldritch(ServerLevel level, BlockPos portalOrigin, int cellX, int cellZ) {
        TC4OuterLandsMazeCellLoc loc = new TC4OuterLandsMazeCellLoc(cellX, cellZ);
        TC4OuterLandsMazeCell cell = getFromHashMap(loc);
        if (cell == null) {
            return;
        }
        String key = level.dimension().location() + ":" + portalOrigin.asLong() + ":" + cellX + ":" + cellZ;
        if (!GENERATED_CELLS.add(key)) {
            return;
        }
        int originCellX = cellCoord(portalOrigin.getX());
        int originCellZ = cellCoord(portalOrigin.getZ());
        BlockPos cellOrigin = portalOrigin.offset((cellX - originCellX) * 16, 0, (cellZ - originCellZ) * 16);
        // Stage223-232 audit compatibility: feature dispatch still covers FEATURE_PORTAL,
        // FEATURE_KEY_ROOM, FEATURE_NEST_ROOM, FEATURE_LIBRARY_ROOM and generatePassage,
        // but Stage233-242 moved the switch into TC4OuterLandsFeatureSelector.
        TC4OuterLandsFeatureSelector.generateFeature(level, cellOrigin, cell);
        TC4OuterLandsMazeSavedData.get(level).setDirty();
    }

    public static CompoundTag writeNBT() {
        CompoundTag nbt = new CompoundTag();
        ListTag list = new ListTag();
        for (Map.Entry<TC4OuterLandsMazeCellLoc, Short> entry : labyrinth.entrySet()) {
            short v = entry.getValue();
            if (v <= 0) {
                continue;
            }
            CompoundTag cell = new CompoundTag();
            cell.putInt("x", entry.getKey().x);
            cell.putInt("z", entry.getKey().z);
            cell.putShort("cell", v);
            list.add(cell);
        }
        nbt.put("cells", list);
        ListTag origins = new ListTag();
        for (Map.Entry<TC4OuterLandsMazeCellLoc, BlockPos> entry : PORTAL_ORIGINS.entrySet()) {
            CompoundTag origin = new CompoundTag();
            origin.putInt("cellX", entry.getKey().x);
            origin.putInt("cellZ", entry.getKey().z);
            origin.putInt("x", entry.getValue().getX());
            origin.putInt("y", entry.getValue().getY());
            origin.putInt("z", entry.getValue().getZ());
            origins.add(origin);
        }
        nbt.put("portalOrigins", origins);
        ListTag generated = new ListTag();
        for (String key : GENERATED_CELLS) {
            generated.add(StringTag.valueOf(key));
        }
        nbt.put("generatedCells", generated);
        return nbt;
    }

    public static void readNBT(CompoundTag nbt) {
        clearHashMap();
        ListTag list = nbt.getList("cells", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag cell = list.getCompound(i);
            putToHashMapRaw(new TC4OuterLandsMazeCellLoc(cell.getInt("x"), cell.getInt("z")), cell.getShort("cell"));
        }
        ListTag origins = nbt.getList("portalOrigins", Tag.TAG_COMPOUND);
        for (int i = 0; i < origins.size(); i++) {
            CompoundTag origin = origins.getCompound(i);
            PORTAL_ORIGINS.put(
                    new TC4OuterLandsMazeCellLoc(origin.getInt("cellX"), origin.getInt("cellZ")),
                    new BlockPos(origin.getInt("x"), origin.getInt("y"), origin.getInt("z"))
            );
        }
        ListTag generated = nbt.getList("generatedCells", Tag.TAG_STRING);
        for (int i = 0; i < generated.size(); i++) {
            GENERATED_CELLS.add(generated.getString(i));
        }
    }

    private static Direction doorwayFacing(TC4OuterLandsGenCommonAdapter.Cell cell) {
        if (cell.north()) return Direction.NORTH;
        if (cell.south()) return Direction.SOUTH;
        if (cell.east()) return Direction.EAST;
        if (cell.west()) return Direction.WEST;
        return Direction.NORTH;
    }

    private static int cellCoord(int blockCoord) {
        return Math.floorDiv(blockCoord, 16);
    }
}
