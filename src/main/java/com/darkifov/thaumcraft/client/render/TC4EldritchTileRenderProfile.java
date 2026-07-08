package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.resources.ResourceLocation;

/** Stage303-322 constants copied from TC4 TileEldritchNothingRenderer/obelisk tile render contracts. */
public final class TC4EldritchTileRenderProfile {
    private TC4EldritchTileRenderProfile() {}
    public static final ResourceLocation ELDRITCH_CUBE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/eldritch_cube.png");
    public static final ResourceLocation ELDRITCH_PORTAL = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/misc/eldritch_portal.png");
    public static final int NOTHING_FOG_COLOR = 0x201030;
    public static final int LOCK_PUMP_CADENCE = 5;
    public static final int LOCK_BOSS_CYCLE_TICKS = 100;
    public static final int CAP_KEY_META = 3;
    public static final int TRAP_DAMAGE = 2;
    public static final float CRYSTAL_BOB_SCALE = 0.08F;
}
