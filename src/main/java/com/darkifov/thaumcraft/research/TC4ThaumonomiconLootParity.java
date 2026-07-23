package com.darkifov.thaumcraft.research;

import java.util.List;

/** Dependency-free acquisition contracts from TC4 Config and ComponentWizardTower. */
public final class TC4ThaumonomiconLootParity {
    public static final String CONTRACT_VERSION = "11.64.26";
    public static final int MIN_COUNT = 1;
    public static final int MAX_COUNT = 1;
    public static final int RARE_CHEST_WEIGHT = 1;
    public static final int WIZARD_TOWER_WEIGHT = 20;
    public static final List<String> RARE_CHEST_TABLES = List.of(
            "dungeonChest", "pyramidJungleChest", "pyramidDesertyChest",
            "mineshaftCorridor", "strongholdCorridor", "strongholdCrossing",
            "strongholdLibrary");

    private TC4ThaumonomiconLootParity() {}

    /** The port has no TC4 wizard-tower structure registry; this exact contract remains an explicit integration boundary. */
    public static boolean requiresOriginalWizardTowerIntegration() { return true; }

    /** The seven legacy ChestGenHooks pools likewise require an exact global-loot adapter, not an approximate injection. */
    public static boolean requiresLegacyGlobalChestIntegration() { return true; }
}
