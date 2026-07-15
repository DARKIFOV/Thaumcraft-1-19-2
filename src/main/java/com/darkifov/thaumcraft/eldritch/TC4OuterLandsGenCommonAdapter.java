package com.darkifov.thaumcraft.eldritch;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.TC4LootBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Stage219 1.19.2-safe adapter for TC4 GenCommon / GenPortal / GenPassage.
 *
 * <p>The old classes place numeric palette codes through placeBlock(world,x,y,z,b,dir,cell).
 * This adapter keeps those original codes visible and maps them to the currently
 * available 1.19.2 port blocks.  Missing TC4-only blocks such as blockEldritchNothing
 * are represented as explicitly tagged bridge placements rather than hidden chests
 * or unrelated placeholders.</p>
 */
// Stage219 marker: GenCommon.placeBlock code palette, GenPortal 16x16x13, GenPassage doorway connections
public final class TC4OuterLandsGenCommonAdapter {
    public static final int CODE_BEDROCK_IF_AIR = 1;
    public static final int CODE_COSMETIC_SOLID = 2;
    public static final int CODE_STAIRS_UP_A = 3;
    public static final int CODE_STAIRS_UP_B = 4;
    public static final int CODE_STAIRS_DOWN_A = 5;
    public static final int CODE_STAIRS_DOWN_B = 6;
    public static final int CODE_ELDRITCH_META_4 = 7;
    public static final int CODE_ELDRITCH_NOTHING = 8;
    public static final int CODE_AIR = 9;
    public static final int CODE_STAIRS_NORMAL = 10;
    public static final int CODE_STAIRS_INVERTED = 11;
    public static final int CODE_CRAB_SPAWNER_A = 15;
    public static final int CODE_ELDRITCH_LOCK = 16;
    public static final int CODE_AIRY_META_12 = 17;
    public static final int CODE_COSMETIC_META_12 = 18;
    public static final int CODE_COSMETIC_META_13 = 19;
    public static final int CODE_ELDRITCH_META_10 = 20;
    public static final int CODE_COSMETIC_META_14 = 21;
    public static final int CODE_BEDROCK = 99;

    private TC4OuterLandsGenCommonAdapter() {
    }

    public static Cell portalCell() {
        return new Cell(true, true, true, true, 0);
    }

