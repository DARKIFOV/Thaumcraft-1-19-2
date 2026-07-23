#!/usr/bin/env python3
"""Forward-compatible Arcane Bellows guard.

The v11.63.23 implementation was superseded by the complete v11.64.18 source/resource closure.
"""
from pathlib import Path
import re,subprocess,sys
R=Path(__file__).resolve().parents[1]
s=(R/'build.gradle').read_text()
m=re.search(r"(?m)^version = '(\d+)\.(\d+)\.(\d+)'",s)
if m and tuple(map(int,m.groups())) >= (11,64,18):
    raise SystemExit(subprocess.run([sys.executable,'tools/tc4_116418_arcane_bellows_full_closure_guard.py'],cwd=R).returncode)
raise SystemExit('TC4 legacy Arcane Bellows guard: unsupported pre-v11.64.18 source tree')
