#!/usr/bin/env python3
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []


def read(path: str) -> str:
    target = ROOT / path
    if not target.exists():
        errors.append(f"missing file: {path}")
        return ""
    return target.read_text(encoding="utf-8")


def must(path: str, *needles: str) -> None:
    text = read(path)
    for needle in needles:
        if needle not in text:
            errors.append(f"{path} missing {needle!r}")


def must_not(path: str, *needles: str) -> None:
    text = read(path)
    for needle in needles:
        if needle in text:
            errors.append(f"{path} must not contain {needle!r}")


def must_absent(path: str) -> None:
    if (ROOT / path).exists():
        errors.append(f"obsolete/non-original file must be absent: {path}")


def load_json(path: str) -> dict:
    target = ROOT / path
    if not target.exists():
        errors.append(f"missing file: {path}")
        return {}
    try:
        return json.loads(target.read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(f"invalid JSON {path}: {exc}")
        return {}


must(
    "build.gradle",
    "version = '11.62.13'",
    "tc4_wand_components_exact_recipes_sceptre_focus_parity",
)
must(
    "src/main/resources/META-INF/mods.toml",
    'version="11.62.13"',
    "tc4_wand_components_exact_recipes_sceptre_focus_parity",
)


must(
    "src/main/java/com/darkifov/thaumcraft/wand/WandRodType.java",
    'WOOD("wood", "wand_rod_wood", 25, 1, "ROD_wood", false, false)',
    'GREATWOOD("greatwood", "wand_rod_greatwood", 50, 3, "ROD_greatwood", false, false)',
    'OBSIDIAN("obsidian", "wand_rod_obsidian", 75, 6, "ROD_obsidian", false, false)',
    'BLAZE("blaze", "wand_rod_blaze", 75, 6, "ROD_blaze", true, false)',
    'ICE("ice", "wand_rod_ice", 75, 6, "ROD_ice", false, false)',
    'QUARTZ("quartz", "wand_rod_quartz", 75, 6, "ROD_quartz", false, false)',
    'BONE("bone", "wand_rod_bone", 75, 6, "ROD_bone", false, false)',
    'REED("reed", "wand_rod_reed", 75, 6, "ROD_reed", false, false)',
    'SILVERWOOD("silverwood", "wand_rod_silverwood", 100, 9, "ROD_silverwood", false, false)',
    'GREATWOOD_STAFF("greatwood_staff", "wand_rod_greatwood", 125, 8, "ROD_greatwood_staff", false, true)',
    'OBSIDIAN_STAFF("obsidian_staff", "wand_rod_obsidian", 175, 14, "ROD_obsidian_staff", false, true)',
    'SILVERWOOD_STAFF("silverwood_staff", "wand_rod_silverwood", 250, 24, "ROD_silverwood_staff", false, true)',
    'PRIMAL_STAFF("primal_staff", "wand_rod_primal", 250, 32, "ROD_primal_staff", true, true)',
)
must(
    "src/main/java/com/darkifov/thaumcraft/wand/WandCapType.java",
    'IRON("iron", "wand_cap_iron", 1.1F, 1, "CAP_iron")',
    'GOLD("gold", "wand_cap_gold", 1.0F, 3, "CAP_gold")',
    'THAUMIUM("thaumium", "wand_cap_thaumium", 0.9F, 6, "CAP_thaumium")',
    'COPPER("copper", "wand_cap_copper", 1.1F, 2, "CAP_copper")',
    'SILVER("silver", "wand_cap_silver", 1.0F, 4, "CAP_silver")',
    'VOID("void", "wand_cap_void", 0.8F, 9, "CAP_void")',
    "this == COPPER && (aspect == Aspect.ORDO || aspect == Aspect.PERDITIO)",
    "this == SILVER && (aspect == Aspect.AER || aspect == Aspect.TERRA || aspect == Aspect.IGNIS || aspect == Aspect.AQUA)",
)

must(
    "src/main/java/com/darkifov/thaumcraft/wand/TC4ConfigRecipesWandIndex.java",
    'addCapRecipe(recipes, "gold"',
    'addCapRecipe(recipes, "copper"',
    'addCapRecipe(recipes, "silver"',
    'addCapRecipe(recipes, "thaumium"',
    "addVoidCapRecipe(recipes)",
    '.patternRow("NNN").patternRow("N N")',
    '.patternRow(" G").patternRow("G ")',
    '.patternRow("  S").patternRow(" G ").patternRow("G  ")',
    'addStaffRodRecipe(recipes, "greatwood"',
    'addStaffRodRecipe(recipes, "obsidian"',
    'addStaffRodRecipe(recipes, "silverwood"',
    'addStaffRodRecipe(recipes, "ice"',
    'addStaffRodRecipe(recipes, "quartz"',
    'addStaffRodRecipe(recipes, "reed"',
    'addStaffRodRecipe(recipes, "blaze"',
    'addStaffRodRecipe(recipes, "bone"',
)

runtime = "src/main/java/com/darkifov/thaumcraft/wand/WandCraftingRuntime.java"
must(
    runtime,
    'sceptre.patternRow(" CH").patternRow(" RC").patternRow("C  ")',
    "isCap(table, 0, 1, assembly.cap())",
    "hasItem(table, 0, 2, CHARM_ITEM)",
    "isCap(table, 1, 2, assembly.cap())",
    "isCap(table, 2, 0, assembly.cap())",
    "slotForGrid(0, 1)",
    "slotForGrid(0, 2)",
    "slotForGrid(1, 1)",
    "slotForGrid(1, 2)",
    "slotForGrid(2, 0)",
    'case WOOD -> "stick"',
    'new ResourceLocation("minecraft", id)',
    "cap.craftCost() * rod.craftCost()",
    "cap.craftCost() * rod.craftCost() * 1.5F",
    '"SCEPTRE"',
)
must_not(
    runtime,
    'sceptre.patternRow("  C").patternRow("CR ").patternRow("HC ")',
    'case WOOD -> "wooden_wand_core"',
)

wand_item = "src/main/java/com/darkifov/thaumcraft/block/WandItem.java"
must(
    wand_item,
    "Original ItemWandCasting lets sceptres equip and cast foci",
    "WandFocusRuntime.onUsingFocusTick(stack, level, player, remainingUseDuration)",
    "WandFocusRuntime.onPlayerStoppedUsingFocus(stack, level, player, remainingUseDuration)",
    "Sceptre: +50% capacity, -10% vis cost, focus-capable",
    "public Component getName(ItemStack stack)",
    "public void onCraftedBy(ItemStack stack, Level level, Player player)",
    "WandComponentData.write(stack, data.rod(), data.cap())",
)
must_not(
    wand_item,
    "tryInstallWandComponent",
    "sceptres are crafting-only",
    "cannot equip or cast wand foci",
    "cannot cast wand foci",
    "!WandComponentData.isSceptre(wandStack) && WandFocusRuntime.shouldUseContinuously",
    "!WandComponentData.isSceptre(stack) && WandFocusRuntime.shouldUseContinuously",
)

component_data = "src/main/java/com/darkifov/thaumcraft/wand/WandComponentData.java"
must(
    component_data,
    'id.equals(new ResourceLocation("minecraft", "stick"))',
    'String object = isSceptre(stack) ? "Sceptre" : (rod.staff() ? "Staff" : "Wand")',
    "capacity = (int)Math.floor(capacity * 1.5F)",
    "modifier -= 0.1F",
)

component_item = "src/main/java/com/darkifov/thaumcraft/item/TC4ResearchComponentItem.java"
must(
    component_item,
    "WandComponentData.rodFromComponent(stack)",
    "WandComponentData.capFromComponent(stack)",
    "Inert wand cap",
    "activate it through the original infusion recipe",
    "flag.isAdvanced()",
)

basic_cap = load_json("src/main/resources/data/thaumcraft/recipes/iron_wand_cap_original_tc4.json")
if basic_cap.get("pattern") != ["NNN", "N N"]:
    errors.append("iron cap recipe pattern must exactly match ConfigRecipes.WandCapIron")
if basic_cap.get("key", {}).get("N", {}).get("item") != "minecraft:iron_nugget":
    errors.append("iron cap recipe must use iron nuggets")
if basic_cap.get("result") != {"item": "thaumcraft:tc4_wand_cap_iron", "count": 1}:
    errors.append("iron cap recipe must output exactly one original TC4 iron cap")

basic_wand = load_json("src/main/resources/data/thaumcraft/recipes/basic_wand_original_tc4.json")
if basic_wand.get("pattern") != ["  I", " S ", "I  "]:
    errors.append("basic wand recipe pattern must exactly match ConfigRecipes.WandBasic")
if basic_wand.get("key", {}).get("I", {}).get("item") != "thaumcraft:tc4_wand_cap_iron":
    errors.append("basic wand recipe must use original TC4 iron caps")
if basic_wand.get("key", {}).get("S", {}).get("item") != "minecraft:stick":
    errors.append("basic wand recipe must use vanilla stick as the wood rod")
if basic_wand.get("result") != {"item": "thaumcraft:iron_capped_wooden_wand", "count": 1}:
    errors.append("basic wand recipe must output one casting-wand carrier")

for obsolete in (
    "gold_wand_cap_original_tc4_style.json",
    "thaumium_wand_cap_original_tc4_style.json",
    "iron_wand_cap_original_tc4_style.json",
    "iron_capped_wooden_wand.json",
    "iron_capped_wooden_wand_original_tc4_style.json",
    "greatwood_wand.json",
    "silverwood_wand.json",
    "greatwood_wand_core_original_style.json",
    "silverwood_wand_core_original_style.json",
    "wooden_wand_core_original_tc4_style.json",
):
    must_absent(f"src/main/resources/data/thaumcraft/recipes/{obsolete}")

expected_infusion = {
    "tc4_wand_cap_silver.json": ("WandCapSilver", "CAP_silver", "thaumcraft:tc4_wand_cap_silver_inert", ["thaumcraft:tc4_dust"] * 2, {"POTENTIA": 8, "AURAM": 4}, 4, "thaumcraft:tc4_wand_cap_silver"),
    "tc4_wand_cap_thaumium.json": ("WandCapThaumium", "CAP_thaumium", "thaumcraft:tc4_wand_cap_thaumium_inert", ["thaumcraft:tc4_dust"] * 3, {"POTENTIA": 12, "AURAM": 6}, 5, "thaumcraft:tc4_wand_cap_thaumium"),
    "tc4_wand_cap_void.json": ("WandCapVoid", "CAP_void", "thaumcraft:tc4_wand_cap_void_inert", ["thaumcraft:tc4_dust"] * 4, {"POTENTIA": 18, "VACUOS": 18, "ALIENIS": 18, "AURAM": 18}, 8, "thaumcraft:tc4_wand_cap_void"),
    "tc4_wand_rod_obsidian.json": ("WandRodObsidian", "ROD_obsidian", "minecraft:obsidian", ["thaumcraft:balanced_shard", "thaumcraft:terra_shard"], {"TERRA": 12, "PRAECANTATIO": 6, "TENEBRAE": 6}, 3, "thaumcraft:tc4_wand_rod_obsidian"),
    "tc4_wand_rod_ice.json": ("WandRodIce", "ROD_ice", "minecraft:packed_ice", ["thaumcraft:balanced_shard", "thaumcraft:aqua_shard"], {"AQUA": 12, "PRAECANTATIO": 6, "GELUM": 6}, 3, "thaumcraft:tc4_wand_rod_ice"),
    "tc4_wand_rod_quartz.json": ("WandRodQuartz", "ROD_quartz", "minecraft:quartz_block", ["thaumcraft:balanced_shard", "thaumcraft:ordo_shard"], {"ORDO": 12, "PRAECANTATIO": 6, "VITREUS": 6}, 3, "thaumcraft:tc4_wand_rod_quartz"),
    "tc4_wand_rod_reed.json": ("WandRodReed", "ROD_reed", "minecraft:sugar_cane", ["thaumcraft:balanced_shard", "thaumcraft:aer_shard"], {"AER": 12, "PRAECANTATIO": 6, "MOTUS": 6}, 3, "thaumcraft:tc4_wand_rod_reed"),
    "tc4_wand_rod_blaze.json": ("WandRodBlaze", "ROD_blaze", "minecraft:blaze_rod", ["thaumcraft:balanced_shard", "thaumcraft:ignis_shard"], {"IGNIS": 12, "PRAECANTATIO": 6, "BESTIA": 6}, 3, "thaumcraft:tc4_wand_rod_blaze"),
    "tc4_wand_rod_bone.json": ("WandRodBone", "ROD_bone", "minecraft:bone", ["thaumcraft:balanced_shard", "thaumcraft:perditio_shard"], {"PERDITIO": 12, "PRAECANTATIO": 6, "EXANIMIS": 6}, 3, "thaumcraft:tc4_wand_rod_bone"),
    "tc4_wand_rod_silverwood.json": ("WandRodSilverwood", "ROD_silverwood", "thaumcraft:silverwood_log", ["thaumcraft:balanced_shard", "thaumcraft:aer_shard", "thaumcraft:ignis_shard", "thaumcraft:aqua_shard", "thaumcraft:terra_shard", "thaumcraft:ordo_shard", "thaumcraft:perditio_shard"], {"AER": 9, "IGNIS": 9, "AQUA": 9, "TERRA": 9, "ORDO": 9, "PERDITIO": 9, "PRAECANTATIO": 9}, 5, "thaumcraft:tc4_wand_rod_silverwood"),
    "tc4_wand_rod_primal_staff.json": ("WandRodPrimalStaff", "ROD_primal_staff", "thaumcraft:tc4_wand_rod_silverwood", ["thaumcraft:tc4_charm", "thaumcraft:tc4_wand_rod_obsidian", "thaumcraft:tc4_wand_rod_ice", "thaumcraft:tc4_wand_rod_quartz", "thaumcraft:tc4_charm", "thaumcraft:tc4_wand_rod_reed", "thaumcraft:tc4_wand_rod_blaze", "thaumcraft:tc4_wand_rod_bone"], {"AER": 32, "IGNIS": 32, "AQUA": 32, "TERRA": 32, "ORDO": 32, "PERDITIO": 32, "PRAECANTATIO": 64}, 8, "thaumcraft:tc4_staff_rod_primal"),
}
base = "src/main/resources/data/thaumcraft/thaumcraft_infusion/"
for filename, expected in expected_infusion.items():
    key, research, catalyst, components, aspects, instability, result = expected
    data = load_json(base + filename)
    checks = {
        "tc4_key": key,
        "tc4_source": "ConfigRecipes.addInfusionCraftingRecipe",
        "research": research,
        "tc4_kind": "INFUSION",
        "catalyst": catalyst,
        "components": components,
        "aspects": aspects,
        "instability": instability,
        "result": {"item": result, "count": 1},
    }
    for field, value in checks.items():
        if data.get(field) != value:
            errors.append(f"{filename}: {field} mismatch; expected {value!r}, got {data.get(field)!r}")

must(
    "src/main/java/com/darkifov/thaumcraft/porting/TC4RegistryGarbageGuard.java",
    '"iron_wand_cap", "gold_wand_cap", "thaumium_wand_cap"',
    '"wooden_wand_core", "greatwood_wand_core", "silverwood_wand_core"',
    '"greatwood_wand", "silverwood_wand"',
)

workflow = ".github/workflows/main.yml"
must(
    workflow,
    "tc4_v11_62_13_wand_component_assembly_parity_audit.py",
    "build/libs/*-github.jar",
    "v11.62.13-github-jar",
    "v11.62.13-build-reports",
)
must_not(workflow, "build/libs/*.jar\n")

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)

print("TC4 v11.62.13 wand component/assembly parity audit: OK")
