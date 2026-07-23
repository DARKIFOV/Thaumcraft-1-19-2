#!/usr/bin/env python3
from pathlib import Path
import subprocess,sys
R=Path(__file__).resolve().parents[1]
checks=[
 ['python3','tools/tc4_116418_arcane_bellows_full_closure_guard.py'],
 ['python3','tools/tc4_116322_arcane_bellows_parity_guard.py'],
 ['python3','tools/tc4_116417_tallow_candle_full_closure_guard.py'],
 ['python3','tools/tc4_116416_hungry_chest_full_closure_guard.py'],
 ['python3','tools/tc4_116415_brain_jar_full_closure_guard.py'],
 ['python3','tools/tc4_116414_bath_salts_full_closure_guard.py'],
 ['python3','tools/tc4_116413_unnatural_hunger_full_closure_guard.py'],
 ['python3','tools/tc4_116412_unnatural_hunger_food_guard.py'],
 ['python3','tools/tc4_116411_sun_scorned_brightness_guard.py'],
 ['python3','tools/tc4_116410_death_gaze_cone_guard.py'],
 ['python3','tools/tc4_116409_warp_research_pool_sync_guard.py'],
 ['python3','tools/tc4_116408_eldritch_milestone_grant_guard.py'],
 ['python3','tools/tc4_116407_warp_message_parity_guard.py'],
 ['python3','tools/tc4_116405_warp_spawn_collision_guard.py'],
 ['python3','tools/tc4_116404_warp_spawn_offset_guard.py'],
 ['python3','tools/tc4_116403_warp_runtime_guard.py'],
 ['python3','tools/tc4_116281_warp_eldritch_parity_guard.py'],
 ['python3','tools/warp_runtime_selftest.py'],
 ['python3','tools/java_parse_guard_116418.py'],
 ['python3','tools/java_syntax_guard.py'],
 ['python3','tools/validate_json_resources.py'],
 ['python3','tools/validate_runtime_manifest.py','--manifest','runtime_artifacts/runtime_test_manifest.template.json','--version','11.64.18','--template'],
 ['python3','tools/tc4_recipe_registration_denominator_guard.py'],
 ['python3','tools/tc4_116394_source_inventory_guard.py'],
 ['python3','tools/tc4_116394_parity_consolidation_guard.py'],
]
for i,c in enumerate(checks,1):
 print(f'[{i}/{len(checks)}] {" ".join(c)}',flush=True)
 r=subprocess.run(c,cwd=R,text=True)
 if r.returncode:
  print(f'FOCUSED STATIC CI v11.64.18: FAIL at check {i}',file=sys.stderr); raise SystemExit(r.returncode)
print(f'FOCUSED STATIC CI v11.64.18: PASS ({len(checks)}/{len(checks)})')
