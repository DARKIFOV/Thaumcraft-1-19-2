package com.darkifov.thaumcraft.eldritch;

import net.minecraft.world.phys.Vec3;

/**
 * Stage303-322 1.19.2-safe bridge for TC4 WorldProviderOuter.
 *
 * <p>This does not forcibly register a dimension in existing saves; it centralizes the
 * exact provider constants and sky/fog math so the eventual level-stem hook can use the
 * same contract without touching old 1.7.10 APIs.</p>
 */
public final class TC4OuterLandsDimensionParity {
    private TC4OuterLandsDimensionParity() {}
    public static final String ORIGINAL_NAME = "Outer Lands";
    public static final int AVERAGE_GROUND_LEVEL = 50;
    public static final boolean CAN_RESPAWN_HERE = false;
    public static final boolean HAS_SKY_LIGHT = false;
    public static final boolean SHOULD_MAP_SPIN = true;
    public static final boolean FREEZE_BLOCKS = false;
    public static final boolean SNOW = false;
    public static final boolean LIGHTNING = false;
    public static final boolean RAIN_SNOW_ICE = false;
    public static final int FOG_COLOR = 10518688;

    public static Vec3 skyColor(float celestialAngle) {
        float f2 = (float)Math.cos(celestialAngle * Math.PI * 2.0F) * 2.0F + 0.5F;
        if (f2 < 0.0F) f2 = 0.0F;
        if (f2 > 1.0F) f2 = 1.0F;
        float r = ((FOG_COLOR >> 16) & 255) / 255.0F;
        float g = ((FOG_COLOR >> 8) & 255) / 255.0F;
        float b = (FOG_COLOR & 255) / 255.0F;
        return new Vec3(r * 0.15F, g * 0.15F, b * 0.15F);
    }
}
