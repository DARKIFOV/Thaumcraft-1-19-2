#!/usr/bin/env python3
import json
import re
from pathlib import Path

root = Path(__file__).resolve().parents[1]

def text(path: str) -> str:
    return (root / path).read_text(encoding="utf-8")

def need(path: str, *tokens: str) -> None:
    value = text(path)
    for token in tokens:
        assert token in value, f"{path}: missing {token!r}"

assert any(f"version = '{v}'" in text("build.gradle") for v in ('11.63.38','11.63.39','11.63.40', '11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50'))
assert any(f'version="{v}"' in text("src/main/resources/META-INF/mods.toml") for v in ('11.63.38','11.63.39','11.63.40', '11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50'))

registry = text("src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java")
required_mappings = {
    "tc4_block_advanced_alchemical_furnace": "ADVANCED_ALCHEMICAL_FURNACE",
    "tc4_block_alchemical_construct": "THAUMATORIUM",
    "tc4_block_alembic": "ALEMBIC",
    "tc4_block_arcane_pedestal": "ARCANE_PEDESTAL",
    "tc4_block_arcane_stone": "ARCANE_STONE",
    "tc4_block_arcane_stone_slab": "TC4_ARCANE_STONE_SLAB",
    "tc4_block_bellows": "BELLOWS",
    "tc4_block_cinderpearl": "TC4_CINDERPEARL",
    "tc4_block_essentia_jar": "ESSENTIA_JAR",
    "tc4_block_essentia_reservoir": "ESSENTIA_RESERVOIR",
    "tc4_block_essentia_tube": "ESSENTIA_TUBE",
    "tc4_block_ethereal_bloom": "TC4_ETHEREAL_BLOOM",
    "tc4_block_flux_scrubber": "FUME_DISSIPATOR",
    "tc4_block_greatwood_leaves": "GREATWOOD_LEAVES",
    "tc4_block_greatwood_log": "GREATWOOD_LOG",
    "tc4_block_greatwood_planks": "GREATWOOD_PLANKS",
    "tc4_block_greatwood_sapling": "GREATWOOD_SAPLING",
    "tc4_block_infusion_matrix": "INFUSION_MATRIX",
    "tc4_block_metal_base": "TC4_METAL_BASE",
    "tc4_block_mnemonic_matrix": "MNEMONIC_MATRIX",
    "tc4_block_node_stabilizer": "NODE_STABILIZER",
    "tc4_block_node_stabilizer_advanced": "ADVANCED_NODE_STABILIZER",
    "tc4_block_node_transducer": "NODE_TRANSDUCER",
    "tc4_block_obsidian_tile": "OBSIDIAN_TILE",
    "tc4_block_paving_travel": "TC4_PAVING_TRAVEL",
    "tc4_block_paving_warding": "TC4_PAVING_WARDING",
    "tc4_block_shimmerleaf": "TC4_SHIMMERLEAF",
    "tc4_block_silverwood_leaves": "SILVERWOOD_LEAVES",
    "tc4_block_silverwood_log": "SILVERWOOD_LOG",
    "tc4_block_silverwood_planks": "SILVERWOOD_PLANKS",
    "tc4_block_silverwood_sapling": "SILVERWOOD_SAPLING",
    "tc4_block_vishroom": "TC4_VISHROOM",
}
for legacy_id, target in required_mappings.items():
    assert re.search(rf'case\s+"{legacy_id}"\s*->\s+new\s+\w+\(ThaumcraftMod\.{target}\.get\(\)', registry), \
        f"missing block alias mapping: {legacy_id} -> {target}"
for legacy_id, target in {
    "tc4_block_node_stabilizer": "NODE_STABILIZER",
    "tc4_block_node_stabilizer_advanced": "ADVANCED_NODE_STABILIZER",
    "tc4_block_node_transducer": "NODE_TRANSDUCER",
}.items():
    assert target in registry and f'case "{legacy_id}"' in registry

# Six exact totem variants must not collapse to one state.
for suffix in ("base", "1", "2", "3", "4", "shaded"):
    legacy_id=f"tc4_block_obsidian_totem_{suffix}"
    assert legacy_id in registry
    assert f"TC4_OBSIDIAN_TOTEM_{suffix.upper()}" in registry

need("src/main/java/com/darkifov/thaumcraft/block/TC4MagicalPlantBlock.java",
     "enum Kind", "VISHROOM", "MobEffects.CONFUSION, 200", "ETHEREAL_BLOOM",
     "purifyOneTaintBlock", "TAINTED_SOIL", "FLUX_GAS", "scheduleTick(pos, this, 20)")
need("src/main/java/com/darkifov/thaumcraft/block/TC4PavingStoneBlock.java",
     "TRAVEL", "WARDING", "MobEffects.MOVEMENT_SPEED, 40, 1",
     "MobEffects.JUMP, 40, 0", "hasNeighborSignal(pos)", "!(living instanceof Player)",
     "!living.isOnGround()", "scheduleTick(pos, this, 5)")
need("src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java",
     'BLOCKS.register("tc4_block_shimmerleaf"', 'BLOCKS.register("tc4_block_ethereal_bloom"',
     'BLOCKS.register("tc4_block_arcane_stone_slab"', 'BLOCKS.register("tc4_block_paving_travel"',
     'BLOCKS.register("tc4_block_paving_warding"')
