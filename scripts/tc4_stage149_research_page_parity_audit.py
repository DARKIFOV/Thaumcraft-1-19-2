#!/usr/bin/env python3
from __future__ import annotations

import json
import re
import sys
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[1]


def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding="utf-8", errors="ignore")


def exists(rel: str) -> bool:
    return (ROOT / rel).exists()


def split_top_level(value: str) -> list[str]:
    out: list[str] = []
    start = 0
    depth = 0
    in_string = False
    escape = False
    for i, char in enumerate(value):
        if in_string:
            if escape:
                escape = False
            elif char == "\\":
                escape = True
            elif char == '"':
                in_string = False
        else:
            if char == '"':
                in_string = True
            elif char in "({[":
                depth += 1
            elif char in ")}]":
                depth -= 1
            elif char == "," and depth == 0:
                out.append(value[start:i].strip())
                start = i + 1
    out.append(value[start:].strip())
    return out


def string_array(arg: str) -> list[str]:
    if arg.strip() == "new String[0]":
        return []
    return re.findall(r'"([^"]*)"', arg)


def research_entry_bodies(source: str) -> list[str]:
    bodies: list[str] = []
    idx = 0
    while True:
        match = re.search(r"new ResearchEntry\(", source[idx:])
        if not match:
            return bodies
        start = idx + match.start()
        pos = source.find("(", start) + 1
        depth = 1
        i = pos
        in_string = False
        escape = False
        while i < len(source) and depth:
            char = source[i]
            if in_string:
                if escape:
                    escape = False
                elif char == "\\":
                    escape = True
                elif char == '"':
                    in_string = False
            else:
                if char == '"':
                    in_string = True
                elif char in "({[":
                    depth += 1
                elif char in ")}]":
                    depth -= 1
            i += 1
        bodies.append(source[pos:i - 1])
        idx = i


def is_recipe_like(page_type: str) -> bool:
    upper = (page_type or "").upper()
    return any(token in upper for token in ["CRAFT", "RECIPE", "INFUSION", "CRUCIBLE", "SMELT", "COMPOUND", "ITEMSTACK_PAGE"])


def current_pages() -> dict[str, list[dict[str, str]]]:
    source = read("src/main/java/com/darkifov/thaumcraft/research/TC4ResearchRuntimeBridge.java")
    result: dict[str, list[dict[str, str]]] = {}
    for body in research_entry_bodies(source):
        args = split_top_level(body)
        key_match = re.match(r'"([^"]+)"', args[0])
        if not key_match:
            continue
        key = key_match.group(1)
        text_keys = string_array(args[12])
        page_types = string_array(args[13])
        recipe_keys = string_array(args[14])
        text_index = 0
        recipe_index = 0
        pages: list[dict[str, str]] = []
        for page_type in page_types:
            if is_recipe_like(page_type):
                recipe_key = recipe_keys[recipe_index] if recipe_index < len(recipe_keys) else ""
                pages.append({"type": page_type, "text_key": "", "recipe_key": recipe_key})
                recipe_index += 1
            else:
                text_key = text_keys[text_index] if text_index < len(text_keys) else ""
                pages.append({"type": page_type, "text_key": text_key, "recipe_key": ""})
                text_index += 1
        result[key] = pages
    return result


build = read("build.gradle")
mods = read("src/main/resources/META-INF/mods.toml")
workflow = read(".github/workflows/main.yml")
guard = read("scripts/github_ci_guard.py")
bridge = read("src/main/java/com/darkifov/thaumcraft/research/TC4ResearchRuntimeBridge.java")
screen = read("src/main/java/com/darkifov/thaumcraft/client/screen/TC4ResearchPageScreen.java")
status = read("docs/ORIGINAL_TC4_PORTING_STATUS.md") if exists("docs/ORIGINAL_TC4_PORTING_STATUS.md") else ""
prompt = read("docs/NEXT_CHAT_PROMPT_STAGE149.md") if exists("docs/NEXT_CHAT_PROMPT_STAGE149.md") else ""
expected_path = ROOT / "src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_original_research_pages_stage149.json"
expected_items: list[dict[str, Any]] = json.loads(expected_path.read_text(encoding="utf-8")) if expected_path.exists() else []
expected = {entry["key"]: entry["pages"] for entry in expected_items}
current = current_pages()

