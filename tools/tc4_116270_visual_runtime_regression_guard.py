#!/usr/bin/env python3
"""Regression guard for the screenshot-driven 11.62.70 parity repair."""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ERRORS: list[str] = []
CHECKS = 0


def require(path: str, *tokens: str) -> str:
    global CHECKS
    file = ROOT / path
    CHECKS += 1
    if not file.is_file():
        ERRORS.append(f"missing file: {path}")
        return ""
    text = file.read_text(encoding="utf-8")
    for token in tokens:
        CHECKS += 1
        if token not in text:
            ERRORS.append(f"{path}: missing token {token!r}")
    return text


def forbid(path: str, *tokens: str) -> str:
    text = require(path)
    global CHECKS
    for token in tokens:
        CHECKS += 1
        if token in text:
            ERRORS.append(f"{path}: forbidden regression token {token!r}")
    return text


# Mystical/Arcane Workbench must use the original TC4 worktable atlas and six-part model.
worktable_path = "src/main/resources/assets/thaumcraft/models/block/arcane_workbench.json"
worktable = require(worktable_path, "original/thaumcraft4/models/worktable", '"elements"')
if worktable:
    try:
        data = json.loads(worktable)
        CHECKS += 1
        if len(data.get("elements", [])) != 6:
            ERRORS.append(f"{worktable_path}: expected 6 original model elements")
    except json.JSONDecodeError as exc:
        ERRORS.append(f"{worktable_path}: invalid JSON: {exc}")

# Restore the original ItemWandRenderer matrices; reject the HOTFIX1 invented offsets.
wand_path = "src/main/java/com/darkifov/thaumcraft/client/render/WandItemRenderer.java"
wand = require(wand_path,
               "poseStack.translate(0.50D, 1.50D, 0.50D)",
               "poseStack.scale(1.00F, 1.10F, 1.00F)",
               "rotationDegrees(180.0F)",
               "rotationDegrees(66.0F)")
for token in ("MODEL_CENTER_Y", "0.11D", "-0.11D"):
    CHECKS += 1
    if token in wand:
        ERRORS.append(f"{wand_path}: contains broken HOTFIX hand/centering offset {token}")

# Expertise/Mastery feature flags must come from synchronized client research data.
require("src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableContainerScreen.java",
        'ClientResearchData.hasResearch("RESEARCHER1")',
        'ClientResearchData.hasResearch("RESEARCHER2")')
require("src/main/java/com/darkifov/thaumcraft/research/ResearchNoteSolver.java",
        'PlayerThaumData.hasResearch(player, "RESEARCHER2")',
        'player.getRandom().nextFloat() < 0.10F',
        'return roll < 0.50F',
        'return roll < 0.25F')

# Thaumonomicon must keep the exact 152px page spacing and original y offsets.
book = require("src/main/java/com/darkifov/thaumcraft/client/screen/TC4ResearchPageScreen.java",
               "leftPos + 152",
               "firstPageWithTitle ? 25 : 0",
               "page.recipe() || aspectPage ? -8 : -10",
               "page.recipe() ? -4 : aspectPage ? -8 : -15",
               "130.0F / width")
CHECKS += 1
if "titleExtra" in book:
    ERRORS.append("TC4ResearchPageScreen.java: synthetic wrapped-title offset returned")

# Candles: exact 4x8x4 core, no collision, no cross-plane models.
candle_java = require("src/main/java/com/darkifov/thaumcraft/block/TallowCandleBlock.java",
                      "Block.box(6.0D, 0.0D, 6.0D, 10.0D, 8.0D, 10.0D)",
                      "return Shapes.empty()",
                      "ParticleTypes.SMOKE",
                      "ParticleTypes.FLAME",
                      "implements InfusionStabilizer")
