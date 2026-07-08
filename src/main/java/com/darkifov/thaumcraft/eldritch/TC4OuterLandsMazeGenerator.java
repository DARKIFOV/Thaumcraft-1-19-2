package com.darkifov.thaumcraft.eldritch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Stage233-242: direct 1.19.2-safe port of TC4 MazeGenerator/MazeThread.
 *
 * <p>Stage223-232 used a conservative fixed 7x7 ring.  This class now mirrors
 * the original 1.7.10 algorithm: width/height odd maze sizes, randomized 2x2
 * boss feature anchors (feature 2..5), central portal (feature 1), randomized
 * blockage seeds, DFS/backtracking with TC4's getNextIndex bias, dead-end key
 * room (feature 6), dead-end feature rooms 7..9, and random passage/2x2 feature
 * promotion 8/10/11/12/13/14.  It still writes packed Cell shorts through the
 * 1.19.2 MazeHandler bridge instead of old World/NBT APIs.</p>
 */
public final class TC4OuterLandsMazeGenerator {
    public static final int FEATURE_PORTAL = 1;
    public static final int FEATURE_BOSS_2 = 2;
    public static final int FEATURE_BOSS_3 = 3;
    public static final int FEATURE_BOSS_4 = 4;
    public static final int FEATURE_BOSS_5 = 5;
    public static final int FEATURE_KEY_ROOM = 6;
    public static final int FEATURE_NEST_ROOM = 7;
    public static final int FEATURE_LIBRARY_ROOM = 8;
    public static final int FEATURE_DEAD_END_ALT = 9;
    public static final int FEATURE_PASSAGE_MIN = 9;
    public static final int FEATURE_PASSAGE_MAX = 14;

    public static final int N = 1;
    public static final int S = 2;
    public static final int E = 4;
    public static final int W = 8;
    public static final int A = 16;
    public static final int B = 32;
    private static final int TEMP_BOSS_CONNECT = 99;

    private final int width;
    private final int height;
    private final Random rand;
    public final int[][] grid;

    public TC4OuterLandsMazeGenerator(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.rand = new Random(seed);
        this.grid = new int[height][width];
    }

    public static int getOpposite(int in) {
        return switch (in) {
            case N -> S;
            case S -> N;
            case E -> W;
            case W -> E;
            default -> -99;
        };
    }

    public static int getDX(int in) {
        return switch (in) {
            case E -> 1;
            case W -> -1;
            case N, S -> 0;
            default -> -99;
        };
    }

    public static int getDY(int in) {
        return switch (in) {
            case N -> -1;
            case S -> 1;
            case E, W -> 0;
            default -> -99;
        };
    }

