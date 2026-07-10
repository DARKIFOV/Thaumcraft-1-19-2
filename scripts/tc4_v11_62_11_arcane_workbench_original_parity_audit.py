#!/usr/bin/env python3
from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []


def read(path: str) -> str:
    p = ROOT / path
    if not p.exists():
        errors.append(f"missing file: {path}")
        return ""
    return p.read_text(encoding="utf-8")


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


must(
    "build.gradle",
    "version = '11.62.11'",
    "tc4_arcane_workbench_server_authoritative_ghost_vis_reset",
)
must(
    "src/main/resources/META-INF/mods.toml",
    'version="11.62.11"',
    "tc4_arcane_workbench_server_authoritative_ghost_vis_reset",
)

must(
    "src/main/java/com/darkifov/thaumcraft/arcane/TC4ArcaneWorkbenchParity.java",
    "GUI_WIDTH = 190",
    "GUI_HEIGHT = 234",
    "GRID_X = 40",
    "GRID_Y = 40",
    "GRID_SPACING = 24",
    "WAND_SLOT_X = 160",
    "WAND_SLOT_Y = 24",
    "OUTPUT_SLOT_X = 160",
    "OUTPUT_SLOT_Y = 64",
    "{72, 21}",
    "{24, 43}",
    "{24, 102}",
    "{72, 124}",
    "{120, 102}",
    "{120, 43}",
)

must(
    "src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java",
    "previewVanillaCraftingResult(player)",
    "findMatchingArcaneRecipeForGrid",
    "if (recipe == null || !canAffordArcaneRecipe(player, recipe))",
    "clearOutputPreview();",
    "public boolean canAffordArcaneRecipe",
    "public int modifiedArcaneCost",
    "consumeArcaneVisCost(getItem(SLOT_WAND), recipe)",
    "consumeOriginalCraftMatrix(player)",
)

must(
    "src/main/java/com/darkifov/thaumcraft/menu/ArcaneWorkbenchMenu.java",
    "ContainerData",
    "SimpleContainerData(DATA_COUNT)",
    "DATA_RECIPE_PRESENT",
    "DATA_AFFORDABLE",
    "DATA_GHOST_ITEM_ID",
    "DATA_COST_START",
    "addDataSlots(arcaneData)",
    "refreshArcaneState()",
    "findMatchingArcaneRecipeForGrid(player)",
    "ghostArcaneResult()",
    "arcaneCost(Aspect aspect)",
)

must(
    "src/main/java/com/darkifov/thaumcraft/client/screen/ArcaneWorkbenchContainerScreen.java",
    "strict port of TC4 GuiArcaneWorkbench",
    "OriginalGuiTextures.ARCANE_WORKBENCH",
    "renderOriginalAspectCosts",
    "renderOriginalInsufficientVisGhost",
    'Component.literal("Insufficient vis")',
    "leftPos + 168.0F",
    "topPos + 46.0F",
    "menu.ghostArcaneResult()",
    "menu.arcaneCost(aspect)",
    "RenderSystem.setShaderColor(0.33F, 0.33F, 0.33F, 0.66F)",
)
must_not(
    "src/main/java/com/darkifov/thaumcraft/client/screen/ArcaneWorkbenchContainerScreen.java",
    "0x99000000",
)
must_not(
    "src/main/java/com/darkifov/thaumcraft/client/screen/ArcaneWorkbenchContainerScreen.java",
    "ClientArcaneRecipeRegistry",
    "ClientRecipeRegistry",
    "findRecipeByLooseItemId",
)

must(
    "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java",
    'pillarBlock("greatwood_log"',
)

must(
    "src/main/java/com/darkifov/thaumcraft/wand/TC4ConfigRecipesWandIndex.java",
    'recipe("tc4_configrecipes_rod_greatwood"',
    'item("thaumcraft", "greatwood_log")',
    'item("thaumcraft", "tc4_wand_rod_greatwood")',
    '"ROD_greatwood"',
    '"ConfigRecipes.WandRodGreatwood"',
    '.patternRow(" G").patternRow("G ").patternKey(\'G\', log)',
    ".require(Aspect.PERDITIO, WandRodType.GREATWOOD.craftCost())",
)

must_not(
    "src/main/java/com/darkifov/thaumcraft/wand/TC4ConfigRecipesWandIndex.java",
    'ResourceLocation log = item("thaumcraft", "tc4_block_greatwood_log")',
)

must(
    "src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java",
    "Exact 1.7.10 ShapedArcaneRecipe matching",
    "findPatternPlacement",
    "offsetX <= 3 - width",
    "offsetY <= 3 - height",
    "checkPatternPlacement",
    "boolean mirrored",
    "PatternPlacement mirrored",
)

must(
    "src/main/java/com/darkifov/thaumcraft/arcane/ArcaneWorkbenchRecipes.java",
    "STRICT_ORIGINAL",
    "if (!recipe.tc4Key().isBlank())",
    "TC4ConfigRecipesWandIndex.generatedArcaneComponentRecipes()",
    "WandCraftingRuntime.generatedWandAssemblyRecipes()",
)

must(
    ".github/workflows/main.yml",
    "tc4_v11_62_11_arcane_workbench_original_parity_audit.py",
    "build/libs/*-github.jar",
    "v11.62.11-github-jar",
)

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)

print("TC4 v11.62.11 Arcane Workbench original parity audit: OK")
