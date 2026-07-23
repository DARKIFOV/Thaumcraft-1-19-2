#!/usr/bin/env python3
from pathlib import Path
import subprocess
ROOT=Path(__file__).resolve().parents[1]
COMMANDS=[
 'python3 tools/forge_only_guard.py',
 'python3 tools/java_syntax_guard.py',
 'python3 tools/forge_1192_compile_api_guard.py',
 'python3 tools/java_parse_guard.py',
 'python3 tools/tc4_116394_source_inventory_guard.py',
 'python3 tools/tc4_116394_parity_consolidation_guard.py',
 'python3 tools/tc4_116395_thaumometer_parity_guard.py',
 'python3 tools/thaumometer_scan_guard.py',
 'python3 tools/validate_runtime_manifest.py --manifest runtime_artifacts/runtime_test_manifest.template.json --version 11.63.95 --template',
 'python3 tools/validate_json_resources.py',
 'python3 tools/tc4_recipe_registration_denominator_guard.py',
]
passed=0
for i,command in enumerate(COMMANDS,1):
    print(f'[{i}/{len(COMMANDS)}] {command}',flush=True)
    result=subprocess.run(command,shell=True,cwd=ROOT,text=True)
    if result.returncode:
        print('FAIL',flush=True)
    else:
        passed+=1
        print('PASS',flush=True)
print(f'SUMMARY {passed}/{len(COMMANDS)} PASS; {len(COMMANDS)-passed} FAIL')
raise SystemExit(0 if passed==len(COMMANDS) else 1)