    /** Run the original TC4 maze generation algorithm against this instance. */
    public boolean generate() {
        int bx = 0;
        int by = 0;
        switch (rand.nextInt(4)) {
            case 0 -> { bx = 0; by = 0; }
            case 1 -> { bx = width - 2; by = height - 2; }
            case 2 -> { bx = width - 2; by = 0; }
            case 3 -> { bx = 0; by = height - 2; }
            default -> { }
        }
        grid[by][bx] = FEATURE_BOSS_2 << 8;
        grid[by][bx + 1] = FEATURE_BOSS_3 << 8;
        grid[by + 1][bx] = FEATURE_BOSS_4 << 8;
        grid[by + 1][bx + 1] = FEATURE_BOSS_5 << 8;

        int px = 1 + width / 2;
        int py = 1 + height / 2;
        grid[py][px] = FEATURE_PORTAL << 8;

        ArrayList<Loc> cells = new ArrayList<>();
        int l = (width + height) / 4;
        for (int z = 0; z < l; z++) {
            int w = 1 + rand.nextInt(3);
            if (w > 2) {
                l--;
            }
            int qq = rand.nextInt(width - w);
            int ww = rand.nextInt(height - w);
            for (int a = qq; a < qq + w; a++) {
                for (int b = ww; b < ww + w; b++) {
                    if (grid[b][a] == 0) {
                        grid[b][a] = -1;
                    }
                }
            }
        }

        List<Integer> directions = Arrays.asList(N, S, E, W);
        Collections.shuffle(directions, rand);
        int xx = px + getDX(directions.get(0));
        int yy = py + getDY(directions.get(0));
        grid[py][px] |= directions.get(0);
        if (grid[yy][xx] < 0) {
            grid[yy][xx] = 0;
        }
        grid[yy][xx] |= getOpposite(directions.get(0));
        cells.add(new Loc(xx, yy));

        boolean success = false;
        while (!cells.isEmpty()) {
            int index = getNextIndex(cells.size());
            int x = cells.get(index).x;
            int y = cells.get(index).y;
            Collections.shuffle(directions, rand);
            boolean carved = false;
            for (int dir : directions) {
                int nx = x + getDX(dir);
                int ny = y + getDY(dir);
                if (0 < nx && nx < width - 1 && 0 < ny && ny < height - 1 && grid[ny][nx] == 0) {
                    grid[y][x] |= dir;
                    grid[ny][nx] |= getOpposite(dir);
                    cells.add(new Loc(nx, ny));
                    carved = true;
                }
                if (carved) {
                    success = true;
                    break;
                }
            }
            if (!carved) {
                cells.remove(index);
            }
        }
        if (!success) {
            return false;
        }

        for (int aa = 0; aa < height; aa++) {
            for (int bb = 0; bb < width; bb++) {
                if (grid[aa][bb] < 0) {
                    grid[aa][bb] = 0;
                }
            }
        }

        Collections.shuffle(directions, rand);
        for (int dir : directions) {
            int nx = px + getDX(dir);
            int ny = py + getDY(dir);
            if (0 < nx && nx < width - 1 && 0 < ny && ny < height - 1 && grid[ny][nx] > 0 && rand.nextBoolean()) {
                grid[ny][nx] |= getOpposite(dir);
                grid[py][px] |= dir;
            }
        }

        Collections.shuffle(directions, rand);
        boolean connected = false;
        bossConnect:
        for (int ax = 0; ax < 2; ax++) {
            for (int ay = 0; ay < 2; ay++) {
                for (int dir : directions) {
                    int nx = bx + ax + getDX(dir);
                    int ny = by + ay + getDY(dir);
                    if (0 < nx && nx < width - 1 && 0 < ny && ny < height - 1 && grid[ny][nx] > 0 && new TC4OuterLandsMazeCell((short) grid[ny][nx]).feature == 0) {
                        grid[ny][nx] |= getOpposite(dir);
                        grid[by + ay][bx + ax] |= dir;
                        connected = true;
                        break bossConnect;
                    }
                }
            }
        }

        if (!connected) {
            List<Integer> directions2 = Arrays.asList(N, S, E, W);
            Collections.shuffle(directions2, rand);
            success = false;
            connectSearch:
            for (int ax = 0; ax < 2; ax++) {
                for (int ay = 0; ay < 2; ay++) {
                    for (int dir2 : directions2) {
                        int qx = bx + ax + getDX(dir2);
                        int qy = by + ay + getDY(dir2);
                        if (0 < qx && qx < width - 1 && 0 < qy && qy < height - 1 && grid[qy][qx] == 0) {
                            cells.add(new Loc(qx, qy));
                            while (!cells.isEmpty()) {
                                int index = getNextIndex(cells.size());
                                int x = cells.get(index).x;
                                int y = cells.get(index).y;
                                Collections.shuffle(directions, rand);
                                boolean carved = false;
                                for (int dir : directions) {
                                    int nx = x + getDX(dir);
                                    int ny = y + getDY(dir);
                                    if (0 < nx && nx < width - 1 && 0 < ny && ny < height - 1) {
                                        if (grid[ny][nx] == 0) {
                                            grid[y][x] |= dir;
                                            grid[y][x] |= TEMP_BOSS_CONNECT << 8;
                                            grid[ny][nx] |= getOpposite(dir);
                                            grid[ny][nx] |= TEMP_BOSS_CONNECT << 8;
                                            cells.add(new Loc(nx, ny));
                                            carved = true;
                                        } else if (new TC4OuterLandsMazeCell((short) grid[ny][nx]).feature == 0) {
                                            grid[y][x] |= dir;
                                            grid[ny][nx] |= getOpposite(dir);
                                            grid[qy][qx] |= getOpposite(dir2);
                                            grid[by + ay][bx + ax] |= dir2;
                                            success = true;
                                            break connectSearch;
                                        }
                                        if (carved) {
                                            break;
                                        }
                                    }
                                }
                                if (!carved) {
                                    cells.remove(index);
                                }
                            }
                        }
                    }
                }
            }
            if (!success) {
                return false;
            }
        }

        clearTemporaryBossConnectors();
        ArrayList<CellLoc> deadEnds = findDeadEnds();
        if (deadEnds.isEmpty()) {
            return false;
        }

        int r = rand.nextInt(deadEnds.size());
        CellLoc keyLoc = deadEnds.get(r);
        TC4OuterLandsMazeCell key = new TC4OuterLandsMazeCell((short) grid[keyLoc.row][keyLoc.col]);
        key.feature = FEATURE_KEY_ROOM;
        grid[keyLoc.row][keyLoc.col] = key.pack();
        deadEnds.remove(r);

        if (!deadEnds.isEmpty()) {
            int count = 0;
            while (count < deadEnds.size() / 2) {
                r = rand.nextInt(deadEnds.size());
                CellLoc ll = deadEnds.get(r);
                TC4OuterLandsMazeCell c = new TC4OuterLandsMazeCell((short) grid[ll.row][ll.col]);
                if (c.feature == 0) {
                    c.feature = (byte) (7 + rand.nextInt(3));
                    grid[ll.row][ll.col] = c.pack();
                    deadEnds.remove(r);
                    count++;
                }
            }
        }

        for (int aa = 0; aa < height; aa++) {
            for (int bb = 0; bb < width; bb++) {
                TC4OuterLandsMazeCell c = new TC4OuterLandsMazeCell((short) grid[aa][bb]);
                if (c.feature == 0 && (c.north || c.south || c.west || c.east) && rand.nextInt(25) == 0) {
                    switch (rand.nextInt(8)) {
                        case 0 -> c.feature = 8;
                        case 1 -> c.feature = 10;
                        case 2, 3 -> c.feature = 11;
                        case 4, 5 -> c.feature = 12;
                        case 6 -> c.feature = 13;
                        case 7 -> c.feature = 14;
                        default -> { }
                    }
                    grid[aa][bb] = c.pack();
                }
            }
        }
        return true;
    }

