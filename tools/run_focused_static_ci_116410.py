#!/usr/bin/env python3
from pathlib import Path
import subprocess
import sys

R = Path(__file__).resolve().parents[1]
checks = [
    ['python3', 'tools/tc4_116410_death_gaze_cone_guard.py'],
    ['python3', 'tools/tc4_116409_warp_research_pool_sync_guard.py'],
    ['python3', 'tools/tc4_116408_eldritch_milestone_grant_guard.py'],
    ['python3', 'tools/tc4_116407_warp_message_parity_guard.py'],
    ['python3', 'tools/tc4_116405_warp_spawn_collision_guard.py'],
    ['python3', 'tools/tc4_116404_warp_spawn_offset_guard.py'],
    ['python3', 'tools/tc4_116403_warp_runtime_guard.py'],
    ['python3', 'tools/tc4_116281_warp_eldritch_parity_guard.py'],
    ['python3', 'tools/warp_runtime_selftest.py'],
    ['python3', 'tools/java_parse_guard_116410.py'],
    ['python3', 'tools/java_syntax_guard.py'],
    ['python3', 'tools/validate_json_resources.py'],
    ['python3', 'tools/validate_runtime_manifest.py', '--manifest',
     'runtime_artifacts/runtime_test_manifest.template.json', '--version', '11.64.10', '--template'],
    ['python3', 'tools/tc4_recipe_registration_denominator_guard.py'],
    ['python3', 'tools/tc4_116394_source_inventory_guard.py'],
    ['python3', 'tools/tc4_116394_parity_consolidation_guard.py'],
]

for index, command in enumerate(checks, 1):
    print(f'[{index}/{len(checks)}] {" ".join(command)}', flush=True)
    result = subprocess.run(command, cwd=R, text=True)
    if result.returncode:
        print(f'FOCUSED STATIC CI v11.64.10: FAIL at check {index}', file=sys.stderr)
        raise SystemExit(result.returncode)
print(f'FOCUSED STATIC CI v11.64.10: PASS ({len(checks)}/{len(checks)})')
