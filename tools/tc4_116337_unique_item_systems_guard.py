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

build = text("build.gradle")
mods = text("src/main/resources/META-INF/mods.toml")
assert any(f"version = '{v}'" in build for v in ("11.63.37", "11.63.38", "11.63.39", "11.63.40", "11.63.41", "11.63.42", "11.63.43", "11.63.44", "11.63.45", "11.63.46", "11.63.47"))
assert any(f'version="{v}"' in mods for v in ("11.63.37", "11.63.38", "11.63.39", "11.63.40", "11.63.41", "11.63.42", "11.63.43", "11.63.44", "11.63.45", "11.63.46", "11.63.47"))

registry = text("src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java")
for legacy_id, class_name in {
    "tc4_crimson_blade": "TC4CrimsonBladeItem",
    "tc4_ironbell": "GolemBellItem",
    "tc4_lightningring": "TC4LegacyTextureArtifactItem",
    "tc4_ob_placer": "TC4ObeliskPlacerItem",
    "tc4_resonator": "TC4ResonatorItem",
    "tc4_sinister_stone": "TC4SinisterStoneItem",
    "tc4_sinister_stone_active": "TC4SinisterStoneItem",
}.items():
    assert re.search(rf'case\s+[^;]*"{legacy_id}"[^;]*->\s+new\s+{class_name}\b', registry, re.S), \
        f"missing unique item mapping: {legacy_id} -> {class_name}"

need("src/main/java/com/darkifov/thaumcraft/item/TC4CrimsonToolTier.java",
     "getUses() { return 200; }", "getSpeed() { return 8.0F; }",
     "getAttackDamageBonus() { return 3.5F; }", "getLevel() { return 4; }",
     "getEnchantmentValue() { return 20; }", 'get("tc4_charm")')
need("src/main/java/com/darkifov/thaumcraft/item/TC4CrimsonBladeItem.java",
     "extends SwordItem", "entity.tickCount % 20 == 0", "MobEffects.WITHER, 60",
     "MobEffects.WEAKNESS, 120", "isValidRepairItem")
need("src/main/java/com/darkifov/thaumcraft/runic/TC4WarpingGearAdapter.java",
     'Map.entry("thaumcraft:tc4_crimson_blade", 2)')

need("src/main/java/com/darkifov/thaumcraft/item/TC4ResonatorItem.java",
     "EssentiaTubeBlockEntity", "EssentiaJarBlockEntity", "EssentiaReservoirBlockEntity",
     "AlembicBlockEntity", "getSuctionType(clickedFace)", "getSuctionAmount(clickedFace)",
     'TC4Sounds.event("alembicknock")')
need("src/main/java/com/darkifov/thaumcraft/item/TC4ObeliskPlacerItem.java",
     "getAbilities().instabuild", "int[] offsets = {1, 3, 4, 5, 6, 7}",
     "ELDRITCH_ALTAR", "ELDRITCH_STONE", "ELDRITCH_OBELISK")
need("src/main/java/com/darkifov/thaumcraft/item/TC4SinisterStoneItem.java",
     "AuraNodeType.DARK", "256.0D * 256.0D", "ClipContext.Block.COLLIDER",
     "forcedActiveAlias", "entity.tickCount % 10")
need("src/main/java/com/darkifov/thaumcraft/item/TC4LegacyTextureArtifactItem.java",
     "Save-compatible registry alias", "legacy_texture_artifact")
need("src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java",
     'get("tc4_sinister_stone")', 'get("tc4_sinister_stone_active")',
     'new ResourceLocation(ThaumcraftMod.MOD_ID, "active")',
     "TC4SinisterStoneItem.modelActive")

need("src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneAccessTarget.java",
     "keyBindingPos", "authorizedUsers", "sharesAuthorization")
need("src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneDoorBlockEntity.java",
     "implements ArcaneAccessTarget", 'putUUID("Owner"', '"StandardAccess"', '"FullAccess"',
     "setChangedAndSync", "ClientboundBlockEntityDataPacket")
