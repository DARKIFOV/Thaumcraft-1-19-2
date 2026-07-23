#!/usr/bin/env python3
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
PURE = [
    ROOT / 'src/main/java/com/darkifov/thaumcraft/blockentity/TC4HungryChestParity.java',
    ROOT / 'tools/java_contracts/TC4HungryChestParitySelfTest.java',
]
FORGE = [
    ROOT / 'src/main/java/com/darkifov/thaumcraft/block/HungryChestBlock.java',
    ROOT / 'src/main/java/com/darkifov/thaumcraft/block/HungryChestBlockItem.java',
    ROOT / 'src/main/java/com/darkifov/thaumcraft/blockentity/HungryChestBlockEntity.java',
    ROOT / 'src/main/java/com/darkifov/thaumcraft/client/render/model/TC4HungryChestModel.java',
    ROOT / 'src/main/java/com/darkifov/thaumcraft/client/render/HungryChestRenderer.java',
    ROOT / 'src/main/java/com/darkifov/thaumcraft/client/render/HungryChestItemRenderer.java',
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
        print('Targeted Java parse guard v11.64.16: FAIL (pure contract/self-test)')
        print(result.stdout)
        raise SystemExit(1)
    result = subprocess.run(['java', '-cp', td, 'TC4HungryChestParitySelfTest'],
        cwd=ROOT, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=30)
    if result.returncode or 'PASS' not in result.stdout:
        print('Targeted Java parse guard v11.64.16: FAIL (self-test runtime)')
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
        print(f'Targeted Java parse guard v11.64.16: FAIL ({path.name})')
        print('\n'.join(bad[:200]))
        raise SystemExit(1)

print('Targeted Java parse guard v11.64.16: PASS (pure Java 17 self-test + 6 Forge source parse checks)')
