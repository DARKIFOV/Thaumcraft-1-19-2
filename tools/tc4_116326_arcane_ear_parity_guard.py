#!/usr/bin/env python3
"""Compatibility entry point: current Arcane Ear closure supersedes v11.63.26."""
from pathlib import Path
import runpy
R=Path(__file__).resolve().parents[1]
runpy.run_path(str(R/'tools/tc4_116419_arcane_ear_full_closure_guard.py'),run_name='__main__')
