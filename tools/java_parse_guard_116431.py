#!/usr/bin/env python3
from pathlib import Path
import subprocess,tempfile
R=Path(__file__).resolve().parents[1]
FORGE=[R/p for p in [
'src/main/java/com/darkifov/thaumcraft/arcane/TC4ArcaneWorkbenchVisCostParity.java',
'src/main/java/com/darkifov/thaumcraft/arcane/TC4ArcaneWorkbenchParity.java',
'src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java',
'src/main/java/com/darkifov/thaumcraft/block/ArcaneWorkbenchBlock.java',
'src/main/java/com/darkifov/thaumcraft/block/TableBlock.java',
'src/main/java/com/darkifov/thaumcraft/block/WandItem.java',
'src/main/java/com/darkifov/thaumcraft/client/render/ArcaneWorkbenchRenderer.java',
'src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java',
'src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java',
'src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java']]
MARKERS=('illegal start of','class, interface, enum, or record expected',"';' expected",'reached end of file while parsing','not a statement','identifier expected','unclosed string literal','unclosed character literal',"')' expected","'}' expected")
with tempfile.TemporaryDirectory() as td:
 pure=[R/'src/main/java/com/darkifov/thaumcraft/arcane/TC4ArcaneWorkbenchVisCostParity.java',R/'tools/java_contracts/TC4ArcaneWorkbenchParitySelfTest.java']
 r=subprocess.run(['javac','--release','17','-proc:none','-d',td,*map(str,pure)],cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=60)
 if r.returncode: print(r.stdout);raise SystemExit(1)
 r=subprocess.run(['java','-cp',td,'TC4ArcaneWorkbenchParitySelfTest'],cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=30)
 if r.returncode or 'PASS' not in r.stdout: print(r.stdout);raise SystemExit(1)
with tempfile.TemporaryDirectory() as out:
 q=subprocess.run(['javac','--release','17','-proc:none','-d',out,'-XDshould-stop.at=PARSE','-Xmaxerrs','4000',*map(str,FORGE)],cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=240)
 bad=[line for line in q.stdout.splitlines() if any(m in line for m in MARKERS)]
if bad: print('\n'.join(bad[:300]));raise SystemExit(1)
print(f'Targeted Java parse guard v11.64.31: PASS (Java 17 self-test + {len(FORGE)} Forge parse checks)')
