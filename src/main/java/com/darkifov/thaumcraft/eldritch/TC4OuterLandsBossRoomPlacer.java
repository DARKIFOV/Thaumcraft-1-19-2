package com.darkifov.thaumcraft.eldritch;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.EldritchGolemEntity;
import com.darkifov.thaumcraft.entity.EldritchGuardianEntity;
import com.darkifov.thaumcraft.entity.EldritchWardenEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stage217 1.19.2 structure-placement adapter for TC4 GenBossRoom / GenKeyRoom.
 *
 * <p>This intentionally does not import the old 1.7.10 worldgen classes.  It consumes
 * {@link TC4OuterLandsBossRoomMetadata#PAT_DOORWAY} and writes equivalent runtime
 * blocks, persistent metadata and boss spawns in Forge 1.19.2 terms.  It is a bridge
 * between the current portal arena and the later full Outer Lands dimension feature.</p>
 */
public final class TC4OuterLandsBossRoomPlacer {
    public static final int ROOM_RADIUS = 7;
    public static final int ROOM_HEIGHT = 6;

    private TC4OuterLandsBossRoomPlacer() {
    }

    public static void placePortalRoom(ServerLevel level, BlockPos origin) {
        TC4OuterLandsGenCommonAdapter.generatePortalRoom(level, origin, TC4OuterLandsGenCommonAdapter.portalCell());
    }

    public static void placeLibraryRoom(ServerLevel level, BlockPos origin) {
        TC4OuterLandsGenCommonAdapter.generateLibraryRoom(level, origin, TC4OuterLandsGenCommonAdapter.portalCell());
    }

    public static void placeNestRoom(ServerLevel level, BlockPos origin) {
        TC4OuterLandsGenCommonAdapter.generateNestRoom(level, origin, TC4OuterLandsGenCommonAdapter.portalCell());
    }

    public static void placeBossRoom(ServerLevel level, BlockPos center, Direction doorwayFacing, int feature, boolean golemRoom) {
        Direction facing = horizontal(doorwayFacing);
        BlockState floor = ThaumcraftMod.ELDRITCH_STONE.get().defaultBlockState();
        BlockState wall = ThaumcraftMod.OBSIDIAN_TILE.get().defaultBlockState();
        BlockState cap = ThaumcraftMod.ELDRITCH_OBELISK.get().defaultBlockState();

        for (int x = -ROOM_RADIUS; x <= ROOM_RADIUS; x++) {
            for (int z = -ROOM_RADIUS; z <= ROOM_RADIUS; z++) {
                int manhattan = Math.max(Math.abs(x), Math.abs(z));
                BlockPos floorPos = center.offset(x, -1, z);
                level.setBlock(floorPos, floor, 3);
                for (int y = 0; y <= ROOM_HEIGHT; y++) {
                    BlockPos p = center.offset(x, y, z);
                    if (manhattan == ROOM_RADIUS || y == ROOM_HEIGHT) {
                        level.setBlock(p, y == ROOM_HEIGHT ? cap : wall, 3);
                    } else {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }

        placeDoorway(level, center.relative(facing, ROOM_RADIUS), facing, TC4OuterLandsBossRoomMetadata.bossRoomTag(feature, facing));
        if (golemRoom) {
            spawnGolem(level, center);
        } else {
            spawnWarden(level, center);
        }
        level.playSound(null, center, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.55F, 0.55F);
    }

    public static void placeKeyRoom(ServerLevel level, BlockPos center, Direction doorwayFacing) {
        Direction facing = horizontal(doorwayFacing);
        BlockState shell = ThaumcraftMod.OBSIDIAN_TILE.get().defaultBlockState();
        BlockState trim = ThaumcraftMod.ELDRITCH_STONE.get().defaultBlockState();
        BlockState inner = ThaumcraftMod.ELDRITCH_OBELISK.get().defaultBlockState();

        // TC4 GenKeyRoom is a 15x15x13 shell around local x/z 1..15, centered at 8,8.
        for (int a = -7; a <= 7; a++) {
            for (int b = -7; b <= 7; b++) {
                for (int c = 0; c < 13; c++) {
                    int aa = a + 8;
                    int bb = b + 8;
                    BlockPos pos = center.offset(a, c, b);
                    if (aa == 1 || aa == 15 || bb == 1 || bb == 15) {
                        level.setBlock(pos, shell, 3);
                    } else if (c == 0 || c == 12) {
                        level.setBlock(pos, trim, 3);
                    } else if ((aa == 2 || aa == 14 || bb == 2 || bb == 14) && c < 10) {
                        level.setBlock(pos, trim, 3);
                    } else if ((aa == 3 || aa == 13 || bb == 3 || bb == 13) && c > 1 && c < 11) {
                        level.setBlock(pos, ((c > 3 && c < 9 && (aa == 8 || bb == 8)) || (c > 4 && c < 8 && (aa == 7 || bb == 7 || aa == 9 || bb == 9))) ? inner : shell, 3);
                    } else {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }

        // Original line 76: blockEldritch meta 3 at x+8,y+2,z+8; line 78: EntityPermanentItem with itemEldritchObject meta 2.
        level.setBlock(center.above(2), ThaumcraftMod.ELDRITCH_ALTAR.get().defaultBlockState(), 3);
        TC4OuterLandsLootAdapter.spawnPermanentKeyItem(level, center.above(2));
        spawnKeyRoomGuardians(level, center.above(2));
        placeDoorway(level, center.relative(facing, 7), facing, TC4OuterLandsBossRoomMetadata.keyRoomTag(facing));
        level.playSound(null, center, SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 0.6F, 0.6F);
    }

    /**
     * Stage222 live Outer Lands room selector bridge.
     *
     * <p>This is intentionally deterministic around the current portal adapter, but it
     * routes generation through the same direct 1.19.2 adapters used for TC4 Gen2x2,
     * GenPassage, GenLibraryRoom and GenNestRoom.  It replaces the old isolated
     * portal/boss/key placement with a connected miniature room graph.</p>
     */
    public static void placeRoomSelectorRing(ServerLevel level, BlockPos portalOrigin) {
        // Stage223-232 mega-stage: replace fixed mini-ring with MazeHandler / Cell graph
        // parity bridge.  The 1.7.10 MazeHandler stores packed Cell shorts keyed by
        // CellLoc chunk coordinates and dispatches to GenPortal, GenBossRoom,
        // GenKeyRoom, GenNestRoom, GenLibraryRoom or GenPassage by feature id.
        TC4OuterLandsMazeHandler.ensurePortalMaze(level, portalOrigin);
        TC4OuterLandsMazeHandler.generateAround(level, portalOrigin, 3);
    }

    private static void placeDoorway(ServerLevel level, BlockPos origin, Direction facing, CompoundTag metadata) {
        Direction right = facing.getClockWise();
        for (int a = 0; a < TC4OuterLandsBossRoomMetadata.PAT_DOORWAY.length; a++) {
            for (int b = 0; b < TC4OuterLandsBossRoomMetadata.PAT_DOORWAY[a].length; b++) {
                int cell = TC4OuterLandsBossRoomMetadata.PAT_DOORWAY[a][b];
                BlockPos pos = origin.relative(right, a - 3).above(b - 3);
                if (cell == 0) {
                    continue;
                }
                if (cell == 1) {
                    level.setBlock(pos, ThaumcraftMod.ELDRITCH_ALTAR.get().defaultBlockState(), 3);
                    if (level.getBlockEntity(pos) != null) {
                        level.getBlockEntity(pos).load(metadata.copy());
                    }
                } else if (cell == 2) {
                    level.setBlock(pos, ThaumcraftMod.OBSIDIAN_TILE.get().defaultBlockState(), 3);
                } else if (cell == 9) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }


    /** Stage243-252: cell-local bridge for original GenBossRoom.generateRoom. */
    public static void placeBossRoomCell(ServerLevel level, BlockPos cellOrigin, TC4OuterLandsGenCommonAdapter.Cell cell, int feature) {
        TC4OuterLandsRoomAdapter.Quadrant quadrant = switch (feature) {
            case TC4OuterLandsMazeGenerator.FEATURE_BOSS_2 -> TC4OuterLandsRoomAdapter.Quadrant.UPPER_LEFT;
            case TC4OuterLandsMazeGenerator.FEATURE_BOSS_3 -> TC4OuterLandsRoomAdapter.Quadrant.UPPER_RIGHT;
            case TC4OuterLandsMazeGenerator.FEATURE_BOSS_4 -> TC4OuterLandsRoomAdapter.Quadrant.LOWER_LEFT;
            case TC4OuterLandsMazeGenerator.FEATURE_BOSS_5 -> TC4OuterLandsRoomAdapter.Quadrant.LOWER_RIGHT;
            default -> TC4OuterLandsRoomAdapter.Quadrant.UPPER_LEFT;
        };
        TC4OuterLandsRoomAdapter.generate2x2(level, cellOrigin, cell, quadrant);
        placeOriginalBossDoor(level, cellOrigin, cell, feature);

        // GenBossRoom only builds the 2x2 chamber and places TileEldritchLock
        // in PAT_DOORWAY.  The boss must not exist until the player activates
        // that lock and its 100-tick pump sequence completes.
    }

    /** Stage243-252: cell-local bridge for original GenKeyRoom.generateRoom. */
    public static void placeKeyRoomCell(ServerLevel level, BlockPos origin, TC4OuterLandsGenCommonAdapter.Cell cell) {
        for (int a = 1; a <= 15; a++) {
            for (int b = 1; b <= 15; b++) {
                for (int c = 0; c < 13; c++) {
                    if (a == 1 || a == 15 || b == 1 || b == 15) {
                        TC4OuterLandsGenCommonAdapter.placeBlock(level, origin.offset(a, c, b), TC4OuterLandsGenCommonAdapter.CODE_BEDROCK_IF_AIR, Direction.NORTH, cell);
                    }
                }
            }
        }
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                for (int c = 1; c < 12; c++) {
                    if ((a == 2 || a == 14 || b == 2 || b == 14)
                            && ((a != 2) || b <= 3 || b >= 12 || !cell.west() || c >= 10)
                            && ((a != 14) || b <= 3 || b >= 12 || !cell.east() || c >= 10)
                            && ((b != 2) || a <= 3 || a >= 12 || !cell.north() || c >= 10)
                            && ((b != 14) || a <= 3 || a >= 12 || !cell.south() || c >= 10)) {
                        TC4OuterLandsGenCommonAdapter.placeBlock(level, origin.offset(a, c, b), TC4OuterLandsGenCommonAdapter.CODE_ELDRITCH_NOTHING, Direction.NORTH, cell);
                    }
                }
            }
        }
        for (int a = 3; a <= 13; a++) {
            for (int b = 3; b <= 13; b++) {
                for (int c = 2; c < 11; c++) {
                    if (a == 3 || a == 13 || b == 3 || b == 13) {
                        boolean innerRune = ((c > 3 && c < 9 && (a == 8 || b == 8)) || (c > 4 && c < 8 && (a == 7 || b == 7 || a == 9 || b == 9)));
                        if (innerRune && ((a != 8 && b != 8) || c != 6)) {
                            TC4OuterLandsGenCommonAdapter.placeBlock(level, origin.offset(a, c, b), TC4OuterLandsGenCommonAdapter.CODE_COSMETIC_META_13, Direction.NORTH, cell);
                        } else if (!innerRune) {
                            TC4OuterLandsGenCommonAdapter.placeBlock(level, origin.offset(a, c, b), TC4OuterLandsGenCommonAdapter.CODE_COSMETIC_META_12, Direction.NORTH, cell);
                        }
                    }
                }
            }
        }
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                TC4OuterLandsGenCommonAdapter.placeBlock(level, origin.offset(a, -1, b), TC4OuterLandsGenCommonAdapter.CODE_BEDROCK_IF_AIR, Direction.NORTH, cell);
                TC4OuterLandsGenCommonAdapter.placeBlock(level, origin.offset(a, 0, b), TC4OuterLandsGenCommonAdapter.CODE_ELDRITCH_NOTHING, Direction.NORTH, cell);
                TC4OuterLandsGenCommonAdapter.placeBlock(level, origin.offset(a, 1, b), TC4OuterLandsGenCommonAdapter.CODE_COSMETIC_SOLID, Direction.NORTH, cell);
                TC4OuterLandsGenCommonAdapter.placeBlock(level, origin.offset(a, 13, b), TC4OuterLandsGenCommonAdapter.CODE_BEDROCK_IF_AIR, Direction.NORTH, cell);
                TC4OuterLandsGenCommonAdapter.placeBlock(level, origin.offset(a, 12, b), TC4OuterLandsGenCommonAdapter.CODE_ELDRITCH_NOTHING, Direction.NORTH, cell);
                TC4OuterLandsGenCommonAdapter.placeBlock(level, origin.offset(a, 11, b), TC4OuterLandsGenCommonAdapter.CODE_COSMETIC_SOLID, Direction.NORTH, cell);
                if (a > 1 && a < 15 && b > 1 && b < 15) {
                    int q = Math.min(Math.abs(8 - a), Math.abs(8 - b));
                    for (int g = 0; g < q - 1; g++) {
                        TC4OuterLandsGenCommonAdapter.placeBlock(level, origin.offset(a, 1 + g, b), TC4OuterLandsGenCommonAdapter.CODE_COSMETIC_SOLID, Direction.NORTH, cell);
                    }
                }
                if (a > 3 && a < 13 && b > 3 && b < 13) {
                    int q = Math.min(Math.abs(8 - a), Math.abs(8 - b));
                    for (int g = 0; g < q; g++) {
                        TC4OuterLandsGenCommonAdapter.placeBlock(level, origin.offset(a, 11 - g, b), TC4OuterLandsGenCommonAdapter.CODE_COSMETIC_SOLID, Direction.NORTH, cell);
                    }
                }
            }
        }
        for (int g = 0; g < 5; g++) {
            TC4OuterLandsGenCommonAdapter.placeBlock(level, origin.offset(6 + g, 2, 4), TC4OuterLandsGenCommonAdapter.CODE_STAIRS_NORMAL, Direction.NORTH, cell);
            TC4OuterLandsGenCommonAdapter.placeBlock(level, origin.offset(6 + g, 2, 12), TC4OuterLandsGenCommonAdapter.CODE_STAIRS_NORMAL, Direction.SOUTH, cell);
            TC4OuterLandsGenCommonAdapter.placeBlock(level, origin.offset(12, 2, 6 + g), TC4OuterLandsGenCommonAdapter.CODE_STAIRS_NORMAL, Direction.EAST, cell);
            TC4OuterLandsGenCommonAdapter.placeBlock(level, origin.offset(4, 2, 6 + g), TC4OuterLandsGenCommonAdapter.CODE_STAIRS_NORMAL, Direction.WEST, cell);
        }
        TC4OuterLandsGenCommonAdapter.generateConnections(level, origin, cell, 3, true);
        level.setBlock(origin.offset(8, 2, 8), TC4EldritchBlockVariantAdapter.stateForMeta(TC4EldritchBlockVariantAdapter.META_KEY_ALTAR), 3);
        TC4OuterLandsLootAdapter.spawnPermanentKeyItem(level, origin.offset(8, 3, 8));
        spawnKeyRoomGuardians(level, origin.offset(8, 2, 8));
        level.playSound(null, origin.offset(8, 2, 8), SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 0.6F, 0.6F);
    }

    private static void placeOriginalBossDoor(ServerLevel level, BlockPos origin, TC4OuterLandsGenCommonAdapter.Cell cell, int feature) {
        Direction dir = TC4OuterLandsFeatureSelector.doorwayFacing(cell);
        for (int a = 0; a < TC4OuterLandsBossRoomMetadata.PAT_DOORWAY.length; a++) {
            for (int b = 0; b < TC4OuterLandsBossRoomMetadata.PAT_DOORWAY[a].length; b++) {
                int xx = 0;
                int zz = 0;
                if (cell.north()) { xx = 5 + a; zz = 3; dir = Direction.NORTH; }
                if (cell.south()) { xx = 5 + a; zz = 13; dir = Direction.SOUTH; }
                if (cell.east()) { xx = 13; zz = 5 + a; dir = Direction.EAST; }
                if (cell.west()) { xx = 3; zz = 5 + a; dir = Direction.WEST; }
                int code = TC4OuterLandsBossRoomMetadata.PAT_DOORWAY[a][b];
                if (code == 1) {
                    TC4OuterLandsGenCommonAdapter.placeBlock(level, origin.offset(xx, 2 + b, zz), TC4OuterLandsGenCommonAdapter.CODE_ELDRITCH_LOCK, dir, cell);
                } else if (code == 2) {
                    TC4OuterLandsGenCommonAdapter.placeBlock(level, origin.offset(xx, 2 + b, zz), TC4OuterLandsGenCommonAdapter.CODE_CRAB_SPAWNER_A, dir, cell);
                } else if (code == 9) {
                    TC4OuterLandsGenCommonAdapter.placeBlock(level, origin.offset(xx, 2 + b, zz), TC4OuterLandsGenCommonAdapter.CODE_AIRY_META_12, dir, cell);
                }
            }
        }
    }

    private static void spawnKeyRoomGuardians(ServerLevel level, BlockPos center) {
        int count = TC4OuterLandsLootAdapter.keyRoomGuardianCount(level);
        for (int index = 0; index < count; index++) {
            EldritchGuardianEntity guardian = ThaumcraftMod.ELDRITCH_GUARDIAN.get().create(level);
            if (guardian == null) {
                continue;
            }
            int signX = level.random.nextBoolean() ? 1 : -1;
            int signZ = level.random.nextBoolean() ? 1 : -1;
            double x = center.getX() + 0.5D + (1 + level.random.nextInt(3)) * signX;
            double z = center.getZ() + 0.5D + (1 + level.random.nextInt(3)) * signZ;
            guardian.moveTo(x, center.getY(), z, level.random.nextFloat() * 360.0F, 0.0F);
            guardian.finalizeSpawn(level, level.getCurrentDifficultyAt(guardian.blockPosition()), MobSpawnType.STRUCTURE, null, null);
            guardian.restrictTo(center, 16);
            TC4OuterLandsLootAdapter.applyKeyRoomChampionRule(guardian, index, count);
            level.addFreshEntity(guardian);
        }
    }

    private static void spawnWarden(ServerLevel level, BlockPos center) {
        EldritchWardenEntity warden = ThaumcraftMod.ELDRITCH_WARDEN.get().create(level);
        if (warden == null) {
            return;
        }
        warden.moveTo(center.getX() + 0.5D, center.getY(), center.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        warden.setSpawnTimer(150);
        level.addFreshEntity(warden);
    }

    private static void spawnGolem(ServerLevel level, BlockPos center) {
        EldritchGolemEntity golem = ThaumcraftMod.ELDRITCH_GOLEM.get().create(level);
        if (golem == null) {
            return;
        }
        golem.moveTo(center.getX() + 0.5D, center.getY(), center.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        golem.setSpawnTimer(150);
        level.addFreshEntity(golem);
    }

    private static Direction horizontal(Direction direction) {
        if (direction == null || direction.getAxis() == Direction.Axis.Y) {
            return Direction.NORTH;
        }
        return direction;
    }
}
