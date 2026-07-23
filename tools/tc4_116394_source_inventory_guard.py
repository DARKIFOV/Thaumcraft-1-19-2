#!/usr/bin/env python3
"""Informational source inventory; deliberately not represented as GameTests."""
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
BASE=ROOT/'src/main/java/com/darkifov/thaumcraft'
expected={'entity':38,'menu':21,'item':39,'network':38,'blockentity':54}
for folder,count in expected.items():
    actual=len(list((BASE/folder).glob('*.java')))
    if actual < count:
        raise SystemExit(f'Source inventory guard: FAIL: {folder} expected {count}, got {actual}')
print('Source inventory guard: PASS (entity 38, menu 21, item 39, network 38, blockentity 54+ top-level Java files)')
print('Note: source-file counts are packaging inventory, not original TC4 runtime registration denominators.')
