#!/usr/bin/env python3
from pathlib import Path
import subprocess,tempfile
R=Path(__file__).resolve().parents[1]
PURE=[R/'src/main/java/com/darkifov/thaumcraft/block/TC4ArcaneLampParity.java',
      R/'tools/java_contracts/TC4ArcaneLampParitySelfTest.java']
FORGE=[R/'src/main/java/com/darkifov/thaumcraft/block/ArcaneLampBlock.java',
       R/'src/main/java/com/darkifov/thaumcraft/block/ArcaneLampLightBlock.java',
       R/'src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneLampBlockEntity.java',
       R/'src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneLampLightBlockEntity.java',
       R/'src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneBoreBlockEntity.java',
       R/'src/main/java/com/darkifov/thaumcraft/client/render/ArcaneLampRenderer.java',
       R/'src/main/java/com/darkifov/thaumcraft/client/render/model/TC4ArcaneBoreModel.java',
       R/'src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java',
       R/'src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java',
       R/'src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java']
MARKERS=('illegal start of','class, interface, enum, or record expected',"';' expected",'reached end of file while parsing','not a statement','identifier expected','unclosed string literal','unclosed character literal',"')' expected","'}' expected",'unreachable statement')
with tempfile.TemporaryDirectory() as td:
 r=subprocess.run(['javac','--release','17','-proc:none','-d',td,*map(str,PURE)],cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=60)
 if r.returncode: print('Targeted Java parse guard v11.64.22: FAIL pure'); print(r.stdout); raise SystemExit(1)
 r=subprocess.run(['java','-cp',td,'TC4ArcaneLampParitySelfTest'],cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=30)
 if r.returncode or 'PASS' not in r.stdout: print('Targeted Java parse guard v11.64.22: FAIL self-test'); print(r.stdout); raise SystemExit(1)
for p in FORGE:
 with tempfile.TemporaryDirectory() as out:
  q=subprocess.run(['javac','--release','17','-proc:none','-d',out,'-XDshould-stop.at=PARSE','-Xmaxerrs','1000',str(p)],cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=300)
 bad=[line for line in q.stdout.splitlines() if any(m in line for m in MARKERS)]
 if bad: print('Targeted Java parse guard v11.64.22: FAIL '+p.name); print('\n'.join(bad[:200])); raise SystemExit(1)
print('Targeted Java parse guard v11.64.22: PASS (pure Java self-test + 10 Forge parse checks)')
