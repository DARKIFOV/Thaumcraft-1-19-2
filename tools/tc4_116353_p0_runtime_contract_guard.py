#!/usr/bin/env python3
"""v11.63.53 P0 dedicated-server runtime contract guard."""
from __future__ import annotations
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding="utf-8")

def req(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"TC4 v11.63.53 P0 runtime contract guard: FAIL: {message}")

def main() -> None:
    req(any(f"version = '{v}'" in read("build.gradle") for v in ('11.63.53','11.63.54','11.63.55','11.63.56', '11.63.58','11.63.59')), "build version")
    req(any(f'version="{v}"' in read("src/main/resources/META-INF/mods.toml") for v in ('11.63.53','11.63.54','11.63.55','11.63.56', '11.63.58','11.63.59')), "mods.toml version")

    tests = read("src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java")
    required = {
        "auraNodeLegacyItemConvertsStoredNodeNbt",
        "boneBowRetainsOriginalFastChargeContract",
        "travelingTrunkPersistsInventoryUpgradeAndCapability",
        "crimsonCultistRolesKeepAttributesAndPersistence",
        "fortressArmorSetAndMaskNbtMatchTc4",
        "exactDuplicateLegacyItemAliasesAreNotRegistered",
    }
    for method in required:
        req(method in tests, f"missing GameTest method {method}")
    req(tests.count("@GameTest(") >= 24, "expected at least 24 required GameTests")

    aura = read("src/main/java/com/darkifov/thaumcraft/block/AuraNodeLegacyItem.java")
    req("public static ItemStack convertLegacyStack" in aura, "deterministic aura-node migration API")
    req("TC4NodeJarRuntime.TAG_NODE_JAR" in aura, "node-jar NBT bridge")
    req("legacy.hasCustomHoverName()" in aura, "custom-name preservation")
    req("return ItemStack.EMPTY;" in aura, "invalid-payload rejection")

    java_root = ROOT / "src/main/java"
    forbidden = ("de.keksuccino.fancymenu.mixin", "ContainerWidgetPointerTracker")
    for path in java_root.rglob("*.java"):
        text = path.read_text(encoding="utf-8")
        for token in forbidden:
            req(token not in text, f"direct FancyMenu mixin reference in {path.relative_to(ROOT)}")

    manifest = json.loads(read("runtime_artifacts/runtime_test_manifest.template.json"))
    req(manifest.get("version") in {"11.63.53", "11.63.54", "11.63.55", "11.63.56", "11.63.58", "11.63.59", "11.63.60", "11.63.61", "11.63.62", "11.63.63", "11.63.64", "11.63.65"}, "runtime manifest version")
    req(len(manifest.get("tests", [])) >= 352, "runtime manifest scenario count")
    ids = {entry.get("id") for entry in manifest.get("tests", [])}
    expected_ids = {
        "gametest.aura_node_legacy_item_conversion",
        "gametest.bone_bow_fast_charge_contract",
        "gametest.traveling_trunk_persistence_contract",
        "gametest.crimson_cultist_role_contract",
        "gametest.fortress_armor_set_mask_contract",
    }
    req(expected_ids <= ids, "missing P0 runtime scenarios")

    for workflow in (".github/workflows/build.yml", ".github/workflows/release.yml"):
        text = read(workflow)
        req(any(f"--version {v}" in text for v in ("11.63.53", "11.63.54", "11.63.55", "11.63.56", "11.63.58", "11.63.59", "11.63.60", "11.63.61", "11.63.62")), f"{workflow}: manifest version")
        req("tc4_116353_p0_runtime_contract_guard.py" in text, f"{workflow}: guard wiring")

    print(f"TC4 v11.63.53 P0 runtime contract guard: PASS ({tests.count('@GameTest(')} GameTests, {len(manifest.get('tests', []))} scenarios, no direct FancyMenu mixin references)")

if __name__ == "__main__":
    main()
