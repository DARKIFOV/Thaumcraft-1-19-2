#!/usr/bin/env python3
from pathlib import Path
import subprocess,sys
R=Path(__file__).resolve().parents[1]
checks=[
 ['python3','tools/tc4_116427_research_system_full_closure_guard.py'],
 ['python3','tools/java_parse_guard_116427.py'],
 ['python3','tools/tc4_116426_thaumonomicon_full_closure_guard.py'],
 ['python3','tools/tc4_116425_thaumometer_full_closure_guard.py'],
 ['python3','tools/tc4_116395_thaumometer_parity_guard.py'],
 ['python3','tools/thaumometer_scan_guard.py'],
 ['python3','tools/tc4_116424_arcane_bore_full_closure_guard.py'],
 ['python3','tools/tc4_116325_arcane_bore_parity_guard.py'],
 ['python3','tools/tc4_116423_arcane_spa_full_closure_guard.py'],
 ['python3','tools/tc4_116422_arcane_lamp_full_closure_guard.py'],
 ['python3','tools/tc4_116421_arcane_pressure_plate_full_closure_guard.py'],
 ['python3','tools/tc4_116327_arcane_pressure_plate_parity_guard.py'],
 ['python3','tools/tc4_116420_arcane_levitator_full_closure_guard.py'],
 ['python3','tools/tc4_116323_arcane_levitator_parity_guard.py'],
 ['python3','tools/tc4_116419_arcane_ear_full_closure_guard.py'],
 ['python3','tools/tc4_116326_arcane_ear_parity_guard.py'],
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
 ['python3','tools/java_parse_guard_116426.py'],
 ['python3','tools/java_parse_guard_116425.py'],
 ['python3','tools/java_syntax_guard.py'],
 ['python3','tools/validate_json_resources.py'],
 ['python3','tools/validate_runtime_manifest.py','--manifest','runtime_artifacts/runtime_test_manifest.template.json','--version','11.64.27','--template'],
 ['python3','tools/tc4_recipe_registration_denominator_guard.py'],
 ['python3','tools/tc4_116394_source_inventory_guard.py'],
 ['python3','tools/tc4_116394_parity_consolidation_guard.py'],
]
from concurrent.futures import ThreadPoolExecutor, as_completed
def run_one(item):
 i,c=item; r=subprocess.run(c,cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
 return i,c,r.returncode,r.stdout
results={}
with ThreadPoolExecutor(max_workers=8) as pool:
 futures=[pool.submit(run_one,item) for item in enumerate(checks,1)]
 for future in as_completed(futures):
  i,c,code,out=future.result();results[i]=(c,code,out)
for i in range(1,len(checks)+1):
 c,code,out=results[i]
 print(f'[{i}/{len(checks)}] {" ".join(c)}',flush=True)
 if out: print(out,end='' if out.endswith('\n') else '\n')
 if code:
  print(f'FOCUSED STATIC CI v11.64.27: FAIL at check {i}',file=sys.stderr);raise SystemExit(code)
print(f'FOCUSED STATIC CI v11.64.27: PASS ({len(checks)}/{len(checks)})')
