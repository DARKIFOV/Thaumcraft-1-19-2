package com.darkifov.thaumcraft.client;

import java.util.HashMap;
import java.util.Map;

/** Client-side mirror of TC4 EventHandlerRunic.runicCharge/runicInfo for HUD/FX. */
public final class RunicShieldClientState {
    private static final Map<Integer, Integer> CHARGE = new HashMap<>();
    private static final Map<Integer, Integer> MAX = new HashMap<>();

    private RunicShieldClientState() {
    }

    public static void set(int entityId, int amount, int max) {
        CHARGE.put(entityId, Math.max(0, amount));
        MAX.put(entityId, Math.max(0, max));
    }

    public static int charge(int entityId) {
        return CHARGE.getOrDefault(entityId, 0);
    }

    public static int max(int entityId) {
        return MAX.getOrDefault(entityId, 0);
    }
}
