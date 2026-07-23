#!/usr/bin/env python3
import json
import re
from pathlib import Path

root = Path(__file__).resolve().parents[1]
registry = (root / "src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java").read_text(encoding="utf-8")
main = (root / "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java").read_text(encoding="utf-8")
item = (root / "src/main/java/com/darkifov/thaumcraft/item/TC4SanityCheckerItem.java").read_text(encoding="utf-8")
overlay = (root / "src/main/java/com/darkifov/thaumcraft/client/SanityCheckerOverlayEvents.java").read_text(encoding="utf-8")
en = json.loads((root / "src/main/resources/assets/thaumcraft/lang/en_us.json").read_text(encoding="utf-8"))

expected = {
    "tc4_thaumonomicon": "ThaumonomiconItem",
    "tc4_thaumonomiconcheat": "CreativeThaumonomiconItem",
    "tc4_researchnotes": "ResearchNoteItem",
    "tc4_discovery": "ResearchNoteItem",
    "tc4_focuspouch": "FocusPouchItem",
    "tc4_focuspouchbauble": "FocusPouchBaubleItem",
    "tc4_gogglesrevealing": "GogglesOfRevealingItem",
    "tc4_inkwell": "ScribingToolsItem",
    "tc4_soap": "SanitySoapItem",
    "tc4_sanitychecker": "TC4SanityCheckerItem",
}
for legacy_id, class_name in expected.items():
    assert re.search(rf'case "{legacy_id}" -> new {class_name}\b', registry), f"missing functional alias: {legacy_id} -> {class_name}"

assert "extends Item" in item and "properties.stacksTo(1)" in item
assert 'Component.translatable("tooltip.thaumcraft.sanity_checker")' in item
assert "RenderGuiOverlayEvent.Post" in overlay
assert "VanillaGuiOverlay.HOTBAR.type()" in overlay
assert "getMainHandItem()" in overlay and "instanceof TC4SanityCheckerItem" in overlay
for token in ("permanentWarp()", "stickyWarp()", "temporaryWarp()", "BAR_HEIGHT = 48"):
    assert token in overlay, f"missing sanity HUD contract: {token}"
for atlas in ("152, 0, 20, 76", "176, 0, 20, 76", "200, sourceOffset", "216, 0, 20, 16"):
    assert atlas in overlay, f"missing original hud.png region: {atlas}"
for tint in ("1.0F, 0.5F, 1.0F", "0.75F, 0.0F, 0.75F", "0.5F, 0.0F, 0.5F"):
    assert tint in overlay, f"missing original warp tint: {tint}"

for snippet in (
    ".stacksTo(1).rarity(Rarity.UNCOMMON)",
    ".stacksTo(1).rarity(Rarity.EPIC)",
    "GogglesOfRevealingItem(new Item.Properties().tab(THAUMCRAFT_TAB).rarity(Rarity.RARE))",
    ".stacksTo(1).rarity(Rarity.RARE)",
    "FocusPouchBaubleItem(new Item.Properties().tab(THAUMCRAFT_TAB).rarity(Rarity.RARE))",
):
    assert snippet in main, f"missing canonical legacy property: {snippet}"

assert en["item.thaumcraft.tc4_sanitychecker"] == "Sanity Checker"
assert en["item.thaumcraft.goggles_of_revealing"] == "Goggles of Revealing"
manifest = json.loads((root / "runtime_artifacts/runtime_test_manifest.template.json").read_text(encoding="utf-8"))
assert tuple(map(int, manifest["version"].split("."))) >= (11, 63, 35)
manifest_ids = {test["id"] for test in manifest["tests"]}
for runtime_id in (
    "items.tc4_legacy_thaumonomicons_open_modes",
    "research.tc4_legacy_notes_and_discovery_nbt",
    "items.tc4_legacy_focus_pouches_inventory",
    "items.tc4_legacy_research_utilities_behavior",
    "hud.tc4_sanity_checker_three_warp_buckets",
):
    assert runtime_id in manifest_ids, f"missing runtime protocol: {runtime_id}"

# Recompute the fallback count independently.
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
assert len(fallback) <= 100, f"fallback count regressed above 100: {len(fallback)}"
assert sum(not x.startswith("tc4_block_") for x in fallback) <= 58
assert sum(x.startswith("tc4_block_") for x in fallback) <= 42
assert not (set(expected) & set(fallback)), "new aliases still appear in fallback"
for workflow_name in ("build.yml", "release.yml"):
    workflow = (root / ".github/workflows" / workflow_name).read_text(encoding="utf-8")
    assert "python3 tools/tc4_116335_research_utility_aliases_guard.py" in workflow
assert "11.63.35 — Research Utilities and Sanity Checker HUD" in (root / "README.md").read_text(encoding="utf-8")
assert "11.63.35 Research utilities and Sanity Checker runtime notes" in (root / "KNOWN_DEVIATIONS.md").read_text(encoding="utf-8")
assert (root / "TC4_11.63.35_RESEARCH_UTILITY_ALIASES_PORT_REPORT_RU.md").is_file()
assert (root / "TC4_11.63.35_REMAINING_OBJECTS_AUDIT_RU.md").is_file()
assert (root / "reports/remaining_objects_v11.63.35.json").is_file()
print("TC4 v11.63.35 research utility aliases and sanity HUD guard: PASS")
