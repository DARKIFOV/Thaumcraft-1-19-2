#!/usr/bin/env python3
import json
import re
from pathlib import Path

root = Path(__file__).resolve().parents[1]
registry_path = root / "src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java"
registry = registry_path.read_text(encoding="utf-8")

# Every formerly generic item ID must now own a concrete/canonical branch.
expected_classes = {
    "tc4_bauble_amulet": "TC4BaubleItem",
    "tc4_bauble_belt": "TC4BaubleItem",
    "tc4_bauble_ring": "TC4BaubleItem",
    "tc4_bauble_ring_iron": "TC4BaubleItem",
    "tc4_runic_ring_lesser": "TC4RunicBaubleItem",
    "tc4_runic_ring": "TC4RunicBaubleItem",
    "tc4_runic_ring_charged": "TC4RunicBaubleItem",
    "tc4_runic_ring_regen": "TC4RunicBaubleItem",
    "tc4_runic_amulet": "TC4RunicBaubleItem",
    "tc4_runic_amulet_emergency": "TC4RunicBaubleItem",
    "tc4_runic_girdle": "TC4RunicBaubleItem",
    "tc4_runic_girdle_kinetic": "TC4RunicBaubleItem",
    "tc4_vis_amulet": "TC4VisAmuletItem",
    "tc4_vis_amulet_lesser": "TC4VisAmuletItem",
    "tc4_focus": "TC4LegacyFocusComponentItem",
    "tc4_focus_reversal": "TC4LegacyFocusComponentItem",
    "tc4_focus_excavation": "WandFocusItem",
    "tc4_focus_fire": "WandFocusItem",
    "tc4_focus_frost": "WandFocusItem",
    "tc4_focus_portablehole": "WandFocusItem",
    "tc4_focus_primal": "WandFocusItem",
    "tc4_focus_shock": "WandFocusItem",
    "tc4_focus_trade": "WandFocusItem",
    "tc4_focus_warding": "WandFocusItem",
    "tc4_mana_bean": "TC4ManaBeanItem",
    "tc4_nitor": "NitorItem",
    "tc4_phial": "EssentiaPhialItem",
    "tc4_charm": "TC4SimpleResourceItem",
    "tc4_clothboots": "TC4ClothRobeItem",
    "tc4_clothbootsover": "TC4ClothRobeItem",
    "tc4_clothchest": "TC4ClothRobeItem",
    "tc4_clothchestover": "TC4ClothRobeItem",
    "tc4_clothlegs": "TC4ClothRobeItem",
    "tc4_clothlegsover": "TC4ClothRobeItem",
    "tc4_cultistboots": "TC4CultistArmorItem",
    "tc4_cultistrobehelm": "TC4CultistArmorItem",
    "tc4_cultistrobechest": "TC4CultistArmorItem",
    "tc4_cultistrobelegs": "TC4CultistArmorItem",
    "tc4_cultistplatehelm": "TC4CultistArmorItem",
    "tc4_cultistplatechest": "TC4CultistArmorItem",
    "tc4_cultistplatelegs": "TC4CultistArmorItem",
    "tc4_cultistplateleaderhelm": "TC4CultistArmorItem",
    "tc4_cultistplateleaderchest": "TC4CultistArmorItem",
    "tc4_cultistplateleaderlegs": "TC4CultistArmorItem",
    "tc4_voidrobeboots": "TC4VoidRobeItem",
    "tc4_voidrobehelm": "TC4VoidRobeItem",
    "tc4_voidrobechest": "TC4VoidRobeItem",
    "tc4_voidrobechestover": "TC4VoidRobeItem",
    "tc4_voidrobelegs": "TC4VoidRobeItem",
    "tc4_voidrobelegsover": "TC4VoidRobeItem",
}
for legacy_id, class_name in expected_classes.items():
    assert re.search(rf'case\s+[^;]*"{legacy_id}"[^;]*->\s+new\s+{class_name}\b', registry, re.S), \
        f"missing concrete simple-mechanics alias: {legacy_id} -> {class_name}"

# Focus family maps to actual already-ported focus implementations.
for token in (
    "WandFocusType.EXCAVATION", "WandFocusType.FIRE", "WandFocusType.FROST",
    "WandFocusType.PORTABLE_HOLE", "WandFocusType.PRIMAL", "WandFocusType.SHOCK",
    "WandFocusType.EQUAL_TRADE", "WandFocusType.WARDING",
):
    assert token in registry, f"missing focus mapping: {token}"

