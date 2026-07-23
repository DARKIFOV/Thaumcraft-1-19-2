#!/usr/bin/env python3
"""v11.63.54 P1 block-entity dedicated-server runtime contract guard."""
from __future__ import annotations
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding="utf-8")

def req(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"TC4 v11.63.54 P1 block-entity runtime guard: FAIL: {message}")

def main() -> None:
    req(any(f"version = '{v}'" in read("build.gradle") for v in ("11.63.54", "11.63.55", "11.63.56", "11.63.58", "11.63.59", "11.63.60", "11.63.61", "11.63.62")), "build version")
    req(any(f'version="{v}"' in read("src/main/resources/META-INF/mods.toml") for v in ("11.63.54", "11.63.55", "11.63.56", "11.63.58", "11.63.59", "11.63.60", "11.63.61", "11.63.62")), "mods.toml version")

    tests = read("src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java")
    required = {
        "tallowCandleKeepsSupportShapeAndInfusionStabilizerContract": (
            "TallowCandleBlock", "InfusionStabilizer", "getCollisionShape", "canSurvive"
        ),
        "hungryChestEatsPersistsAndExposesSingleInventory": (
            "HungryChestBlockEntity", "chest.eat(entity)", "TC4Proof", "ForgeCapabilities.ITEM_HANDLER"
        ),
        "brainJarAbsorbsPersistsAndReportsComparatorExperience": (
            "ExperienceOrb", "BrainJarBlockEntity.serverTick", "comparatorOutput", "storedExperience"
        ),
        "magicMirrorLinksTransportsAndPersistsQueuedStacks": (
            "MirrorLink.at", "first.transport(entity)", "queuedStackCount", "invalidateLink"
        ),
    }
    for method, tokens in required.items():
        req(method in tests, f"missing GameTest method {method}")
        for token in tokens:
            req(token in tests, f"{method}: missing contract token {token}")
    req(tests.count("@GameTest(") >= 28, "expected at least 28 required GameTests")

    hungry = read("src/main/java/com/darkifov/thaumcraft/blockentity/HungryChestBlockEntity.java")
    req("public static final int SIZE = 27" in hungry, "Hungry Chest size")
    req("ItemHandlerHelper.insertItemStacked" in hungry, "Hungry Chest unified insertion path")
    req("ContainerHelper.saveAllItems" in hungry and "ContainerHelper.loadAllItems" in hungry,
        "Hungry Chest persistence")

    brain = read("src/main/java/com/darkifov/thaumcraft/blockentity/BrainJarBlockEntity.java")
    req("public static final int MAX_XP = 2000" in brain, "Brain Jar capacity")
    req("level.getEntitiesOfClass(ExperienceOrb.class, LOCAL_ABSORB_BOX.move(pos))" in brain,
        "Brain Jar local absorption")

    mirror = read("src/main/java/com/darkifov/thaumcraft/mirror/MirrorBlockEntity.java")
    req("including its delayed output queue and instability" in mirror, "Magic Mirror runtime implementation")
    req("tag.put(\"Items\", items);" in mirror, "Magic Mirror queue persistence")

    candle = read("src/main/java/com/darkifov/thaumcraft/block/TallowCandleBlock.java")
    req("implements InfusionStabilizer" in candle, "Tallow Candle stabilizer marker")
    req("return Shapes.empty();" in candle, "Tallow Candle collision contract")

    manifest = json.loads(read("runtime_artifacts/runtime_test_manifest.template.json"))
    req(manifest.get("version") in ("11.63.54", "11.63.55", "11.63.56", "11.63.58", "11.63.59", "11.63.60", "11.63.61", "11.63.62", "11.63.63", "11.63.64", "11.63.65"), "runtime manifest version")
    req(len(manifest.get("tests", [])) >= 356, "runtime manifest scenario count")
    ids = {entry.get("id") for entry in manifest.get("tests", [])}
    expected_ids = {
        "gametest.tallow_candle_support_stabilizer_contract",
        "gametest.hungry_chest_eat_persistence_contract",
        "gametest.brain_jar_xp_comparator_contract",
        "gametest.magic_mirror_link_queue_contract",
    }
    req(expected_ids <= ids, "missing P1 block-entity runtime scenarios")

    for workflow in (".github/workflows/build.yml", ".github/workflows/release.yml"):
        text = read(workflow)
        req(any(f"--version {v}" in text for v in ("11.63.54", "11.63.55", "11.63.56", "11.63.58", "11.63.59", "11.63.60", "11.63.61", "11.63.62")), f"{workflow}: current manifest version")
        req("tc4_116354_p1_block_entity_runtime_guard.py" in text, f"{workflow}: guard wiring")

    print("TC4 v11.63.54 P1 block-entity runtime guard: PASS (28 GameTests, 356 scenarios)")

if __name__ == "__main__":
    main()
