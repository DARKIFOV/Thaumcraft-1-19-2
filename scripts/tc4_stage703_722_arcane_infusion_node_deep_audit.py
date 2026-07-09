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

# Arcane Workbench: one coordinate ledger now covers slots + aspect hovers, and craft uses TC4 sound key.
require(
    'src/main/java/com/darkifov/thaumcraft/arcane/TC4ArcaneWorkbenchParity.java',
    'SLOT_SIZE',
    'insideOutputSlot',
    'insideWandSlot',
    'aspectAt'
)
require(
    'src/main/java/com/darkifov/thaumcraft/client/screen/ArcaneWorkbenchContainerScreen.java',
    'renderOriginalAspectHover',
    'TC4ArcaneWorkbenchParity.aspectAt',
    'WandItem.modifiedVisCost'
)
require(
    'src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java',
    'playOriginalCraftSound',
    'TC4Sounds.event("craftstart")',
    'SoundSource.BLOCKS'
)

# Infusion Matrix: timings/sounds/no debug-display policy centralized, essentia FX come from the drained jar.
require(
    'src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionCraftCycleParity.java',
    'SHOW_WAITING_DEBUG_MESSAGES = false',
    'SOUND_ESSENTIA_DRAIN = "infuser"',
    'SOUND_COMPONENT_PULL = "craftstart"',
    'SOUND_FINISH = "infuserstart"',
    'SOUND_FAIL = "craftfail"'
)
require(
    'src/main/java/com/darkifov/thaumcraft/infusion/InfusionProcessHelper.java',
    'consumeOneAspectSource',
    'return jar.getBlockPos()',
    'do not add non-TC4 happy-villager progress markers'
)
inf = require(
    'src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java',
    'TC4InfusionCraftCycleParity.CRAFT_CYCLE_DELAY',
    'InfusionProcessHelper.consumeOneAspectSource',
    'InfusionProcessHelper.spawnSourceParticles(serverLevel, essentiaSource, worldPosition, false)',
    'TC4InfusionCraftCycleParity.SHOW_WAITING_DEBUG_MESSAGES',
    'TC4InfusionCraftCycleParity.SHOW_START_COMPLETE_DEBUG_MESSAGES'
)
if 'Infusion is waiting for essentia:' in inf and 'SHOW_WAITING_DEBUG_MESSAGES && owner != null' not in inf:
    raise AssertionError('waiting essentia debug message is not gated')
if 'Infusion is waiting for components:' in inf and 'SHOW_WAITING_DEBUG_MESSAGES && owner != null' not in inf:
    raise AssertionError('waiting component debug message is not gated')

# Node HUD/renderer: shared pixel constants, no undefined FRAMES compile risk, no happy-villager fake FX.
require(
    'src/main/java/com/darkifov/thaumcraft/client/TC4AuraNodeHudParity.java',
    'HUD_RING_U',
    'HUD_BAR_U',
    'HUD_NODE_SIZE',
    'HUD_BUBBLE_SIZE',
    'HUD_MAX_ASPECTS'
)
require(
    'src/main/java/com/darkifov/thaumcraft/client/TC4RevealerHudAdapter.java',
    'TC4AuraNodeHudParity.HUD_RING_U',
    'TC4AuraNodeHudParity.HUD_BUBBLE_SIZE',
    'no debug-style type/total/E text overlay'
)
renderer = require(
    'src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java',
    'TC4AuraNodeHudParity.NODE_SHEET_FRAMES',
    'TC4AuraNodeHudParity.ORIGINAL_NODES',
    'TC4AuraNodeHudParity.ORIGINAL_NODE_BUBBLE'
)
if '% FRAMES' in renderer:
    raise AssertionError('AuraNodeRenderer still has undefined % FRAMES token')
helper = read('src/main/java/com/darkifov/thaumcraft/infusion/InfusionProcessHelper.java')
if 'HAPPY_VILLAGER' in helper:
    raise AssertionError('non-TC4 happy-villager progress marker returned')

print('Stage703-722 arcane/infusion/node deep parity audit: OK')
