#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(rel):
    p = ROOT / rel
    if not p.exists():
        raise AssertionError(f"missing {rel}")
    return p.read_text(encoding="utf-8")

def require(rel, *needles):
    text = read(rel)
    for needle in needles:
        if needle not in text:
            raise AssertionError(f"{rel} missing marker: {needle}")
    return text

def forbid(rel, *needles):
    text = read(rel)
    for needle in needles:
        if needle in text:
            raise AssertionError(f"{rel} still contains forbidden marker: {needle}")
    return text

require('build.gradle', "version = '7.82.0'", "version = '7.62.0'")
require('src/main/resources/META-INF/mods.toml', 'version="7.82.0"', 'version="7.62.0"')

# v7.82 is a short parity batch: terminal failure must enter the already-ported
# TC4 weighted event table directly, while normal craft ticks keep the old roll gate.
require(
    'src/main/java/com/darkifov/thaumcraft/infusion/InfusionInstabilityEvents.java',
    'public static boolean triggerWeightedEvent',
    'return triggerWeightedEvent(level, matrixPos, player, recipe, report);',
    'level.random.nextInt(EVENT_ROLL_BOUND)',
    'case 0, 2, 10, 13 -> ejectItem'
)
require(
    'src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionFailureParity.java',
    'InfusionInstabilityEvents.triggerWeightedEvent',
    'does not pass through a second probability gate',
    'TERMINAL_FAILURE_EVENT_PASSES',
    'NBT_LAST_FAILURE_REASON',
    'NBT_LAST_FAILURE_INSTABILITY'
)
forbid('src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionFailureParity.java',
       'maybeTrigger(level, matrixPos, owner, recipe, report, 0, failureInstability)')

# Stabilizer scan parity: original getSurroundings scans around the matrix block,
# with y matrix-10..matrix+5 after converting the old yy loop.
require(
    'src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionStabilityParity.java',
    'STABILIZER_SCAN_DOWN = -10',
    'STABILIZER_SCAN_UP = 5',
    'mirrorAround(center, pos)'
)
require(
    'src/main/java/com/darkifov/thaumcraft/infusion/InfusionMatrixAuxiliaryHelper.java',
    'StabilitySnapshot stability = TC4InfusionStabilityParity.scan(level, matrixPos)',
    'mirrors stabilizers around the matrix block itself'
)
require(
    'src/main/java/com/darkifov/thaumcraft/infusion/InfusionAltarStructure.java',
    'StabilitySnapshot stabilizerSnapshot = TC4InfusionStabilityParity.scan(level, matrixPos)',
    'matrixPos.offset(-STABILIZER_RADIUS, -10, -STABILIZER_RADIUS)',
    'matrixPos.offset(STABILIZER_RADIUS, 5, STABILIZER_RADIUS)'
)
forbid('src/main/java/com/darkifov/thaumcraft/infusion/InfusionAltarStructure.java',
       'matrixPos.offset(-STABILIZER_RADIUS, -5, -STABILIZER_RADIUS)',
       'matrixPos.offset(STABILIZER_RADIUS, 10, STABILIZER_RADIUS)',
       'stabilizingBlocks += 10')

# Thaumometer/goggles/HUD parity: no fake scan/progression from aspectless blocks,
# while the existing revealer HUD ordering remains shared.
require(
    'src/main/java/com/darkifov/thaumcraft/block/ThaumometerItem.java',
    'if (aspects.isEmpty())',
    'successful research scans',
    'return InteractionResult.PASS;'
)
thaumometer = read('src/main/java/com/darkifov/thaumcraft/block/ThaumometerItem.java')
empty_guard = thaumometer.index('if (aspects.isEmpty())')
mark_scan = thaumometer.index('PlayerThaumData.markScannedObject(player, key)')
if empty_guard > mark_scan:
    raise AssertionError('Thaumometer aspectless-block guard must run before markScannedObject')
require(
    'src/main/java/com/darkifov/thaumcraft/client/TC4AuraNodeHudParity.java',
    'sortedAspectsForHud',
    'HUD_ASPECT_ICON_SIZE',
    'ringIconX',
    'ringIconY'
)
require(
    'src/main/java/com/darkifov/thaumcraft/block/GogglesOfRevealingItem.java',
    'no fake scan',
    'showNodes',
    'showIngamePopups'
)

# Arcane Workbench guard is intentionally retained from v7.62; this batch does
# not add new GUI/progression/content.
require(
    'src/main/java/com/darkifov/thaumcraft/menu/ArcaneWorkbenchMenu.java',
    'index == MENU_SLOT_OUTPUT && !slot.mayPickup(player)',
    'stale preview could be moved without paying wand vis'
)
require(
    'src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java',
    'sameItemSameTagsAndCount',
    'expected.getCount() == actual.getCount()'
)

workflow = read('.github/workflows/main.yml')
if 'tc4_v7_82_infusion_matrix_arcane_node_parity_audit.py' not in workflow:
    raise AssertionError('workflow does not run v7.82 audit')

require('V7_82_TC4_INFUSION_MATRIX_ARCANE_NODE_PARITY_REPORT.json',
        '"compact_label": "v7.82"',
        '"previous_compact_label": "v7.62"',
        '"no_new_items": true',
        '"no_new_progression": true')
require('docs/NEXT_CHAT_PROMPT_V7_82.md', 'архива v7.82', 'Следующий публичный batch: v8.02', 'без новых предметов')

print('v7.82 infusion matrix/arcane/node parity audit: OK')
