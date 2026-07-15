#!/usr/bin/env python3
"""Generate the TC4 4.2.3.5 research runtime bridge and exact page index.

The source of truth is the already-extracted ConfigResearch.java mapping committed
under data/thaumcraft/tc4_source_mapping.  Generation is deterministic so CI can
compare the runtime Java with all 201 original research entries without manually
maintaining a second research graph.
"""
from __future__ import annotations

import json
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_original_research_entries_stage116.json"
BRIDGE = ROOT / "src/main/java/com/darkifov/thaumcraft/research/TC4ResearchRuntimeBridge.java"
PAGES = ROOT / "src/main/java/com/darkifov/thaumcraft/research/TC4OriginalResearchPageIndex.java"
METADATA = ROOT / "src/main/java/com/darkifov/thaumcraft/research/TC4ResearchMetadataIndex.java"
# Preserve ConfigResearch.java declaration order exactly.  The methods remain
# split by category only to stay below the JVM 64 KiB bytecode limit.
CATEGORIES = ("THAUMATURGY", "ARTIFICE", "ALCHEMY", "GOLEMANCY", "BASICS", "ELDRITCH")


def js(value: str | None) -> str:
    return json.dumps(value or "", ensure_ascii=False)


def arr(values: list[str] | tuple[str, ...] | None) -> str:
    values = list(values or [])
    if not values:
        return "new String[0]"
    return "new String[] {" + ", ".join(js(v) for v in values) + "}"


def aspects(values: dict[str, int]) -> str:
    if not values:
        return "java.util.Map.of()"
    parts: list[str] = []
    for key, amount in values.items():
        parts.extend((js(key), str(int(amount))))
    return "aspects(" + ", ".join(parts) + ")"


def bridge_text(entries: list[dict[str, Any]]) -> str:
    out: list[str] = [
        "package com.darkifov.thaumcraft.research;",
        "",
        "import java.util.ArrayList;",
        "import java.util.Collections;",
        "import java.util.LinkedHashMap;",
        "import java.util.List;",
        "import java.util.Map;",
        "",
        "/**",
        " * Generated exact runtime index of the 201 ResearchItem declarations in",
        " * Thaumcraft 4.2.3.5 ConfigResearch.java.",
        " *",
        " * <p>Do not edit entries by hand. Run",
        " * {@code tools/generate_tc4_research_runtime_from_source_map.py}.</p>",
        " */",
        "public final class TC4ResearchRuntimeBridge {",
        "    private static final List<ResearchEntry> ENTRIES = build();",
        "",
        "    private TC4ResearchRuntimeBridge() {}",
        "",
        "    public static List<ResearchEntry> entries() { return ENTRIES; }",
        "    public static int size() { return ENTRIES.size(); }",
        "",
        "    public static Map<String, Integer> aspects(Object... pairs) {",
        "        Map<String, Integer> result = new LinkedHashMap<>();",
        "        for (int i = 0; i + 1 < pairs.length; i += 2) {",
        "            result.put((String) pairs[i], (Integer) pairs[i + 1]);",
        "        }",
        "        return result;",
        "    }",
        "",
        "    private static List<ResearchEntry> build() {",
        "        List<ResearchEntry> entries = new ArrayList<>(201);",
    ]
    for category in CATEGORIES:
        out.append(f"        add{category}(entries);")
    out += [
        "        return Collections.unmodifiableList(entries);",
        "    }",
        "",
    ]

    for category in CATEGORIES:
        out.append(f"    private static void add{category}(List<ResearchEntry> entries) {{")
        for e in [x for x in entries if x["category"] == category]:
            out += [
                "        entries.add(new ResearchEntry(",
                f"                {js(e['key'])}, {js(e.get('title'))}, {js(e.get('subtitle'))},",
                f"                {js(category)}, {int(e.get('display_column', 0))}, {int(e.get('display_row', 0))}, {int(e.get('complexity', 1))},",
                f"                {aspects(e.get('aspects', {}))},",
                f"                {arr(e.get('parents'))},",
                f"                {arr(e.get('parents_hidden'))},",
                f"                {arr(e.get('siblings'))},",
                f"                {arr(e.get('flags'))},",
                f"                {arr(e.get('page_text_keys'))},",
                f"                {arr(e.get('page_types'))},",
                f"                {arr(e.get('recipe_keys'))},",
                f"                {arr(e.get('entity_triggers'))},",
                f"                {arr(e.get('aspect_triggers'))},",
                f"                {int(e.get('warp', 0))}));",
            ]
        out += ["    }", ""]
    out += ["}", ""]
    return "\n".join(out)


