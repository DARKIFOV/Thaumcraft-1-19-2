#!/usr/bin/env python3
"""Historical Arcane Lamp guard delegated to the full v11.64.22 closure guard."""
from pathlib import Path
import subprocess,sys
R=Path(__file__).resolve().parents[1]
r=subprocess.run(['python3','tools/tc4_116422_arcane_lamp_full_closure_guard.py'],cwd=R)
if r.returncode: raise SystemExit(r.returncode)
print('TC4 v11.63.28 Arcane Lamp historical guard: PASS via v11.64.22 full closure')
