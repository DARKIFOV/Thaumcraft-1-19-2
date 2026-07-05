package com.darkifov.thaumcraft.porting;

import java.util.List;

/**
 * Stage113: map of the 1.19.2 Thaumcraft reboot archives provided by the user.
 *
 * These projects are used as the modern Forge 1.19.2 base/reference layer.
 * TC4 1.7.10 remains the strict behavior source of truth.
 */
public final class Provided1192BaseMap {
    private Provided1192BaseMap() {
    }

    public static final String PRIMARY_1192_BASE = "thaumcraftreboot-main";
    public static final String SECONDARY_1192_SKELETON = "thaumcraft-reboot-fbe41a6513a84f3e637778cc308fbc84b2aaeec0";
    public static final String STRICT_BEHAVIOR_SOURCE = "Thaumcraft4-1.7.10-master";

    public static final List<String> PRIMARY_BASE_RUNTIME_CLASSES = List.of(
            "thaumcraft.Thaumcraft",
            "thaumcraft.api.items.ThaumcraftCreativeModeTab",
            "thaumcraft.api.items.ThaumcraftItems",
            "thaumcraft.common.block.ThaumcraftBlocks"
    );

    public static final List<String> PRIMARY_BASE_RUNTIME_RESOURCES = List.of(
            "assets/thaumcraft/blockstates/table_wood.json",
            "assets/thaumcraft/models/block/table_wood.json",
            "assets/thaumcraft/models/item/table_wood.json",
            "assets/thaumcraft/models/item/brain.json",
            "assets/thaumcraft/textures/block/table_wood.png",
            "assets/thaumcraft/textures/item/brain.png",
            "assets/thaumcraft/sounds/brain1.ogg",
            "assets/thaumcraft/sounds/brain2.ogg",
            "assets/thaumcraft/sounds/brain3.ogg",
            "assets/thaumcraft/sounds/brain4.ogg"
    );

    public static final List<String> ACTIVE_PROJECT_DECISION = List.of(
            "Do not replace the active Stage112 project wholesale: it has far more ported runtime files.",
            "Use thaumcraftreboot-main as Forge 1.19.2 base reference for mod id, Gradle structure, registry shape, table/brain resource names.",
            "Use Thaumcraft4-1.7.10-master as strict behavior, research, aspect, GUI, recipe, asset and progression source.",
            "Keep code changes compile-safe and port TC4 systems one subsystem at a time."
    );
}
