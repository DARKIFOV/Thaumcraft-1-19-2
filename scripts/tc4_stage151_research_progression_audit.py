#!/usr/bin/env python3
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding='utf-8', errors='ignore')

def exists(rel: str) -> bool:
    return (ROOT / rel).exists()

progression_spec = ROOT / 'src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_original_research_progression_stage151.json'
progression_items = json.loads(progression_spec.read_text(encoding='utf-8')) if progression_spec.exists() else []
source_items = json.loads((ROOT / 'src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_original_research_items.json').read_text(encoding='utf-8'))
expected_progression = [
    {'key': e['key'], 'item_triggers_raw': e.get('item_triggers_raw', []), 'entity_triggers': e.get('entity_triggers', []), 'aspect_triggers': e.get('aspect_triggers', []), 'auto_unlock': 'auto_unlock' in e.get('flags', []), 'warp': e.get('warp', 0)}
    for e in source_items
    if e.get('item_triggers_raw') or e.get('entity_triggers') or e.get('aspect_triggers') or 'auto_unlock' in e.get('flags', [])
]

progression = read('src/main/java/com/darkifov/thaumcraft/research/OriginalResearchProgression.java') if exists('src/main/java/com/darkifov/thaumcraft/research/OriginalResearchProgression.java') else ''
metadata_index = read('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchMetadataIndex.java') if exists('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchMetadataIndex.java') else ''
bridge = read('src/main/java/com/darkifov/thaumcraft/research/OriginalResearchBridge.java')
thaumometer = read('src/main/java/com/darkifov/thaumcraft/block/ThaumometerItem.java')
research_point = read('src/main/java/com/darkifov/thaumcraft/block/ResearchPointItem.java')
foundation = read('src/main/java/com/darkifov/thaumcraft/research/ResearchTableFoundation.java')
common_events = read('src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java')
book = read('src/main/java/com/darkifov/thaumcraft/block/ThaumonomiconItem.java')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')
workflow = read('.github/workflows/main.yml')
guard = read('scripts/github_ci_guard.py')
status = read('docs/ORIGINAL_TC4_PORTING_STATUS.md') if exists('docs/ORIGINAL_TC4_PORTING_STATUS.md') else ''
prompt = read('docs/NEXT_CHAT_PROMPT_STAGE151.md') if exists('docs/NEXT_CHAT_PROMPT_STAGE151.md') else ''

checks = {
    'version_stage151_or_later': any((f"version = '{v}'" in build and f'version="{v}"' in mods) for v in ['2.04.0', '2.02.0', '2.00.0', '1.98.0', '1.78.0', '1.76.0', '1.70.0', '1.65.0', '1.64.0', '1.63.0', '1.62.0', '1.61.0', '1.60.0', '1.59.0', '1.58.0', '1.57.0', '1.56.0', '1.55.0', '1.54.0', '1.51.0', '1.53.0', '1.52.0']),
    'progression_spec_present_and_exact': progression_items == expected_progression and len(progression_items) == 46,
    'progression_runtime_class_present': 'class OriginalResearchProgression' in progression,
    'auto_unlock_runtime_uses_original_index': 'seedAutoUnlocks' in progression and 'TC4ResearchMetadataIndex.autoUnlockKeys()' in progression,
    'scan_trigger_runtime_uses_item_entity_aspect_maps': all(s in progression for s in ['researchKeysForItemTrigger', 'researchKeysForEntityTrigger', 'researchKeysForAspectTrigger', 'applyScanTriggers']),
    'trigger_unlock_checks_original_parents': 'parentsComplete' in progression and 'entry.requirements()' in progression and 'entry.hiddenRequirements()' in progression,
    'warp_side_effects_applied_once_on_unlock': 'applyUnlockSideEffects' in progression and 'PlayerThaumData.addWarpPermanent(player, warp)' in progression,
    'sync_after_scan_trigger_unlock': 'ThaumcraftNetwork.syncResearch(serverPlayer)' in progression,
    'bridge_unlock_uses_progression_side_effects': 'OriginalResearchProgression.applyUnlockSideEffects(player, entry)' in bridge,
    'bridge_first_available_limited_to_original_tc4': 'ResearchRegistry.originalEntries()' in bridge,
    'research_point_limited_to_original_tc4': 'ResearchRegistry.originalEntries()' in research_point and 'applyUnlockSideEffects' in research_point,
    'thaumometer_calls_scan_triggers': 'OriginalResearchProgression.applyScanTriggers' in thaumometer and 'aspects.entries().keySet()' in thaumometer,
    'auto_unlock_seeded_on_login_table_and_book': 'OriginalResearchProgression.seedAutoUnlocks(player)' in common_events and 'OriginalResearchProgression.seedAutoUnlocks(player)' in foundation and 'OriginalResearchProgression.seedAutoUnlocks(serverPlayer)' in book,
    'metadata_index_has_all_trigger_lookup_methods': all(s in metadata_index for s in ['researchKeysForItemTrigger', 'researchKeysForEntityTrigger', 'researchKeysForAspectTrigger', 'autoUnlockKeys']),
    'workflow_runs_stage151_audit': 'tc4_stage151_research_progression_audit.py' in workflow,
    'github_guard_runs_stage151_audit': 'tc4_stage151_research_progression_audit.py' in guard,
    'status_and_prompt_updated': 'Stage151' in status and ('Stage152' in prompt or 'Stage153' in prompt) and ('41-66' in status or '40-65', '39-64' in status),
}
report = {
    'stage': 151,
    'goal': 'strict original TC4 research progression: auto-unlock, scan triggers and warp side-effects',
    'progression_entries': len(progression_items),
    'remaining_stage_estimate_after_stage151': '41-66 stages',
    'checks': checks,
    'passed': all(checks.values()),
}
print(json.dumps(report, indent=2, ensure_ascii=False))
if not report['passed']:
    for name, ok in checks.items():
        if not ok:
            print(f'::error::Stage151 research progression audit failed: {name}')
    sys.exit(1)
