#!/usr/bin/env python3
from pathlib import Path
import subprocess,tempfile
R=Path(__file__).resolve().parents[1]
FORGE=[R/p for p in [
'src/main/java/com/darkifov/thaumcraft/block/InfernalFurnaceLayer.java',
'src/main/java/com/darkifov/thaumcraft/block/TC4InfernalFurnaceParity.java',
'src/main/java/com/darkifov/thaumcraft/block/InfernalFurnaceMultiblock.java',
'src/main/java/com/darkifov/thaumcraft/block/InfernalFurnaceBlock.java',
'src/main/java/com/darkifov/thaumcraft/blockentity/InfernalFurnaceBlockEntity.java',
'src/main/java/com/darkifov/thaumcraft/blockentity/InfernalFurnaceNozzleBlockEntity.java',
'src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java',
'src/main/java/com/darkifov/thaumcraft/block/WandItem.java',
'src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java',
'src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java']]
MARKERS=('illegal start of','class, interface, enum, or record expected',"';' expected",'reached end of file while parsing','not a statement','identifier expected','unclosed string literal','unclosed character literal',"')' expected","'}' expected")
with tempfile.TemporaryDirectory() as td:
 t=Path(td);(t/'net/minecraft/core').mkdir(parents=True);(t/'net/minecraft/util').mkdir(parents=True)
 (t/'net/minecraft/core/Direction.java').write_text('package net.minecraft.core; public enum Direction { DOWN,UP,NORTH,SOUTH,WEST,EAST; }')
 (t/'net/minecraft/util/StringRepresentable.java').write_text('package net.minecraft.util; public interface StringRepresentable { String getSerializedName(); }')
 pure=[t/'net/minecraft/core/Direction.java',t/'net/minecraft/util/StringRepresentable.java',R/'src/main/java/com/darkifov/thaumcraft/block/InfernalFurnaceLayer.java',R/'src/main/java/com/darkifov/thaumcraft/block/TC4InfernalFurnaceParity.java',R/'tools/java_contracts/TC4InfernalFurnaceParitySelfTest.java']
 r=subprocess.run(['javac','--release','17','-proc:none','-d',td,*map(str,pure)],cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=60)
 if r.returncode: print(r.stdout);raise SystemExit(1)
 r=subprocess.run(['java','-cp',td,'TC4InfernalFurnaceParitySelfTest'],cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=30)
 if r.returncode or 'PASS' not in r.stdout: print(r.stdout);raise SystemExit(1)
with tempfile.TemporaryDirectory() as out:
 q=subprocess.run(['javac','--release','17','-proc:none','-d',out,'-XDshould-stop.at=PARSE','-Xmaxerrs','2500',*map(str,FORGE)],cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=240)
 bad=[line for line in q.stdout.splitlines() if any(m in line for m in MARKERS)]
if bad: print('\n'.join(bad[:250]));raise SystemExit(1)
print(f'Targeted Java parse guard v11.64.28: PASS (Java 17 self-test + {len(FORGE)} Forge parse checks)')
