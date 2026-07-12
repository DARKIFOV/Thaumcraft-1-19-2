#!/usr/bin/env python3
"""Regression guard for v11.62.47 Arcane Workbench and wand-cost parity."""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ERRORS: list[str] = []


def read(rel: str) -> str:
    path = ROOT / rel
    if not path.is_file():
        ERRORS.append(f"missing file: {rel}")
        return ""
    return path.read_text(encoding="utf-8")


def require(text: str, token: str, label: str) -> None:
    if token not in text:
        ERRORS.append(f"{label}: missing {token!r}")


def forbid(text: str, token: str, label: str) -> None:
    if token in text:
        ERRORS.append(f"{label}: forbidden legacy token {token!r}")


build = read("build.gradle")
mods = read("src/main/resources/META-INF/mods.toml")
build_match = re.search(r"^version\s*=\s*'([0-9]+(?:\.[0-9]+){2})(?:-[A-Za-z0-9.-]+)?'", build, re.MULTILINE)
mods_match = re.search(r'^version="([0-9]+(?:\.[0-9]+){2})(?:-[A-Za-z0-9.-]+)?"', mods, re.MULTILINE)
def version_tuple(value: str) -> tuple[int, ...]:
    return tuple(int(part) for part in value.split("."))
for label, match in (("build.gradle", build_match), ("mods.toml", mods_match)):
    if match is None or version_tuple(match.group(1)) < (11, 62, 47):
        ERRORS.append(f"{label}: current version must preserve v11.62.47 or later")

recipe = read("src/main/java/com/darkifov/thaumcraft/arcane/ArcaneWorkbenchRecipe.java")
for token in (
    "public List<ResourceLocation> normalizedLooseIngredients()",
    "normalized.remove(index)",
    "ingredientListContainsCatalystOccurrence",
    "Collections.unmodifiableList(ingredients)",
):
    require(recipe, token, "ArcaneWorkbenchRecipe")

workbench = read("src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java")
for token in (
    "matchesLooseRecipeExactly(recipe, catalystSlot)",
    "boolean[] usedSlots",
    "if (!getItem(slot).isEmpty() && !usedSlots[index])",
    "consumeMatchedArcaneGrid(player, catalystSlot)",
    "catalystSlot == SLOT_LEGACY_CATALYST",
    "WandItem.modifiedVisCost(wand, player",
    "WandItem.consumeVisCost(wand, player",
):
    require(workbench, token, "ArcaneWorkbenchBlockEntity")
for token in (
    "Map<ResourceLocation, Integer> required",
    "available.merge",
    "consumeIngredients(recipe",
    "consumePatternIngredients(recipe",
):
    forbid(workbench, token, "ArcaneWorkbenchBlockEntity")

wand = read("src/main/java/com/darkifov/thaumcraft/block/WandItem.java")
for token in (
    "public static float consumptionModifier",
    "TC4VisDiscountRuntime.totalDiscount(player, aspect)",
    "if (!crafting && WandFocusRuntime.hasFocus(wandStack))",
    "return Math.max(0, (int) (baseAmount * consumptionModifier",
    "modifiedVisCost(stack, player, aspect, amount * 100, true)",
):
    require(wand, token, "WandItem")
forbid(wand, "Math.ceil(baseAmount * modifier)", "WandItem")

focus = read("src/main/java/com/darkifov/thaumcraft/wand/WandFocusRuntime.java")
for token in (
    "modifiedFocusVisCost(wandStack, player",
    "return WandItem.modifiedVisCost(wandStack, player, aspect, baseAmount, false)",
):
    require(focus, token, "WandFocusRuntime")
forbid(focus, "Math.ceil(baseAmount * Math.max", "WandFocusRuntime")

runtime = read("src/main/java/com/darkifov/thaumcraft/wand/TC4VisDiscountRuntime.java")
for token in (
    "for (ItemStack armor : player.getArmorSlots())",
    "TC4BaubleSlotAdapter.findEquippedBaubles(serverPlayer)",
    "return Math.max(0, totalPercent) / 100.0F",
):
    require(runtime, token, "TC4VisDiscountRuntime")

gear = read("src/main/java/com/darkifov/thaumcraft/wand/TC4VisDiscountGear.java")
require(gear, "int getVisDiscount", "TC4VisDiscountGear")

goggles = read("src/main/java/com/darkifov/thaumcraft/block/GogglesOfRevealingItem.java")
require(goggles, "implements TC4VisDiscountGear", "GogglesOfRevealingItem")
require(goggles, "return visDiscount(stack, wearer)", "GogglesOfRevealingItem")

