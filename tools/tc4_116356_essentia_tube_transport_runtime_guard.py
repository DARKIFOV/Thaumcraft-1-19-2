#!/usr/bin/env python3
"""v11.63.56 essentia tube transport and buffer rollback runtime contract guard."""
from __future__ import annotations
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding="utf-8")

def req(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"TC4 v11.63.56 essentia tube runtime guard: FAIL: {message}")

def main() -> None:
    req("version = '11." in read("build.gradle"), "build version")
    req('version="11.' in read("src/main/resources/META-INF/mods.toml"), "mods.toml version")

    tests = read("src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java")
    required = {
        "normalAndRestrictedTubesPropagateOriginalJarSuction": (
            "getSuctionAmount(Direction.DOWN) == 31", "getSuctionAmount(Direction.DOWN) == 16", "tickTube"
        ),
        "filteredTubeLocksSuctionAndTransferToLabelAspect": (
            "ESSENTIA_JAR", "setAspectFilter(Aspect.AER)", "allowsAspectForTransfer(Aspect.IGNIS)"
        ),
        "oneWayTubePropagatesSuctionOnlyAlongOriginalFacing": (
            "ESSENTIA_TUBE_ONEWAY", "setFacing(Direction.UP)", "setFacing(Direction.NORTH)"
        ),
        "bufferTubeCapsPersistsAndSynchronizesRollbackState": (
            "BUFFER_CAPACITY", "restoreBufferForNetwork", "chokeState(Direction.NORTH) == 2", "saveWithoutMetadata"
        ),
    }
    for method, tokens in required.items():
        req(method in tests, f"missing GameTest {method}")
        for token in tokens:
            req(token in tests, f"{method}: missing {token}")
    req(tests.count("@GameTest(") >= 36, "expected retained 36 GameTests")

    subtype = read("src/main/java/com/darkifov/thaumcraft/essentia/EssentiaTubeSubtype.java")
    parity = read("src/main/java/com/darkifov/thaumcraft/essentia/TC4EssentiaTubeParity.java")
    req("TC4EssentiaTubeParity.propagatedSuction" in subtype, "tube subtype parity binding")
    for token in ("neighbourSuction / 2", "neighbourSuction - 1"):
        req(token in parity, f"tube parity contract {token}")
    for token in ("allowsAspect", "directionalFlow"):
        req(token in subtype, f"tube subtype contract {token}")

    tube = read("src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java")
    for token in (
        "public static final int BUFFER_CAPACITY = TC4EssentiaTubeParity.BUFFER_CAPACITY",
        "BUFFER_CAPACITY - bufferAspects.totalAmount()",
        "essentiaType = bufferAspects.firstAspect()",
        "essentiaAmount = bufferAspects.totalAmount()",
        "if (directional && facing == direction.getOpposite())",
    ):
        req(token in tube, f"tube runtime contract {token}")

    manifest = json.loads(read("runtime_artifacts/runtime_test_manifest.template.json"))
    req(str(manifest.get("version", "")).startswith("11."), "manifest version")
    req(len(manifest.get("tests", [])) >= 364, "manifest scenario count")
    ids = {entry.get("id") for entry in manifest.get("tests", [])}
    expected = {
        "gametest.essentia_tube_normal_restrict_suction_contract",
        "gametest.essentia_tube_filter_aspect_contract",
        "gametest.essentia_tube_oneway_facing_contract",
        "gametest.essentia_tube_buffer_capacity_rollback_contract",
    }
    req(expected <= ids, "missing tube runtime scenarios")

    for workflow in (".github/workflows/build.yml", ".github/workflows/release.yml"):
        text = read(workflow)
        req("--version 11." in text, f"{workflow}: manifest version")
        req("tc4_116356_essentia_tube_transport_runtime_guard.py" in text, f"{workflow}: guard wiring")

    print("TC4 v11.63.56 essentia tube runtime guard: PASS (36 GameTests, 364 scenarios)")

if __name__ == "__main__":
    main()
