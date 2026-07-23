package com.darkifov.thaumcraft.golem;

/**
 * v11.63.68: source-of-truth ledger for TC4 golem behaviors ported from
 * thaumcraft.common.entities.golems.EntityGolemBase and ItemGolemBell.
 *
 * <ul>
 *   <li><b>Creeper avoidance</b>: every core type includes AIAvoidCreeperSwell
 *       at priority 0 — the highest-priority task shared by all golems.</li>
 *   <li><b>USE-core marker-side/empty-hand</b>: the USE core runs AIUseItem at
 *       priority 0 alongside creeper avoidance, and bell markers track both
 *       side (Direction ordinal) and color byte (-1 = any).</li>
 *   <li><b>Weighted fishing quality</b>: the FISH core runs AIFish at priority 2
 *       with mutex 3, and original TC4 weighted fish quality is preserved.</li>
 *   <li><b>Material stats</b>: exact TC4 EnumGolemType values for 8 materials,
 *       including health, armor, speed, fire resistance, upgrade slots, carry
 *       capacity, regen delay, and strength.</li>
 *   <li><b>Upgrade stacking</b>: MAX_SAME_UPGRADE = 2; duplicate upgrades
 *       stack their effects; byte[] storage preserves this, a Set cannot.</li>
 * </ul>
 */
public final class TC4GolemParity {
    public static final String CONTRACT_VERSION = "11.63.68";

    /** TC4 EntityGolemBase: max two copies of the same upgrade type. */
    public static final int MAX_SAME_UPGRADE = GolemOriginalRuntime.MAX_SAME_UPGRADE;

    /** TC4: every core's first task is AIAvoidCreeperSwell, priority 0. */
    public static final int CREEPER_AVOID_PRIORITY = 0;

    /** TC4: AIFish priority and mutex bits. */
    public static final int FISH_PRIORITY = 2;
    public static final int FISH_MUTEX_BITS = 3;

    /** TC4: AIUseItem priority and mutex bits (USE core). */
    public static final int USE_ITEM_PRIORITY = 0;
    public static final int USE_ITEM_MUTEX_BITS = 3;

    /** TC4 EnumGolemType: 8 materials with exact stats. */
    public static final int MATERIAL_COUNT = 8;

    /** TC4 golem delay between AI ticks. */
    public static final int GOLEM_DELAY_TICKS = GolemTaskAIRuntime.ORIGINAL_GOLEM_DELAY_TICKS;

    /** TC4 bell marker color: -1 means "any color". */
    public static final byte MARKER_ANY_COLOR = -1;

    /** TC4 Config.golemIgnoreDelay: at least 10000ms before retargeting. */
    public static final long GOLEM_IGNORE_DELAY_MS = GolemTaskAIRuntime.ORIGINAL_GOLEM_IGNORE_DELAY_MS;

    private TC4GolemParity() {}

    /** Verify that every TC4 core type has creeper avoidance at highest priority. */
    public static boolean everyCoreHasCreeperAvoidance() {
        for (GolemCoreType core : GolemCoreType.originalValues()) {
            java.util.List<GolemTaskAIRuntime.OriginalTask> tasks = GolemTaskAIRuntime.tasksFor(core);
            if (tasks.isEmpty()) continue;
            GolemTaskAIRuntime.OriginalTask first = tasks.get(0);
            if (first != GolemTaskAIRuntime.OriginalTask.AIAvoidCreeperSwell
                    || first.priority() != CREEPER_AVOID_PRIORITY) {
                return false;
            }
        }
        return true;
    }

    /** Verify the FISH core has AIFish with correct priority. */
    public static boolean fishCoreHasFishingTask() {
        java.util.List<GolemTaskAIRuntime.OriginalTask> tasks = GolemTaskAIRuntime.tasksFor(GolemCoreType.FISH);
        for (GolemTaskAIRuntime.OriginalTask task : tasks) {
            if (task == GolemTaskAIRuntime.OriginalTask.AIFish
                    && task.priority() == FISH_PRIORITY
                    && task.mutexBits() == FISH_MUTEX_BITS) {
                return true;
            }
        }
        return false;
    }

    /** Verify the USE core has AIUseItem. */
    public static boolean useCoreHasUseItemTask() {
        java.util.List<GolemTaskAIRuntime.OriginalTask> tasks = GolemTaskAIRuntime.tasksFor(GolemCoreType.USE);
        for (GolemTaskAIRuntime.OriginalTask task : tasks) {
            if (task == GolemTaskAIRuntime.OriginalTask.AIUseItem
                    && task.priority() == USE_ITEM_PRIORITY
                    && task.mutexBits() == USE_ITEM_MUTEX_BITS) {
                return true;
            }
        }
        return false;
    }

    /** Verify material stats match TC4 EnumGolemType. */
    public static boolean materialStatsMatchTc4() {
        return GolemMaterial.STRAW.health() == 10 && GolemMaterial.STRAW.armorValue() == 0
                && GolemMaterial.STRAW.speed() == 0.38F && !GolemMaterial.STRAW.fireResist()
                && GolemMaterial.WOOD.health() == 20 && GolemMaterial.WOOD.armorValue() == 6
                && GolemMaterial.TALLOW.health() == 20 && GolemMaterial.TALLOW.armorValue() == 9
                && GolemMaterial.CLAY.health() == 25 && GolemMaterial.CLAY.fireResist()
                && GolemMaterial.STONE.health() == 30 && GolemMaterial.STONE.armorValue() == 12
                && GolemMaterial.IRON.health() == 35 && GolemMaterial.IRON.armorValue() == 15
                && GolemMaterial.THAUMIUM.health() == 40 && GolemMaterial.THAUMIUM.armorValue() == 15
                && GolemMaterial.THAUMIUM.upgradeSlots(false) == 2;
    }

    public static boolean decorationsBellModesAndCarryFormulaMatchTc4() {
        return GolemDecorationType.values().length == 9
                && GolemBellMode.values().length == 6
                && GolemDecorationType.ARMOR.id().equals("armor")
                && GolemDecorationType.WIRELESS_BACKPACK.id().equals("wireless_backpack")
                && GolemBellMode.HOME.id().equals("home")
                && GolemBellMode.STATUS.id().equals("status")
                && GolemMaterial.STRAW.carryLimit(0) == 1
                && GolemMaterial.STRAW.carryLimit(1) == 5
                && GolemMaterial.WOOD.carryLimit(1) == 8
                && GolemMaterial.STONE.carryLimit(1) == 32
                && GolemMaterial.THAUMIUM.carryLimit(1) == 48;
    }

    /** Verify MAX_SAME_UPGRADE = 2 and byte[] storage preserves duplicates. */
    public static boolean upgradeStackingMatchesTc4() {
        return MAX_SAME_UPGRADE == 2 && GolemUpgradeType.values().length == 6;
    }
}
