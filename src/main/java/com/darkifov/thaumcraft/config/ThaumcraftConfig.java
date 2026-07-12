package com.darkifov.thaumcraft.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ThaumcraftConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue WARP_EVENT_MIN_CHANCE;
    public static final ForgeConfigSpec.IntValue WARP_EVENT_MAX_CHANCE;
    public static final ForgeConfigSpec.IntValue WARP_EVENT_CHANCE_PER_WARP;
    public static final ForgeConfigSpec.IntValue WARP_EVENT_COOLDOWN_MIN_TICKS;
    public static final ForgeConfigSpec.IntValue WARP_EVENT_COOLDOWN_RANDOM_TICKS;
    public static final ForgeConfigSpec.IntValue WARP_WARD_SECONDS;
    public static final ForgeConfigSpec.IntValue SANITY_SOAP_WARP_REMOVE;
    public static final ForgeConfigSpec.IntValue SANITY_SOAP_COOLDOWN_SECONDS;

    public static final ForgeConfigSpec.IntValue INFUSION_BASE_DURATION;
    public static final ForgeConfigSpec.IntValue INFUSION_COMPONENT_DURATION;
    public static final ForgeConfigSpec.IntValue INFUSION_INSTABILITY_DURATION;
    public static final ForgeConfigSpec.IntValue INFUSION_SYMMETRY_PENALTY_DURATION;

    public static final ForgeConfigSpec.IntValue ELDRITCH_PORTAL_BASE_STABILITY;
    public static final ForgeConfigSpec.IntValue ELDRITCH_PORTAL_AWAKENED_STABILITY;
    public static final ForgeConfigSpec.IntValue ELDRITCH_PORTAL_STABILITY_DRAIN;
    public static final ForgeConfigSpec.IntValue ELDRITCH_PORTAL_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.IntValue ELDRITCH_PORTAL_FAIL_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.IntValue ELDRITCH_ENCOUNTER_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue ELDRITCH_PORTAL_REQUIRED_WARP;
    public static final ForgeConfigSpec.IntValue ELDRITCH_PORTAL_REQUIRED_ATTUNEMENT;

    public static final ForgeConfigSpec.IntValue ESSENTIA_TUBE_TRANSFER_INTERVAL_TICKS;
    public static final ForgeConfigSpec.IntValue ESSENTIA_TUBE_MAX_NETWORK;

    public static final ForgeConfigSpec.IntValue RUNIC_SHIELD_RECHARGE_MS;
    public static final ForgeConfigSpec.IntValue RUNIC_SHIELD_WAIT_TICKS;
    public static final ForgeConfigSpec.IntValue RUNIC_SHIELD_COST;

    public static final ForgeConfigSpec.BooleanValue CHAMPION_MOBS;
    public static final ForgeConfigSpec.BooleanValue WAND_DIAL_BOTTOM;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("warp");
        WARP_EVENT_MIN_CHANCE = builder.comment("Minimum Warp event chance percent when event check runs.").defineInRange("eventMinChance", 2, 0, 100);
        WARP_EVENT_MAX_CHANCE = builder.comment("Maximum Warp event chance percent when event check runs.").defineInRange("eventMaxChance", 42, 0, 100);
        WARP_EVENT_CHANCE_PER_WARP = builder.comment("Percent chance added per Warp point.").defineInRange("eventChancePerWarp", 2, 0, 20);
        WARP_EVENT_COOLDOWN_MIN_TICKS = builder.comment("Minimum cooldown between Warp events in ticks.").defineInRange("eventCooldownMinTicks", 20 * 30, 0, 20 * 60 * 30);
        WARP_EVENT_COOLDOWN_RANDOM_TICKS = builder.comment("Additional random cooldown between Warp events in ticks.").defineInRange("eventCooldownRandomTicks", 20 * 30, 0, 20 * 60 * 30);
        WARP_WARD_SECONDS = builder.comment("Warp Ward Talisman duration in seconds.").defineInRange("wardDurationSeconds", 60 * 5, 10, 60 * 60);
        SANITY_SOAP_WARP_REMOVE = builder.comment("How much Warp Sanity Soap removes.").defineInRange("sanitySoapWarpRemove", 1, 0, 50);
        SANITY_SOAP_COOLDOWN_SECONDS = builder.comment("How long Sanity Soap delays Warp events in seconds.").defineInRange("sanitySoapEventDelaySeconds", 60 * 2, 0, 60 * 30);
        builder.pop();

        builder.push("infusion");
        INFUSION_BASE_DURATION = builder.comment("Base timed infusion duration in ticks.").defineInRange("baseDurationTicks", 160, 20, 20 * 60 * 20);
        INFUSION_COMPONENT_DURATION = builder.comment("Extra infusion duration per component.").defineInRange("componentDurationTicks", 35, 0, 20 * 60);
        INFUSION_INSTABILITY_DURATION = builder.comment("Extra infusion duration per recipe instability.").defineInRange("instabilityDurationTicks", 20, 0, 20 * 60);
        INFUSION_SYMMETRY_PENALTY_DURATION = builder.comment("Extra infusion duration per symmetry penalty.").defineInRange("symmetryPenaltyDurationTicks", 15, 0, 20 * 60);
        builder.pop();

        builder.push("eldritch");
        ELDRITCH_PORTAL_BASE_STABILITY = builder.comment("Portal stability for normal Crimson Key arena.").defineInRange("portalBaseStability", 90, 1, 1000);
        ELDRITCH_PORTAL_AWAKENED_STABILITY = builder.comment("Portal stability for Awakened Crimson Key arena.").defineInRange("portalAwakenedStability", 120, 1, 1000);
        ELDRITCH_PORTAL_STABILITY_DRAIN = builder.comment("Stability drain per interval during arena.").defineInRange("portalStabilityDrain", 8, 0, 200);
        ELDRITCH_PORTAL_COOLDOWN_SECONDS = builder.comment("Cooldown after completed Eldritch arena.").defineInRange("portalCooldownSeconds", 60 * 5, 0, 60 * 60);
        ELDRITCH_PORTAL_FAIL_COOLDOWN_SECONDS = builder.comment("Cooldown after failed Eldritch arena.").defineInRange("portalFailCooldownSeconds", 60 * 4, 0, 60 * 60);
        ELDRITCH_ENCOUNTER_DURATION_TICKS = builder.comment("Duration needed to survive Eldritch arena.").defineInRange("encounterDurationTicks", 520, 100, 20 * 60 * 30);
        ELDRITCH_PORTAL_REQUIRED_WARP = builder.comment("Warp required to start portal arena.").defineInRange("requiredWarp", 18, 0, 1000);
        ELDRITCH_PORTAL_REQUIRED_ATTUNEMENT = builder.comment("Eldritch Attunement required to start portal arena.").defineInRange("requiredAttunement", 30, 0, 100);
        builder.pop();

        builder.push("essentia");
        ESSENTIA_TUBE_TRANSFER_INTERVAL_TICKS = builder.comment("How often tubes move essentia.").defineInRange("tubeTransferIntervalTicks", 20, 1, 20 * 60);
        ESSENTIA_TUBE_MAX_NETWORK = builder.comment("Maximum tube blocks in one network scan.").defineInRange("tubeMaxNetwork", 48, 4, 256);
        builder.pop();

        builder.push("champions");
        CHAMPION_MOBS = builder.comment("TC4 Config.championMobs: allow natural champion mob generation; dangerous places can still force a reduced chance when disabled.").define("championMobs", true);
        builder.pop();

        builder.push("client_parity");
        WAND_DIAL_BOTTOM = builder.comment("TC4 Config.wand_dial_bottom: place the 32x32 wand vis dial at the bottom-left instead of its original top-left default.").define("wandDialBottom", false);
        builder.pop();

        builder.push("runic_shielding");
        RUNIC_SHIELD_RECHARGE_MS = builder.comment("TC4 Config.shieldRecharge: milliseconds between runic shield charge restores; charged ring variants subtract 500ms each, min 500.").defineInRange("runicRechargeMs", 2000, 500, 60_000);
        RUNIC_SHIELD_WAIT_TICKS = builder.comment("TC4 Config.shieldWait: delay in ticks before recharge resumes after the shield is depleted.").defineInRange("runicRechargeDelayTicks", 80, 0, 20 * 60);
        RUNIC_SHIELD_COST = builder.comment("TC4 Config.shieldCost: Aer + Terra vis consumed from inventory wands per restored shield point.").defineInRange("runicCost", 50, 0, 10_000);
        builder.pop();

        SPEC = builder.build();
    }

    private ThaumcraftConfig() {
    }
}
