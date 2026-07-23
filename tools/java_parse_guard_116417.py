#!/usr/bin/env python3
from pathlib import Path
import subprocess, tempfile

ROOT=Path(__file__).resolve().parents[1]
PURE=[
 ROOT/'src/main/java/com/darkifov/thaumcraft/block/TC4TallowCandleParity.java',
 ROOT/'tools/java_contracts/TC4TallowCandleParitySelfTest.java',
]
FORGE=[
 ROOT/'src/main/java/com/darkifov/thaumcraft/block/TallowCandleBlock.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/block/TallowCandleBlockItem.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/blockentity/TallowCandleBlockEntity.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/client/render/TallowCandleRenderer.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/client/render/TallowCandleItemRenderer.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/source/TC4ObjectAspectRegistry.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java',
]
MARKERS=('illegal start of','class, interface, enum, or record expected',"';' expected",
 'reached end of file while parsing','not a statement','identifier expected',
 'unclosed string literal','unclosed character literal',"')' expected","'}' expected",'unreachable statement')
with tempfile.TemporaryDirectory() as td:
    r=subprocess.run(['javac','--release','17','-proc:none','-d',td,*map(str,PURE)],cwd=ROOT,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=60)
    if r.returncode:
        print('Targeted Java parse guard v11.64.17: FAIL (pure contract/self-test)'); print(r.stdout); raise SystemExit(1)
    r=subprocess.run(['java','-cp',td,'TC4TallowCandleParitySelfTest'],cwd=ROOT,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=30)
    if r.returncode or 'PASS' not in r.stdout:
        print('Targeted Java parse guard v11.64.17: FAIL (self-test runtime)'); print(r.stdout); raise SystemExit(1)
for path in FORGE:
    with tempfile.TemporaryDirectory() as out:
        r=subprocess.run(['javac','--release','17','-proc:none','-d',out,'-XDshould-stop.at=PARSE','-Xmaxerrs','1000',str(path)],cwd=ROOT,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=300)
    bad=[line for line in r.stdout.splitlines() if any(m in line for m in MARKERS)]
    if bad:
        print(f'Targeted Java parse guard v11.64.17: FAIL ({path.name})'); print('\n'.join(bad[:200])); raise SystemExit(1)
print('Targeted Java parse guard v11.64.17: PASS (pure Java 17 self-test + 8 Forge source parse checks)')
