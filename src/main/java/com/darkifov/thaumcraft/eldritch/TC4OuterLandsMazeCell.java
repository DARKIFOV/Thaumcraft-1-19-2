package com.darkifov.thaumcraft.eldritch;

/**
 * Stage223-232 mega-stage: 1.19.2-safe mirror of TC4's Cell class.
 *
 * <p>Original TC4 packed six doorway/vertical booleans into the low bits and
 * stored the room feature in the high byte.  Keeping this compact contract
 * makes MazeHandler save/load and feature dispatch auditable without importing
 * 1.7.10 NBTTag or World APIs.</p>
 */
public final class TC4OuterLandsMazeCell {
    public boolean north;
    public boolean south;
    public boolean east;
    public boolean west;
    public boolean above;
    public boolean below;
    public byte feature;

    public TC4OuterLandsMazeCell() {
    }

    public TC4OuterLandsMazeCell(short data) {
        unpack(data);
    }

    public TC4OuterLandsMazeCell(boolean north, boolean south, boolean east, boolean west, boolean above, boolean below, int feature) {
        this.north = north;
        this.south = south;
        this.east = east;
        this.west = west;
        this.above = above;
        this.below = below;
        this.feature = (byte) feature;
    }

    public TC4OuterLandsGenCommonAdapter.Cell toGenCell() {
        return new TC4OuterLandsGenCommonAdapter.Cell(north, south, east, west, feature & 255);
    }

    public short pack() {
        int out = 0;
        if (north) out |= 1;
        if (south) out |= 1 << 1;
        if (east) out |= 1 << 2;
        if (west) out |= 1 << 3;
        if (above) out |= 1 << 4;
        if (below) out |= 1 << 5;
        out |= (feature & 255) << 8;
        return (short) out;
    }

    private void unpack(short data) {
        int value = data & 0xFFFF;
        north = (value & 1) != 0;
        south = (value & (1 << 1)) != 0;
        east = (value & (1 << 2)) != 0;
        west = (value & (1 << 3)) != 0;
        above = (value & (1 << 4)) != 0;
        below = (value & (1 << 5)) != 0;
        feature = (byte) (value >> 8);
    }
}
