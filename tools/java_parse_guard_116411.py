#!/usr/bin/env python3
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
PURE = ROOT / 'src/main/java/com/darkifov/thaumcraft/warp/TC4WarpRuntimeParity.java'
FORGE = [
    ROOT / 'src/main/java/com/darkifov/thaumcraft/effect/TC4WarpMobEffect.java',
    ROOT / 'src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java',
]
MARKERS = (
    'illegal start of', 'class, interface, enum, or record expected', "';' expected",
    'reached end of file while parsing', 'not a statement', 'identifier expected',
    'unclosed string literal', 'unclosed character literal', "')' expected", "'}' expected",
    'unreachable statement'
)

with tempfile.TemporaryDirectory() as td:
    result = subprocess.run(
        ['javac', '--release', '17', '-proc:none', '-d', td, str(PURE)],
        cwd=ROOT, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=60)
    if result.returncode:
        print('Targeted Java parse guard v11.64.11: FAIL (pure contract)')
        print(result.stdout)
        raise SystemExit(1)

for path in FORGE:
    with tempfile.TemporaryDirectory() as out:
        result = subprocess.run(
            ['javac', '--release', '17', '-proc:none', '-d', out,
             '-XDshould-stop.at=PARSE', '-Xmaxerrs', '400', str(path)],
            cwd=ROOT, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=180)
    bad = [line for line in result.stdout.splitlines() if any(marker in line for marker in MARKERS)]
    if bad:
        print(f'Targeted Java parse guard v11.64.11: FAIL ({path.name})')
        print('\n'.join(bad[:150]))
        raise SystemExit(1)

print('Targeted Java parse guard v11.64.11: PASS (3 changed Java files; pure contract compiled with --release 17)')
