#!/usr/bin/env python3
"""Stage144 focused audit for TC4 Eldritch/Warp/Taint/Cultist pass."""
from pathlib import Path
import json
import sys

root = Path(__file__).resolve().parents[1]
read = lambda rel: (root / rel).read_text(encoding='utf-8')

files = {
    "player_data": read("src/main/java/com/darkifov/thaumcraft/data/PlayerThaumData.java"),
    "warp_events": read("src/main/java/com/darkifov/thaumcraft/event/WarpEvents.java"),
    "progression": read("src/main/java/com/darkifov/thaumcraft/eldritch/TC4EldritchProgression.java"),
    "portal_be": read("src/main/java/com/darkifov/thaumcraft/blockentity/EldritchPortalBlockEntity.java"),
    "altar": read("src/main/java/com/darkifov/thaumcraft/block/EldritchAltarBlock.java"),
    "mod": read("src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java"),
    "taint": read("src/main/java/com/darkifov/thaumcraft/taint/TaintSpreadRuntime.java"),
    "client": read("src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java"),
    "items": read("src/main/java/com/darkifov/thaumcraft/event/EldritchItemEvents.java"),
}

checks = {
    "split_warp_perm_sticky_temp_counter": all(x in files["player_data"] for x in ["WARP_STICKY", "WARP_TEMPORARY", "WARP_COUNTER", "getActualWarp"]),
    "tc4_thresholds_present": all(x in files["progression"] for x in ["BATHSALTS_WARP = 10", "ELDRITCH_MINOR_WARP = 25", "ELDRITCH_MAJOR_WARP = 50"]),
    "crimson_rites_event_bridge": "tc4_crimson_rites" in files["items"] and "readCrimsonRites" in files["items"],
    "eldritch_object_event_bridge": "tc4_eldritch_object_3" in files["items"] and "attuneWithEldritchEye" in files["items"],
    "custom_eldritch_entities_registered": all(x in files["mod"] for x in ["ELDRITCH_GUARDIAN", "CRIMSON_CULTIST", "CRIMSON_KNIGHT", "CRIMSON_CLERIC", "CRIMSON_PRAETOR"]),
    "custom_entity_attributes_registered": all(x in files["mod"] for x in ["createLeaderAttributes", "EldritchGuardianEntity.createAttributes"]),
    "portal_uses_custom_mobs": all(x in files["portal_be"] for x in ["spawnCultist", "spawnGuardian", "CRIMSON_PRAETOR", "ELDRITCH_GUARDIAN"]),
    "portal_no_vanilla_enderman_vex": not any(x in files["portal_be"] for x in ["EntityType.ENDERMAN", "EntityType.VEX", "EnderMan", "Vex"]),
    "altar_uses_tc4_progression_gate": "TC4EldritchProgression.canOpenOuterLands" in files["altar"],
    "warp_event_spawns_guardian_and_taint": "spawnGuardian" in files["warp_events"] and "TaintSpreadRuntime.convert" in files["warp_events"],
    "taint_runtime_shared": all(x in files["taint"] for x in ["randomTick", "trySpreadNear", "convert", "isTaintable"]),
    "client_renderers_registered": all(x in files["client"] for x in ["TC4BlockMobRenderer", "CRIMSON_PRAETOR", "ELDRITCH_GUARDIAN"]),
}

passed = all(checks.values())
print(json.dumps({"stage": 144, "goal": "TC4 Eldritch/Warp/Taint/Cultist focused audit", "checks": checks, "passed": passed}, indent=2))
if not passed:
    sys.exit(1)
