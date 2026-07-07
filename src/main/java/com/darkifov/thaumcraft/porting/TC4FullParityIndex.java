package com.darkifov.thaumcraft.porting;

import java.util.List;

/**
 * Stage194 whole-Thaumcraft parity index.
 * This is an explicit runtime-visible ledger of the port, not a substitute for
 * actual porting. It prevents us from losing systems while moving stage by
 * stage toward TC4 1.7.10 behavior on Forge 1.19.2.
 */
public final class TC4FullParityIndex {
    private TC4FullParityIndex() {}

    public record SystemStatus(String system, String status, String note) {}

    public static final int ORIGINAL_TC4_SOURCE_FILES = 859;
    public static final int CURRENT_PORT_JAVA_FILES = 322;
    public static final int ORIGINAL_TC4_PNG_TEXTURES = 820;
    public static final int CURRENT_PORT_PNG_TEXTURES = 3474;
    public static final int ORIGINAL_TC4_SOUND_FILES = 111;
    public static final int CURRENT_PORT_SOUND_FILES = 222;
    public static final int ORIGINAL_TC4_SOUND_EVENTS = 66;

    public static final List<SystemStatus> SYSTEMS = List.of(
            new SystemStatus("ASPECTS", "runtime", "48 TC4 aspects + object/entity tags ported"),
            new SystemStatus("OBJECT_TAGS", "runtime", "ConfigAspects object/entity tags loaded from TC4 mappings"),
            new SystemStatus("RESEARCH_DATA", "runtime", "201 TC4 research entries and 6 categories loaded"),
            new SystemStatus("THAUMONOMICON_BROWSER", "runtime", "TC4-style research browser/pages present, still needs exact item-stack rendering for every page"),
            new SystemStatus("RESEARCH_NOTE", "runtime", "paper + scribing tools targeted research note workflow present"),
            new SystemStatus("RESEARCH_ITEMS", "runtime", "metadata split into 1.19.2 tc4_* research/component items"),
            new SystemStatus("RECIPES", "partial", "ConfigRecipes materialized partially; unresolved metadata remains"),
            new SystemStatus("INFUSION", "runtime", "matrix/pedestal/essentia/component cycle, structure and instability events present"),
            new SystemStatus("ARCANE_WORKBENCH", "runtime-audited", "Stage193/194: original slot/order/output-take flow active; browser-era packets/screens removed; slot 11 is migration-only"),
            new SystemStatus("CRUCIBLE_ALCHEMY", "runtime", "heat, boil, thrown items, renderer, spill remnants and flux world blocks present; exact FX refinements pending"),
            new SystemStatus("ESSENTIA_TRANSPORT", "partial", "jars/tubes exist; needs exact suction network from TC4"),
            new SystemStatus("AURA_NODES_VIS", "runtime", "node generation, types, modifiers, stabilizer/transducer, Node Jar and energized relay recharge chain present"),
            new SystemStatus("WANDS_FOCI", "runtime-audited", "Stage171-192: original focus NBT/upgrades, pouch/equip, WandManager/IArchitect, projectiles, continuous use, renderer and crafting regression audited"),
            new SystemStatus("GOLEMS", "partial", "entities/items exist; needs TC4 golem AI, cores, upgrades and seals parity"),
            new SystemStatus("ELDRITCH", "partial", "blocks/entities exist; eldritch progression/dim/rituals not complete"),
            new SystemStatus("TAINT_FLUX", "runtime", "Stage145: TC4 blockTaint metadata split, taint fibres age bridge, spread adjacency logic and output texture guard present; biome conversion and all taint mobs still pending"),
            new SystemStatus("ENTITIES", "partial", "some entities exist; must port all TC4 mobs/projectiles/trades"),
            new SystemStatus("SOUNDS", "runtime", "all TC4 sounds.json events registered in Stage124"),
            new SystemStatus("TEXTURES", "audited", "all model texture refs verified; Stage145 verifies required textures are present inside the GitHub output jar"),
            new SystemStatus("LOCALIZATION", "partial", "original lang files preserved; 1.19.2 lang generated partially")
    );

    public static long completedOrRuntimeCount() {
        return SYSTEMS.stream().filter(s -> s.status().equals("runtime") || s.status().equals("audited")).count();
    }

    public static long partialCount() {
        return SYSTEMS.stream().filter(s -> s.status().equals("partial")).count();
    }
}
