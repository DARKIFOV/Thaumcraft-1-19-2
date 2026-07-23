#!/usr/bin/env python3
"""v11.63.72 Essentia storage/transport parity guard."""
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]

def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding="utf-8")

def req(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"TC4 v11.63.72 essentia parity guard: FAIL: {message}")

def main() -> None:
    req("version = '11." in read("build.gradle"), "build version marker")
    req('version="11.' in read("src/main/resources/META-INF/mods.toml"), "mods version marker")

    parity = read("src/main/java/com/darkifov/thaumcraft/essentia/TC4EssentiaParity.java")
    jar = read("src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaJarBlockEntity.java")
    reservoir = read("src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaReservoirBlockEntity.java")
    tube = read("src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java")
    tube_parity = read("src/main/java/com/darkifov/thaumcraft/essentia/TC4EssentiaTubeParity.java")
    mirror = read("src/main/java/com/darkifov/thaumcraft/mirror/EssentiaMirrorBlockEntity.java")
    tests = read("src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java")

    for token in (
        'CONTRACT_VERSION = "11.63.72"',
        "TUBE_SUBTYPE_COUNT = 6",
        "storageCapacitiesAndSuctionMatchTc4",
        "storagePullCadenceMatchesTc4",
        "tubeSubtypesAndPropagationMatchTc4",
        "mirrorTransferContractMatchesTc4",
        "EssentiaJarBlock.CAPACITY == 64",
        "EssentiaReservoirBlockEntity.CAPACITY == 256",
        "EssentiaSuction.JAR_FILTERED == 64",
        "EssentiaSuction.JAR_VOID_FILTERED == 48",
    ):
        req(token in parity, f"missing parity token {token}")

    req("ORIGINAL_FILL_INTERVAL_TICKS = TC4EssentiaJarParity.FILL_INTERVAL_TICKS" in jar, "jar pull constant")
    req("% ORIGINAL_FILL_INTERVAL_TICKS" in jar, "jar production does not use pull constant")
    req("ORIGINAL_FILL_INTERVAL_TICKS = 5" in reservoir, "reservoir pull constant")
    req("% ORIGINAL_FILL_INTERVAL_TICKS" in reservoir, "reservoir production does not use pull constant")

    for token in (
        "ORIGINAL_SUCTION_RECALC_INTERVAL_TICKS = TC4EssentiaTubeParity.SUCTION_RECALC_INTERVAL_TICKS",
        "ORIGINAL_EQUALIZE_INTERVAL_TICKS = TC4EssentiaTubeParity.TRANSFER_INTERVAL_TICKS",
        "ORIGINAL_BUFFER_BELLOWS_REFRESH_TICKS = TC4EssentiaTubeParity.BUFFER_BELLOWS_REFRESH_TICKS",
        "% ORIGINAL_SUCTION_RECALC_INTERVAL_TICKS",
        "% ORIGINAL_EQUALIZE_INTERVAL_TICKS",
        "% ORIGINAL_BUFFER_BELLOWS_REFRESH_TICKS",
    ):
        req(token in tube, f"missing/unused tube production token {token}")
    for token in (
        "SUCTION_RECALC_INTERVAL_TICKS = 2",
        "TRANSFER_INTERVAL_TICKS = 5",
        "BUFFER_BELLOWS_REFRESH_TICKS = 20",
    ):
        req(token in tube_parity, f"missing tube parity constant {token}")

    req("TRANSFER_UNIT = 1" in mirror, "mirror transfer unit")
    req("amount != TRANSFER_UNIT" in mirror and "source.remove(TRANSFER_UNIT)" in mirror,
        "mirror production does not use transfer unit")

    for method in (
        "essentiaStorageCapacitiesAndSuctionMatchTc4",
        "essentiaStoragePullCadenceMatchesTc4",
        "essentiaTubeSubtypesAndPropagationMatchTc4",
        "essentiaMirrorTransferContractMatchesTc4",
    ):
        req(method in tests, f"missing GameTest {method}")

    req(tests.count("@GameTest(") >= 89, f"expected at least 89 GameTests, found {tests.count('@GameTest(')}")
    manifest = json.loads(read("runtime_artifacts/runtime_test_manifest.template.json"))
    req(str(manifest.get("version", "")).startswith("11."), "manifest version")
    req(len(manifest.get("tests", [])) >= 417, f"expected at least 417 manifest scenarios, found {len(manifest.get('tests', []))}")
    print("TC4 v11.63.72 essentia parity guard: PASS (baseline 89/417; forward-compatible current release)")

if __name__ == "__main__":
    main()