menu = read("src/main/java/com/darkifov/thaumcraft/menu/ArcaneWorkbenchMenu.java")
require(menu, "modifiedArcaneCost(recipe, aspect, player)", "ArcaneWorkbenchMenu")

overlay = read("src/main/java/com/darkifov/thaumcraft/client/WandVisOverlayEvents.java")
require(overlay, "modifiedVisCost(wandStack, minecraft.player, aspect, baseFocusCost, false)", "WandVisOverlayEvents")


build_workflow = read(".github/workflows/build.yml")
release_workflow = read(".github/workflows/release.yml")
for workflow, label in ((build_workflow, "build workflow"), (release_workflow, "release workflow")):
    if re.search(r"THAUMCRAFT_LEGACY_REBUILD_V11_62_[0-9]+_EXPERT_FULL_TECHNICAL_REPORT_R[0-9]+\.md", workflow) is None:
        ERRORS.append(f"{label}: missing consolidated expert report")
    for legacy_report in (
        "REPORT_V11_62_47",
        "PORT_STATUS_V11_62_47",
        "RUNTIME_TEST_MATRIX_V11_62_47",
        "REGISTRY_AUDIT_V11_62_47.md",
        "ITEM_VISUAL_AUDIT_V11_62_47.md",
        "MODEL_TRANSFORM_AUDIT_V11_62_47.md",
        "BEWLR_CONTRACT_AUDIT_V11_62_47.md",
        "AURA_NODE_PARITY_AUDIT_V11_62_47.md",
    ):
        forbid(workflow, legacy_report, label)

# Data-driven regression: all materialized shapeless recipes must normalize to
# exactly their original occupied-slot count, independently of whether the
# materializer included the compatibility catalyst in the ingredient list.
recipe_root = ROOT / "src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench"
shapeless: list[tuple[str, str, list[str], list[str]]] = []
for path in sorted(recipe_root.glob("*.json")):
    data = json.loads(path.read_text(encoding="utf-8"))
    pattern = data.get("pattern", [])
    if pattern or data.get("tc4_kind") != "ARCANE_SHAPELESS":
        continue
    catalyst = data.get("catalyst", "")
    ingredients = list(data.get("ingredients", []))
    normalized = ingredients.copy()
    if catalyst in normalized:
        normalized.remove(catalyst)
    shapeless.append((path.stem, catalyst, ingredients, normalized))

if len(shapeless) != 5:
    ERRORS.append(f"expected 5 materialized shapeless arcane recipes, found {len(shapeless)}")

expected_slots = {
    "tc4_mirrorglass": 2,
    "tc4_tubefilter": 2,
    "tc4_tubeoneway": 2,
    "tc4_tuberestrict": 2,
    "tc4_tubevalve": 2,
}
for name, catalyst, ingredients, normalized in shapeless:
    if not catalyst:
        ERRORS.append(f"{name}: missing compatibility catalyst")
        continue
    occupied_slots = 1 + len(normalized)
    expected = expected_slots.get(name)
    if expected is None:
        ERRORS.append(f"unexpected shapeless recipe: {name}")
    elif occupied_slots != expected:
        ERRORS.append(
            f"{name}: normalized occupied-slot count {occupied_slots}, expected {expected}; "
            f"catalyst={catalyst}, ingredients={ingredients}"
        )

# Exact-slot behavior sanity checks: a stack count cannot satisfy two recipe
# occurrences, and one unrelated occupied slot must make the recipe fail.
def exact_match(required: list[str], slots: list[str]) -> bool:
    remaining = list(required)
    for item in slots:
        if not item:
            continue
        try:
            remaining.remove(item)
        except ValueError:
            return False
    return not remaining

if exact_match(["tube", "tube"], ["tube"]):
    ERRORS.append("slot semantics: one occupied stack incorrectly satisfies two identical inputs")
if exact_match(["tube", "filter"], ["tube", "filter", "dirt"]):
    ERRORS.append("slot semantics: unrelated extra occupied slot was accepted")
if not exact_match(["tube", "filter"], ["filter", "tube"]):
    ERRORS.append("slot semantics: valid shapeless permutation was rejected")

if ERRORS:
    print("v11.62.47 arcane/wand guard: FAIL")
    for error in ERRORS:
        print(" -", error)
    raise SystemExit(1)

print(
    "v11.62.47 arcane/wand guard: OK "
    f"({len(shapeless)} shapeless recipes, exact occupied-slot matching, catalyst normalization, "
    "player-aware armor/bauble vis discounts, TC4 truncation)"
)
