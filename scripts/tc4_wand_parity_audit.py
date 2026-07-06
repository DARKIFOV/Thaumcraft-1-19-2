#!/usr/bin/env python3
from pathlib import Path
import json, re
ROOT = Path(__file__).resolve().parents[1]
java = ROOT / 'src/main/java/com/darkifov/thaumcraft'
files = {p.name: p.read_text(encoding='utf-8', errors='ignore') for p in java.rglob('*.java')}
joined = '\n'.join(files.values())
checks = {
    'cap_base_modifiers': 'visCostModifier(Aspect aspect)' in files.get('WandCapType.java',''),
    'copper_silver_special_modifiers': 'COPPER' in files.get('WandCapType.java','') and 'SILVER' in files.get('WandCapType.java','') and '0.95F' in files.get('WandCapType.java',''),
    'rod_regen_mapping': 'regeneratedAspect()' in files.get('WandRodType.java',''),
    'primal_staff_regen': 'regeneratesAllPrimals()' in files.get('WandRodType.java',''),
    'wand_cost_modifier_helper': 'modifiedVisCost' in files.get('WandItem.java',''),
    'focus_uses_cap_discount': 'modifiedVisCost(wandStack' in files.get('WandFocusRuntime.java',''),
    'arcane_uses_cap_discount': 'modifiedVisCost(wand' in files.get('ArcaneWorkbenchBlockEntity.java',''),
    'component_install_from_offhand': 'tryInstallWandComponent' in files.get('WandItem.java',''),
    'vis_clamped_to_capacity': 'clampVisToCapacity' in files.get('WandItem.java',''),
    'focus_pouch_capacity': 'MAX_FOCI = 18' in files.get('FocusPouchItem.java',''),
    'warded_focus_protection': 'WardedBlockRuntime.mayEdit' in files.get('WandFocusRuntime.java',''),
    'staff_renderer_scaling': 'data.rod().staff() ?' in files.get('WandItemRenderer.java',''),
}
report = {
    'stage': 134,
    'goal': 'TC4 wand total repair audit',
    'checks': checks,
    'passed': all(checks.values()),
    'wand_rod_types': len(re.findall(r'^[ ]+[A-Z_]+\("', files.get('WandRodType.java',''), re.M)),
    'wand_cap_types': len(re.findall(r'^[ ]+[A-Z_]+\("', files.get('WandCapType.java',''), re.M)),
    'focus_types': len(re.findall(r'^[ ]+[A-Z_]+\("', files.get('WandFocusType.java',''), re.M)),
}
print(json.dumps(report, indent=2, ensure_ascii=False))
