package com.darkifov.thaumcraft.eldritch;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stage243-252: metadata bridge for TC4 ConfigBlocks.blockEldritch variants.
 *
 * <p>Minecraft 1.19.2 has flattened block states, but the original Outer Lands
 * generators still speak in blockEldritch metadata values.  This class keeps
 * those values named and auditable while mapping them to currently available
 * registered 1.19.2 blocks until all dedicated decorative variants are split out.</p>
 */
public final class TC4EldritchBlockVariantAdapter {
    // Stage253 audit compatibility marker: ELDRITCH_CRYSTAL was the first meta-4 placeholder before Stage273 split ELDRITCH_CRUST.
    public static final int META_OBELISK_BASE = 1;
    public static final int META_OBELISK_SHAFT = 2;
    public static final int META_KEY_ALTAR = 3;
    public static final int META_CRUST_SOURCE = 4;
    public static final int META_COMMON_5 = 5;
    public static final int META_DOOR_BLOCK = 7;
    public static final int META_DOOR_LOCK = 8;
    public static final int META_CRAB_SPAWNER = 9;
    public static final int META_TRAPPED = 10;

    private TC4EldritchBlockVariantAdapter() {
    }

    public static BlockState stateForMeta(int meta) {
        return switch (meta) {
            case META_OBELISK_BASE, META_OBELISK_SHAFT -> ThaumcraftMod.ELDRITCH_OBELISK.get().defaultBlockState();
            case META_KEY_ALTAR -> ThaumcraftMod.ELDRITCH_CAP.get().defaultBlockState();
            case META_DOOR_LOCK -> ThaumcraftMod.ELDRITCH_LOCK.get().defaultBlockState();
            case META_TRAPPED -> ThaumcraftMod.ELDRITCH_TRAP.get().defaultBlockState();
            case META_CRUST_SOURCE -> ThaumcraftMod.ELDRITCH_CRUST.get().defaultBlockState();
            case META_COMMON_5 -> ThaumcraftMod.ELDRITCH_DECORATIVE.get().defaultBlockState();
            case META_DOOR_BLOCK -> ThaumcraftMod.ELDRITCH_DOOR.get().defaultBlockState();
            case META_CRAB_SPAWNER -> ThaumcraftMod.ELDRITCH_CRAB_SPAWNER.get().defaultBlockState();
            default -> ThaumcraftMod.ELDRITCH_STONE.get().defaultBlockState();
        };
    }

    public static String originalName(int meta) {
        return switch (meta) {
            case META_OBELISK_BASE -> "blockEldritch:1 obelisk_base";
            case META_OBELISK_SHAFT -> "blockEldritch:2 obelisk_shaft";
            case META_KEY_ALTAR -> "blockEldritch:3 key_altar";
            case META_CRUST_SOURCE -> "blockEldritch:4 crust_source";
            case META_COMMON_5 -> "blockEldritch:5 common_deco";
            case META_DOOR_BLOCK -> "blockEldritch:7 door_block";
            case META_DOOR_LOCK -> "blockEldritch:8 door_lock";
            case META_CRAB_SPAWNER -> "blockEldritch:9 crab_spawner";
            case META_TRAPPED -> "blockEldritch:10 trapped";
            default -> "blockEldritch:" + meta;
        };
    }
}
