#!/usr/bin/env python3
from pathlib import Path
import json, sys

ROOT = Path(__file__).resolve().parents[1]
java = ROOT / 'src/main/java/com/darkifov/thaumcraft'
resources = ROOT / 'src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench'

files = {
    'recipe': java / 'arcane/ArcaneWorkbenchRecipe.java',
    'workbench': java / 'blockentity/ArcaneWorkbenchBlockEntity.java',
    'workbench_screen': java / 'client/screen/ArcaneWorkbenchContainerScreen.java',
    'book_pages': java / 'client/screen/TC4ResearchPageScreen.java',
}
texts = {k: p.read_text(encoding='utf-8', errors='ignore') for k, p in files.items()}
recipe_files = list(resources.glob('*.json'))
pattern_files = []
center_catalyst_patterns = 0
for path in recipe_files:
    try:
        obj = json.loads(path.read_text(encoding='utf-8'))
    except Exception:
        continue
    pattern = obj.get('pattern') or []
    if pattern:
        pattern_files.append(path.name)
        if len(pattern) > 1 and len(pattern[1]) > 1 and pattern[1][1] != ' ' and obj.get('catalyst'):
            center_catalyst_patterns += 1

checks = {
    'recipe_infers_symbol_map': 'inferredPatternMap()' in texts['recipe'] and 'inferredCatalystSymbol' in texts['recipe'],
    'workbench_exact_pattern_counts': 'hasPatternRequiredItems' in texts['workbench'] and 'needed.put(id, needed.getOrDefault(id, 0) + 1)' in texts['workbench'],
    'workbench_keeps_tc4_catalyst_slot_compatibility': 'the old optional catalyst slot' in texts['workbench'],
    'workbench_consumes_by_pattern_slots': 'consumePatternIngredients' in texts['workbench'] and 'symbolMap.get(symbol)' in texts['workbench'],
    'arcane_gui_uses_same_symbol_map': 'inferredClientPatternMap' in texts['workbench_screen'],
    'thaumonomicon_recipe_pages_use_same_symbol_map': 'inferredOriginalPatternMap' in texts['book_pages'],
    'materialized_pattern_recipes_present': len(pattern_files) >= 10,
    'center_catalyst_patterns_detected': center_catalyst_patterns >= 5,
}
report = {
    'stage': 138,
    'name': 'TC4 arcane exact pattern completion pass',
    'arcane_recipe_json_files': len(recipe_files),
    'arcane_pattern_recipe_json_files': len(pattern_files),
    'center_catalyst_pattern_recipe_json_files': center_catalyst_patterns,
    'checks': checks,
    'ok': all(checks.values()),
}
(ROOT / 'STAGE138_ARCANE_PATTERN_AUDIT.json').write_text(json.dumps(report, indent=2), encoding='utf-8')
if not report['ok']:
    for name, ok in checks.items():
        if not ok:
            print(f'::error::{name}')
    sys.exit(1)
print('Stage138 arcane pattern audit: OK')
