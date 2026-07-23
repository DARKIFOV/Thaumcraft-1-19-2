package com.darkifov.thaumcraft.runic;

import com.darkifov.thaumcraft.damage.TC4DamageSources;

/** v11.63.94 source-linked runic shield, damage-source and fortress contract. */
public final class TC4RunicParity {
    public static final String CONTRACT_VERSION = "11.63.94";
    private TC4RunicParity() {}

    public static boolean shieldConstantsMatchTc4() {
        return TC4RunicShieldRuntime.DEFAULT_SHIELD_RECHARGE_MS == 2000
                && TC4RunicShieldRuntime.DEFAULT_SHIELD_WAIT_TICKS == 80
                && TC4RunicShieldRuntime.DEFAULT_SHIELD_COST == 50;
    }

    public static boolean damageSourcesMatchTc4() {
        return TC4DamageSources.TAINT.isBypassArmor()
                && TC4DamageSources.TAINT.isMagic()
                && TC4DamageSources.DISSOLVE.isBypassArmor();
    }

    public static boolean fortressArmorConstantsMatchTc4() {
        return TC4FortressArmorRuntime.ORIGINAL_FORTRESS_PIECE_COUNT == 3
                && close(TC4FortressArmorRuntime.ORIGINAL_SET_BASE, 0.875D)
                && close(TC4FortressArmorRuntime.ORIGINAL_SET_PIECE_BONUS, 0.125D)
                && close(TC4FortressArmorRuntime.ORIGINAL_MASK_BONUS, 0.05D)
                && close(TC4FortressArmorRuntime.ORIGINAL_ORDINARY_DIVISOR, 25.0D)
                && close(TC4FortressArmorRuntime.ORIGINAL_UNBLOCKABLE_DIVISOR, 35.0D)
                && close(TC4FortressArmorRuntime.ORIGINAL_FIRE_EXPLOSION_MAGIC_DIVISOR, 20.0D);
    }

    private static boolean close(double actual, double expected) {
        return Math.abs(actual - expected) < 0.000001D;
    }
}
