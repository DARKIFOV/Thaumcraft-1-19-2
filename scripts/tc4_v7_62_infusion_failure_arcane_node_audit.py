#!/usr/bin/env python3
from pathlib import Path

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

require('build.gradle', "version = '7.62.0'", "version = '7.42.0'")
require('src/main/resources/META-INF/mods.toml', 'version="7.62.0"', 'version="7.42.0"')

# Infusion failure must use the ported TC4 weighted instability table, not only a smoke puff.
require(
    'src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionFailureParity.java',
    'TERMINAL_FAILURE_EVENT_PASSES',
    'TERMINAL_FAILURE_INSTABILITY_BONUS',
    'InfusionInstabilityEvents.maybeTrigger',
    'SOUND_TERMINAL_FAIL_KEY',
    'NBT_LAST_FAILURE_REASON',
    'NBT_LAST_FAILURE_INSTABILITY'
)
require(
    'src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java',
    'TC4InfusionFailureParity.applyTerminalFailure',
    'lastFailureReason',
    'lastFailureInstability',
    'NBT_LAST_FAILURE_REASON',
    'NBT_LAST_FAILURE_INSTABILITY'
)

# Arcane Workbench shift-click must obey the same canTakeStack guard as normal output pickup.
require(
    'src/main/java/com/darkifov/thaumcraft/menu/ArcaneWorkbenchMenu.java',
    'index == MENU_SLOT_OUTPUT && !slot.mayPickup(player)',
    'stale preview could be moved without paying wand vis'
)
require(
    'src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java',
    'sameItemSameTagsAndCount',
    'expected.getCount() == actual.getCount()',
    'over-stacked stale preview'
)

# Aura Node HUD must share original ordering and ring coordinates through one ledger.
require(
    'src/main/java/com/darkifov/thaumcraft/client/TC4AuraNodeHudParity.java',
    'sortedAspectsForHud',
    'HUD_ASPECT_ICON_SIZE',
    'ringIconX',
    'ringIconY'
)
require(
    'src/main/java/com/darkifov/thaumcraft/client/TC4RevealerHudAdapter.java',
    'TC4AuraNodeHudParity.sortedAspectsForHud',
    'TC4AuraNodeHudParity.ringIconX',
    'TC4AuraNodeHudParity.ringIconY'
)

workflow = read('.github/workflows/main.yml')
if 'tc4_v7_62_infusion_failure_arcane_node_audit.py' not in workflow:
    raise AssertionError('workflow does not run v7.62 audit')


require('V7_62_TC4_INFUSION_FAILURE_ARCANE_NODE_REPORT.json', '"compact_label": "v7.62"', '"previous_compact_label": "v7.42"', '"legacy_stage_range": "743-762"')
require('docs/NEXT_CHAT_PROMPT_V7_62.md', 'архива v7.62', 'Следующий публичный batch: v7.82', 'Старый диапазон')

print('v7.62 infusion failure/arcane/node audit: OK')
