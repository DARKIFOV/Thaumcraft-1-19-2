#!/usr/bin/env python3
from pathlib import Path
import subprocess,tempfile
R=Path(__file__).resolve().parents[1]
PURE=[
 R/'src/main/java/com/darkifov/thaumcraft/research/TC4ThaumonomiconParity.java',
 R/'src/main/java/com/darkifov/thaumcraft/research/TC4ThaumonomiconLootParity.java',
 R/'tools/java_contracts/TC4ThaumonomiconFullClosureSelfTest.java']
FORGE=[R/p for p in [
'src/main/java/com/darkifov/thaumcraft/research/PlayerAspectKnowledge.java',
'src/main/java/com/darkifov/thaumcraft/research/OriginalResearchProgression.java',
'src/main/java/com/darkifov/thaumcraft/block/ThaumonomiconItem.java',
'src/main/java/com/darkifov/thaumcraft/block/CreativeThaumonomiconItem.java',
'src/main/java/com/darkifov/thaumcraft/client/ClientHooks.java',
'src/main/java/com/darkifov/thaumcraft/client/screen/TC4ThaumonomiconClientState.java',
'src/main/java/com/darkifov/thaumcraft/client/screen/TC4ThaumonomiconPageHistory.java',
'src/main/java/com/darkifov/thaumcraft/client/screen/ThaumonomiconScreen.java',
'src/main/java/com/darkifov/thaumcraft/client/screen/TC4ResearchPageScreen.java',
'src/main/java/com/darkifov/thaumcraft/research/OriginalResearchBridge.java',
'src/main/java/com/darkifov/thaumcraft/network/RequestCompleteSelectedResearchPacket.java',
'src/main/java/com/darkifov/thaumcraft/research/TC4ResearchNoteCreator.java',
'src/main/java/com/darkifov/thaumcraft/block/WandItem.java',
'src/main/java/com/darkifov/thaumcraft/recipe/TC4RecipeRuntimeBridge.java',
'src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java',
'src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java']]
MARKERS=('illegal start of','class, interface, enum, or record expected',"';' expected",'reached end of file while parsing','not a statement','identifier expected','unclosed string literal','unclosed character literal',"')' expected","'}' expected")
with tempfile.TemporaryDirectory() as td:
 r=subprocess.run(['javac','--release','17','-proc:none','-d',td,*map(str,PURE)],cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=60)
 if r.returncode: print(r.stdout);raise SystemExit(1)
 r=subprocess.run(['java','-cp',td,'TC4ThaumonomiconFullClosureSelfTest'],cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=30)
 if r.returncode or 'PASS' not in r.stdout: print(r.stdout);raise SystemExit(1)
with tempfile.TemporaryDirectory() as out:
 q=subprocess.run(['javac','--release','17','-proc:none','-d',out,'-XDshould-stop.at=PARSE','-Xmaxerrs','1000',*map(str,FORGE)],cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=120)
 bad=[line for line in q.stdout.splitlines() if any(m in line for m in MARKERS)]
if bad: print('\n'.join(bad[:200]));raise SystemExit(1)
print(f'Targeted Java parse guard v11.64.26: PASS (Java 17 self-test + {len(FORGE)} Forge parse checks)')
