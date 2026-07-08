#!/usr/bin/env python3
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding="utf-8", errors="ignore")

checks = {}
checks["version_build_252"] = "version = '2.52.0'" in read("build.gradle")
checks["version_mods_252"] = 'version="2.52.0"' in read("src/main/resources/META-INF/mods.toml")
checks["next_prompt_252"] = (ROOT / "docs/NEXT_CHAT_PROMPT_STAGE252.md").exists()
checks["report_252"] = (ROOT / "STAGE243_252_TC4_OUTER_LANDS_BOSS_DECOR_DIMENSION_BATCH_REPORT.json").exists()

decor = read("src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsDecorationAdapter.java")
checks["deferred_deco_common"] = "DECO_COMMON" in decor and "decoCommon" in decor
checks["deferred_crab_spawner"] = "CRAB_SPAWNER" in decor and "crabSpawner" in decor
checks["deferred_deco_urn"] = "DECO_URN" in decor and "decoUrn" in decor
checks["process_exposed_sides"] = "countExposedSides" in decor and "isBedrockShowing" in decor and "isAdjacentToEldritchStone" in decor
checks["tc4_urn_ordering"] = "rr < 0.1F ? 1 : rr < 0.025F ? 2 : 0" in decor

gen = read("src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsGenCommonAdapter.java")
checks["placeblock_records_decor"] = "TC4OuterLandsDecorationAdapter.recordPlacement" in gen
checks["connection_uses_real_cell"] = "placeConnectionSlice(level, origin, yOffset, d, Direction.NORTH, justTheTip, cell)" in gen and "placeBlock(level, pos, code, facing, cell)" in gen
checks["variant_adapter_used"] = "TC4EldritchBlockVariantAdapter.stateForMeta" in gen

variant = read("src/main/java/com/darkifov/thaumcraft/eldritch/TC4EldritchBlockVariantAdapter.java")
checks["metadata_names"] = "META_DOOR_LOCK" in variant and "blockEldritch:10 trapped" in variant

selector = read("src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsFeatureSelector.java")
checks["selector_begin_process"] = "beginRoom()" in selector and "processDecorations(level)" in selector
checks["selector_cell_boss_key"] = "placeBossRoomCell" in selector and "placeKeyRoomCell" in selector

boss = read("src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsBossRoomPlacer.java")
checks["boss_cell_local"] = "placeBossRoomCell" in boss and "GenBossRoom" in boss and "generate2x2" in boss
checks["boss_pat_doorway"] = "placeOriginalBossDoor" in boss and "PAT_DOORWAY" in boss and "origin.offset(xx, 2 + b, zz)" in boss
checks["keyroom_exact_loops"] = "placeKeyRoomCell" in boss and "a <= 15" in boss and "c < 13" in boss and "q - 1" in boss
checks["keyroom_connections_guardians"] = "generateConnections(level, origin, cell, 3, true)" in boss and "spawnPermanentKeyItem" in boss and "spawnKeyRoomGuardians" in boss

dim = read("src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsDimensionAdapter.java")
world = read("src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenRuntime.java")
checks["dimension_adapter"] = "OUTER_LANDS_ID" in dim and "supportsPortalMaze" in dim and "shouldRunSurfaceWorldgen" in dim
checks["worldgen_separates_outerlands"] = "supportsPortalMaze" in world and "shouldRunSurfaceWorldgen" in world and "TC4OuterLandsMazeHandler.tickPlayerArea" in world

legacy_forbidden = ["net.minecraft.world.World", "ForgeDirection", "ChunkCoordinates", "NBTTagCompound", "func_"]
joined = decor + gen + boss + dim
checks["no_legacy_api"] = not any(token in joined for token in legacy_forbidden)

failed = [name for name, ok in checks.items() if not ok]
if failed:
    print(json.dumps({"stage_batch": "243-252", "checks": checks, "failed": failed}, indent=2, sort_keys=True))
    sys.exit(1)
print(json.dumps({"stage_batch": "243-252", "checks": checks}, indent=2, sort_keys=True))