    public static void generatePortalRoom(ServerLevel level, BlockPos origin, Cell cell) {
        for (int a = 1; a <= 15; a++) {
            for (int b = 1; b <= 15; b++) {
                for (int c = 0; c < 13; c++) {
                    if (a == 1 || a == 15 || b == 1 || b == 15) {
                        placeBlock(level, origin.offset(a, c, b), CODE_BEDROCK_IF_AIR, Direction.NORTH, cell);
                    }
                }
            }
        }
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                for (int c = 1; c < 12; c++) {
                    if ((a == 2 || a == 14 || b == 2 || b == 14)
                            && ((a != 2) || b <= 3 || b >= 12 || !cell.west || c >= 10)
                            && ((a != 14) || b <= 3 || b >= 12 || !cell.east || c >= 10)
                            && ((b != 2) || a <= 3 || a >= 12 || !cell.north || c >= 10)
                            && ((b != 14) || a <= 3 || a >= 12 || !cell.south || c >= 10)) {
                        placeBlock(level, origin.offset(a, c, b), CODE_ELDRITCH_NOTHING, Direction.NORTH, cell);
                    }
                }
            }
        }
        for (int a = 3; a <= 13; a++) {
            for (int b = 3; b <= 13; b++) {
                for (int c = 2; c < 11; c++) {
                    if ((a == 3 || a == 13 || b == 3 || b == 13)
                            && (a > 4 || b > 4)
                            && (a > 4 || b < 12)
                            && (a < 12 || b > 4)
                            && (a < 12 || b < 12)) {
                        placeBlock(level, origin.offset(a, c, b), CODE_COSMETIC_SOLID, Direction.NORTH, cell);
                    }
                }
            }
        }
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                placeBlock(level, origin.offset(a, -1, b), CODE_BEDROCK_IF_AIR, Direction.NORTH, cell);
                placeBlock(level, origin.offset(a, 0, b), CODE_ELDRITCH_NOTHING, Direction.NORTH, cell);
                placeBlock(level, origin.offset(a, 1, b), CODE_COSMETIC_META_13, Direction.NORTH, cell);
                placeBlock(level, origin.offset(a, 13, b), CODE_BEDROCK_IF_AIR, Direction.NORTH, cell);
                placeBlock(level, origin.offset(a, 12, b), CODE_ELDRITCH_NOTHING, Direction.NORTH, cell);
                placeBlock(level, origin.offset(a, 11, b), CODE_COSMETIC_SOLID, Direction.NORTH, cell);

                // GenPortal lines 48-57: stepped lower floor and mirrored upper
                // ceiling.  These loops are part of the visible TC4 portal-room
                // silhouette and were missing from the previous bridge.
                if (a > 1 && a < 15 && b > 1 && b < 15) {
                    int q = Math.min(Math.abs(8 - a), Math.abs(8 - b));
                    for (int g = 0; g < q - 1; g++) {
                        placeBlock(level, origin.offset(a, 1 + g, b), CODE_COSMETIC_META_13, Direction.NORTH, cell);
                    }
                }
                if (a > 3 && a < 13 && b > 3 && b < 13) {
                    int q = Math.min(Math.abs(8 - a), Math.abs(8 - b));
                    for (int g = 0; g < q; g++) {
                        placeBlock(level, origin.offset(a, 11 - g, b), CODE_COSMETIC_META_13, Direction.NORTH, cell);
                    }
                }
            }
        }
        for (int g = 0; g < 5; g++) {
            placeBlock(level, origin.offset(6 + g, 2, 4), CODE_STAIRS_NORMAL, Direction.NORTH, cell);
            placeBlock(level, origin.offset(6 + g, 2, 12), CODE_STAIRS_NORMAL, Direction.SOUTH, cell);
            placeBlock(level, origin.offset(12, 2, 6 + g), CODE_STAIRS_NORMAL, Direction.EAST, cell);
            placeBlock(level, origin.offset(4, 2, 6 + g), CODE_STAIRS_NORMAL, Direction.WEST, cell);
        }
        generateConnections(level, origin, cell, 3, true);

        // GenPortal lines 69-103: hollow the four corner shafts and restore the
        // lower/upper stair triplets around them.  Without this the room looked
        // like a generic square hall even though the correct textures existed.
        for (int a = 3; a <= 13; a++) {
            for (int b = 3; b <= 13; b++) {
                for (int c = 1; c < 12; c++) {
                    boolean corner = (a <= 4 && b <= 4)
                            || (a <= 4 && b >= 12)
                            || (a >= 12 && b <= 4)
                            || (a >= 12 && b >= 12);
                    if (corner) {
                        placeBlock(level, origin.offset(a, c, b), CODE_AIR, Direction.NORTH, cell);
                    }
                }
            }
        }
        placePortalCornerStairs(level, origin, cell);

        level.setBlock(origin.offset(8, 2, 8), ThaumcraftMod.ELDRITCH_ALTAR.get().defaultBlockState(), 3);
        level.setBlock(origin.offset(8, 3, 8), ThaumcraftMod.ELDRITCH_PORTAL.get().defaultBlockState(), 3);
        genObelisk(level, origin.offset(8, 4, 8));
    }


    private static void placePortalCornerStairs(ServerLevel level, BlockPos origin, Cell cell) {
        // Lower north-west / north-east.
        placeBlock(level, origin.offset(5, 3, 5), CODE_STAIRS_NORMAL, Direction.NORTH, cell);
        placeBlock(level, origin.offset(4, 3, 5), CODE_STAIRS_NORMAL, Direction.NORTH, cell);
        placeBlock(level, origin.offset(5, 3, 4), CODE_STAIRS_NORMAL, Direction.WEST, cell);
        placeBlock(level, origin.offset(12, 3, 5), CODE_STAIRS_NORMAL, Direction.NORTH, cell);
        placeBlock(level, origin.offset(11, 3, 5), CODE_STAIRS_NORMAL, Direction.NORTH, cell);
        placeBlock(level, origin.offset(11, 3, 4), CODE_STAIRS_NORMAL, Direction.EAST, cell);

        // Lower south-west / south-east.
        placeBlock(level, origin.offset(5, 3, 11), CODE_STAIRS_NORMAL, Direction.SOUTH, cell);
        placeBlock(level, origin.offset(4, 3, 11), CODE_STAIRS_NORMAL, Direction.SOUTH, cell);
        placeBlock(level, origin.offset(5, 3, 12), CODE_STAIRS_NORMAL, Direction.WEST, cell);
        placeBlock(level, origin.offset(12, 3, 11), CODE_STAIRS_NORMAL, Direction.SOUTH, cell);
        placeBlock(level, origin.offset(11, 3, 11), CODE_STAIRS_NORMAL, Direction.SOUTH, cell);
        placeBlock(level, origin.offset(11, 3, 12), CODE_STAIRS_NORMAL, Direction.EAST, cell);

        // Mirrored upper triplets from GenPortal use the inverted stair palette.
        placeBlock(level, origin.offset(5, 8, 5), CODE_STAIRS_INVERTED, Direction.NORTH, cell);
        placeBlock(level, origin.offset(4, 8, 5), CODE_STAIRS_INVERTED, Direction.NORTH, cell);
        placeBlock(level, origin.offset(5, 8, 4), CODE_STAIRS_INVERTED, Direction.WEST, cell);
        placeBlock(level, origin.offset(12, 8, 5), CODE_STAIRS_INVERTED, Direction.NORTH, cell);
        placeBlock(level, origin.offset(11, 8, 5), CODE_STAIRS_INVERTED, Direction.NORTH, cell);
        placeBlock(level, origin.offset(11, 8, 4), CODE_STAIRS_INVERTED, Direction.EAST, cell);
        placeBlock(level, origin.offset(5, 8, 11), CODE_STAIRS_INVERTED, Direction.SOUTH, cell);
        placeBlock(level, origin.offset(4, 8, 11), CODE_STAIRS_INVERTED, Direction.SOUTH, cell);
        placeBlock(level, origin.offset(5, 8, 12), CODE_STAIRS_INVERTED, Direction.WEST, cell);
        placeBlock(level, origin.offset(12, 8, 11), CODE_STAIRS_INVERTED, Direction.SOUTH, cell);
        placeBlock(level, origin.offset(11, 8, 11), CODE_STAIRS_INVERTED, Direction.SOUTH, cell);
        placeBlock(level, origin.offset(11, 8, 12), CODE_STAIRS_INVERTED, Direction.EAST, cell);
    }



    public static void generateLibraryRoom(ServerLevel level, BlockPos origin, Cell cell) {
        // Stage220 direct GenLibraryRoom adapter: 15x15x13 shell, shelves at code 10/11, pillar groups, connections y+3.
        for (int a = 1; a <= 15; a++) for (int b = 1; b <= 15; b++) for (int c = 0; c < 13; c++) if (a == 1 || a == 15 || b == 1 || b == 15) placeBlock(level, origin.offset(a, c, b), CODE_BEDROCK_IF_AIR, Direction.NORTH, cell);
        for (int a = 2; a <= 14; a++) for (int b = 2; b <= 14; b++) for (int c = 1; c < 12; c++) if ((a == 2 || a == 14 || b == 2 || b == 14) && ((a != 2) || b <= 3 || b >= 12 || !cell.west || c >= 10) && ((a != 14) || b <= 3 || b >= 12 || !cell.east || c >= 10) && ((b != 2) || a <= 3 || a >= 12 || !cell.north || c >= 10) && ((b != 14) || a <= 3 || a >= 12 || !cell.south || c >= 10)) placeBlock(level, origin.offset(a, c, b), CODE_ELDRITCH_NOTHING, Direction.NORTH, cell);
        for (int a = 3; a <= 13; a++) for (int b = 3; b <= 13; b++) for (int c = 2; c < 11; c++) if (a == 3 || a == 13 || b == 3 || b == 13) placeBlock(level, origin.offset(a, c, b), CODE_COSMETIC_SOLID, Direction.NORTH, cell);
        for (int a = 2; a <= 14; a++) for (int b = 2; b <= 14; b++) {
            placeBlock(level, origin.offset(a, -1, b), CODE_BEDROCK_IF_AIR, Direction.NORTH, cell);
            placeBlock(level, origin.offset(a, 0, b), CODE_ELDRITCH_NOTHING, Direction.NORTH, cell);
            placeBlock(level, origin.offset(a, 1, b), CODE_COSMETIC_SOLID, Direction.NORTH, cell);
            placeBlock(level, origin.offset(a, 12, b), CODE_BEDROCK_IF_AIR, Direction.NORTH, cell);
            placeBlock(level, origin.offset(a, 11, b), CODE_ELDRITCH_NOTHING, Direction.NORTH, cell);
            placeBlock(level, origin.offset(a, 10, b), CODE_COSMETIC_SOLID, Direction.NORTH, cell);
            if (a > 3 && a < 13 && b > 3 && b < 13 && (((a <= 5) && (b <= 5)) || ((a <= 5) && (b >= 11)) || ((a >= 11) && (b <= 5)) || ((a >= 11) && (b >= 11)))) {
                placeBlock(level, origin.offset(a, 2, b), CODE_COSMETIC_SOLID, Direction.NORTH, cell);
                placeBlock(level, origin.offset(a, 9, b), CODE_COSMETIC_SOLID, Direction.NORTH, cell);
            }
        }
        for (int g = 0; g < 5; g++) {
            placeBlock(level, origin.offset(6 + g, 2, 4), CODE_STAIRS_NORMAL, Direction.NORTH, cell);
            placeBlock(level, origin.offset(6 + g, 2, 12), CODE_STAIRS_NORMAL, Direction.SOUTH, cell);
            placeBlock(level, origin.offset(12, 2, 6 + g), CODE_STAIRS_NORMAL, Direction.EAST, cell);
            placeBlock(level, origin.offset(4, 2, 6 + g), CODE_STAIRS_NORMAL, Direction.WEST, cell);
            placeBlock(level, origin.offset(6 + g, 9, 4), CODE_STAIRS_INVERTED, Direction.NORTH, cell);
            placeBlock(level, origin.offset(6 + g, 9, 12), CODE_STAIRS_INVERTED, Direction.SOUTH, cell);
            placeBlock(level, origin.offset(12, 9, 6 + g), CODE_STAIRS_INVERTED, Direction.EAST, cell);
            placeBlock(level, origin.offset(4, 9, 6 + g), CODE_STAIRS_INVERTED, Direction.WEST, cell);
        }
        placeBlock(level, origin.offset(8, 2, 8), CODE_COSMETIC_META_12, Direction.NORTH, cell);
        placeBlock(level, origin.offset(8, 3, 8), CODE_ELDRITCH_META_4, Direction.NORTH, cell);
        placeBlock(level, origin.offset(8, 4, 8), CODE_STAIRS_NORMAL, Direction.NORTH, cell);
        placeLibraryPedestalsAndSlabs(level, origin);
        generateConnections(level, origin, cell, 3, true);
    }

    public static void generateNestRoom(ServerLevel level, BlockPos origin, Cell cell) {
        // Stage220 direct GenNestRoom adapter: 15x15x11 shell, code 21 organic shell, central nests, loot urn/crate placeholders.
        for (int a = 1; a <= 15; a++) for (int b = 1; b <= 15; b++) for (int c = 0; c < 11; c++) if (a == 1 || a == 15 || b == 1 || b == 15) placeBlock(level, origin.offset(a, c, b), CODE_BEDROCK_IF_AIR, Direction.NORTH, cell);
        for (int a = 2; a <= 14; a++) for (int b = 2; b <= 14; b++) for (int c = 1; c < 10; c++) if ((a == 2 || a == 14 || b == 2 || b == 14) && ((a != 2) || b <= 3 || b >= 12 || !cell.west || c >= 10) && ((a != 14) || b <= 3 || b >= 12 || !cell.east || c >= 10) && ((b != 2) || a <= 3 || a >= 12 || !cell.north || c >= 10) && ((b != 14) || a <= 3 || a >= 12 || !cell.south || c >= 10)) placeBlock(level, origin.offset(a, c, b), CODE_ELDRITCH_NOTHING, Direction.NORTH, cell);
        for (int a = 2; a <= 14; a++) for (int b = 2; b <= 14; b++) {
            placeBlock(level, origin.offset(a, -1, b), CODE_BEDROCK_IF_AIR, Direction.NORTH, cell);
            placeBlock(level, origin.offset(a, 0, b), CODE_ELDRITCH_NOTHING, Direction.NORTH, cell);
            placeBlock(level, origin.offset(a, 1, b), CODE_COSMETIC_META_14, Direction.NORTH, cell);
            placeBlock(level, origin.offset(a, 11, b), CODE_BEDROCK_IF_AIR, Direction.NORTH, cell);
            placeBlock(level, origin.offset(a, 10, b), CODE_ELDRITCH_NOTHING, Direction.NORTH, cell);
            placeBlock(level, origin.offset(a, 9, b), CODE_COSMETIC_META_14, Direction.NORTH, cell);
            if (level.random.nextBoolean()) {
                placeBlock(level, origin.offset(a, 8, b), CODE_COSMETIC_META_14, Direction.NORTH, cell);
            } else if (level.random.nextBoolean() && level.isEmptyBlock(origin.offset(a, 8, b))) {
                level.setBlock(origin.offset(a, 8, b), ThaumcraftMod.ELDRITCH_CRYSTAL.get().defaultBlockState(), 3);
            }
        }
        for (int y = 2; y <= 4; y++) placeBlock(level, origin.offset(8, y, 8), CODE_COSMETIC_META_14, Direction.NORTH, cell);
        placeBlock(level, origin.offset(7, 2, 8), CODE_COSMETIC_META_14, Direction.NORTH, cell);
        placeBlock(level, origin.offset(8, 2, 7), CODE_COSMETIC_META_14, Direction.NORTH, cell);
        placeBlock(level, origin.offset(9, 2, 8), CODE_COSMETIC_META_14, Direction.NORTH, cell);
        placeBlock(level, origin.offset(8, 2, 9), CODE_COSMETIC_META_14, Direction.NORTH, cell);
        if (level.random.nextBoolean()) placeBlock(level, origin.offset(7, 3, 8), CODE_COSMETIC_META_14, Direction.NORTH, cell);
        if (level.random.nextBoolean()) placeBlock(level, origin.offset(8, 3, 7), CODE_COSMETIC_META_14, Direction.NORTH, cell);
        if (level.random.nextBoolean()) placeBlock(level, origin.offset(9, 3, 8), CODE_COSMETIC_META_14, Direction.NORTH, cell);
        if (level.random.nextBoolean()) placeBlock(level, origin.offset(8, 3, 9), CODE_COSMETIC_META_14, Direction.NORTH, cell);
        for (int y = 6; y <= 8; y++) placeBlock(level, origin.offset(8, y, 8), CODE_COSMETIC_META_14, Direction.NORTH, cell);
        if (level.random.nextBoolean()) placeBlock(level, origin.offset(7, 7, 8), CODE_COSMETIC_META_14, Direction.NORTH, cell);
        if (level.random.nextBoolean()) placeBlock(level, origin.offset(8, 7, 7), CODE_COSMETIC_META_14, Direction.NORTH, cell);
        if (level.random.nextBoolean()) placeBlock(level, origin.offset(9, 7, 8), CODE_COSMETIC_META_14, Direction.NORTH, cell);
        if (level.random.nextBoolean()) placeBlock(level, origin.offset(8, 7, 9), CODE_COSMETIC_META_14, Direction.NORTH, cell);
        placeBlock(level, origin.offset(8, 5, 8), level.random.nextBoolean() ? CODE_ELDRITCH_META_4 : CODE_CRAB_SPAWNER_A, Direction.DOWN, cell);
        generateConnections(level, origin, cell, 3, true);
        for (int a = -5; a <= 5; a++) for (int b = -5; b <= 5; b++) if (level.random.nextFloat() < 0.15F && level.isEmptyBlock(origin.offset(8 + a, 2, 8 + b))) placeLootBlock(level, origin.offset(8 + a, 2, 8 + b));
    }

    private static void placeLibraryPedestalsAndSlabs(ServerLevel level, BlockPos origin) {
        placeLibraryColumn(level, origin.offset(5, 4, 5), false);
        placeLibraryColumn(level, origin.offset(5, 4, 11), false);
        placeLibraryColumn(level, origin.offset(11, 4, 5), false);
        placeLibraryColumn(level, origin.offset(11, 4, 11), false);
        placeLibraryColumn(level, origin.offset(5, 7, 5), true);
        placeLibraryColumn(level, origin.offset(5, 7, 11), true);
        placeLibraryColumn(level, origin.offset(11, 7, 5), true);
        placeLibraryColumn(level, origin.offset(11, 7, 11), true);
        level.setBlock(origin.offset(8, 2, 8), ThaumcraftMod.OBSIDIAN_TILE.get().defaultBlockState(), 3);
        level.setBlock(origin.offset(8, 3, 8), TC4EldritchBlockVariantAdapter.stateForMeta(TC4EldritchBlockVariantAdapter.META_COMMON_5), 3);
        placeSlab(level, origin.offset(8, 4, 8), false);
        level.setBlock(origin.offset(8, 9, 8), ThaumcraftMod.OBSIDIAN_TILE.get().defaultBlockState(), 3);
        level.setBlock(origin.offset(8, 8, 8), TC4EldritchBlockVariantAdapter.stateForMeta(TC4EldritchBlockVariantAdapter.META_COMMON_5), 3);
        placeSlab(level, origin.offset(8, 7, 8), true);
    }

    private static void placeLibraryColumn(ServerLevel level, BlockPos base, boolean topSlab) {
        level.setBlock(base, TC4EldritchBlockVariantAdapter.stateForMeta(TC4EldritchBlockVariantAdapter.META_COMMON_5), 3);
        placeSlab(level, topSlab ? base.below() : base.above(), topSlab);
    }

    private static void placeSlab(ServerLevel level, BlockPos pos, boolean top) {
        BlockState slab = Blocks.SMOOTH_STONE_SLAB.defaultBlockState();
        if (top) {
            slab = slab.setValue(SlabBlock.TYPE, SlabType.TOP);
        }
        level.setBlock(pos, slab, 3);
    }

    private static void placeLootBlock(ServerLevel level, BlockPos pos) {
        float rr = level.random.nextFloat();
        int md = rr < 0.15F ? 2 : rr < 0.4F ? 1 : 0; // Stage222 reachable rare/uncommon bridge for TC4 md 0/1/2
        boolean crate = level.random.nextFloat() < 0.2F;
        BlockState state = (crate ? ThaumcraftMod.OUTER_LANDS_LOOT_CRATE.get() : ThaumcraftMod.OUTER_LANDS_LOOT_URN.get()).defaultBlockState()
                .setValue(TC4LootBlock.VARIANT, md);
        level.setBlock(pos, state, 3);
    }

    public static void generateConnections(ServerLevel level, BlockPos origin, Cell cell, int yOffset, boolean justTheTip) {
        // Stage233-242: direct TC4 GenCommon.generateConnections loops with PAT_CONNECT.
        if (cell.north) {
            for (int d = 0; d <= 3; d++) {
                placeConnectionSlice(level, origin, yOffset, d, Direction.NORTH, justTheTip, cell);
            }
        }
        if (cell.south) {
            for (int d = 0; d <= 3; d++) {
                placeConnectionSlice(level, origin, yOffset, d, Direction.SOUTH, justTheTip, cell);
            }
        }
        if (cell.east) {
            for (int d = 0; d <= 3; d++) {
                placeConnectionSlice(level, origin, yOffset, d, Direction.EAST, justTheTip, cell);
            }
        }
        if (cell.west) {
            for (int d = 0; d <= 3; d++) {
                placeConnectionSlice(level, origin, yOffset, d, Direction.WEST, justTheTip, cell);
            }
        }
    }

    private static void placeConnectionSlice(ServerLevel level, BlockPos origin, int yOffset, int depth, Direction facing, boolean justTheTip, Cell cell) {
        int wStart = (depth == 2 && justTheTip) ? 1 : (depth == 3 && justTheTip) ? 2 : 0;
        int wEnd = (depth == 2 && justTheTip) ? 10 : (depth == 3 && justTheTip) ? 9 : 11;
        int hStart = (depth == 2 && justTheTip) ? 1 : (depth == 3 && justTheTip) ? 2 : 0;
        int hEnd = (depth == 2 && justTheTip) ? 10 : (depth == 3 && justTheTip) ? 9 : 11;
        for (int w = wStart; w < wEnd; w++) {
            for (int h = hStart; h < hEnd; h++) {
                int code = PAT_CONNECT[h][w];
                if (depth == 3 && justTheTip && code == CODE_ELDRITCH_NOTHING) {
                    continue;
                }
                BlockPos pos = switch (facing) {
                    case NORTH -> origin.offset(3 + w, yOffset + 10 - h, depth);
                    case SOUTH -> origin.offset(3 + w, yOffset + 10 - h, 16 - depth);
                    case EAST -> origin.offset(16 - depth, yOffset + 10 - h, 3 + w);
                    case WEST -> origin.offset(depth, yOffset + 10 - h, 3 + w);
                    default -> origin.offset(3 + w, yOffset + 10 - h, depth);
                };
                placeBlock(level, pos, code, facing, cell);
            }
        }
    }

    public static void placeBlock(ServerLevel level, BlockPos pos, int code, Direction dir, Cell cell) {
        TC4OuterLandsDecorationAdapter.recordPlacement(level, pos, code, cell == null ? 0 : cell.feature(), dir);
        BlockState state = switch (code) {
            case CODE_BEDROCK_IF_AIR -> level.isEmptyBlock(pos) ? Blocks.BEDROCK.defaultBlockState() : null;
            case CODE_BEDROCK -> Blocks.BEDROCK.defaultBlockState();
            case CODE_COSMETIC_SOLID, CODE_COSMETIC_META_12, CODE_COSMETIC_META_13, CODE_COSMETIC_META_14 -> ThaumcraftMod.OBSIDIAN_TILE.get().defaultBlockState();
            case CODE_ELDRITCH_META_4 -> TC4EldritchBlockVariantAdapter.stateForMeta(TC4EldritchBlockVariantAdapter.META_CRUST_SOURCE);
            case CODE_ELDRITCH_META_10 -> TC4EldritchBlockVariantAdapter.stateForMeta(TC4EldritchBlockVariantAdapter.META_TRAPPED);
            case CODE_CRAB_SPAWNER_A -> ThaumcraftMod.ELDRITCH_CRAB_SPAWNER.get().defaultBlockState()
                    .setValue(BlockStateProperties.FACING, horizontal(dir));
            case CODE_ELDRITCH_LOCK -> ThaumcraftMod.ELDRITCH_LOCK.get().defaultBlockState()
                    .setValue(com.darkifov.thaumcraft.block.EldritchLockBlock.FACING, horizontal(dir))
                    .setValue(com.darkifov.thaumcraft.block.EldritchLockBlock.OPEN, false);
            case CODE_ELDRITCH_NOTHING, CODE_AIRY_META_12 -> ThaumcraftMod.ELDRITCH_NOTHING.get().defaultBlockState();
            case CODE_AIR -> Blocks.AIR.defaultBlockState();
            case CODE_STAIRS_NORMAL, CODE_STAIRS_INVERTED, CODE_STAIRS_UP_A, CODE_STAIRS_UP_B, CODE_STAIRS_DOWN_A, CODE_STAIRS_DOWN_B -> stairsFor(dir, code);
            default -> null;
        };
        if (state != null) {
            level.setBlock(pos, state, state.isAir() || state.is(ThaumcraftMod.ELDRITCH_NOTHING.get()) || state.is(Blocks.BEDROCK) ? 0 : 3);
        }
    }

    public static void genObelisk(ServerLevel level, BlockPos base) {
        level.setBlock(base, ThaumcraftMod.ELDRITCH_OBELISK.get().defaultBlockState(), 3);
        level.setBlock(base.above(1), ThaumcraftMod.ELDRITCH_OBELISK.get().defaultBlockState(), 3);
        level.setBlock(base.above(2), ThaumcraftMod.ELDRITCH_OBELISK.get().defaultBlockState(), 3);
        level.setBlock(base.above(3), ThaumcraftMod.ELDRITCH_OBELISK.get().defaultBlockState(), 3);
        level.setBlock(base.above(4), ThaumcraftMod.ELDRITCH_OBELISK.get().defaultBlockState(), 3);
    }

    public static final int[][] PAT_CONNECT = new int[][] {
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {1, 8, 8, 8, 8, 8, 8, 8, 8, 8, 1},
            {1, 8, 8, 2, 2, 2, 2, 2, 8, 8, 1},
            {1, 8, 2, 5, 9, 9, 9, 6, 2, 8, 1},
            {1, 8, 2, 9, 9, 9, 9, 9, 2, 8, 1},
            {1, 8, 2, 9, 9, 9, 9, 9, 2, 8, 1},
            {1, 8, 2, 9, 9, 9, 9, 9, 2, 8, 1},
            {1, 8, 2, 3, 9, 9, 9, 4, 2, 8, 1},
            {1, 8, 8, 2, 2, 2, 2, 2, 8, 8, 1},
            {1, 8, 8, 8, 8, 8, 8, 8, 8, 8, 1},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0}
    };

    private static Direction horizontal(Direction direction) {
        return direction == null || direction.getAxis() == Direction.Axis.Y ? Direction.NORTH : direction;
    }

    private static BlockState stairsFor(Direction dir, int code) {
        BlockState state = Blocks.STONE_BRICK_STAIRS.defaultBlockState();
        Direction facing = dir == null || dir.getAxis() == Direction.Axis.Y ? Direction.NORTH : dir;
        state = state.setValue(StairBlock.FACING, facing);
        if (code == CODE_STAIRS_INVERTED || code == CODE_STAIRS_DOWN_A || code == CODE_STAIRS_DOWN_B) {
            state = state.setValue(StairBlock.HALF, net.minecraft.world.level.block.state.properties.Half.TOP);
        }
        return state;
    }

    public record Cell(boolean north, boolean south, boolean east, boolean west, int feature) {
    }
}
