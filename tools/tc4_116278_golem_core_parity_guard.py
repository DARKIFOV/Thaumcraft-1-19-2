#!/usr/bin/env python3
"""Regression guard for v11.62.82 TC4 golem core parity fixes."""
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
ENTITY = ROOT / "src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java"
MANIFEST = ROOT / "runtime_artifacts/runtime_test_manifest.template.json"

checks = []

def require(name: str, condition: bool) -> None:
    checks.append((name, bool(condition)))

src = ENTITY.read_text(encoding="utf-8")
require("functional golems pre-empt normal tasks for creeper swell avoidance",
        "if (!avoidSwellingCreeperLikeTC4())" in src)
require("creeper avoidance accepts normal swelling and explicit ignition",
        "creeper.getSwellDir() > 0 || creeper.isIgnited()" in src)
require("creeper avoidance keeps original priority diagnostic",
        'lastOriginalTask = "AIAvoidCreeperSwell"' in src)
require("USE core permits empty-hand action only without a home inventory",
        "itemCarried.isEmpty() && originalHomeContainer() != null" in src)
require("USE marker free-side check follows marker.side rather than opposite",
        "level.getBlockState(pos.relative(side)).isAir()" in src)
require("USE empty-space marker clicks the block behind the marked air cell",
        "pos.relative(side.getOpposite())" in src and "interactionPos" in src)
require("USE navigation still targets the marker location",
        "BlockPos targetPos = target.markerPos();" in src)
require("USE click acts on resolved interaction position",
        "BlockPos pos = target.interactionPos();" in src)
require("FakePlayer first by-product becomes the new carried stack",
        "if (itemCarried.isEmpty())" in src and "itemCarried = extra.copy();" in src)
require("fishing core passes the selected water position to the loot roll",
        "rollFishingCatchLikeTC4(water)" in src)
require("fishing checks junk before treasure like TC4 AIFish",
        src.find("if (roll < junkChance)") < src.find("if (roll < treasureChance)"))
require("fishing water quality adjusts junk and treasure chances",
        "fishingChancesLikeTC4" in src and "level.canSeeSky(neighbor.above())" in src)
require("fishing junk table uses explicit TC4-style weights",
        "pickWeightedFishingStack" in src and "int[] weights = {10, 10, 10, 10, 5, 2" in src)

manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
ids = {entry.get("id") for entry in manifest.get("tests", [])}
for test_id in (
    "golems.use_core_marker_side_empty_hand",
    "golems.fishing_water_quality_weighted_loot",
    "golems.creeper_swell_avoidance",
):
    require(f"runtime manifest contains {test_id}", test_id in ids)
require("runtime manifest version is v11.62.82", manifest.get("version") == "11.62.82")

failed = [name for name, ok in checks if not ok]
for name, ok in checks:
    print(("PASS" if ok else "FAIL") + ": " + name)
print(f"Golem core parity guard: {len(checks)-len(failed)}/{len(checks)}")
if failed:
    sys.exit(1)