    private void clearTemporaryBossConnectors() {
        for (int aa = 0; aa < height; aa++) {
            for (int bb = 0; bb < width; bb++) {
                TC4OuterLandsMazeCell c = new TC4OuterLandsMazeCell((short) grid[aa][bb]);
                if ((c.feature & 255) == TEMP_BOSS_CONNECT) {
                    c.feature = 0;
                    grid[aa][bb] = c.pack();
                }
            }
        }
    }

    private ArrayList<CellLoc> findDeadEnds() {
        ArrayList<CellLoc> deadEnds = new ArrayList<>();
        for (int aa = 0; aa < height; aa++) {
            for (int bb = 0; bb < width; bb++) {
                TC4OuterLandsMazeCell c = new TC4OuterLandsMazeCell((short) grid[aa][bb]);
                int exits = (c.north ? 1 : 0) + (c.south ? 1 : 0) + (c.east ? 1 : 0) + (c.west ? 1 : 0);
                if (exits == 1 && c.feature == 0) {
                    deadEnds.add(new CellLoc(aa, bb));
                }
            }
        }
        return deadEnds;
    }

    private int getNextIndex(int ceil) {
        float r = rand.nextFloat();
        if (r <= 0.45F) {
            return ceil - 1;
        }
        if (r <= 0.9F) {
            return rand.nextInt(ceil);
        }
        return 0;
    }

    /** Stage233-242 MazeThread.run bridge: generate a TC4 grid and copy it into labyrinth coordinates. */
    public static void generateAt(int centerX, int centerZ, int width, int height, long seed) {
        TC4OuterLandsMazeHandler.putToHashMapRaw(new TC4OuterLandsMazeCellLoc(centerX, centerZ), (short) 0);
        TC4OuterLandsMazeHandler.putToHashMapRaw(new TC4OuterLandsMazeCellLoc(centerX - width, centerZ - height), (short) 0);
        TC4OuterLandsMazeHandler.putToHashMapRaw(new TC4OuterLandsMazeCellLoc(centerX + width, centerZ + height), (short) 0);
        TC4OuterLandsMazeHandler.putToHashMapRaw(new TC4OuterLandsMazeCellLoc(centerX - width, centerZ + height), (short) 0);
        TC4OuterLandsMazeHandler.putToHashMapRaw(new TC4OuterLandsMazeCellLoc(centerX + width, centerZ - height), (short) 0);

        TC4OuterLandsMazeGenerator gen = new TC4OuterLandsMazeGenerator(width, height, seed++);
        int attempts = 0;
        while (!gen.generate() && attempts++ < 256) {
            gen = new TC4OuterLandsMazeGenerator(width, height, seed++);
        }
        if (attempts >= 256) {
            throw new IllegalStateException("Unable to generate TC4 Outer Lands maze after 256 attempts");
        }

        int col = centerX - (1 + width / 2);
        int row = centerZ - (1 + height / 2);
        for (int a = 0; a < width; a++) {
            for (int b = 0; b < height; b++) {
                if (gen.grid[b][a] > 0) {
                    TC4OuterLandsMazeHandler.putToHashMapRaw(new TC4OuterLandsMazeCellLoc(a + col, b + row), (short) gen.grid[b][a]);
                }
            }
        }
        cleanupSentinel(centerX, centerZ, width, height);
    }

    public static void generateAt(int centerX, int centerZ, long seed) {
        Random random = new Random(seed ^ 0x54434F555445524CL);
        int width = 15 + random.nextInt(8) * 2;
        int height = 15 + random.nextInt(8) * 2;
        generateAt(centerX, centerZ, width, height, seed);
    }

    private static void cleanupSentinel(int x, int z, int w, int h) {
        removeIfZero(new TC4OuterLandsMazeCellLoc(x, z));
        removeIfZero(new TC4OuterLandsMazeCellLoc(x - w, z - h));
        removeIfZero(new TC4OuterLandsMazeCellLoc(x + w, z + h));
        removeIfZero(new TC4OuterLandsMazeCellLoc(x - w, z + h));
        removeIfZero(new TC4OuterLandsMazeCellLoc(x + w, z - h));
    }

    private static void removeIfZero(TC4OuterLandsMazeCellLoc loc) {
        if (TC4OuterLandsMazeHandler.getFromHashMapRaw(loc) == 0) {
            TC4OuterLandsMazeHandler.removeFromHashMap(loc);
        }
    }

    private record Loc(int x, int y) { }
    private record CellLoc(int row, int col) { }
}