need("src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java",
     "TC4_SHIMMERLEAF.get()", "TC4_CINDERPEARL.get()", "TC4_ETHEREAL_BLOOM.get()", "TC4_VISHROOM.get()")

exact_ids = [
    "tc4_block_shimmerleaf", "tc4_block_cinderpearl", "tc4_block_ethereal_bloom", "tc4_block_vishroom",
    "tc4_block_arcane_stone_slab", "tc4_block_metal_base",
    "tc4_block_obsidian_totem_base", "tc4_block_obsidian_totem_1", "tc4_block_obsidian_totem_2",
    "tc4_block_obsidian_totem_3", "tc4_block_obsidian_totem_4", "tc4_block_obsidian_totem_shaded",
    "tc4_block_paving_travel", "tc4_block_paving_warding",
]
for block_id in exact_ids:
    assert (root / f"src/main/resources/assets/thaumcraft/blockstates/{block_id}.json").is_file(), block_id
    assert (root / f"src/main/resources/data/thaumcraft/loot_tables/blocks/{block_id}.json").is_file(), block_id
    for lang in ("en_us", "ru_ru"):
        assert f'"block.thaumcraft.{block_id}"' in text(f"src/main/resources/assets/thaumcraft/lang/{lang}.json")

pickaxe = json.loads(text("src/main/resources/data/minecraft/tags/blocks/mineable/pickaxe.json"))["values"]
stone = json.loads(text("src/main/resources/data/minecraft/tags/blocks/needs_stone_tool.json"))["values"]
for block_id in exact_ids[4:]:
    assert f"thaumcraft:{block_id}" in pickaxe, f"missing pickaxe tag: {block_id}"
    assert f"thaumcraft:{block_id}" in stone, f"missing stone-tool tag: {block_id}"

# Exact reproducible fallback target.
entries_part = registry.split("private static final Map<String, Entry> BY_ID")[0]
entries = re.findall(r'e\("([^"]+)"\s*,', entries_part)
switch = registry[registry.index("private static Item createItem"):registry.index("public static Entry[] entries")]
case_groups = re.findall(r'case\s+((?:"[^"]+"\s*,?\s*)+)->', switch)
dedicated = {item_id for group in case_groups for item_id in re.findall(r'"([^"]+)"', group)}
pre_registered = {
    "tc4_crystalessence", "tc4_block_banner", "tc4_bath_salts", "tc4_bucket_pure", "tc4_bucket_death",
    "tc4_block_arcane_spa", "tc4_block_arcane_bore_base", "tc4_block_arcane_bore", "tc4_block_arcane_ear",
    "tc4_block_arcane_lamp", "tc4_block_arcane_pressure_plate", "tc4_arcanedoor", "tc4_block_levitator",
    "tc4_jar_brain", "tc4_mirrorframe", "tc4_mirrorframe2", "tc4_mirrorhand",
}
skipped = {"tc4_block_focal_manipulator", "tc4_block_thaumium", "tc4_block_tallow", "tc4_block_crystal_cluster"}
fallback = sorted(x for x in entries if x not in dedicated and x not in pre_registered and x not in skipped)
expected = ["tc4_block_lamp_fertility", "tc4_block_lamp_growth", "tc4_block_wand_pedestal", "tc4_block_wand_pedestal_focus"]
assert set(fallback).issubset(set(expected)) and len(fallback) <= 4, f"fallback must only shrink from the four systems, got {fallback}"

manifest = json.loads(text("runtime_artifacts/runtime_test_manifest.template.json"))
assert manifest["version"] in {"11.63.38", "11.63.39", "11.63.40", "11.63.41", "11.63.42", "11.63.43", "11.63.44", "11.63.45", "11.63.46", "11.63.47", "11.63.48", "11.63.49", "11.63.50", "11.63.52", "11.63.53", "11.63.54", "11.63.55", "11.63.56", "11.63.58", "11.63.59", "11.63.60", "11.63.61"}
assert len(manifest["tests"]) >= 287
ids = {test["id"] for test in manifest["tests"]}
for test_id in (
    "blocks.tc4_legacy_tree_alias_placement", "blocks.tc4_custom_plants_effects",
    "blocks.tc4_ethereal_bloom_purification", "blocks.tc4_paving_travel",
    "blocks.tc4_paving_warding", "blocks.tc4_decorative_alias_models_loot",
    "blocks.tc4_functional_device_alias_routing",
):
    assert test_id in ids, f"missing runtime scenario: {test_id}"

for workflow_name in ("build.yml", "release.yml"):
    assert "python3 tools/tc4_116338_block_alias_normalization_guard.py" in text(f".github/workflows/{workflow_name}")
assert "11.63.38 — Block Alias Normalization, Plants and Paving" in text("README.md")
assert "11.63.38 Block alias normalization runtime notes" in text("KNOWN_DEVIATIONS.md")
for path in (
    "TC4_11.63.38_BLOCK_ALIAS_NORMALIZATION_PORT_REPORT_RU.md",
    "TC4_11.63.38_BUILD_STATUS.txt",
    "TC4_11.63.38_REMAINING_OBJECTS_AUDIT_RU.md",
    "reports/remaining_objects_v11.63.38.json",
):
    assert (root / path).is_file(), f"missing stage artifact: {path}"

print("TC4 v11.63.38 block alias normalization guard: PASS")