mana = (root / "src/main/java/com/darkifov/thaumcraft/item/simple/TC4ManaBeanItem.java").read_text(encoding="utf-8")
for token in ("alwaysEat().meat()", "RANDOM_EFFECTS", "nextInt(4) == 0", "PlayerAspectKnowledge.addPool", "syncAspectKnowledge"):
    assert token in (registry + mana), f"missing Mana Bean contract: {token}"

vis_item = (root / "src/main/java/com/darkifov/thaumcraft/item/simple/TC4VisAmuletItem.java").read_text(encoding="utf-8")
vis_runtime = (root / "src/main/java/com/darkifov/thaumcraft/item/simple/TC4VisAmuletRuntime.java").read_text(encoding="utf-8")
common = (root / "src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java").read_text(encoding="utf-8")
for token in ("2500", "25000"):
    assert token in registry, f"missing Vis Amulet capacity: {token}"
for token in ("player.tickCount % 5", "Math.min(5", "WandItem.addRealVis"):
    assert token in vis_runtime, f"missing Vis Amulet transfer contract: {token}"
assert "TC4VisAmuletRuntime.tick(player)" in common

cloth = (root / "src/main/java/com/darkifov/thaumcraft/item/simple/TC4ClothRobeItem.java").read_text(encoding="utf-8")
cultist = (root / "src/main/java/com/darkifov/thaumcraft/item/simple/TC4CultistArmorItem.java").read_text(encoding="utf-8")
void = (root / "src/main/java/com/darkifov/thaumcraft/item/simple/TC4VoidRobeItem.java").read_text(encoding="utf-8")
material = (root / "src/main/java/com/darkifov/thaumcraft/item/simple/TC4RobeArmorMaterial.java").read_text(encoding="utf-8")
warp = (root / "src/main/java/com/darkifov/thaumcraft/runic/TC4WarpingGearAdapter.java").read_text(encoding="utf-8")
revealer = (root / "src/main/java/com/darkifov/thaumcraft/client/TC4RevealerHudAdapter.java").read_text(encoding="utf-8")
node_renderer = (root / "src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java").read_text(encoding="utf-8")
for token in ("SPECIAL(25, new int[] {1, 2, 3, 1}, 25", "FORTRESS(40, new int[] {3, 6, 7, 3}, 25"):
    assert token in material, f"missing armor material parity: {token}"
for token in ('new ResourceLocation("thaumcraft", "tc4_cloth")', "armorSlot == EquipmentSlot.FEET ? 1 : 2", "DyeableLeatherItem"):
    assert token in cloth, f"missing cloth robe contract: {token}"
for token in ("ingredient.is(Items.IRON_INGOT)", "family == Family.ROBE ? 1 : 0", "Family.LEADER"):
    assert token in cultist, f"missing cultist armor contract: {token}"
for token in ("ingots/void_metal", "isRevealingHelmet", "return 5", "holder.tickCount % 20"):
    assert token in void, f"missing Void Robe contract: {token}"
for item_id in ("tc4_voidrobehelm", "tc4_voidrobechest", "tc4_voidrobelegs", "tc4_voidrobeboots"):
    assert f'Map.entry("thaumcraft:{item_id}", 2)' in warp, f"Void Robe warp must be 2: {item_id}"
for item_id in ("tc4_cultistplatehelm", "tc4_cultistplatechest", "tc4_cultistplatelegs"):
    assert f'Map.entry("thaumcraft:{item_id}"' not in warp, f"cultist plate must not invent warp: {item_id}"
assert "instanceof GogglesOfRevealingItem" in revealer
assert "instanceof TC4VoidRobeItem robe && robe.isRevealingHelmet()" in revealer
assert "TC4RevealerHudAdapter.isHeadRevealerStack" in node_renderer

# Curios uses its documented vanilla tag surface and remains optional.
curios = root / "src/main/resources/data/curios/tags/items"
ring = json.loads((curios / "ring.json").read_text(encoding="utf-8"))["values"]
necklace = json.loads((curios / "necklace.json").read_text(encoding="utf-8"))["values"]
belt = json.loads((curios / "belt.json").read_text(encoding="utf-8"))["values"]
assert "thaumcraft:tc4_runic_ring_charged" in ring
assert "thaumcraft:tc4_vis_amulet" in necklace
assert "thaumcraft:tc4_runic_girdle_kinetic" in belt
adapter = (root / "src/main/java/com/darkifov/thaumcraft/runic/TC4BaubleSlotAdapter.java").read_text(encoding="utf-8")
assert "Class.forName(CURIOS_API_CLASS)" in adapter and "Optional dependency absent" in adapter