missing_keys = sorted(set(expected) - set(current))
extra_keys = sorted(set(current) - set(expected))
page_mismatches: list[dict[str, Any]] = []
for key, expected_pages in expected.items():
    actual_pages = current.get(key)
    if actual_pages != expected_pages:
        page_mismatches.append({"key": key, "expected": expected_pages, "actual": actual_pages})

all_page_types = [page["type"] for pages in current.values() for page in pages]
all_recipe_keys = [page["recipe_key"] for pages in current.values() for page in pages if is_recipe_like(page["type"])]
empty_recipe_slots = [page for pages in current.values() for page in pages if is_recipe_like(page["type"]) and not page["recipe_key"]]

required_exact_pages = {
    "ARCTABLE": [{"type": "TEXT", "text_key": "tc.research_page.ARCTABLE.1", "recipe_key": ""}, {"type": "COMPOUND_CRAFTING", "text_key": "", "recipe_key": "ArcTable"}],
    "RESTABLE": [{"type": "TEXT", "text_key": "tc.research_page.RESTABLE.1", "recipe_key": ""}, {"type": "COMPOUND_CRAFTING", "text_key": "", "recipe_key": "ResTable"}],
    "CRUCIBLE": [
        {"type": "TEXT", "text_key": "tc.research_page.CRUCIBLE.1", "recipe_key": ""},
        {"type": "TEXT", "text_key": "tc.research_page.CRUCIBLE.2", "recipe_key": ""},
        {"type": "TEXT", "text_key": "tc.research_page.CRUCIBLE.3", "recipe_key": ""},
        {"type": "COMPOUND_CRAFTING", "text_key": "", "recipe_key": "Crucible"},
        {"type": "TEXT", "text_key": "tc.research_page.CRUCIBLE.4", "recipe_key": ""},
        {"type": "CRUCIBLE_CRAFTING", "text_key": "", "recipe_key": "RESEARCH:CRUCIBLE"},
        {"type": "TEXT", "text_key": "tc.research_page.CRUCIBLE.5", "recipe_key": ""},
        {"type": "ITEMSTACK_PAGE", "text_key": "", "recipe_key": "new ItemStack(ConfigItems.itemShard, 1, 6)"},
    ],
    "TRAVELTRUNK": expected.get("TRAVELTRUNK", []),
    "COREFISHING": expected.get("COREFISHING", []),
}

