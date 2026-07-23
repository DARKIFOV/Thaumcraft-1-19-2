package com.darkifov.thaumcraft.world;

/** v11.63.94 source-linked Magical Forest/Eldritch biome and tree contract. */
public final class TC4WorldgenParity {
    public static final String CONTRACT_VERSION = "11.63.94";
    private TC4WorldgenParity() {}

    public static boolean biomeAndDecoratorConstantsMatchTc4() {
        return TC4Biomes.ORIGINAL_MAGICAL_FOREST_WEIGHT == 5
                && TC4Biomes.MAGICAL_FOREST != null
                && TC4Biomes.ELDRITCH != null
                && TC4WorldgenRuntime.ORIGINAL_MANA_POD_ATTEMPTS == 10;
    }

    public static boolean treeEntryPointsExist() {
        try {
            return TC4TreeGenerator.class.getMethod("growGreatwood",
                    net.minecraft.server.level.ServerLevel.class,
                    net.minecraft.core.BlockPos.class,
                    net.minecraft.util.RandomSource.class, boolean.class) != null
                    && TC4TreeGenerator.class.getMethod("growSilverwood",
                    net.minecraft.server.level.ServerLevel.class,
                    net.minecraft.core.BlockPos.class,
                    net.minecraft.util.RandomSource.class, boolean.class) != null;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }
}
