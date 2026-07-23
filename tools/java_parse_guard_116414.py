#!/usr/bin/env python3
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
PURE = [
    ROOT / 'src/main/java/com/darkifov/thaumcraft/warp/TC4BathSaltsParity.java',
    ROOT / 'tools/java_contracts/TC4BathSaltsParitySelfTest.java',
]
FORGE = [
    ROOT / 'src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java',
    ROOT / 'src/main/java/com/darkifov/thaumcraft/block/BathSaltsItem.java',
    ROOT / 'src/main/java/com/darkifov/thaumcraft/block/PurifyingFluidBlock.java',
    ROOT / 'src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java',
    ROOT / 'src/main/java/com/darkifov/thaumcraft/client/fx/TC4PurifyingBubbleParticle.java',
    ROOT / 'src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java',
    ROOT / 'src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneSpaBlockEntity.java',
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
        ['javac', '--release', '17', '-proc:none', '-d', td, *map(str, PURE)],
        cwd=ROOT, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=60)
    if result.returncode:
        print('Targeted Java parse guard v11.64.14: FAIL (pure contract/self-test)')
        print(result.stdout)
        raise SystemExit(1)
    result = subprocess.run(['java', '-cp', td, 'TC4BathSaltsParitySelfTest'],
        cwd=ROOT, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=30)
    if result.returncode or 'PASS' not in result.stdout:
        print('Targeted Java parse guard v11.64.14: FAIL (self-test runtime)')
        print(result.stdout)
        raise SystemExit(1)

for path in FORGE:
    with tempfile.TemporaryDirectory() as out:
        result = subprocess.run(
            ['javac', '--release', '17', '-proc:none', '-d', out,
             '-XDshould-stop.at=PARSE', '-Xmaxerrs', '1000', str(path)],
            cwd=ROOT, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=240)
    bad = [line for line in result.stdout.splitlines() if any(marker in line for marker in MARKERS)]
    if bad:
        print(f'Targeted Java parse guard v11.64.14: FAIL ({path.name})')
        print('\n'.join(bad[:200]))
        raise SystemExit(1)

print('Targeted Java parse guard v11.64.14: PASS (pure Java 17 self-test + 8 Forge source parse checks)')
