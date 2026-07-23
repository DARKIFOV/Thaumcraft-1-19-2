package com.darkifov.thaumcraft.block;

/**
 * Dependency-free TC4 4.2.3.5 contract for the Arcane Pressure Plate and its keys.
 * Production code calls these methods directly; the Java 17 self-test exercises
 * the same formulas without loading Minecraft or Forge.
 */
public final class TC4ArcanePressurePlateParity {
    public static final String CONTRACT_VERSION = "11.64.21";

    public static final float WARDED_HARDNESS = -1.0F;
    public static final float UNWARDED_HARDNESS = 2.0F;
    public static final float EXPLOSION_RESISTANCE = 999.0F;
    public static final int CHECK_INTERVAL_TICKS = 20;
    public static final int MAX_SETTING = 2;

    public static final double SCAN_MIN_XZ = 0.125D;
    public static final double SCAN_MAX_XZ = 0.875D;
    public static final double SCAN_MAX_Y = 0.25D;
    public static final double OUTLINE_MIN_XZ = 0.0625D;
    public static final double OUTLINE_MAX_XZ = 0.9375D;
    public static final double OUTLINE_UNPRESSED_HEIGHT = 0.0625D;
    public static final double OUTLINE_PRESSED_HEIGHT = 0.03125D;

    public static final byte KEY_TARGET_DOOR = 0;
    public static final byte KEY_TARGET_PLATE = 1;

    private TC4ArcanePressurePlateParity() {
    }

    public static int clampSetting(int value) {
        return Math.max(0, Math.min(MAX_SETTING, value));
    }

    public static int nextSetting(int current) {
        return (clampSetting(current) + 1) % (MAX_SETTING + 1);
    }

    /**
     * Original trigger contract:
     * 0 = every non-ignoring entity;
     * 1 = every non-player plus players without owner/key access;
     * 2 = only players with owner/key access.
     */
    public static boolean shouldTrigger(int setting, boolean player, boolean ignoresTriggers,
                                        boolean authorized) {
        if (ignoresTriggers) return false;
        return switch (clampSetting(setting)) {
            case 0 -> true;
            case 1 -> !player || !authorized;
            case 2 -> player && authorized;
            default -> false;
        };
    }

    /** Owner may copy either key; full-access users may copy iron keys only. */
    public static boolean mayBindKey(boolean owner, boolean fullAccess, boolean goldKey) {
        return owner || (!goldKey && fullAccess);
    }

    public static int weakSignal(boolean powered) {
        return powered ? 15 : 0;
    }

    public static int strongSignal(boolean powered, boolean upwardSide) {
        return powered && upwardSide ? 15 : 0;
    }

    public static String legacyLocation(int x, int y, int z) {
        return x + "," + y + "," + z;
    }

    public static boolean locationMatches(String location, int x, int y, int z) {
        return legacyLocation(x, y, z).equals(location);
    }

    /** TC4 tooltip deliberately displays x, z, y. */
    public static String tooltipLocation(String location) {
        if (location == null) return "";
        String[] parts = location.split(",", -1);
        if (parts.length != 3) return location;
        return "x " + parts[0] + ", z " + parts[2] + ", y " + parts[1];
    }
}
