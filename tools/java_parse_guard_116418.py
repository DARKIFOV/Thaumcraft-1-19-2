#!/usr/bin/env python3
from pathlib import Path
import subprocess,tempfile
R=Path(__file__).resolve().parents[1]
PURE=[R/'src/main/java/com/darkifov/thaumcraft/block/TC4ArcaneBellowsParity.java',R/'tools/java_contracts/TC4ArcaneBellowsParitySelfTest.java']
FORGE=[R/'src/main/java/com/darkifov/thaumcraft/block/BellowsBlock.java',R/'src/main/java/com/darkifov/thaumcraft/blockentity/BellowsBlockEntity.java',R/'src/main/java/com/darkifov/thaumcraft/client/render/BellowsRenderer.java',R/'src/main/java/com/darkifov/thaumcraft/client/render/BellowsItemRenderer.java',R/'src/main/java/com/darkifov/thaumcraft/client/render/model/TC4BellowsModel.java',R/'src/main/java/com/darkifov/thaumcraft/blockentity/CrucibleBlockEntity.java',R/'src/main/java/com/darkifov/thaumcraft/blockentity/AlchemicalFurnaceBlockEntity.java',R/'src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java',R/'src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java']
MARKERS=('illegal start of','class, interface, enum, or record expected',"';' expected",'reached end of file while parsing','not a statement','identifier expected','unclosed string literal','unclosed character literal',"')' expected","'}' expected",'unreachable statement')
with tempfile.TemporaryDirectory() as td:
 r=subprocess.run(['javac','--release','17','-proc:none','-d',td,*map(str,PURE)],cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=60)
 if r.returncode: print('Targeted Java parse guard v11.64.18: FAIL pure'); print(r.stdout); raise SystemExit(1)
 r=subprocess.run(['java','-cp',td,'TC4ArcaneBellowsParitySelfTest'],cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=30)
 if r.returncode or 'PASS' not in r.stdout: print('Targeted Java parse guard v11.64.18: FAIL self-test'); print(r.stdout); raise SystemExit(1)
for p in FORGE:
 with tempfile.TemporaryDirectory() as out:
  r=subprocess.run(['javac','--release','17','-proc:none','-d',out,'-XDshould-stop.at=PARSE','-Xmaxerrs','1000',str(p)],cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=300)
 bad=[line for line in r.stdout.splitlines() if any(m in line for m in MARKERS)]
 if bad: print('Targeted Java parse guard v11.64.18: FAIL '+p.name); print('\n'.join(bad[:200])); raise SystemExit(1)
print('Targeted Java parse guard v11.64.18: PASS (pure Java self-test + 9 Forge parse checks)')
