#!/usr/bin/env python3
from pathlib import Path
import subprocess,tempfile
R=Path(__file__).resolve().parents[1]
PURE=[R/'src/main/java/com/darkifov/thaumcraft/block/TC4ArcaneEarParity.java',R/'tools/java_contracts/TC4ArcaneEarParitySelfTest.java']
FORGE=[R/'src/main/java/com/darkifov/thaumcraft/block/ArcaneEarBlock.java',R/'src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneEarBlockEntity.java',R/'src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java',R/'src/main/java/com/darkifov/thaumcraft/source/TC4ObjectAspectRegistry.java',R/'src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java',R/'src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java']
MARKERS=('illegal start of','class, interface, enum, or record expected',"';' expected",'reached end of file while parsing','not a statement','identifier expected','unclosed string literal','unclosed character literal',"')' expected","'}' expected",'unreachable statement')
with tempfile.TemporaryDirectory() as td:
 r=subprocess.run(['javac','--release','17','-proc:none','-d',td,*map(str,PURE)],cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=60)
 if r.returncode: print('Targeted Java parse guard v11.64.19: FAIL pure'); print(r.stdout); raise SystemExit(1)
 r=subprocess.run(['java','-cp',td,'TC4ArcaneEarParitySelfTest'],cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=30)
 if r.returncode or 'PASS' not in r.stdout: print('Targeted Java parse guard v11.64.19: FAIL self-test'); print(r.stdout); raise SystemExit(1)
for p in FORGE:
 with tempfile.TemporaryDirectory() as out:
  r=subprocess.run(['javac','--release','17','-proc:none','-d',out,'-XDshould-stop.at=PARSE','-Xmaxerrs','1000',str(p)],cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=300)
 bad=[line for line in r.stdout.splitlines() if any(m in line for m in MARKERS)]
 if bad: print('Targeted Java parse guard v11.64.19: FAIL '+p.name); print('\n'.join(bad[:200])); raise SystemExit(1)
print('Targeted Java parse guard v11.64.19: PASS (pure Java self-test + 6 Forge parse checks)')
