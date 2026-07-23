package com.darkifov.thaumcraft.aura;

/** v11.63.94 source-linked aura-node type, modifier and jar-capture contract. */
public final class TC4AuraParity {
    public static final String CONTRACT_VERSION = "11.63.94";
    private TC4AuraParity() {}

    public static boolean nodeTypesMatchTc4() {
        return AuraNodeType.values().length == 6
                && AuraNodeType.NORMAL.color() == 0xBFA6FF
                && AuraNodeType.PURE.color() == 0x9EEBFF
                && AuraNodeType.DARK.color() == 0x5F3E8A
                && AuraNodeType.UNSTABLE.color() == 0xFFB84A
                && AuraNodeType.TAINTED.color() == 0x8F38B8
                && AuraNodeType.HUNGRY.color() == 0x4F2A6E;
    }

    public static boolean modifierDamageChainMatchesTc4() {
        return AuraNodeModifier.values().length == 4
                && AuraNodeModifier.NORMAL.name().equals(TC4NodeJarRuntime.weakenModifier(AuraNodeModifier.BRIGHT.name()))
                && AuraNodeModifier.PALE.name().equals(TC4NodeJarRuntime.weakenModifier(AuraNodeModifier.NORMAL.name()))
                && AuraNodeModifier.FADING.name().equals(TC4NodeJarRuntime.weakenModifier(AuraNodeModifier.PALE.name()))
                && AuraNodeModifier.FADING.name().equals(TC4NodeJarRuntime.weakenModifier(AuraNodeModifier.FADING.name()))
                && Math.abs(TC4NodeJarRuntime.MODIFIER_DAMAGE_CHANCE - 0.75F) < 0.0001F
                && TC4NodeJarRuntime.ORIGINAL_PRESERVATION_PERCENT == 100;
    }
}