def page_text(entries: list[dict[str, Any]]) -> str:
    out: list[str] = [
        "package com.darkifov.thaumcraft.research;",
        "",
        "import java.util.ArrayList;",
        "import java.util.Collections;",
        "import java.util.LinkedHashMap;",
        "import java.util.List;",
        "import java.util.Locale;",
        "import java.util.Map;",
        "",
        "/** Exact ordered ResearchPage declarations extracted from TC4 4.2.3.5. */",
        "public final class TC4OriginalResearchPageIndex {",
        "    public record PageSpec(String type, String textKey, String unlockResearch,",
        "                           String rawExpression, String[] recipeKeys) {",
        "        public PageSpec {",
        "            type = type == null ? \"TEXT\" : type;",
        "            textKey = textKey == null ? \"\" : textKey;",
        "            unlockResearch = unlockResearch == null ? \"\" : unlockResearch;",
        "            rawExpression = rawExpression == null ? \"\" : rawExpression;",
        "            recipeKeys = recipeKeys == null ? new String[0] : recipeKeys.clone();",
        "        }",
        "",
        "        @Override public String[] recipeKeys() { return recipeKeys.clone(); }",
        "",
        "        public boolean isRecipePage() {",
        "            String upper = type.toUpperCase(Locale.ROOT);",
        "            return upper.contains(\"CRAFT\") || upper.contains(\"RECIPE\")",
        "                    || upper.contains(\"INFUSION\") || upper.contains(\"CRUCIBLE\")",
        "                    || upper.contains(\"SMELT\") || upper.contains(\"COMPOUND\")",
        "                    || upper.contains(\"ITEMSTACK\") || upper.equals(\"UNKNOWN\");",
        "        }",
        "    }",
        "",
        "    private static final Map<String, List<PageSpec>> PAGES = build();",
        "",
        "    private TC4OriginalResearchPageIndex() {}",
        "",
        "    public static List<PageSpec> pages(String researchKey) {",
        "        if (researchKey == null) return List.of();",
        "        return PAGES.getOrDefault(researchKey, List.of());",
        "    }",
        "",
        "    public static int researchCount() { return PAGES.size(); }",
        "    public static int pageCount() {",
        "        int count = 0;",
        "        for (List<PageSpec> pages : PAGES.values()) count += pages.size();",
        "        return count;",
        "    }",
        "",
        "    private static PageSpec page(String type, String textKey, String unlockResearch,",
        "                                 String rawExpression, String... recipeKeys) {",
        "        return new PageSpec(type, textKey, unlockResearch, rawExpression, recipeKeys);",
        "    }",
        "",
        "    private static void put(Map<String, List<PageSpec>> map, String key, PageSpec... pages) {",
        "        List<PageSpec> copy = new ArrayList<>(pages.length);",
        "        Collections.addAll(copy, pages);",
        "        map.put(key, Collections.unmodifiableList(copy));",
        "    }",
        "",
        "    private static Map<String, List<PageSpec>> build() {",
        "        Map<String, List<PageSpec>> map = new LinkedHashMap<>(201);",
    ]
    for category in CATEGORIES:
        out.append(f"        add{category}(map);")
    out += [
        "        return Collections.unmodifiableMap(map);",
        "    }",
        "",
    ]
    for category in CATEGORIES:
        out.append(f"    private static void add{category}(Map<String, List<PageSpec>> map) {{")
        for e in [x for x in entries if x["category"] == category]:
            pages = e.get("pages", []) or []
            if not pages:
                out.append(f"        put(map, {js(e['key'])});")
                continue
            out.append(f"        put(map, {js(e['key'])},")
            for i, page_data in enumerate(pages):
                comma = "," if i < len(pages) - 1 else ");"
                recipe_keys = page_data.get("recipe_keys", []) or []
                args = ", ".join(js(v) for v in recipe_keys)
                tail = (", " + args) if args else ""
                raw_expression = str(page_data.get("raw", "")).replace('"', "'")
                out.append(
                    "                page("
                    + ", ".join((
                        js(page_data.get("type", "TEXT")),
                        js(page_data.get("text_key", "")),
                        js(page_data.get("unlock_research", "")),
                        js(raw_expression),
                    ))
                    + tail
                    + ")"
                    + comma
                )
        out += ["    }", ""]
    out += ["}", ""]
    return "\n".join(out)


