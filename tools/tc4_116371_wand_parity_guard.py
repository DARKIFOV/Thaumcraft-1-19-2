#!/usr/bin/env python3
"""v11.63.71 Wand component and regeneration parity guard.

The guard is forward-compatible with v11.63.72 because that package contains
v11.63.71 as an explicitly restored intermediate round.
"""
from pathlib import Path
import json
import re

ROOT = Path(__file__).resolve().parents[1]

def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding="utf-8")

def req(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"TC4 v11.63.71 wand parity guard: FAIL: {message}")

def main() -> None:
    build = read("build.gradle")
    mods = read("src/main/resources/META-INF/mods.toml")
    parity = read("src/main/java/com/darkifov/thaumcraft/wand/TC4WandParity.java")
    rod = read("src/main/java/com/darkifov/thaumcraft/wand/WandRodType.java")
    wand = read("src/main/java/com/darkifov/thaumcraft/block/WandItem.java")
    tests = read("src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java")

    req("version = '11." in build, "build version marker")
    req('version="11.' in mods, "mods version marker")
    for token in (
        'CONTRACT_VERSION = "11.64.32"',
        "ROD_TYPE_COUNT = 19",
        "CRAFTABLE_ROD_COUNT = 18",
        "CAP_TYPE_COUNT = 7",
        "CRAFTABLE_CAP_COUNT = 6",
        "rodCatalogueMatchesTc4",
        "capCatalogueAndModifiersMatchTc4",
        "elementalRegenerationMatchesTc4",
    ):
        req(token in parity, f"missing parity token {token}")

    for token in (
        "ELEMENTAL_REGEN_INTERVAL_TICKS = 200",
        "PRIMAL_STAFF_REGEN_INTERVAL_TICKS = 50",
        "REGEN_THRESHOLD_DIVISOR = 10",
        "REGEN_AMOUNT_VIS = 1",
    ):
        req(token in rod, f"missing rod production constant {token}")

    for token in (
        "TC4WandComponentMath.regenerationThresholdCentivis",
        "WandRodType.PRIMAL_STAFF_REGEN_INTERVAL_TICKS",
        "WandRodType.ELEMENTAL_REGEN_INTERVAL_TICKS",
        "WandRodType.REGEN_AMOUNT_VIS",
    ):
        req(token in wand, f"WandItem does not consume {token}")

    for method in (
        "wandRodCatalogueMatchesTc4",
        "wandCapModifiersMatchTc4",
        "wandElementalRodRegenerationMatchesTc4",
    ):
        req(method in tests, f"missing GameTest {method}")

    game_tests = tests.count("@GameTest(")
    req(game_tests >= 85, f"expected at least 85 GameTests, found {game_tests}")
    manifest = json.loads(read("runtime_artifacts/runtime_test_manifest.template.json"))
    req(len(manifest.get("tests", [])) >= 413, "expected at least 413 manifest scenarios")
    print(f"TC4 v11.63.71 wand parity guard: PASS ({game_tests} current GameTests; restored baseline 85/413)")

if __name__ == "__main__":
    main()
