package com.darkifov.thaumcraft.research;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ResearchRegistry {
    private static final List<ResearchEntry> ENTRIES = new ArrayList<>();
    private static final int ORIGINAL_TC4_ENTRY_COUNT = TC4ResearchRuntimeBridge.size();

    static {
        ENTRIES.addAll(TC4ResearchRuntimeBridge.entries());
        add("FIRST_STEPS", "First Steps", "Craft a Thaumometer and begin scanning magical traces.");
        add("PRIMAL_ASPECTS", "Primal Aspects", "Aer, Terra, Ignis, Aqua, Ordo and Perditio are the roots of magical matter.", "FIRST_STEPS");
        add("ARCANE_STONE", "Arcane Stone", "A stable stone matrix for magical structures.", "PRIMAL_ASPECTS");
        add("THAUMCRAFT_TABLE", "Thaumcraft Table", "A crafted table used for original-style Thaumcraft progression.", "ARCANE_STONE");
        add("THAUMONOMICON", "Thaumonomicon", "A bookshelf transformed by a wand becomes the guide to thaumaturgy.", "FIRST_STEPS");

        add("AURA_NODES", "Aura Nodes", "Nodes store vis and begin the aura system.", "PRIMAL_ASPECTS");
        add("NODE_TYPES", "Node Types", "Pure, tainted and hungry nodes alter the world around them.", "AURA_NODES");
        add("NODE_STABILIZATION", "Node Stabilization", "A stabilizer can calm dangerous node effects.", "NODE_TYPES");

        add("ARCANE_WORKBENCH", "Arcane Workbench", "A wand-transformed Thaumcraft Table with catalyst, ingredients, output and vis.", "THAUMCRAFT_TABLE");
        add("ARCANE_JSON_RECIPES", "Arcane JSON Recipes", "Arcane workbench recipes are synced from server JSON data.", "ARCANE_WORKBENCH");
        add("WANDS", "Wands", "Wand caps, cores and vis storage form the base of controlled magic.", "ARCANE_WORKBENCH");
        add("THAUMIUM", "Thaumium", "Iron touched by primal magic becomes thaumium.", "ARCANE_WORKBENCH");

        add("CRUCIBLE", "Crucible", "A cauldron transformed by a wand becomes a crucible for alchemy.", "THAUMIUM");
        add("ESSENTIA", "Essentia", "Aspects can be stored as essentia in phials and jars.", "CRUCIBLE");
        add("ESSENTIA_TRANSPORT", "Essentia Transport", "Alchemical Furnace and Essentia Tubes move essentia into jars.", "ESSENTIA");
        add("ESSENTIA_SUCTION", "Essentia Suction", "Filtered and void jars create stronger suction priority in tube networks.", "ESSENTIA_TRANSPORT");
        add("ESSENTIA_CONTROL", "Essentia Control", "Valves, labels and overlays give better control over essentia transport networks.", "ESSENTIA_SUCTION");

        add("RESEARCH_TABLE", "Research Table", "Scribing Tools and paired tables unlock structured research.", "THAUMONOMICON");
        add("GOGGLES", "Goggles of Revealing", "Goggles reveal magical information more clearly.", "RESEARCH_TABLE");
        add("GOLEMS", "Golemancy", "Small magical servants can be bound to simple tasks.", "RESEARCH_TABLE");
        add("GOLEM_TASKS", "Golem Tasks", "Golems can use seals and tools to perform jobs.", "GOLEMS");

        add("INFUSION", "Infusion", "A dangerous ritual system that combines catalyst, components and essentia.", "ESSENTIA_TRANSPORT");
        add("INFUSION_STABILITY", "Infusion Stability", "Symmetry and stabilizing blocks reduce infusion danger.", "INFUSION");
        add("TIMED_INFUSION", "Timed Infusion", "Infusion now runs over time with progress, instability and possible failure.", "INFUSION_STABILITY");
        add("INFUSION_VISUALS", "Infusion Visuals", "Active infusion shows beams, progress and clearer instability effects.", "TIMED_INFUSION");

        add("WARP", "Warp", "Dangerous magic leaves marks on the mind.", "INFUSION");
        add("WARP_EVENTS", "Warp Events", "Accumulated Warp can trigger whispers, effects, taint leaks and other consequences.", "WARP");
        add("WARP_WARDING", "Warp Warding", "Protective talismans and cleansing soap can delay or soften Warp events.", "WARP_EVENTS");
        add("TAINT", "Taint", "Flux, Warp and dangerous nodes can corrupt the land.", "WARP_EVENTS");

        add("ELDRITCH_WHISPERS", "Eldritch Whispers", "Forbidden thoughts begin the Eldritch path.", "WARP_EVENTS");
        add("ELDRITCH_START", "The Eldritch Door", "Something beyond the world has noticed you.", "ELDRITCH_WHISPERS");
        add("ELDRITCH_ALTAR", "Eldritch Altar", "An altar and obelisk structure can open the first Eldritch portal.", "ELDRITCH_START");
        add("CRIMSON_KEY", "Crimson Key", "A key for forcing the Eldritch door open once the altar is ready.", "ELDRITCH_ALTAR");
        add("ELDRITCH_ARENA", "Eldritch Arena", "The portal can manifest a temporary arena and guardian waves.", "CRIMSON_KEY");
        add("ELDRITCH_GUARDIAN", "Eldritch Guardian", "Survive the portal waves to claim relics from beyond.", "ELDRITCH_ARENA");
        add("AWAKENED_CRIMSON_KEY", "Awakened Crimson Key", "An upgraded key that stabilizes future portal encounters.", "ELDRITCH_GUARDIAN");

        add("BALANCE_CONFIG", "Balance Config", "The mod now has a common tuning config for Warp, Infusion, Essentia and Eldritch systems.", "FIRST_STEPS");
        add("THAUMCRAFT_EXTRAS", "Thaumcraft Extras", "A compatibility/remake branch inspired by the old Thaumcraft Extras addon.", "ARCANE_WORKBENCH");
        add("THAUMCRAFT_EXTRAS_FOCI", "Extra Wand Foci", "Blink, Arrow, Heal, Speed, Return, Exchange, Smelting, Dispel, Destroy and Freeze foci.", "THAUMCRAFT_EXTRAS", "WANDS");
        add("THAUMCRAFT_EXTRAS_PECH", "Pech Trade", "Tiered Pech trade tokens, custom Pech entity, favor, GUI and direct trading.", "THAUMCRAFT_EXTRAS");
        add("THAUMCRAFT_EXTRAS_EXPERIENCE", "Experience Extraction", "Experience Extractor and Focus Experience crystallize XP into shards.", "THAUMCRAFT_EXTRAS");
        add("THAUMCRAFT_EXTRAS_ELEMENTAL_BLOCKS", "Elemental Blocks", "Fire, Air, Water, Earth, Light and Ender blocks return as functional remake blocks.", "THAUMCRAFT_EXTRAS");
        add("THAUMCRAFT_EXTRAS_COMPLETE", "Thaumcraft Extras Complete Pass", "Extra foci now consume vis, Pech tokens trade rewards, XP shards are usable and Ignis Fuel works in the Alchemical Furnace.", "THAUMCRAFT_EXTRAS_FOCI", "THAUMCRAFT_EXTRAS_PECH", "THAUMCRAFT_EXTRAS_EXPERIENCE");
        add("REAL_PECH_TRADE", "Real Pech Trade", "Pech Summon now creates a custom Pech entity, and trade tokens are exchanged directly with it.", "THAUMCRAFT_EXTRAS_COMPLETE");
        add("PECH_FAVOR", "Pech Favor", "Peches now remember your reputation, accept gifts and improve rewards when they like you.", "REAL_PECH_TRADE");
        add("PECH_TRADE_GUI", "Pech Trade GUI", "Peches now open a dedicated trade menu with tier buttons and a gift action.", "PECH_FAVOR");

        add("THAUMIC_ENERGISTICS", "Thaumic Energistics", "A bridge branch for digital essentia storage and Applied Energistics style integration.", "ESSENTIA_CONTROL");
        add("DIGITAL_ESSENTIA_STORAGE", "Digital Essentia Storage", "Essentia can be stored inside digital cells.", "THAUMIC_ENERGISTICS");
        add("THAUMIC_ENERGISTICS_FULL", "Thaumic Energistics Full Mechanics", "Arcane Assembler, providers, wireless terminal and encoded patterns automate Thaumcraft items with digital essentia.", "DIGITAL_ESSENTIA_STORAGE");
        add("ARCANE_ASSEMBLER", "Arcane Assembler", "Encoded patterns can craft arcane/infusion targets using inventory items and nearby digital essentia.", "THAUMIC_ENERGISTICS_FULL");
        add("ESSENTIA_PROVIDERS", "Essentia Providers", "Providers import jars, feed digital storage and assist infusion automation.", "THAUMIC_ENERGISTICS_FULL");
        add("ADDONS_FINAL_COMPLETION", "Addons Final Completion", "Final addon ledger and unified progression for Thaumic Energistics, Thaumic Tinkerer and Thaumcraft Extras.", "THAUMIC_ENERGISTICS_FULL");
        add("THAUMIC_ENERGISTICS_TRUE_PARITY", "Thaumic Energistics True Parity", "Original Thaumic Energistics 1.7.10 blocks, parts, cells, cores and diagnostics mapped into this standalone rebuild.", "ADDONS_FINAL_COMPLETION");
        add("AE2_INTERNAL_PARITY", "AE2 Internal Parity", "Standalone grid nodes, channels, energy acceptor and crafting CPU emulate the old AE2 internals for this port.", "THAUMIC_ENERGISTICS_TRUE_PARITY");
        add("ALL_ADDONS_TRUE_PARITY", "All Addons True Parity", "Unified true parity branch for Thaumic Energistics, Thaumic Tinkerer and Thaumcraft Extras public surfaces.", "AE2_INTERNAL_PARITY");
        add("THAUMIC_TINKERER_TRUE_PARITY", "Thaumic Tinkerer True Parity", "Original TT blocks, foci, KAMI resources, gases, transvector and machine surfaces mapped into the 1.19.2 rebuild.", "ALL_ADDONS_TRUE_PARITY");
        add("THAUMCRAFT_EXTRAS_TRUE_PARITY", "Thaumcraft Extras True Parity", "Original Thaumcraft Extras machines, warded blocks, dark infuser, charger, tools and API materials mapped into the 1.19.2 rebuild.", "ALL_ADDONS_TRUE_PARITY");
        add("ESSENTIA_BUSES", "Essentia Buses", "Import, export, storage and interface blocks move essentia between jars and cells.", "DIGITAL_ESSENTIA_STORAGE");
        add("ESSENTIA_TERMINAL_RESEARCH", "Essentia Terminal", "A terminal reports nearby jars and held digital essentia cells.", "ESSENTIA_BUSES");
        add("ARCANE_PATTERN_ENCODING", "Arcane Pattern Encoding", "Encoded pattern base for Arcane Workbench, Crucible and Infusion automation.", "ESSENTIA_TERMINAL_RESEARCH");
        add("ESSENTIA_TERMINAL_GUI", "Essentia Terminal GUI", "The terminal now opens a GUI with scan actions for adjacent jars and digital cells.", "ESSENTIA_TERMINAL_RESEARCH");
        add("ESSENTIA_DRIVE_RESEARCH", "Essentia Drive", "A drive-like monitor block summarizes digital essentia cells in the player inventory.", "ESSENTIA_TERMINAL_GUI");
        add("ESSENTIA_PARTITIONING", "Essentia Cell Partitioning", "Partition cards prepare cells for aspect-specific storage rules.", "ESSENTIA_DRIVE_RESEARCH");
        add("ESSENTIA_SPEED_CARDS", "Essentia Speed Cards", "AE2-style speed and acceleration cards increase essentia transfer limits.", "ESSENTIA_BUSES");
        add("ADVANCED_AE2_CARDS", "Advanced AE2 Cards", "Acceleration, co-processor and fuzzy cards prepare the system for advanced AE2-style automation.", "ESSENTIA_SPEED_CARDS", "ESSENTIA_PARTITIONING");
        add("REAL_ESSENTIA_DRIVE", "Real Essentia Drive", "The Essentia Drive now has its own inventory for cells and upgrade cards.", "ESSENTIA_DRIVE_RESEARCH", "ESSENTIA_SPEED_CARDS");
        add("ESSENTIA_DRIVE_NETWORK", "Essentia Drive Network", "Import, Export, Interface, Terminal and Monitor can now work with a nearby Essentia Drive.", "REAL_ESSENTIA_DRIVE");
        add("ESSENTIA_TERMINAL_FILTER", "Essentia Terminal Filter", "The terminal can now scan a selected aspect across jars, Drive and player cells.", "ESSENTIA_DRIVE_NETWORK");
        add("ENCODED_PATTERNS", "Encoded Patterns", "Encoded Essentia Patterns now store a pattern type in NBT.", "ARCANE_PATTERN_ENCODING", "ESSENTIA_TERMINAL_FILTER");
        add("AUTOCRAFTING_BASE", "Autocrafting Base", "Pattern Encoder and Pattern Provider prepare the base for AE2-style arcane autocrafting.", "ENCODED_PATTERNS");

        add("THAUMIC_TINKERER", "Thaumic Tinkerer", "A remake branch for extra useful Thaumcraft-style devices, tools and later KAMI-tier content.", "THAUMONOMICON");
        add("OSMOTIC_ENCHANTER", "Osmotic Enchanter", "A structure-based enchantment device inspired by Thaumic Tinkerer.", "THAUMIC_TINKERER");
        add("OSMOTIC_ENCHANTER_GUI", "Osmotic Enchanter GUI", "The Osmotic Enchanter now has a GUI with selectable enchantment buttons.", "OSMOTIC_ENCHANTER");
        add("TT_DEVICES", "Thaumic Tinkerer Devices", "Ethereal Platform and Fume Dissipator begin the utility device branch.", "OSMOTIC_ENCHANTER");
        add("ETHEREAL_PLATFORM_POLISH", "Ethereal Platform Polish", "Ethereal Platform now has solid/phase states, thin collision, redstone lock and particle feedback.", "TT_DEVICES");
        add("FUME_DISSIPATOR_AREA", "Fume Dissipator Area", "Fume Dissipator now cleanses nearby entities and reduces crucible flux in an area.", "TT_DEVICES");
        add("TRANSVECTOR_TOOLS", "Transvector Tools", "A binder and interface prepare the base for remote block interaction.", "TT_DEVICES");
        add("TRANSVECTOR_REMOTE_INTERFACE", "Transvector Remote Interface", "The Transvector Interface can now bind to a target block, save the link and remotely inspect target information.", "TRANSVECTOR_TOOLS");
        add("TRANSVECTOR_ACTIVE_USE", "Transvector Active Use", "The interface can now perform safe remote actions for jars, drives and key thaumic machines.", "TRANSVECTOR_REMOTE_INTERFACE");
        add("TT_UTILITY_ITEMS", "Thaumic Tinkerer Utility Items", "Knowledge sharing, infused scribing tools, bottomless pouch and revealing helmet branch.", "FUME_DISSIPATOR_AREA", "TRANSVECTOR_ACTIVE_USE");
        add("BOTTOMLESS_POUCH_STORAGE", "Bottomless Pouch Storage", "Bottomless Pouch now stores and retrieves stacks in item NBT.", "TT_UTILITY_ITEMS");
        // Stage206 quarantine: removed fake Helmet of Revealing scan research.
        // TC4 reveal gear is passive IRevealer/IGoggles-style behavior, not a research-unlocking scan branch.
        add("KAMI_ICHOR_BASE", "KAMI and Ichor", "Endgame KAMI/Ichor material and gear base branch.", "TT_UTILITY_ITEMS", "ELDRITCH_ARENA");
        add("ICHOR_GEAR_REALIZATION", "Ichor Gear Realization", "Ichor tools and Ichorcloth pieces now have a dedicated gear class with durability, enchantability and effects.", "KAMI_ICHOR_BASE");
        add("KAMI_COMPLETION_GATE", "KAMI Completion Gate", "KAMI Research Core now checks warp and grants eldritch insight as a gated endgame unlock.", "ICHOR_GEAR_REALIZATION", "ELDRITCH_ARENA");
        add("ICHOR_REAL_GEAR", "Real Ichor Gear", "Ichor tools and Ichorcloth armor now use real SwordItem, PickaxeItem and ArmorItem classes.", "ICHOR_GEAR_REALIZATION");
        add("BOTTOMLESS_POUCH_GUI", "Bottomless Pouch GUI", "Bottomless Pouch now has a real 27-slot GUI backed by item NBT.", "BOTTOMLESS_POUCH_STORAGE");
        // Stage206 quarantine: removed fake Helmet of Revealing overlay research node.
        add("TT_TRUE_COMPLETION", "Thaumic Tinkerer True Completion", "True-completion pass: real gear classes, pouch GUI, reveal gear adapter, KAMI gate and build-fix checks.", "ICHOR_REAL_GEAR", "BOTTOMLESS_POUCH_GUI", "TT_UTILITY_ITEMS", "KAMI_COMPLETION_GATE");
        add("MAX_PORT_PARITY", "Maximum Port Parity", "Stage 67 audits every registered item/block across core and addon branches and adds parity tags, ledger and conservative missing recipes.", "TT_TRUE_COMPLETION", "AUTOCRAFTING_BASE", "THAUMCRAFT_EXTRAS_COMPLETE");
        add("DEEP_GAMEPLAY_PARITY", "Deep Gameplay Parity", "Stage 68 gives formerly plain registry items active gameplay behavior: aspect crystals, shards, goggles, research notes, golem seals and TE cards.", "MAX_PORT_PARITY");
        add("MATRIX_AUXILIARIES", "Matrix Auxiliaries", "Adds Infusion Matrix Accelerator and Matrix Stabilization Pylon. Accelerators speed infusion up to x5; powered symmetric pylons stabilize up to 100%.", "INFUSION");
        add("INFUSION_MATRIX_AUXILIARIES", "Infusion Matrix Auxiliaries", "Adds Matrix Accelerator and Matrix Stabilizer blocks for faster and safer infusion altar operation.", "INFUSION", "DEEP_GAMEPLAY_PARITY");
        add("PLAYABLE_RC", "Playable Release Candidate", "A first pass over balance, progression and recipe structure for a playable build.", "BALANCE_CONFIG", "AWAKENED_CRIMSON_KEY");
    }

    private ResearchRegistry() {
    }

    private static void add(String key, String title, String description, String... requirements) {
        if (byKey(key).isPresent()) {
            return;
        }
        ENTRIES.add(new ResearchEntry(key, title, description, requirements));
    }

    public static List<ResearchEntry> entries() {
        return Collections.unmodifiableList(ENTRIES);
    }


    public static List<ResearchEntry> originalEntries() {
        return Collections.unmodifiableList(ENTRIES.subList(0, Math.min(ORIGINAL_TC4_ENTRY_COUNT, ENTRIES.size())));
    }

    public static int originalSize() {
        return ORIGINAL_TC4_ENTRY_COUNT;
    }

    public static int size() {
        return ENTRIES.size();
    }

    public static ResearchEntry get(int index) {
        if (index < 0 || index >= ENTRIES.size()) {
            return ENTRIES.get(0);
        }

        return ENTRIES.get(index);
    }

    public static Optional<ResearchEntry> byKey(String key) {
        if (key == null) {
            return Optional.empty();
        }
        for (ResearchEntry entry : ENTRIES) {
            if (entry.key().equals(key) || entry.key().equalsIgnoreCase(key)) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    public static List<ResearchEntry> entriesForCategory(String category) {
        List<ResearchEntry> result = new ArrayList<>();
        for (ResearchEntry entry : ENTRIES) {
            if (entry.category().equals(category)) {
                result.add(entry);
            }
        }
        return Collections.unmodifiableList(result);
    }

}
