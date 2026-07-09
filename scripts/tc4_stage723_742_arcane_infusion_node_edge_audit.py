#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]

def read(rel):
    p = ROOT / rel
    if not p.exists():
        raise AssertionError(f"missing {rel}")
    return p.read_text(encoding='utf-8')

def require(rel, *needles):
    text = read(rel)
    for needle in needles:
        if needle not in text:
            raise AssertionError(f"{rel} missing marker: {needle}")
    return text

# Version marker for this batch.
require('build.gradle', "version = '7.42.0'", "version = '7.22.0'")
require('src/main/resources/META-INF/mods.toml', 'version="7.42.0"', 'version="7.22.0"')

# Infusion Matrix: dynamic TC4 stabilizer/symmetry snapshot, original NBT keys, no catalyst drift.
require(
    'src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionStabilityParity.java',
    'STABILIZER_SCAN_RADIUS',
    'MAX_EFFECTIVE_STABILIZER_PAIRS',
    'NBT_RECIPE_STABILIZERS',
    'mirrorAround',
    'isOriginalStyleStabilizer',
    'BlockTags.CANDLES'
)
require(
    'src/main/java/com/darkifov/thaumcraft/infusion/MatrixAuxiliaryReport.java',
    'unpairedStabilizers',
    'stabilizerSignature',
    'unpairedInstabilityPenalty',
    'TC4InfusionStabilityParity.MAX_EFFECTIVE_STABILIZER_PAIRS'
)
require(
    'src/main/java/com/darkifov/thaumcraft/infusion/InfusionMatrixAuxiliaryHelper.java',
    'TC4InfusionStabilityParity.scan',
    'stability.signature()',
    'stability.unpaired()'
)
inf = require(
    'src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java',
    'rememberInfusionStabilitySnapshot',
    'updateRunningStabilitySnapshot',
    'NBT_RECIPE_STABILIZERS',
    'NBT_RECIPE_SYMMETRY',
    'auxiliary.unpairedInstabilityPenalty()',
    'currentInstability = Math.max(currentInstability, recalculated)'
)
if 'findMatchingOriginalInfusionRecipe(catalyst, componentPedestals) == recipe' not in inf:
    raise AssertionError('infusion recipe lock drift guard missing')

# Arcane Workbench: old direct craft helper now preserves container items, matching slot-output path.
arc = require(
    'src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java',
    'consumeSlotPreservingContainer',
    'getCraftingRemainingItem',
    'consumePatternIngredients(recipe, catalystSlot, player)',
    'consumeIngredients(recipe.ingredients(), catalystSlot, player)'
)
if 'getItem(catalystSlot).shrink(1)' in arc:
    raise AssertionError('direct catalyst shrink returned; container item parity broken')

# Aura Node/Thaumometer: shared scan formatter + shared HUD modifier frame/alpha mapping.
require(
    'src/main/java/com/darkifov/thaumcraft/aura/TC4AuraNodeScanParity.java',
    'ORIGINAL_AURA_NODE_SCAN_KEY',
    'LEGACY_OBJECT_ID',
    'header',
    'visLine',
    'aspectLine'
)
require(
    'src/main/java/com/darkifov/thaumcraft/block/ThaumometerItem.java',
    'TC4AuraNodeScanParity.header',
    'TC4AuraNodeScanParity.visLine',
    'TC4AuraNodeScanParity.aspectLine',
    'TC4AuraNodeScanParity.ORIGINAL_AURA_NODE_SCAN_KEY'
)
require(
    'src/main/java/com/darkifov/thaumcraft/client/TC4AuraNodeHudParity.java',
    'HUD_NODE_CENTER_OFFSET_X',
    'HUD_ASPECT_RING_RADIUS',
    'frameOffsetFor',
    'alphaFor'
)
require(
    'src/main/java/com/darkifov/thaumcraft/client/TC4RevealerHudAdapter.java',
    'TC4AuraNodeHudParity.frameOffsetFor',
    'HUD_ASPECT_RING_RADIUS',
    'HUD_ASPECT_COLUMN_STEP'
)
require(
    'src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java',
    'TC4AuraNodeHudParity.frameOffsetFor',
    'TC4AuraNodeHudParity.alphaFor',
    'TC4AuraNodeHudParity.WORLD_BUBBLE_ALPHA'
)

print('Stage723-742 arcane/infusion/node edge parity audit: OK')
