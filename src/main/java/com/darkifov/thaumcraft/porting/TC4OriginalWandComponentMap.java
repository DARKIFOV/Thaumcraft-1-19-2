package com.darkifov.thaumcraft.porting;

import java.util.List;

/** Original TC4 wand cap/rod/staff definitions from ConfigItems. */
public final class TC4OriginalWandComponentMap {
    private TC4OriginalWandComponentMap() {}

    public record CapEntry(String constant, String tag, float baseCostModifier, int craftCost, String specialAspects, float specialCostModifier, String source) {}
    public record RodEntry(String constant, String type, String tag, int capacity, int craftCost, boolean glowing, String primalUpdate, String source) {}

    public static final List<CapEntry> CAPS = List.of(
            new CapEntry("WAND_CAP_IRON", "iron", 1.1F, 1, "", 0.0F, "ConfigItems.java:WAND_CAP_IRON"),
            new CapEntry("WAND_CAP_GOLD", "gold", 1.0F, 3, "", 0.0F, "ConfigItems.java:WAND_CAP_GOLD"),
            new CapEntry("WAND_CAP_THAUMIUM", "thaumium", 0.9F, 6, "", 0.0F, "ConfigItems.java:WAND_CAP_THAUMIUM"),
            new CapEntry("WAND_CAP_VOID", "void", 0.8F, 9, "", 0.0F, "ConfigItems.java:WAND_CAP_VOID"),
            new CapEntry("WAND_CAP_COPPER", "copper", 1.1F, 2, "ORDER,ENTROPY", 1.0F, "ConfigItems.java:WAND_CAP_COPPER"),
            new CapEntry("WAND_CAP_SILVER", "silver", 1.0F, 4, "AIR,EARTH,FIRE,WATER", 0.95F, "ConfigItems.java:WAND_CAP_SILVER")
    );

    public static final List<RodEntry> RODS = List.of(
            new RodEntry("WAND_ROD_WOOD", "WandRod", "wood", 25, 1, false, "", "ConfigItems.java:WAND_ROD_WOOD"),
            new RodEntry("WAND_ROD_GREATWOOD", "WandRod", "greatwood", 50, 3, false, "", "ConfigItems.java:WAND_ROD_GREATWOOD"),
            new RodEntry("WAND_ROD_OBSIDIAN", "WandRod", "obsidian", 75, 6, false, "EARTH", "ConfigItems.java:WAND_ROD_OBSIDIAN"),
            new RodEntry("WAND_ROD_BLAZE", "WandRod", "blaze", 75, 6, true, "FIRE", "ConfigItems.java:WAND_ROD_BLAZE"),
            new RodEntry("WAND_ROD_ICE", "WandRod", "ice", 75, 6, false, "WATER", "ConfigItems.java:WAND_ROD_ICE"),
            new RodEntry("WAND_ROD_QUARTZ", "WandRod", "quartz", 75, 6, false, "ORDER", "ConfigItems.java:WAND_ROD_QUARTZ"),
            new RodEntry("WAND_ROD_BONE", "WandRod", "bone", 75, 6, false, "ENTROPY", "ConfigItems.java:WAND_ROD_BONE"),
            new RodEntry("WAND_ROD_REED", "WandRod", "reed", 75, 6, false, "AIR", "ConfigItems.java:WAND_ROD_REED"),
            new RodEntry("WAND_ROD_SILVERWOOD", "WandRod", "silverwood", 100, 9, false, "", "ConfigItems.java:WAND_ROD_SILVERWOOD"),
            new RodEntry("STAFF_ROD_GREATWOOD", "StaffRod", "greatwood_staff", 125, 8, false, "", "ConfigItems.java:STAFF_ROD_GREATWOOD"),
            new RodEntry("STAFF_ROD_OBSIDIAN", "StaffRod", "obsidian_staff", 175, 14, false, "EARTH", "ConfigItems.java:STAFF_ROD_OBSIDIAN"),
            new RodEntry("STAFF_ROD_BLAZE", "StaffRod", "blaze_staff", 175, 14, true, "FIRE", "ConfigItems.java:STAFF_ROD_BLAZE"),
            new RodEntry("STAFF_ROD_ICE", "StaffRod", "ice_staff", 175, 14, false, "WATER", "ConfigItems.java:STAFF_ROD_ICE"),
            new RodEntry("STAFF_ROD_QUARTZ", "StaffRod", "quartz_staff", 175, 14, false, "ORDER", "ConfigItems.java:STAFF_ROD_QUARTZ"),
            new RodEntry("STAFF_ROD_BONE", "StaffRod", "bone_staff", 175, 14, false, "ENTROPY", "ConfigItems.java:STAFF_ROD_BONE"),
            new RodEntry("STAFF_ROD_REED", "StaffRod", "reed_staff", 175, 14, false, "AIR", "ConfigItems.java:STAFF_ROD_REED"),
            new RodEntry("STAFF_ROD_SILVERWOOD", "StaffRod", "silverwood_staff", 250, 24, false, "", "ConfigItems.java:STAFF_ROD_SILVERWOOD"),
            new RodEntry("STAFF_ROD_PRIMAL", "StaffRod", "primal_staff", 250, 32, true, "ALL_PRIMAL", "ConfigItems.java:STAFF_ROD_PRIMAL")
    );
}
