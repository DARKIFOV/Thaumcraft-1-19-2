#!/usr/bin/env python3
"""Static guard for v11.63.10 runtime visual and Outer Lands loading hotfixes."""
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
checks = []

def text(rel):
    p = ROOT / rel
    return p.read_text(encoding="utf-8", errors="ignore") if p.is_file() else ""

def check(name, condition):
    checks.append((name, bool(condition)))

build = text("build.gradle")
mods = text("src/main/resources/META-INF/mods.toml")
teleporter = text("src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsTeleporter.java")
live = text("src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsLivePopulateAdapter.java")
wand = text("src/main/java/com/darkifov/thaumcraft/client/render/WandItemRenderer.java")
transducer = text("src/main/java/com/darkifov/thaumcraft/client/render/NodeTransducerRenderer.java")
transducer_item = text("src/main/java/com/darkifov/thaumcraft/block/NodeTransducerItem.java")
transducer_item_renderer = text("src/main/java/com/darkifov/thaumcraft/client/render/NodeTransducerItemRenderer.java")
mod = text("src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java")
model = text("src/main/resources/assets/thaumcraft/models/item/node_transducer.json")
alembic = text("src/main/java/com/darkifov/thaumcraft/block/AlembicBlock.java")
readme = text("README.md")
status = text("TC4_PORT_STATUS_V3.md")
build_wf = text(".github/workflows/build.yml")
release_wf = text(".github/workflows/release.yml")
manifest = json.loads(text("runtime_artifacts/runtime_test_manifest.template.json"))
manifest_ids = {entry.get("id") for entry in manifest.get("tests", [])}

check("build_version", "version = '11.63.23'" in build)
check("mods_version", 'version="11.63.23"' in mods)
check("manifest_version", manifest.get("version") in ("11.63.23", "11.63.24", "11.63.26", "11.63.27", "11.63.28", "11.63.29", "11.63.30", "11.63.31", "11.63.32", "11.63.33", "11.63.36", "11.63.37", "11.63.38", "11.63.39", "11.63.40", "11.63.41", "11.63.42", "11.63.43", "11.63.44", "11.63.45", "11.63.46", "11.63.47", "11.63.48", "11.63.49", "11.63.50", "11.63.52", "11.63.53", "11.63.54", "11.63.55", "11.63.56", "11.63.58", "11.63.59", "11.63.60", "11.63.61"))
check("readme_stage", "11.63.10" in readme and "Loading terrain" in readme)
check("status_stage", "11.63.10" in status and "one-room" in status)

check("teleport_uses_server_player_teleport_to", "player.teleportTo(destination" in teleporter)
check("teleporter_no_sync_radius_two", "generateAround(level, mazeOrigin, 2)" not in teleporter)
check("teleporter_entry_cell_only", "TC4OuterLandsMazeHandler.generateEldritch(level, mazeOrigin, originCellX, originCellZ)" in teleporter)
check("teleporter_landing_chunk_loaded", "level.getChunkAt(generatedPortal)" in teleporter)
check("teleporter_safe_air_clear", "removeBlock(generatedPortal.east()" in teleporter and "east().above()" in teleporter)

check("live_center_first_offsets", "PLAYER_CHUNK_OFFSETS" in live and "{0, 0}" in live)
check("live_one_success_per_pass", "if (populateChunkOnce" in live and "return;" in live)
check("live_40_tick_budget", "getGameTime() % 40L" in live)
check("live_no_nested_radius_loop", "PLAYER_CHUNK_RADIUS" not in live)

check("wand_first_person_modern_origin", "poseStack.translate(0.50D, 0.78D, 0.50D)" in wand)
check("wand_first_person_bounded_scale", "staff ? 0.34F : 0.52F" in wand)
check("wand_third_person_bounded_scale", "staff ? 0.42F : 0.58F" in wand)
check("wand_legacy_giant_origin_removed", "poseStack.translate(0.50D, 1.50D, 0.50D)" not in wand)
check("wand_original_geometry_retained", "renderOriginalTC4WandComponents" in wand and "legacyModelQuad" in wand)

check("transducer_shared_standalone", "public static void renderStandalone" in transducer)
check("transducer_original_textures", "node_converter.png" in transducer and "node_converter_over.png" in transducer)
check("transducer_legacy_lightmap", "tc4OverlayLight" in transducer and "LightTexture.pack" in transducer)
check("transducer_overlay_opaque", "rgb[0], rgb[1], rgb[2], 255" in transducer)
check("transducer_no_forced_fullbright", "LightTexture.FULL_BRIGHT" not in transducer)
check("transducer_custom_item_class", "extends BlockItem" in transducer_item and "getCustomRenderer" in transducer_item)
check("transducer_custom_item_renderer", "NodeTransducerRenderer.renderStandalone" in transducer_item_renderer)
check("transducer_registered_custom_item", "new NodeTransducerItem" in mod)
check("transducer_builtin_entity_model", '"minecraft:builtin/entity"' in model and '"elements"' not in model)

check("alembic_status_actionbar", ".append(alembic.aspects().toComponent()),\n                    true" in alembic)
for tid in [
    "visual.wand_first_person_local_bounds",
    "visual.node_transducer_overlay_legacy_lightmap",
    "visual.node_transducer_item_original_obj",
    "outer_lands.entry_portal_room_without_loading_stall",
    "outer_lands.progressive_one_room_population_budget",
    "alchemy.alembic_status_uses_actionbar",
]:
    check("manifest_" + tid, tid in manifest_ids)
check("build_workflow_guard", "tc4_116303_runtime_visual_outer_lands_hotfix_guard.py" in build_wf)
check("release_workflow_guard", "tc4_116303_runtime_visual_outer_lands_hotfix_guard.py" in release_wf)

failed = [name for name, ok in checks if not ok]
for name, ok in checks:
    print(("PASS" if ok else "FAIL") + " | " + name)
print(f"SUMMARY | {len(checks)-len(failed)}/{len(checks)} passed")
if failed:
    sys.exit(1)