# Exact reproducible remaining upper bound.
entries_part = registry.split("private static final Map<String, Entry> BY_ID")[0]
entries = re.findall(r'e\("([^"]+)"\s*,', entries_part)
switch = registry[registry.index("private static Item createItem"):registry.index("public static Entry[] entries")]
case_groups = re.findall(r'case\s+((?:"[^"]+"\s*,?\s*)+)->', switch)
dedicated = {item_id for group in case_groups for item_id in re.findall(r'"([^"]+)"', group)}
pre_registered = {
    "tc4_crystalessence", "tc4_block_banner", "tc4_bath_salts", "tc4_bucket_pure", "tc4_bucket_death",
    "tc4_block_arcane_spa", "tc4_block_arcane_bore_base", "tc4_block_arcane_bore", "tc4_block_arcane_ear",
    "tc4_block_arcane_lamp", "tc4_block_arcane_pressure_plate", "tc4_block_levitator", "tc4_jar_brain",
    "tc4_mirrorframe", "tc4_mirrorframe2", "tc4_mirrorhand",
}
skipped = {"tc4_block_focal_manipulator", "tc4_block_thaumium", "tc4_block_tallow", "tc4_block_crystal_cluster"}
fallback = sorted(x for x in entries if x not in dedicated and x not in pre_registered and x not in skipped)
remaining_items = {x for x in fallback if not x.startswith("tc4_block_")}
expected_remaining_items = {
    "tc4_arcanedoor", "tc4_crimson_blade", "tc4_ironbell", "tc4_lightningring",
    "tc4_ob_placer", "tc4_resonator", "tc4_sinister_stone", "tc4_sinister_stone_active",
}
assert len(fallback) <= 50, f"fallback count regressed above 50: {len(fallback)}"
assert remaining_items <= expected_remaining_items, f"unexpected item fallback set: {sorted(remaining_items)}"
assert sum(x.startswith("tc4_block_") for x in fallback) <= 42
assert not (set(expected_classes) & set(fallback))

manifest = json.loads((root / "runtime_artifacts/runtime_test_manifest.template.json").read_text(encoding="utf-8"))
assert manifest["version"] in ("11.63.36", "11.63.37", "11.63.38", "11.63.39", "11.63.40", "11.63.41", "11.63.42", "11.63.43", "11.63.44", "11.63.45", "11.63.46", "11.63.47", "11.63.48", "11.63.49", "11.63.50", "11.63.52", "11.63.53", "11.63.54", "11.63.55", "11.63.56", "11.63.58", "11.63.59", "11.63.60", "11.63.61")
assert len(manifest["tests"]) >= 273
manifest_ids = {test["id"] for test in manifest["tests"]}
for runtime_id in (
    "items.tc4_simple_focus_family_casting", "items.tc4_runic_baubles_curios",
    "items.tc4_vis_amulet_transfer", "items.tc4_mana_bean_aspect_food",
    "armor.tc4_cloth_cultist_void_families", "items.tc4_nitor_phial_charm_aliases",
):
    assert runtime_id in manifest_ids, f"missing runtime scenario: {runtime_id}"

for workflow_name in ("build.yml", "release.yml"):
    workflow = (root / ".github/workflows" / workflow_name).read_text(encoding="utf-8")
    assert "python3 tools/tc4_116336_simple_item_mechanics_guard.py" in workflow
assert "11.63.36 — Simple Item Mechanics Consolidation" in (root / "README.md").read_text(encoding="utf-8")
assert "11.63.36 Simple item mechanics runtime notes" in (root / "KNOWN_DEVIATIONS.md").read_text(encoding="utf-8")
for path in (
    "TC4_11.63.36_SIMPLE_ITEM_MECHANICS_PORT_REPORT_RU.md",
    "TC4_11.63.36_BUILD_STATUS.txt",
    "TC4_11.63.36_REMAINING_OBJECTS_AUDIT_RU.md",
    "reports/remaining_objects_v11.63.36.json",
):
    assert (root / path).is_file(), f"missing stage artifact: {path}"
print("TC4 v11.63.36 simple item mechanics consolidation guard: PASS")