need("src/main/java/com/darkifov/thaumcraft/block/ArcaneDoorBlock.java",
     "ArcanePressurePlateBlock", "door.sharesAuthorization(plate)",
     "playerWillDestroy", "UPDATE_SUPPRESS_DROPS", "PushReaction.BLOCK", "canEntityDestroy")
need("src/main/java/com/darkifov/thaumcraft/item/ArcaneDoorItem.java",
     "determineHinge", "initializeOwner(player)", "shrink(1)")
need("src/main/java/com/darkifov/thaumcraft/item/ArcaneKeyItem.java",
     "instanceof ArcaneAccessTarget", "target.keyBindingPos()")
need("src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java",
     "instanceof ArcaneDoorBlockEntity", "door.isOwner(player)")
need("src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java",
     'BLOCKS.register("tc4_arcane_door_block"', 'ITEMS.register("tc4_arcanedoor"',
     'Map.entry("tc4_arcanedoor", ARCANE_DOOR_ITEM)', "ARCANE_DOOR_BLOCK_ENTITY")

for path in (
    "src/main/resources/assets/thaumcraft/blockstates/tc4_arcane_door_block.json",
    "src/main/resources/assets/thaumcraft/models/block/tc4_arcane_door_lower.json",
    "src/main/resources/assets/thaumcraft/models/block/tc4_arcane_door_upper.json",
    "src/main/resources/assets/thaumcraft/models/item/tc4_sinister_stone.json",
    "src/main/resources/data/thaumcraft/loot_tables/blocks/tc4_arcane_door_block.json",
):
    assert (root / path).is_file(), f"missing resource: {path}"

model = json.loads(text("src/main/resources/assets/thaumcraft/models/item/tc4_sinister_stone.json"))
assert any(x.get("predicate", {}).get("thaumcraft:active") == 1.0 for x in model.get("overrides", []))

# Exact reproducible fallback closure.
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
assert len(fallback) <= 42, f"expected no more than 42 remaining block aliases, got {len(fallback)}"
assert all(x.startswith("tc4_block_") for x in fallback), f"item-like fallback remains: {fallback}"

manifest = json.loads(text("runtime_artifacts/runtime_test_manifest.template.json"))
assert manifest["version"] in ("11.63.37", "11.63.38", "11.63.39", "11.63.40", "11.63.41", "11.63.42", "11.63.43", "11.63.44", "11.63.45", "11.63.46", "11.63.47", "11.63.48", "11.63.49", "11.63.50", "11.63.52", "11.63.53", "11.63.54", "11.63.55", "11.63.56", "11.63.58", "11.63.59", "11.63.60", "11.63.61")
assert len(manifest["tests"]) >= 280
ids = {test["id"] for test in manifest["tests"]}
for test_id in (
    "items.tc4_arcane_door_owner_keys_plate", "items.tc4_crimson_blade_full_behavior",
    "items.tc4_ironbell_alias", "items.tc4_resonator_transport_readout",
    "items.tc4_obelisk_placer_structure", "items.tc4_sinister_stone_dark_node_visibility",
    "items.tc4_texture_only_aliases",
):
    assert test_id in ids, f"missing runtime scenario: {test_id}"

for workflow_name in ("build.yml", "release.yml"):
    workflow = text(f".github/workflows/{workflow_name}")
    assert "python3 tools/tc4_116337_unique_item_systems_guard.py" in workflow
assert "11.63.37 — Unique Item Systems and Item Fallback Closure" in text("README.md")
assert "11.63.37 Unique item systems runtime notes" in text("KNOWN_DEVIATIONS.md")
for path in (
    "TC4_11.63.37_UNIQUE_ITEM_SYSTEMS_PORT_REPORT_RU.md",
    "TC4_11.63.37_BUILD_STATUS.txt",
    "TC4_11.63.37_REMAINING_OBJECTS_AUDIT_RU.md",
    "reports/remaining_objects_v11.63.37.json",
):
    assert (root / path).is_file(), f"missing stage artifact: {path}"

print("TC4 v11.63.37 unique item systems and item fallback closure guard: PASS")
