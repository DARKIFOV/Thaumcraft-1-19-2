package com.darkifov.thaumcraft.eldritch;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Stage243-252: soft dimension bridge for the TC4 Outer Lands.
 *
 * <p>TC4 used a dedicated dimension provider for the Eldritch dimension.  The
 * current 1.19.2 port still has the portal-driven placement adapter, so this
 * class centralizes all dimension checks.  It accepts the Overworld portal test
 * harness used by earlier stages and also recognizes future real dimension ids
 * such as thaumcraft:outer_lands / thaumcraft:eldritch before registry bootstrap
 * is fully ported.</p>
 */
public final class TC4OuterLandsDimensionAdapter {
    public static final ResourceLocation OUTER_LANDS_ID = new ResourceLocation("thaumcraft", "outer_lands");
    public static final ResourceLocation ELDRITCH_ID = new ResourceLocation("thaumcraft", "eldritch");
    public static final int ORIGINAL_ROOM_Y = 50;
    public static final int ORIGINAL_CELL_SIZE = 16;

    private TC4OuterLandsDimensionAdapter() {
    }

    public static boolean isOuterLands(ResourceKey<Level> dimension) {
        ResourceLocation id = dimension.location();
        return id.equals(OUTER_LANDS_ID)
                || id.equals(ELDRITCH_ID)
                || "outer_lands".equals(id.getPath())
                || "eldritch".equals(id.getPath());
    }

    public static boolean supportsPortalMaze(ResourceKey<Level> dimension) {
        return dimension == Level.OVERWORLD || isOuterLands(dimension);
    }

    public static boolean shouldRunSurfaceWorldgen(ResourceKey<Level> dimension) {
        return dimension == Level.OVERWORLD;
    }
}
