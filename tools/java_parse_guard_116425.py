#!/usr/bin/env python3
from pathlib import Path
import subprocess,tempfile
R=Path(__file__).resolve().parents[1]
PURE=[R/'src/main/java/com/darkifov/thaumcraft/aura/TC4ThaumometerParity.java',R/'tools/java_contracts/TC4ThaumometerFullClosureSelfTest.java']
FORGE=[R/p for p in [
'src/main/java/com/darkifov/thaumcraft/aura/TC4ThaumometerScanKeys.java',
'src/main/java/com/darkifov/thaumcraft/aura/TC4ThaumometerTargeting.java',
'src/main/java/com/darkifov/thaumcraft/aura/TC4ThaumometerPhenomenaRegistry.java',
'src/main/java/com/darkifov/thaumcraft/block/ThaumometerItem.java',
'src/main/java/com/darkifov/thaumcraft/data/PlayerThaumData.java',
'src/main/java/com/darkifov/thaumcraft/data/NodeScanData.java',
'src/main/java/com/darkifov/thaumcraft/client/ClientScanData.java',
'src/main/java/com/darkifov/thaumcraft/network/ScanKnowledgeSyncPacket.java',
'src/main/java/com/darkifov/thaumcraft/network/ThaumcraftNetwork.java',
'src/main/java/com/darkifov/thaumcraft/research/OriginalResearchProgression.java',
'src/main/java/com/darkifov/thaumcraft/research/TC4ResearchFlagPolicy.java',
'src/main/java/com/darkifov/thaumcraft/client/screen/OriginalResearchLayout.java',
'src/main/java/com/darkifov/thaumcraft/client/render/ThaumometerItemRenderer.java',
'src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java',
'src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java',
'src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java']]
MARKERS=('illegal start of','class, interface, enum, or record expected',"';' expected",'reached end of file while parsing','not a statement','identifier expected','unclosed string literal','unclosed character literal',"')' expected","'}' expected")
with tempfile.TemporaryDirectory() as td:
 r=subprocess.run(['javac','--release','17','-proc:none','-d',td,*map(str,PURE)],cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=60)
 if r.returncode: print(r.stdout);raise SystemExit(1)
 r=subprocess.run(['java','-cp',td,'TC4ThaumometerFullClosureSelfTest'],cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=30)
 if r.returncode or 'PASS' not in r.stdout: print(r.stdout);raise SystemExit(1)
with tempfile.TemporaryDirectory() as out:
 q=subprocess.run(['javac','--release','17','-proc:none','-d',out,'-XDshould-stop.at=PARSE','-Xmaxerrs','1000',*map(str,FORGE)],cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=90)
 bad=[line for line in q.stdout.splitlines() if any(m in line for m in MARKERS)]
if bad: print('\n'.join(bad[:200]));raise SystemExit(1)
print(f'Targeted Java parse guard v11.64.25: PASS (Java 17 self-test + {len(FORGE)} Forge parse checks)')