checks: dict[str, bool] = {
    "version_stage149_or_later": any((f"version = '{v}'" in build and f'version="{v}"' in mods) for v in ["2.04.0", "2.02.0", "2.00.0", "1.98.0", "1.78.0", "1.76.0", "1.70.0", "1.65.0", "1.64.0", "1.63.0", "1.62.0", "1.61.0", "1.60.0", "1.59.0", "1.58.0", "1.57.0", "1.56.0", "1.55.0", "1.54.0", "1.49.0", "1.50.0", "1.53.0", "1.52.0", "1.51.0"]),
    "workflow_stage149_or_later": "tc4_stage149_research_page_parity_audit.py" in workflow and any(name in workflow for name in ["thaumcraft-legacy-rebuild-stage204-jars", "thaumcraft-legacy-rebuild-stage165-jars", "thaumcraft-legacy-rebuild-stage164-jars", "thaumcraft-legacy-rebuild-stage163-jars", "thaumcraft-legacy-rebuild-stage204-jars", "thaumcraft-legacy-rebuild-stage165-jars", "thaumcraft-legacy-rebuild-stage164-jars", "thaumcraft-legacy-rebuild-stage161-jars", "thaumcraft-legacy-rebuild-stage160-jars", "thaumcraft-legacy-rebuild-stage159-jars", "thaumcraft-legacy-rebuild-stage158-jars", "thaumcraft-legacy-rebuild-stage155-jars", "thaumcraft-legacy-rebuild-stage154-jars", "thaumcraft-legacy-rebuild-stage149-jars", "thaumcraft-legacy-rebuild-stage150-jars", "thaumcraft-legacy-rebuild-stage153-jars", "thaumcraft-legacy-rebuild-stage153-jars", "thaumcraft-legacy-rebuild-stage152-jars", "thaumcraft-legacy-rebuild-stage151-jars"]),
    "github_ci_guard_stage149_or_later": "tc4_stage149_research_page_parity_audit.py" in guard and any(v in guard for v in ["2.04.0", "2.02.0", "2.00.0", "1.98.0", "1.78.0", "1.76.0", "1.70.0", "1.65.0", "1.64.0", "1.63.0", "1.62.0", "1.61.0", "1.60.0", "1.59.0", "1.58.0", "1.57.0", "1.56.0", "1.55.0", "1.54.0", "1.49.0", "1.50.0", "1.53.0", "1.52.0", "1.51.0"]),
    "expected_page_spec_present": expected_path.exists() and len(expected) == 201,
    "current_research_key_count_201": len(current) == 201,
    "no_missing_research_keys": not missing_keys,
    "no_extra_research_keys": not extra_keys,
    "all_pages_match_stage149_spec": not page_mismatches,
    "no_unknown_page_types": "UNKNOWN" not in all_page_types,
    "no_lossy_text_concealed_page_types": "TEXT_CONCEALED" not in all_page_types,
    "gated_pages_keep_original_research_key": all(any(t == f"TEXT_RESEARCH_GATED:{gate}" for t in all_page_types) for gate in ["UPGRADEAIR", "UPGRADEORDER", "UPGRADEFIRE", "UPGRADEWATER", "UPGRADEEARTH", "UPGRADEENTROPY", "ARCANEBORE"]),
    "compound_pages_preserved": all(t in all_page_types for t in ["COMPOUND_CRAFTING", "ITEMSTACK_PAGE"]),
    "no_empty_recipe_slots": not empty_recipe_slots,
    "screen_handles_gated_pages": "TEXT_RESEARCH_GATED:" in screen and "ClientResearchData.hasResearch(gate)" in screen,
    "screen_handles_compound_and_itemstack_pages": "upper.contains(\"COMPOUND\")" in screen and "ITEMSTACK_PAGE" in screen and "RESEARCH:" in screen,
    "continuation_prompt_stage149": "Stage149" in prompt and "Stage150" in prompt and "строгий оригинальный TC4" in prompt,
    "status_remaining_estimate_updated": any(v in status for v in ["8–31", "8-31", "10–33", "10-33", "20–45", "20-45", "22-47", "45-70", "44-69", "43-68", "42-67", "41-66", "40-65", "39-64", "38-63", "36-61", "34-59", "32-57", "30-55", "28-53", "26–51", "26-51"]),
}

for key, pages in required_exact_pages.items():
    checks[f"exact_page_sequence_{key}"] = current.get(key) == pages

passed = all(checks.values())
report = {
    "stage": 149,
    "goal": "strict original TC4 ConfigResearch ResearchPage order/type/key parity",
    "expected_research_keys": len(expected),
    "current_research_keys": len(current),
    "total_expected_pages": sum(len(pages) for pages in expected.values()),
    "total_current_pages": sum(len(pages) for pages in current.values()),
    "missing_keys": missing_keys,
    "extra_keys": extra_keys,
    "page_mismatches": page_mismatches[:10],
    "empty_recipe_slots": empty_recipe_slots[:10],
    "remaining_stage_estimate_after_stage149": "43-68 stages",
    "checks": checks,
    "passed": passed,
}
print(json.dumps(report, indent=2, ensure_ascii=False))
if not passed:
    for name, ok in checks.items():
        if not ok:
            print(f"::error::Stage149 research page parity audit failed: {name}")
    sys.exit(1)
