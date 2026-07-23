#!/usr/bin/env python3
"""Guard the loop-expanded TC4 recipe-registration denominator and raw-file exclusions."""
from __future__ import annotations
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ARCANE_DIR = ROOT / "src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench"
MAPPING = ROOT / "tools/data/tc4_arcane_recipe_full_mapping_v11.62.66.json"
EVIDENCE = ROOT / "src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_recipe_registration_denominator_v11_63_61.json"
CLOSURE = ROOT / "src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_final_recipe_closure_v11_63_50.json"


def load(path: Path):
    return json.loads(path.read_text(encoding="utf-8"))


def req(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> int:
    mapping = load(MAPPING)
    evidence = load(EVIDENCE)
    closure = load(CLOSURE)

    req(mapping["literal_shaped_call_sites"] == 84, "literal shaped call-site count")
    req(mapping["expanded_shaped_registrations"] == 104, "expanded shaped count")
    req(mapping["literal_shapeless_call_sites"] == 5, "literal shapeless count")
    req(mapping["expanded_shapeless_registrations"] == 5, "expanded shapeless count")
    req(mapping["expanded_arcane_workbench_registrations_total"] == 109, "expanded Arcane total")
    req(mapping["mapped"] == 109 and not mapping["missing"], "Arcane mapping completeness")
    breakdown = mapping["implementation_breakdown"]
    req(breakdown == {
        "arcane_shaped_datapack_json": 99,
        "arcane_shaped_generated_java": 5,
        "arcane_shapeless_datapack_json": 5,
        "arcane_shapeless_generated_java": 0,
    }, "Arcane implementation breakdown")

    mapped_json = {
        entry["implementation_path"]
        for entry in mapping["registrations"]
        if entry["implementation"] == "DATAPACK_JSON"
    }
    actual_json = {
        str(path.relative_to(ROOT)).replace("\\", "/")
        for path in ARCANE_DIR.glob("*.json")
    }
    extras = set(actual_json) - mapped_json
    expected_extra_names = set(evidence["arcane_evidence"]["extra_style_compatibility_json"])
    expected_extra_paths = {
        f"src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/{name}.json"
        for name in expected_extra_names
    }
    req(len(mapped_json) == 104, "original Arcane JSON count")
    req(len(actual_json) == 114, "raw Arcane JSON count")
    req(extras == expected_extra_paths, f"unexpected Arcane extras: {sorted(extras ^ expected_extra_paths)}")

    counts = evidence["original_runtime_registrations"]
    req(counts == {
        "crucible": 54,
        "arcane_shaped": 104,
        "arcane_shapeless": 5,
        "arcane_total": 109,
        "infusion": 63,
        "infusion_enchantment": 24,
        "furnace_smelting": 8,
        "total": 258,
        "mapped": 258,
        "missing": 0,
    }, "canonical denominator breakdown")
    req(sum(counts[k] for k in (
        "crucible", "arcane_shaped", "arcane_shapeless", "infusion",
        "infusion_enchantment", "furnace_smelting"
    )) == 258, "denominator arithmetic")

    smelting = evidence["smelting_evidence"]
    req(smelting["furnace_recipe_total"] == 8, "furnace recipe count")
    req(smelting["separate_smelting_bonus_registrations"] == 18, "smelting bonus count")
    req(smelting["smelting_bonus_included_in_recipe_denominator"] is False, "smelting bonus exclusion")

    req(closure["original_recipe_record_count"] == 258, "closure denominator")
    req(closure["previous_exact_runtime_source_record_count"] == 248, "pre-final corrected count")
    req(closure["exact_runtime_source_record_count"] == 258, "closure mapped count")
    req(closure["remaining_unresolved_count"] == 0, "closure unresolved count")
    req(evidence["runtime_verification"]["status"] == "NOT_VERIFIED", "runtime status must remain unverified")

    print("TC4 recipe registration denominator guard: PASS (258/258 STATICALLY MAPPED; Arcane 104+5; smelting bonuses 18 separate; runtime NOT VERIFIED)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
