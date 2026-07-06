#!/usr/bin/env python3
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
arcane_dir = root / 'src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench'
java = root / 'src/main/java'

arcane_files = sorted(arcane_dir.glob('*.json'))
patterned = []
with_key = []
for path in arcane_files:
    data = json.loads(path.read_text())
    if data.get('pattern'):
        patterned.append(path.name)
        if data.get('key') or data.get('symbol_map'):
            with_key.append(path.name)

research_graph = (java / 'com/darkifov/thaumcraft/research/ResearchAspectGraph.java').read_text()
recipe_java = (java / 'com/darkifov/thaumcraft/arcane/ArcaneWorkbenchRecipe.java').read_text()
bridge_java = (java / 'com/darkifov/thaumcraft/research/OriginalResearchBridge.java').read_text()
sweep_java = (java / 'com/darkifov/thaumcraft/porting/TC4Stage139WholePortSweep.java').read_text()

checks = {
    'arcane_patterned_recipe_json_files': len(patterned),
    'arcane_patterned_with_exact_key': len(with_key),
    'arcane_key_coverage_ok': len(patterned) > 0 and len(with_key) == len(patterned),
    'arcane_recipe_parses_key_object': 'readPatternKey(recipe, json.getAsJsonObject("key"))' in recipe_java,
    'arcane_recipe_parses_symbol_map_object': 'readPatternKey(recipe, json.getAsJsonObject("symbol_map"))' in recipe_java,
    'research_aspect_links_are_direct': 'return isDirect(first, second);' in research_graph,
    'research_bridge_case_insensitive': 'equalsIgnoreCase(key)' in bridge_java,
    'stage139_sweep_class_present': 'Stage139 whole-port sweep' in sweep_java,
}

ok = all(v if isinstance(v, bool) else True for v in checks.values())
report = {
    'stage': 139,
    'name': 'TC4 Whole Port Broad Completion Sweep',
    'ok': ok,
    'checks': checks,
    'remaining_declared_not_done': [
        'full golemancy AI/tasks/animation parity',
        'full eldritch dimension/boss/progression parity',
        'exact focus upgrade table UI and all old focus upgrades',
        'remaining old metadata/ore dictionary recipes that still need exact 1.19.2 IDs',
        'final exact renderers for every old TC4 tile model'
    ]
}
(root / 'STAGE139_WHOLE_PORT_SWEEP_AUDIT.json').write_text(json.dumps(report, indent=2, ensure_ascii=False) + '\n')
print(json.dumps(report, indent=2, ensure_ascii=False))
if not ok:
    raise SystemExit(1)
