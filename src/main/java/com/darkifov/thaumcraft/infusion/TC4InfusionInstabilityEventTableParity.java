package com.darkifov.thaumcraft.infusion;

/**
 * Parity contract documenting the exact weighted instability event table from
 * the original Thaumcraft 4 (1.7.10) TileInfusionMatrix#craftCycle.
 *
 * The original method rolled random.nextInt(500) and only entered the
 * weighted table below when that roll was less than or equal to the current
 * raw instability value. Inside the table it rolled
 * random.nextInt(21) and switched on the result. This class records
 * that exact 21-slot mapping as constants/queries so it can be asserted by
 * GameTests instead of relying on prose.
 *
 * CONTRACT_VERSION marks the mod version this contract was authored against.
 */
public final class TC4InfusionInstabilityEventTableParity {

	public static final String CONTRACT_VERSION = "11.63.65";

	/** Original: random.nextInt(500). */
	public static final int GATE_ROLL_BOUND = 500;

	/** Original: random.nextInt(21) switch inside TileInfusionMatrix#craftCycle. */
	public static final int EVENT_ROLL_BOUND = 21;

	/** case 0, 2, 10, 13 -> eject a random item from the recipe's normal component pool (ejectItem type 0). */
	public static final int[] EJECT_TYPE_0_ROLLS = { 0, 2, 10, 13 };

	/** case 6, 17 -> eject flux goo (ejectItem type 1). */
	public static final int[] EJECT_FLUX_GOO_ROLLS = { 6, 17 };

	/** case 1, 11 -> eject flux gas (ejectItem type 2). */
	public static final int[] EJECT_FLUX_GAS_ROLLS = { 1, 11 };

	/** case 3, 8, 14 -> zap a single random target player. */
	public static final int[] ZAP_SINGLE_ROLLS = { 3, 8, 14 };

	/** case 5, 16 -> harm a single random target player (taint poison or weakness). */
	public static final int[] HARM_SINGLE_ROLLS = { 5, 16 };

	/** case 12 -> zap every player within range. */
	public static final int ZAP_ALL_ROLL = 12;

	/** case 19 -> eject item type 3. */
	public static final int EJECT_TYPE_3_ROLL = 19;

	/** case 7 -> eject item type 4. */
	public static final int EJECT_TYPE_4_ROLL = 7;

	/** case 4, 15 -> eject item type 5 and explode a random pedestal. */
	public static final int[] EXPLODE_PEDESTAL_ROLLS = { 4, 15 };

	/** case 18 -> harm every player within range. */
	public static final int HARM_ALL_ROLL = 18;

	/** case 9 -> explode the matrix itself. Original strength: 1.5F + random.nextFloat(). */
	public static final int EXPLODE_MATRIX_ROLL = 9;
	public static final float EXPLODE_MATRIX_BASE_STRENGTH = 1.5F;
	public static final float EXPLODE_MATRIX_RANDOM_STRENGTH_SPAN = 1.0F;

	/**
	 * case 20 -> warp. Original: 25% chance of +1 sticky warp, otherwise a
	 * permanent warp roll of random.nextInt(5) + 1 (1-5).
	 */
	public static final int WARP_ROLL = 20;
	public static final float WARP_STICKY_CHANCE = 0.25F;
	public static final int WARP_STICKY_AMOUNT = 1;
	public static final int WARP_PERMANENT_MIN = 1;
	public static final int WARP_PERMANENT_MAX = 5;

	private TC4InfusionInstabilityEventTableParity() {
	}

	/** Original gate: instability > 0 && random.nextInt(500) <= instability. */
	public static boolean gateAllows(int roll0to499, int instability) {
		return instability > 0 && roll0to499 <= instability;
	}

	public static boolean isEjectType0(int roll) {
		return contains(EJECT_TYPE_0_ROLLS, roll);
	}

	public static boolean isFluxGoo(int roll) {
		return contains(EJECT_FLUX_GOO_ROLLS, roll);
	}

	public static boolean isFluxGas(int roll) {
		return contains(EJECT_FLUX_GAS_ROLLS, roll);
	}

	public static boolean isZapSingle(int roll) {
		return contains(ZAP_SINGLE_ROLLS, roll);
	}

	public static boolean isHarmSingle(int roll) {
		return contains(HARM_SINGLE_ROLLS, roll);
	}

	public static boolean isExplodePedestal(int roll) {
		return contains(EXPLODE_PEDESTAL_ROLLS, roll);
	}

	/** Every roll from 0..20 that is not otherwise assigned falls back to a cosmetic surge with no gameplay effect. */
	public static boolean isCosmeticSurge(int roll) {
		if (roll < 0 || roll >= EVENT_ROLL_BOUND) {
			return false;
		}
		return !isEjectType0(roll) && !isFluxGoo(roll) && !isFluxGas(roll) && !isZapSingle(roll)
				&& !isHarmSingle(roll) && !isExplodePedestal(roll) && roll != ZAP_ALL_ROLL
				&& roll != EJECT_TYPE_3_ROLL && roll != EJECT_TYPE_4_ROLL && roll != HARM_ALL_ROLL
				&& roll != EXPLODE_MATRIX_ROLL && roll != WARP_ROLL;
	}

	/** Counts every roll slot 0..20 exactly once so the table is provably total and non-overlapping.
	 * Each roll must belong to exactly one category: specific handler or cosmetic surge (default). */
	public static int countAssignedRolls() {
		int count = 0;
		for (int roll = 0; roll < EVENT_ROLL_BOUND; roll++) {
			int categories = 0;
			if (isEjectType0(roll)) categories++;
			if (isFluxGoo(roll)) categories++;
			if (isFluxGas(roll)) categories++;
			if (isZapSingle(roll)) categories++;
			if (isHarmSingle(roll)) categories++;
			if (isExplodePedestal(roll)) categories++;
			if (roll == ZAP_ALL_ROLL) categories++;
			if (roll == EJECT_TYPE_3_ROLL) categories++;
			if (roll == EJECT_TYPE_4_ROLL) categories++;
			if (roll == HARM_ALL_ROLL) categories++;
			if (roll == EXPLODE_MATRIX_ROLL) categories++;
			if (roll == WARP_ROLL) categories++;
			if (isCosmeticSurge(roll)) categories++;
			// Each roll must belong to exactly one category (non-overlapping).
			// If it belongs to zero or more than one, the table is broken.
			if (categories == 1) {
				count++;
			}
		}
		return count;
	}

	private static boolean contains(int[] rolls, int value) {
		for (int roll : rolls) {
			if (roll == value) {
				return true;
			}
		}
		return false;
	}
}
