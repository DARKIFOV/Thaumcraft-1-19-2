package com.darkifov.thaumcraft.porting;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stage139: whole-Thaumcraft strict parity sweep marker.
 *
 * This class is intentionally runtime-safe: it does not load old 1.7.10 FML/MCP
 * classes, but it records which original TC4 systems are being actively treated
 * as source-of-truth in the 1.19.2 port. It is used by audits/docs and can be
 * surfaced by debug ledgers without pretending that unfinished systems are done.
 */
public final class TC4Stage139WholePortSweep {
    private static final Map<String, String> SYSTEM_STATUS = buildStatus();

    private TC4Stage139WholePortSweep() {
    }

    private static Map<String, String> buildStatus() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("aspects", "runtime: original Aspect.java + ConfigAspects object/entity tags are active");
        map.put("research", "runtime: original categories/entries/parents/pages are active; note puzzle tightened to direct TC4 links");
        map.put("thaumonomicon", "runtime: TC4 book/browser textures, categories, coordinates and recipe pages active");
        map.put("arcane_workbench", "runtime: wand slot, research lock, vis costs and exact shaped key maps active");
        map.put("crucible", "runtime: boiling water, tossed items, flux goo/gas, renderer and spill remnants active");
        map.put("infusion", "runtime: matrix cycle, essentia drain, pedestal consumption, instability and altar scan active");
        map.put("wands", "runtime: rod/cap capacity, cap modifiers, focus pouch, casting and vis recharge active");
        map.put("aura_nodes", "runtime: node types/modifiers, jar, stabilizer, transducer, relay and recharge active");
        map.put("textures", "audit: all model/blockstate texture references must resolve before a stage is accepted");
        map.put("sounds", "runtime/audit: original TC4 sound files are preserved and registered");
        map.put("remaining_major_work", "golemancy, eldritch dimension/boss flow, exact focus upgrade UI, remaining meta recipes and full tile renderers");
        return Collections.unmodifiableMap(map);
    }

    public static Map<String, String> systemStatus() {
        return SYSTEM_STATUS;
    }

    public static boolean hasStage139ArcaneExactPatternKeySupport() {
        return true;
    }

    public static boolean hasStage139DirectResearchAspectLinks() {
        return true;
    }

    public static String summary() {
        return "Stage139 whole-port sweep: exact arcane pattern keys, direct TC4 research-note aspect links, "
                + "case-insensitive original research keys, texture/sound/core-loop audits.";
    }
}
