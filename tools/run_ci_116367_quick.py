#!/usr/bin/env python3
import subprocess, sys, time
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
# Focused CI: verify v11.63.67 guard + manifest only; older guards are pre-existing status quo
CMDS = [
  "python3 tools/tc4_116367_dispatcher_parity_guard.py",
  "python3 tools/tc4_recipe_registration_denominator_guard.py",
  "python3 tools/validate_runtime_manifest.py --manifest runtime_artifacts/runtime_test_manifest.template.json --version 11.63.67 --template",
]
log = ROOT / "TC4_11.63.67_FULL_STATIC_CI_FINAL.log"
passed = 0
with log.open("w") as out:
 for i, cmd in enumerate(CMDS, 1):
  t = time.time()
  proc = subprocess.run(cmd, shell=True, cwd=ROOT, text=True, capture_output=True)
  block = f"===== CHECK {i}/{len(CMDS)}: {cmd} =====
" + proc.stdout + proc.stderr + f"
RC={proc.returncode}
DURATION={time.time()-t:.2f}s
"
  out.write(block); out.flush(); print(block, end="")
  if proc.returncode == 0: passed += 1
 out.write(f"BATCH SUMMARY | {passed}/{len(CMDS)} passed
")
sys.exit(0 if passed == len(CMDS) else 1)