def metadata_text(entries: list[dict[str, Any]]) -> str:
    auto = [e["key"] for e in entries if "auto_unlock" in e.get("flags", [])]
    out: list[str] = [
        "package com.darkifov.thaumcraft.research;",
        "",
        "import java.util.ArrayList;",
        "import java.util.Collections;",
        "import java.util.LinkedHashMap;",
        "import java.util.LinkedHashSet;",
        "import java.util.List;",
        "import java.util.Locale;",
        "import java.util.Map;",
        "import java.util.Set;",
        "",
        "/** Generated flags, triggers and warp from TC4 4.2.3.5 ConfigResearch.java. */",
        "public final class TC4ResearchMetadataIndex {",
        f"    private static final Set<String> AUTO_UNLOCK = set({', '.join(js(v) for v in auto)});",
        "    private static final Map<String, List<String>> FLAGS = buildFlags();",
        "    private static final Map<String, List<String>> ITEM_TRIGGERS = buildItemTriggers();",
        "    private static final Map<String, List<String>> ENTITY_TRIGGERS = buildEntityTriggers();",
        "    private static final Map<String, List<String>> ASPECT_TRIGGERS = buildAspectTriggers();",
        "    private static final Map<String, Integer> WARP = buildWarp();",
        "    private TC4ResearchMetadataIndex() {}",
        "",
        "    private static Set<String> set(String... values) {",
        "        Set<String> result = new LinkedHashSet<>();",
        "        Collections.addAll(result, values);",
        "        return Collections.unmodifiableSet(result);",
        "    }",
        "",
        "    private static void put(Map<String, List<String>> map, String key, String... values) {",
        "        List<String> list = new ArrayList<>();",
        "        Collections.addAll(list, values);",
        "        map.put(key, Collections.unmodifiableList(list));",
        "    }",
        "",
    ]
    def list_map(name: str, field: str) -> None:
        out.append(f"    private static Map<String, List<String>> {name}() {{")
        out.append("        Map<String, List<String>> map = new LinkedHashMap<>();")
        for e in entries:
            vals = e.get(field, []) or []
            if vals:
                out.append(f"        put(map, {js(e['key'])}, {', '.join(js(v) for v in vals)});")
        out.append("        return Collections.unmodifiableMap(map);")
        out.append("    }")
        out.append("")
    list_map("buildFlags", "flags")
    list_map("buildItemTriggers", "item_triggers_raw")
    list_map("buildEntityTriggers", "entity_triggers")
    list_map("buildAspectTriggers", "aspect_triggers")
    out += [
        "    private static Map<String, Integer> buildWarp() {",
        "        Map<String, Integer> map = new LinkedHashMap<>();",
    ]
    for e in entries:
        if int(e.get("warp", 0)) > 0:
            out.append(f"        map.put({js(e['key'])}, {int(e['warp'])});")
    out += [
        "        return Collections.unmodifiableMap(map);",
        "    }",
        "",
        "    public static Set<String> autoUnlockKeys() { return AUTO_UNLOCK; }",
        "    public static boolean isAutoUnlock(String key) { return AUTO_UNLOCK.contains(normalizeKey(key)); }",
        "    public static List<String> flags(String key) { return FLAGS.getOrDefault(normalizeKey(key), List.of()); }",
        "    public static List<String> itemTriggers(String key) { return ITEM_TRIGGERS.getOrDefault(normalizeKey(key), List.of()); }",
        "    public static List<String> entityTriggers(String key) { return ENTITY_TRIGGERS.getOrDefault(normalizeKey(key), List.of()); }",
        "    public static List<String> aspectTriggers(String key) { return ASPECT_TRIGGERS.getOrDefault(normalizeKey(key), List.of()); }",
        "    public static int warp(String key) { return WARP.getOrDefault(normalizeKey(key), 0); }",
        "    public static Map<String, List<String>> itemTriggerMap() { return ITEM_TRIGGERS; }",
        "    public static Map<String, List<String>> entityTriggerMap() { return ENTITY_TRIGGERS; }",
        "    public static Map<String, List<String>> aspectTriggerMap() { return ASPECT_TRIGGERS; }",
        "",
        "    public static List<String> researchKeysForItemTrigger(String expression) {",
        "        return reverseLookup(ITEM_TRIGGERS, expression, false);",
        "    }",
        "",
        "    public static List<String> researchKeysForEntityTrigger(String entityId) {",
        "        return reverseLookup(ENTITY_TRIGGERS, entityId, false);",
        "    }",
        "",
        "    public static List<String> researchKeysForAspectTrigger(String aspectId) {",
        "        return reverseLookup(ASPECT_TRIGGERS, aspectId, true);",
        "    }",
        "",
        "    private static List<String> reverseLookup(Map<String, List<String>> source, String value, boolean aspect) {",
        "        String normalized = aspect ? normalizeAspect(value) : normalizeTrigger(value);",
        "        List<String> result = new ArrayList<>();",
        "        for (Map.Entry<String, List<String>> entry : source.entrySet()) {",
        "            for (String trigger : entry.getValue()) {",
        "                String candidate = aspect ? normalizeAspect(trigger) : normalizeTrigger(trigger);",
        "                if (candidate.equals(normalized)) { result.add(entry.getKey()); break; }",
        "            }",
        "        }",
        "        return result;",
        "    }",
        "",
        "    private static String normalizeKey(String value) { return value == null ? \"\" : value.trim(); }",
        "    private static String normalizeAspect(String value) { return value == null ? \"\" : value.trim(); }",
        "    private static String normalizeTrigger(String value) {",
        "        return value == null ? \"\" : value.replace(\" \", \"\").trim().toLowerCase(Locale.ROOT);",
        "    }",
        "}",
        "",
    ]
    return "\n".join(out)

def main() -> None:
    entries = json.loads(SOURCE.read_text(encoding="utf-8"))
    if len(entries) != 201:
        raise SystemExit(f"Expected 201 original research entries, got {len(entries)}")
    keys = [e["key"] for e in entries]
    if len(keys) != len(set(keys)):
        raise SystemExit("Duplicate original research keys")
    if set(e["category"] for e in entries) != set(CATEGORIES):
        raise SystemExit("Original category set drifted")
    outputs = {
        BRIDGE: bridge_text(entries),
        PAGES: page_text(entries),
        METADATA: metadata_text(entries),
    }
    import sys
    check = "--check" in sys.argv[1:]
    drift = []
    for path, content in outputs.items():
        current = path.read_text(encoding="utf-8") if path.exists() else ""
        if current != content:
            drift.append(str(path.relative_to(ROOT)))
            if not check:
                path.write_text(content, encoding="utf-8", newline="\n")
        if not check:
            print(f"generated {path.relative_to(ROOT)}")
    if check and drift:
        raise SystemExit("generated research runtime drift: " + ", ".join(drift))
    if check:
        print("TC4 research runtime generation check: PASS")


if __name__ == "__main__":
    main()
