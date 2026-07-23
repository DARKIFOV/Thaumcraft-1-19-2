#!/usr/bin/env python3
"""Targeted javac parse guard for files changed in v11.63.97."""
from pathlib import Path
import subprocess, tempfile

ROOT=Path(__file__).resolve().parents[1]
FILES=[
 ROOT/'src/main/java/com/darkifov/thaumcraft/research/TC4ResearchEfficiencyParity.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/research/TC4ResearchTableBehaviorParity.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/blockentity/ResearchTableBlockEntity.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java',
]
MARKERS=(
 'illegal start of', 'class, interface, enum, or record expected',
 "';' expected", 'reached end of file while parsing', 'not a statement',
 'identifier expected', 'unclosed string literal', 'unclosed character literal',
 "')' expected", "'}' expected",
)

# Pure parity classes must compile fully.
with tempfile.TemporaryDirectory() as out:
    pure=subprocess.run([
        'javac','-proc:none','-d',out,
        str(FILES[0]),str(FILES[1])
    ],cwd=ROOT,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=60)
if pure.returncode:
    print('Targeted Java parse guard: FAIL (pure parity compilation)')
    print(pure.stdout[:8000])
    raise SystemExit(1)

# Forge-dependent files are parsed individually. Missing classpath symbols are expected;
# grammar diagnostics are not.
for path in FILES[2:]:
    with tempfile.TemporaryDirectory() as out:
        proc=subprocess.run([
            'javac','-proc:none','-d',out,'-XDshould-stop.at=PARSE','-Xmaxerrs','200',str(path)
        ],cwd=ROOT,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=150)
    bad=[line for line in proc.stdout.splitlines() if any(m in line for m in MARKERS)]
    if bad:
        print(f'Targeted Java parse guard: FAIL ({path.name})')
        print('\n'.join(bad[:100]))
        raise SystemExit(1)
print(f'Targeted Java parse guard: PASS ({len(FILES)} changed Java files; pure contracts compiled)')