for model in sorted((ROOT / "src/main/resources/assets/thaumcraft/models/block").glob("tallow_candle*.json")):
    CHECKS += 1
    try:
        data = json.loads(model.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        ERRORS.append(f"{model.relative_to(ROOT)}: invalid JSON: {exc}")
        continue
    if data.get("parent") == "minecraft:block/cross":
        ERRORS.append(f"{model.relative_to(ROOT)}: broken giant cross model")
    if len(data.get("elements", [])) < 2:
        ERRORS.append(f"{model.relative_to(ROOT)}: missing candle body/wick geometry")

# Hungry Chest must use the original 1.7.10 ModelChest UV layout through a BER.
require("src/main/java/com/darkifov/thaumcraft/client/render/model/TC4HungryChestModel.java",
        "texOffs(0, 19)",
        "addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F)",
        "addBox(0.0F, -5.0F, -14.0F, 14.0F, 5.0F, 14.0F)",
        "LayerDefinition.create(mesh, 64, 64)")
require("src/main/java/com/darkifov/thaumcraft/client/render/HungryChestRenderer.java",
        "TC4HungryChestModel",
        "chesthungry.png",
        "open * open * open")
require("src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java",
        "TC4HungryChestModel.LAYER",
        "TC4HungryChestModel::createBodyLayer")

# Functional TC4 equipment must be visible again; placeholder mirrors remain quarantined.
registry = require("src/main/java/com/darkifov/thaumcraft/porting/TC4RegistryGarbageGuard.java",
                   "FUNCTIONAL_TC4",
                   '"tc4_thaumiumpick"',
                   '"tc4_voidsword"',
                   '"tc4_thaumiumfortresshelm"')
items = require("src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java",
                "new TC4ThaumiumPickaxeItem(functionalProperties)",
                "new TC4VoidSwordItem(functionalProperties)",
                ".tab(tab)")

# Matrix matching and JEI must use canonical ids and preload all bundled infusion JSON.
require("src/main/java/com/darkifov/thaumcraft/recipe/TC4RecipeItemResolver.java",
        'BLOCK_META.put("blockStoneDevice:1", "thaumcraft:arcane_pedestal")',
        'BLOCK_META.put("blockStoneDevice:2", "thaumcraft:infusion_matrix")')
require("src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionItemMatcher.java",
        "TC4LegacyDuplicateItemMigrator.mappings()",
        "canonical(actualId).equals(canonical(expectedId))")
manager = require("src/main/java/com/darkifov/thaumcraft/infusion/InfusionRecipeManager.java",
                  "BUNDLED_RECIPE_FILES",
                  "loadBundledRecipes()",
                  "data/thaumcraft/thaumcraft_infusion/")
require("src/main/java/com/darkifov/thaumcraft/infusion/InfusionRecipes.java",
        "ensureBundledRecipesLoaded()",
        "TC4InfusionEnchantmentAdapter.materializeRecipes()",
        "TC4InfusionRunicAugmentAdapter.materializeRecipe()")
require("src/main/java/com/darkifov/thaumcraft/compat/jei/TC4JeiPlugin.java",
        "InfusionRecipes.ensureBundledRecipesLoaded()",
        "registration.addRecipes(INFUSION")
files = sorted((ROOT / "src/main/resources/data/thaumcraft/thaumcraft_infusion").glob("*.json"))
CHECKS += 1
if len(files) != 78:
    ERRORS.append(f"expected 78 bundled infusion JSON files, found {len(files)}")
listed = re.findall(r'"([a-z0-9_]+\.json)"', manager)
CHECKS += 1
if len(listed) != 78 or set(listed) != {p.name for p in files}:
    ERRORS.append("InfusionRecipeManager bundled file list does not exactly match the 78 JSON resources")

# Thaumometer node scan rewards: max(4, amount/10), plus original node-type bonuses.
require("src/main/java/com/darkifov/thaumcraft/aura/TC4AuraNodeScanParity.java",
        "scanRewardAspects",
        "Math.max(4, entry.getValue() / 10)",
        "case UNSTABLE -> rewards.add(Aspect.PERDITIO, 4)",
        "case HUNGRY -> rewards.add(Aspect.FAMES, 4)",
        "case TAINTED -> rewards.add(Aspect.VITIUM, 4)",
        "case PURE -> rewards.add(Aspect.SANO, 2).add(Aspect.ORDO, 2)",
        "case DARK -> rewards.add(Aspect.MORTUUS, 2).add(Aspect.TENEBRAE, 2)")
require("src/main/java/com/darkifov/thaumcraft/block/ThaumometerItem.java",
        "AspectList scanAspects = TC4AuraNodeScanParity.scanRewardAspects(node)",
        "absorbScannedAspects(player, scanAspects)")

if ERRORS:
    print(f"TC4 11.62.70 regression guard: FAIL ({len(ERRORS)} problems)")
    for error in ERRORS:
        print(f" - {error}")
    raise SystemExit(1)
print(f"TC4 11.62.70 regression guard: PASS ({CHECKS} checks)")
