#!/usr/bin/env python3
from pathlib import Path
import subprocess,tempfile
R=Path(__file__).resolve().parents[1]
PURE=[
 R/'src/main/java/com/darkifov/thaumcraft/research/TC4ResearchSystemFullClosureParity.java',
 R/'tools/java_contracts/TC4ResearchSystemFullClosureSelfTest.java']
FORGE=[R/p for p in [
'src/main/java/com/darkifov/thaumcraft/research/PlayerAspectKnowledge.java',
'src/main/java/com/darkifov/thaumcraft/research/OriginalAspectWallet.java',
'src/main/java/com/darkifov/thaumcraft/research/ResearchNoteRequirements.java',
'src/main/java/com/darkifov/thaumcraft/research/TC4ResearchNoteCreator.java',
'src/main/java/com/darkifov/thaumcraft/research/OriginalResearchBridge.java',
'src/main/java/com/darkifov/thaumcraft/research/TC4ResearchFlagPolicy.java',
'src/main/java/com/darkifov/thaumcraft/research/ResearchNoteState.java',
'src/main/java/com/darkifov/thaumcraft/research/ResearchTableInventoryRuntime.java',
'src/main/java/com/darkifov/thaumcraft/research/ResearchNoteSolver.java',
'src/main/java/com/darkifov/thaumcraft/research/ResearchTableFoundation.java',
'src/main/java/com/darkifov/thaumcraft/block/ResearchNoteItem.java',
'src/main/java/com/darkifov/thaumcraft/blockentity/ResearchTableBlockEntity.java',
'src/main/java/com/darkifov/thaumcraft/network/RequestSelectResearchPacket.java',
'src/main/java/com/darkifov/thaumcraft/network/RequestResearchTableActionPacket.java',
'src/main/java/com/darkifov/thaumcraft/network/RequestSolveResearchNotePacket.java',
'src/main/java/com/darkifov/thaumcraft/network/RequestPlaceResearchNoteAspectPacket.java',
'src/main/java/com/darkifov/thaumcraft/network/RequestClearResearchNoteSlotPacket.java',
'src/main/java/com/darkifov/thaumcraft/network/RequestCombineAspectsPacket.java',
'src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java']]
MARKERS=('illegal start of','class, interface, enum, or record expected',"';' expected",'reached end of file while parsing','not a statement','identifier expected','unclosed string literal','unclosed character literal',"')' expected","'}' expected")
with tempfile.TemporaryDirectory() as td:
 r=subprocess.run(['javac','--release','17','-proc:none','-d',td,*map(str,PURE)],cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=60)
 if r.returncode: print(r.stdout);raise SystemExit(1)
 r=subprocess.run(['java','-cp',td,'TC4ResearchSystemFullClosureSelfTest'],cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=30)
 if r.returncode or 'PASS' not in r.stdout: print(r.stdout);raise SystemExit(1)
with tempfile.TemporaryDirectory() as out:
 q=subprocess.run(['javac','--release','17','-proc:none','-d',out,'-XDshould-stop.at=PARSE','-Xmaxerrs','1500',*map(str,FORGE)],cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=180)
 bad=[line for line in q.stdout.splitlines() if any(m in line for m in MARKERS)]
if bad: print('\n'.join(bad[:250]));raise SystemExit(1)
print(f'Targeted Java parse guard v11.64.27: PASS (Java 17 self-test + {len(FORGE)} Forge parse checks)')
