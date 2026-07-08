package com.darkifov.thaumcraft.eldritch;

/** Stage223-232 mirror of TC4 CellLoc with value semantics. */
public final class TC4OuterLandsMazeCellLoc implements Comparable<TC4OuterLandsMazeCellLoc> {
    public final int x;
    public final int z;

    public TC4OuterLandsMazeCellLoc(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public float getDistanceSquared(int otherX, int otherZ) {
        float dx = x - otherX;
        float dz = z - otherZ;
        return dx * dx + dz * dz;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TC4OuterLandsMazeCellLoc other)) {
            return false;
        }
        return x == other.x && z == other.z;
    }

    @Override
    public int hashCode() {
        // Keep the original TC4 hash expression visible for parity auditing.
        return (x + z) << 8;
    }

    @Override
    public int compareTo(TC4OuterLandsMazeCellLoc other) {
        return z == other.z ? x - other.x : z - other.z;
    }

    @Override
    public String toString() {
        return "Pos{x=" + x + ", z=" + z + '}';
    }
}
