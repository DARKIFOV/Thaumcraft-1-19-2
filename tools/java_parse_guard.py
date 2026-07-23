#!/usr/bin/env python3
"""Reject Java grammar damage that the legacy escape-only syntax guard misses."""
from pathlib import Path
import subprocess, tempfile, sys

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'src/main/java/com/darkifov/thaumcraft'
files=sorted(JAVA.rglob('TC4*Parity.java'))+[JAVA/'gametest/TC4BlockEntityGameTests.java']
markers=(
    'illegal start of', 'class, interface, enum, or record expected',
    "';' expected", 'reached end of file while parsing', 'not a statement',
    'identifier expected', 'unclosed string literal', 'unclosed character literal',
)
with tempfile.TemporaryDirectory() as out:
    proc=subprocess.run(['javac','-proc:none','-d',out,'-XDshould-stop.at=PARSE',*[str(p) for p in files]],
                        cwd=ROOT,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
text=proc.stdout
bad=[line for line in text.splitlines() if any(m in line for m in markers)]
if bad:
    print('Java parse guard: FAIL')
    print('\n'.join(bad[:100]))
    raise SystemExit(1)
print(f'Java parse guard: PASS ({len(files)} parity/GameTest files; unresolved Forge symbols ignored)')
