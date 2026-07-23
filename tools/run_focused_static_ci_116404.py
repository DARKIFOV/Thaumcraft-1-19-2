#!/usr/bin/env python3
from pathlib import Path
import subprocess,sys
R=Path(__file__).resolve().parents[1]
checks=[
 ['python3','tools/tc4_116404_warp_spawn_offset_guard.py'],
 ['python3','tools/tc4_116403_warp_runtime_guard.py'],
 ['python3','tools/tc4_116281_warp_eldritch_parity_guard.py'],
 ['python3','tools/warp_runtime_selftest.py'],
 ['python3','tools/java_parse_guard_116404.py'],
 ['python3','tools/java_syntax_guard.py'],
 ['python3','tools/validate_json_resources.py'],
 ['python3','tools/validate_runtime_manifest.py','--manifest','runtime_artifacts/runtime_test_manifest.template.json','--version','11.64.04','--template'],
 ['python3','tools/tc4_recipe_registration_denominator_guard.py'],
 ['python3','tools/tc4_116394_source_inventory_guard.py'],
 ['python3','tools/tc4_116394_parity_consolidation_guard.py'],
]
for i,c in enumerate(checks,1):
 print(f'[{i}/{len(checks)}] {" ".join(c)}',flush=True)
 x=subprocess.run(c,cwd=R,text=True)
 if x.returncode:
  print(f'FOCUSED STATIC CI v11.64.04: FAIL at check {i}',file=sys.stderr); raise SystemExit(x.returncode)
print(f'FOCUSED STATIC CI v11.64.04: PASS ({len(checks)}/{len(checks)})')
