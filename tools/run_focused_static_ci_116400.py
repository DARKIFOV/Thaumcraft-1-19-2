#!/usr/bin/env python3
from pathlib import Path
import subprocess,sys
ROOT=Path(__file__).resolve().parents[1]
checks=[
 ['python','tools/tc4_116400_research_note_clear_guard.py'],
 ['python','tools/research_note_clear_selftest.py'],
 ['python','tools/tc4_116399_research_note_graph_guard.py'],
 ['python','tools/research_note_graph_selftest.py'],
 ['python','tools/tc4_116398_research_mastery_combination_guard.py'],
 ['python','tools/research_mastery_combination_selftest.py'],
 ['python','tools/tc4_116397_research_table_behavior_guard.py'],
 ['python','tools/tc4_116396_research_efficiency_parity_guard.py'],
 ['python','tools/tc4_116395_thaumometer_parity_guard.py'],
 ['python','tools/tc4_116394_parity_consolidation_guard.py'],
 ['python','tools/tc4_116394_source_inventory_guard.py'],
 ['python','tools/tc4_116282_research_workflow_parity_guard.py'],
 ['python','tools/tc4_116335_research_utility_aliases_guard.py'],
 ['python','tools/java_syntax_guard.py'],
 ['python','tools/java_parse_guard_116400.py'],
 ['python','tools/validate_json_resources.py'],
 ['python','tools/validate_runtime_manifest.py','--manifest','runtime_artifacts/runtime_test_manifest.template.json','--version','11.64.00','--template'],
 ['python','tools/tc4_recipe_registration_denominator_guard.py'],
]
for i,cmd in enumerate(checks,1):
    print(f'[{i}/{len(checks)}] {" ".join(cmd)}',flush=True)
    result=subprocess.run(cmd,cwd=ROOT,text=True)
    if result.returncode:
        print(f'FOCUSED STATIC CI v11.64.00: FAIL at check {i}',file=sys.stderr)
        raise SystemExit(result.returncode)
print(f'FOCUSED STATIC CI v11.64.00: PASS ({len(checks)}/{len(checks)})')
