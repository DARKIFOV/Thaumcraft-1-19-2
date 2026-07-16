#!/usr/bin/env python3
"""Objective source/resource audit for TC4 custom visual contracts.

This audit deliberately does *not* award visual PASS. It verifies that objects
which used TC4 IItemRenderer/custom entity or armor models no longer fall through
to a flat sprite, a generic block placeholder, or a vanilla humanoid model. The
runtime columns remain NOT_TESTED until named screenshots/video are present.
"""
from __future__ import annotations

import argparse
import json
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java"
MODELS = ROOT / "src/main/resources/assets/thaumcraft/models/item"
REPORTS = ROOT / "reports"


def text(rel: str) -> str:
    path = ROOT / rel
    return path.read_text(encoding="utf-8") if path.is_file() else ""


def model(item: str) -> dict[str, Any]:
    path = MODELS / f"{item}.json"
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception:
        return {}


def has(rel: str, *tokens: str) -> bool:
    source = text(rel)
    return bool(source) and all(token in source for token in tokens)


def source_contains(name: str, *tokens: str) -> bool:
    matches = list(JAVA.rglob(name))
    return len(matches) == 1 and all(token in matches[0].read_text(encoding="utf-8") for token in tokens)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--version", default="11.62.92")
    parser.add_argument("--fail-on-p0", action="store_true")
    args = parser.parse_args()

    item_models = []
    parse_errors = []
    for path in sorted(MODELS.rglob("*.json")):
        rel = path.relative_to(MODELS).with_suffix("").as_posix()
        try:
            data = json.loads(path.read_text(encoding="utf-8"))
            item_models.append({
                "item": rel,
                "parent": data.get("parent"),
                "display_contexts": sorted((data.get("display") or {}).keys()) if isinstance(data.get("display"), dict) else [],
                "override_count": len(data.get("overrides") or []) if isinstance(data.get("overrides"), list) else 0,
            })
        except Exception as exc:
            parse_errors.append({"item": rel, "error": str(exc)})

    p0 = []
    def record(key: str, title: str, checks: dict[str, bool], original: str, *, force_partial: bool = False) -> None:
        p0.append({
            "key": key,
            "title": title,
            "original_contract": original,
            "static_status": ("STATIC_PARTIAL" if force_partial and all(checks.values()) else ("SOURCE_CONTRACT_COMPLETE" if all(checks.values()) else "FAIL")),
            "runtime_visual_status": "NOT_TESTED",
            "checks": checks,
            "missing": [name for name, ok in checks.items() if not ok],
        })

    jar_models = [model(x).get("parent") == "minecraft:builtin/entity" for x in ("essentia_jar", "filtered_essentia_jar", "void_essentia_jar")]
    record("essentia_jars", "Essentia jars in item contexts", {
        "three_builtin_entity_models": all(jar_models),
        "nbt_aware_block_item": source_contains("EssentiaJarBlockItem.java", "BlockEntityTag", "initializeClient", "EssentiaJarItemRenderer"),
        "bewlr_renders_world_contents": source_contains("EssentiaJarItemRenderer.java", "renderItemContents", "renderSingleBlock"),
        "pickup_and_placement_preserve_nbt": source_contains("EssentiaJarBlock.java", "getCloneItemStack", "setPlacedBy", "getDrops"),
    }, "ItemJarFilledRenderer: temporary TileJar, liquid/aspect/filter/label and jar shell")

    record("aura_node_item", "Aura node item policy", {
        "raw_node_is_migration_only": source_contains("AuraNodeLegacyItem.java", "migration", "NODE_JAR", "InteractionResultHolder"),
        "raw_node_has_no_block_loot": text("src/main/resources/data/thaumcraft/loot_tables/blocks/aura_node.json").replace(" ", "").find('"pools":[]') >= 0,
        "node_jar_has_bewlr": source_contains("NodeJarItemRenderer.java", "renderContainedNode", "NODE_SHEET_FRAMES", "AuraNodeModifier"),
        "node_jar_builtin_entity": model("node_jar").get("parent") == "minecraft:builtin/entity",
    }, "ItemNodeRenderer or the normal player-facing Node in a Jar path")

    record("bone_bow", "Bone Bow", {
        "functional_bow_item": source_contains("BoneBowItem.java", "getBonePowerForTime", "power * 2.5F", "ForgeEventFactory.onArrowLoose"),
        "original_charge_cap": source_contains("BoneBowItem.java", "used > 18", "charge / 10.0F"),
        "pull_properties_registered": has("src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java", '"pulling"', '"pull"', "getPullModelValue"),
        "three_pull_models": all((MODELS / f"tc4_bonebow_pulling_{i}.json").is_file() for i in range(3)),
        "three_overrides": len(model("tc4_bonebow").get("overrides") or []) == 3,
    }, "ItemBowBoneRenderer plus 0/8/13-tick icon stages and faster ItemBowBone projectile curve")

    record("traveling_trunk", "Traveling Trunk", {
        "entity_registered": has("src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java", 'ENTITY_TYPES.register("traveling_trunk"', "TravelingTrunkEntity.createAttributes"),
        "inventory_owner_ai_nbt": source_contains("TravelingTrunkEntity.java", "ChestMenu.threeRows", "FollowOwnerGoal", "ContainerHelper.saveAllItems", "dropAllDeathLoot"),
        "animated_entity_model": source_contains("TC4TravelingTrunkModel.java", 'getChild("lid")', "front_left", "Mth.HALF_PI"),
        "entity_renderer": source_contains("TravelingTrunkRenderer.java", "trunkangry.png", "MobRenderer"),
        "item_bewlr": source_contains("TravelingTrunkItem.java", "initializeClient", "TravelingTrunkItemRenderer") and model("tc4_travel_trunk").get("parent") == "minecraft:builtin/entity",
    }, "ItemTrunkSpawnerRenderer + EntityTravelingTrunk, ModelTrunk, inventory/owner/lid behavior")

    client = text("src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java")
    record("crimson_cultists", "Crimson cultists", {
        "humanoid_renderer_registered_four_times": client.count("entityRenderer(TC4CrimsonCultistRenderer::new)") == 4,
        "old_block_placeholders_removed": not any(f"new TC4BlockMobRenderer<>(ctx, () -> ThaumcraftMod.{x}" in client for x in ("OBSIDIAN_TOTEM", "OBSIDIAN_TILE", "NITOR_LIGHT", "ELDRITCH_OBELISK")),
        "role_models": source_contains("TC4CrimsonCultistModel.java", "ROBE", "KNIGHT", "LEADER", "wide_sleeve", "pauldron"),
        "original_textures": source_contains("TC4CrimsonCultistRenderer.java", "cultist.png", "cultist_robe_armor.png", "cultist_plate_armor.png", "cultist_leader_armor.png"),
        "held_item_and_leader_scale": source_contains("TC4CrimsonCultistRenderer.java", "ItemInHandLayer", "1.25F"),
    }, "RenderCultist ModelBiped base, original skin, role equipment/armor layers and 1.25 leader scale")

    record("fortress_armor", "Fortress Armor", {
        "dedicated_model": source_contains("TC4FortressArmorModel.java", "mask_0", "goggles", "shoulder_tier_3", "panel_tier_3"),
        "layer_uses_dedicated_model": source_contains("TC4FortressArmorLayer.java", "TC4FortressArmorModel", "model.configure", "fortress_armor.png"),
        "vanilla_model_approximation_removed": "new HumanoidModel" not in text("src/main/java/com/darkifov/thaumcraft/client/render/TC4FortressArmorLayer.java"),
        "all_three_masks_and_goggles": source_contains("TC4FortressArmorModel.java", "mask0.visible", "mask1.visible", "mask2.visible", "goggles.visible"),
        "set_dependent_details": source_contains("TC4FortressArmorModel.java", "setPieces >= 2", "setPieces >= 3"),
    }, "ModelFortressArmor with custom 128x64 geometry, three NBT masks, goggles and set-dependent ornaments")

    record("outer_lands", "Outer Lands", {
        "dimension_resources": has("src/main/resources/data/thaumcraft/dimension/outer_lands.json", '"type"') and has("src/main/resources/data/thaumcraft/dimension_type/outer_lands.json", '"ultrawarm"'),
        "portal_teleporter": source_contains("TC4OuterLandsTeleporter.java", "outer_lands", "teleport") or source_contains("TC4OuterLandsTeleporter.java", "OUTER_LANDS"),
        "maze_rooms_and_loot": all(source_contains(name, token) for name, token in (("TC4OuterLandsMazeGenerator.java", "generate"), ("TC4OuterLandsFeatureSelector.java", "FEATURE_BOSS"), ("TC4OuterLandsLootAdapter.java", "loot"))),
        "boss_registered": has("src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java", 'ENTITY_TYPES.register("eldritch_warden"'),
        "server_tick_population_wired": has("src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java", "TC4OuterLandsLivePopulateAdapter.tickPlayerArea"),
        "population_persisted": source_contains("TC4OuterLandsMazeSavedData.java", "populatedChunks", "putLongArray") and source_contains("TC4OuterLandsLivePopulateAdapter.java", "markChunkPopulated"),
    }, "ChunkProviderOuter, MazeHandler rooms/passages/loot, portal progression, Warden and persistent return path", force_partial=True)

    failures = [entry for entry in p0 if entry["static_status"] == "FAIL"]
    stats = {
        "version": args.version,
        "item_model_json": len(item_models),
        "parse_errors": len(parse_errors),
        "p0_total": len(p0),
        "p0_source_contract_complete": sum(entry["static_status"] == "SOURCE_CONTRACT_COMPLETE" for entry in p0),
        "p0_static_partial": sum(entry["static_status"] == "STATIC_PARTIAL" for entry in p0),
        "p0_runtime_visual_pass": 0,
        "p0_runtime_not_tested": len(p0),
        "failure_count": len(failures) + len(parse_errors),
    }
    payload = {"stats": stats, "p0": p0, "item_models": item_models, "parse_errors": parse_errors}
    REPORTS.mkdir(exist_ok=True)
    json_path = REPORTS / f"visual_parity_audit_v{args.version}.json"
    md_path = REPORTS / f"VISUAL_PARITY_AUDIT_V{args.version.replace('.', '_')}.md"
    json_path.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    lines = [f"# TC4 visual parity audit v{args.version}", "",
             "`SOURCE_CONTRACT_COMPLETE` означает только замкнутый source/resource contract. `STATIC_PARTIAL` означает неполную систему. Runtime и визуальный PASS не присваиваются без артефактов.", "",
             f"- Item model JSON: **{stats['item_model_json']}**",
             f"- P0 source contracts complete: **{stats['p0_source_contract_complete']} / {stats['p0_total']}**",
             f"- P0 static partial: **{stats['p0_static_partial']} / {stats['p0_total']}**",
             f"- P0 runtime visual PASS: **0 / {stats['p0_total']}**", "", "## P0", ""]
    for entry in p0:
        lines += [f"### {entry['title']}", "", f"- Static: **{entry['static_status']}**", "- Runtime visual: **NOT TESTED**",
                  f"- TC4 contract: {entry['original_contract']}"]
        if entry["missing"]:
            lines.append("- Missing: " + ", ".join(entry["missing"]))
        lines.append("")
    md_path.write_text("\n".join(lines), encoding="utf-8")
    print(f"Visual parity audit: complete={stats['p0_source_contract_complete']}, partial={stats['p0_static_partial']}, runtime PASS 0/{stats['p0_total']}")
    print(json_path.relative_to(ROOT))
    return 1 if args.fail_on_p0 and (failures or parse_errors) else 0


if __name__ == "__main__":
    raise SystemExit(main())
