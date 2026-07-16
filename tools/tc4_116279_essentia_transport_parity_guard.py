#!/usr/bin/env python3
"""Static source-contract guard for v11.62.96 TC4 essentia transport parity.

This proves only that the source contains the audited TC4 pressure/reservoir/
centrifuge rules. It does not award runtime parity.
"""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def text(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


checks: list[tuple[str, bool]] = []

def check(name: str, condition: bool) -> None:
    checks.append((name, bool(condition)))

build = text("build.gradle")
mods = text("src/main/resources/META-INF/mods.toml")
reservoir = text("src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaReservoirBlockEntity.java")
reservoir_block = text("src/main/java/com/darkifov/thaumcraft/block/EssentiaReservoirBlock.java")
tube = text("src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java")
centrifuge = text("src/main/java/com/darkifov/thaumcraft/blockentity/AlchemicalCentrifugeBlockEntity.java")
blockstate = json.loads(text("src/main/resources/assets/thaumcraft/blockstates/essentia_reservoir.json"))
manifest = json.loads(text("runtime_artifacts/runtime_test_manifest.template.json"))

check("build version 11.62.96", "version = '11.62.96'" in build)
check("mods version 11.62.96", 'version="11.62.96"' in mods)
check("reservoir suction is original 24", "ORIGINAL_RESERVOIR_SUCTION = 24" in reservoir)
check("reservoir capacity remains 256", "CAPACITY = 256" in reservoir)
check("reservoir default side down", "private Direction facing = Direction.DOWN" in reservoir)
check("reservoir five-tick active pull", "originalTickCounter % 5 == 0" in reservoir and "fillFromFacingLikeTC4" in reservoir)
check("reservoir uses generic adjacent transport helper", "pullOneIntoReservoirLikeTC4" in reservoir)
check("reservoir clears over-capacity NBT", "aspects.totalAmount() > CAPACITY" in reservoir and "aspects.clear()" in reservoir)
check("reservoir six-direction state property", "BlockStateProperties.FACING" in reservoir_block)
check("reservoir placement down", "setValue(FACING, Direction.DOWN)" in reservoir_block)
check("wand sneak selects clicked side", "player.isShiftKeyDown() ? hit.getDirection()" in reservoir_block)
check("wand normal selects opposite side", ": hit.getDirection().getOpposite()" in reservoir_block)
variants = blockstate.get("variants", {})
for face in ("down", "up", "north", "south", "west", "east"):
    check(f"reservoir blockstate contains {face}", f"facing={face}" in variants)

check("buffer compares real neighbour suction", "source.suctionAmount() >= sideSuction" in tube)
check("buffer compares neighbour minimum suction", "sideSuction < source.minimumSuction()" in tube)
check("source contract exposes suction amount", "int suctionAmount();" in tube)
check("source contract exposes minimum suction", "int minimumSuction();" in tube)
check("source transport delegates suction", "return source.suctionAmount();" in tube)
check("source transport delegates minimum", "return source.minimumSuction();" in tube)
check("buffer removal preserves face arbitration", "takeEssentiaOriginal(aspect, amount, face)" in tube)
check("reservoir source minimum is 24 contract", "return EssentiaReservoirBlockEntity.ORIGINAL_RESERVOIR_SUCTION;" in tube)
check("jar source exposes original minimum", "return jar.originalMinimumSuction(voidJar);" in tube)
check("reservoir suction type is untyped", "instanceof EssentiaReservoirBlockEntity reservoir" in tube and "return null;" in tube[tube.find("private Aspect originalDestinationSuctionType"):tube.find("private Destination destinationFrom")])
check("generic reservoir pull helper exists", "public static int pullOneIntoReservoirLikeTC4" in tube)
check("generic pull compares suction", "source.suctionAmount() >= reservoirSuction" in tube)
check("generic pull compares minimum", "reservoirSuction < source.minimumSuction()" in tube)
check("generic pull rolls back rejected source unit", "source.restore(removed - accepted)" in tube)
check("tube pull rolls back rejected unit", "addEssentiaToLocalTubeLikeTC4(aspect, removed - accepted" in tube)

add_start = centrifuge.find("public int addInput")
add_end = centrifuge.find("private void drawEssentiaFromBelow", add_start)
add_body = centrifuge[add_start:add_end]
check("centrifuge input remains bottom-gated", "!canInputFrom(face)" in add_body)
check("centrifuge rejects primal input", "aspect.isPrimal()" in add_body)
check("centrifuge accepts only one queued input", "aspectIn != null" in add_body)
check("centrifuge does not reject occupied output", "aspectOut" not in add_body)
check("centrifuge does not reject redstone while queueing", "isPowered" not in add_body)
check("centrifuge processing still pauses on redstone", "if (tile.isPowered())" in centrifuge)

required_tests = {
    "essentia.reservoir_24_suction_six_faces_active_pull",
    "essentia.buffer_real_neighbor_suction_minimum_arbitration",
    "processing.centrifuge_input_queue_output_occupied_redstone",
}
manifest_ids = {entry.get("id") for entry in manifest.get("tests", [])}
check("runtime manifest version 11.62.96", manifest.get("version") == "11.62.96")
check("runtime manifest retains at least the 53 transport-era cases", len(manifest.get("tests", [])) >= 53)
for test_id in sorted(required_tests):
    check(f"runtime case {test_id}", test_id in manifest_ids)

failed = [name for name, ok in checks if not ok]
report = {
    "version": "11.62.96",
    "scope": "TC4 essentia reservoir, buffer suction arbitration and centrifuge input queue source contracts",
    "checks_total": len(checks),
    "checks_passed": len(checks) - len(failed),
    "failed": failed,
    "runtime_verified": False,
}
out = ROOT / "reports" / "tc4_116279_essentia_transport_parity_audit.json"
out.parent.mkdir(parents=True, exist_ok=True)
out.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print(f"Essentia transport parity guard: {report['checks_passed']}/{report['checks_total']}")
if failed:
    for name in failed:
        print(f"FAIL: {name}")
    raise SystemExit(1)
print("STATIC SOURCE CONTRACT PASS; runtime remains NOT TESTED")
