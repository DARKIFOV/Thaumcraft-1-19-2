#!/usr/bin/env python3
"""Static contract guard for v11.63.44 required Forge GameTest smoke suite."""
from __future__ import annotations

import gzip
import json
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java"
STRUCTURE = ROOT / "src/main/resources/data/thaumcraft/structures/empty_9x5x9.nbt"
BUILD = ROOT / "build.gradle"
MANIFEST = ROOT / "runtime_artifacts/runtime_test_manifest.template.json"
WORKFLOWS = [ROOT / ".github/workflows/build.yml", ROOT / ".github/workflows/release.yml"]


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def inspect_structure(path: Path) -> None:
    payload = gzip.decompress(path.read_bytes())
    require(payload[:3] == b"\x0a\x00\x00", "structure root is not an unnamed compound")
    require(b"DataVersion" in payload and struct.pack(">i", 3120) in payload,
            "structure must use the Minecraft 1.19.2 data version")
    require(b"size" in payload and b"palette" in payload and b"blocks" in payload and b"entities" in payload,
            "structure is missing required template keys")
    require(b"minecraft:air" in payload, "empty test template must define an air palette")
    require(struct.pack(">iii", 9, 5, 9) in payload, "test structure must be 9x5x9")


def main() -> int:
    text = JAVA.read_text(encoding="utf-8")
    required_tokens = [
        "RegisterGameTestsEvent", "event.register(TC4BlockEntityGameTests.class)",
        "@PrefixGameTestTemplate(false)", 'templateNamespace = ThaumcraftMod.MOD_ID',
        'private static final String TEMPLATE = "empty_9x5x9"',
        "essentiaJarPersistsFilterAndContents", "wandPedestalDrainsNodeAndChargesWand",
        "nodeJarPersistsProfileAndCaptureAnimation", "manaPodGrowsAndPersistsAspect",
        "legacyStackMigrationPreservesPayload", "arcaneDoorUpperHalfDelegatesPersistentAccess",
        "WandItem.getVis(pedestal.stored(), Aspect.AER) == 100",
        "node.aspects().get(Aspect.AER) == 1", "TC4LegacyDuplicateItemMigrator.migrateStackDeep",
        "restored.amount() == 48", "restored.aspect() == Aspect.IGNIS",
    ]
    for token in required_tokens:
        require(token in text, f"missing GameTest contract token: {token}")
    require(text.count("@GameTest(") >= 6, "expected at least the six v11.63.44 required GameTests")
    require("net.minecraft.client" not in text and "net.minecraftforge.client" not in text,
            "headless GameTests must not reference client classes")

    inspect_structure(STRUCTURE)

    build = BUILD.read_text(encoding="utf-8")
    require("version = '11.63." in build, "build version is not in the 11.63 line")
    require("gameTestServer" in build and "forceExit false" in build,
            "ForgeGradle gameTestServer must disable force exit")
    require("forge.enabledGameTestNamespaces', 'thaumcraft'" in build,
            "GameTest namespace is not restricted to thaumcraft")
    require("data/thaumcraft/structures/empty_9x5x9.nbt" in build,
            "test structure is not covered by release resource audit")

    manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
    require(tuple(map(int, manifest.get("version", "0.0.0").split("."))) >= (11, 63, 44), "runtime manifest predates 11.63.44")
    ids = {test.get("id") for test in manifest.get("tests", [])}
    expected_ids = {
        "gametest.essentia_jar_nbt_roundtrip", "gametest.wand_pedestal_node_charge",
        "gametest.node_jar_profile_roundtrip", "gametest.mana_pod_growth_roundtrip",
        "gametest.legacy_stack_payload_migration", "gametest.arcane_door_master_access",
    }
    require(expected_ids <= ids, "runtime manifest is missing GameTest evidence slots")
    by_id = {test.get("id"): test for test in manifest.get("tests", [])}
    for test_id in expected_ids:
        require(by_id[test_id].get("status") == "NOT_TESTED",
                f"{test_id} must remain NOT_TESTED until a real GameTest log exists")

    for workflow in WORKFLOWS:
        wf = workflow.read_text(encoding="utf-8")
        require("python3 tools/tc4_116344_gametest_smoke_guard.py" in wf,
                f"{workflow.name} does not run the v11.63.44 guard")
        require("./gradlew runGameTestServer --stacktrace --no-daemon" in wf,
                f"{workflow.name} does not run required GameTests")
        require("steps.forge_build.outcome == 'success'" in wf,
                f"{workflow.name} must run GameTests only after successful compilation")
        require("gametest-server.log" in wf, f"{workflow.name} does not preserve the GameTest log")

    print("v11.63.44 GameTest smoke guard: PASS (baseline six tests retained; server-only; evidence NOT_TESTED)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
