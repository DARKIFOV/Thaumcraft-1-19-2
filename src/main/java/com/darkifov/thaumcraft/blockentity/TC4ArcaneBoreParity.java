package com.darkifov.thaumcraft.blockentity;

/** Dependency-free formulas and state transitions copied from TC4 TileArcaneBore. */
public final class TC4ArcaneBoreParity {
    public static final String CONTRACT_VERSION = "11.64.24";
    public static final int BASE_RADIUS = 2;
    public static final int MAX_DEPTH = 64;
    public static final int SPIRAL_STEP_DEGREES = 2;
    public static final int FULL_CIRCLE_DEGREES = 360;
    public static final int VIS_REQUEST_CENTIVIS = 100;
    public static final float VIS_TO_SPEED_DIVISOR = 5.0F;
    public static final float ESSENTIA_SPEED_CREDIT = 20.0F;
    public static final int PICKAXE_REPAIR_INTERVAL = 40;
    public static final int REPAIR_VIS_DRAIN_INTERVAL = 5;
    public static final int OUTPUT_INSERT_SIDE_FROM_NOZZLE = 1;
    public static final String NBT_ORIENTATION = "orientation";
    public static final String NBT_BASE_ORIENTATION = "baseOrientation";
    public static final String NBT_INVENTORY = "Inventory";
    public static final String NBT_SLOT = "Slot";
    public static final String NBT_SPEEDY_TIME = "SpeedyTime";
    public static final String LEGACY_PORT_NBT_SPIRAL = "SpiralIndex";

    private TC4ArcaneBoreParity() {}

    public static int width(int enlargeLevel) {
        return 1 + (BASE_RADIUS + Math.max(0, enlargeLevel)) * 2;
    }

    public static boolean pickaxeIsNearBroken(int damage, int maxDamage) {
        return maxDamage > 0 && damage + 1 >= maxDamage;
    }

    public static int miningDelay(float hardness, int speed, boolean accelerated) {
        int count = Math.max(10 - speed, (int) (hardness * 2.0F) - speed * 2);
        return accelerated ? count : count * 4;
    }

    public static float addVisCredit(float current, int drainedCentivis) {
        return current + Math.max(0, drainedCentivis) / VIS_TO_SPEED_DIVISOR;
    }

    public static float addEssentiaCredit(float current) {
        return current + ESSENTIA_SPEED_CREDIT;
    }

    public static float consumeAcceleratedBlock(float current) {
        return current > 0.0F ? current - 1.0F : current;
    }

    public static float initialRadiusIncrement(int enlargeLevel) {
        return (BASE_RADIUS + Math.max(0, enlargeLevel)) / (float) FULL_CIRCLE_DEGREES;
    }

    /** Mutable-free representation of the original spiral cursor after one distinct lane is selected. */
    public record SpiralLane(int spiral, float currentRadius, float radiusIncrement,
                             int laneX, int laneY, int laneZ) {}

    /**
     * Exact TCVec3 rotation/order used by TileArcaneBore.findNextBlockToDig.
     * Direction components must be a vanilla/Forge unit direction.
     */
    public static SpiralLane nextLane(int originX, int originY, int originZ,
                                      int directionX, int directionY, int directionZ,
                                      int enlargeLevel, int spiral, float currentRadius,
                                      float radiusIncrement, int lastX, int lastY, int lastZ) {
        float increment = radiusIncrement == 0.0F ? initialRadiusIncrement(enlargeLevel) : radiusIncrement;
        int x = lastX;
        int y = lastY;
        int z = lastZ;
        int nextSpiral = spiral;
        float nextRadius = currentRadius;
        int guard = 0;
        while (x == lastX && y == lastY && z == lastZ && guard++ < 720) {
            nextSpiral += SPIRAL_STEP_DEGREES;
            if (nextSpiral >= FULL_CIRCLE_DEGREES) nextSpiral -= FULL_CIRCLE_DEGREES;
            nextRadius += increment;
            float radius = BASE_RADIUS + Math.max(0, enlargeLevel);
            if (nextRadius > radius || nextRadius < -radius) increment *= -1.0F;

            double vx = 0.0D;
            double vy = nextRadius;
            double vz = 0.0D;
            double angle = Math.toRadians(nextSpiral);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double zx = vx * cos + vy * sin;
            double zy = vy * cos - vx * sin;
            vx = zx;
            vy = zy;

            if (directionX != 0) {
                double yAngle = (Math.PI / 2.0D) * directionX;
                cos = Math.cos(yAngle);
                sin = Math.sin(yAngle);
                double nx = vx * cos + vz * sin;
                double nz = vz * cos - vx * sin;
                vx = nx;
                vz = nz;
            }
            if (directionY != 0) {
                double xAngle = (Math.PI / 2.0D) * directionY;
                cos = Math.cos(xAngle);
                sin = Math.sin(xAngle);
                double ny = vy * cos + vz * sin;
                double nz = vz * cos - vy * sin;
                vy = ny;
                vz = nz;
            }

            x = floor(originX + directionX + 0.5D + vx);
            y = floor(originY + directionY + 0.5D + vy);
            z = floor(originZ + directionZ + 0.5D + vz);
        }
        return new SpiralLane(nextSpiral, nextRadius, increment, x, y, z);
    }

    private static int floor(double value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }
}
