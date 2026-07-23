#!/usr/bin/env python3
"""Targeted javac parse guard for files changed in v11.63.99."""
from pathlib import Path
import subprocess,tempfile
ROOT=Path(__file__).resolve().parents[1]
PURE=[ROOT/'src/main/java/com/darkifov/thaumcraft/research/TC4ResearchNoteGraphParity.java']
FORGE=[
 ROOT/'src/main/java/com/darkifov/thaumcraft/research/ResearchNoteState.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/research/ResearchTableInventoryRuntime.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/research/ResearchNoteSolver.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/network/RequestPlaceResearchNoteAspectPacket.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableContainerScreen.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java',
]
MARKERS=('illegal start of','class, interface, enum, or record expected',"';' expected",'reached end of file while parsing','not a statement','identifier expected','unclosed string literal','unclosed character literal',"')' expected","'}' expected")
with tempfile.TemporaryDirectory() as out:
    pure=subprocess.run(['javac','-proc:none','-d',out,*map(str,PURE)],cwd=ROOT,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=60)
if pure.returncode:
    print('Targeted Java parse guard: FAIL (pure graph contract compilation)')
    print(pure.stdout[:8000]); raise SystemExit(1)
for path in FORGE:
    with tempfile.TemporaryDirectory() as out:
        proc=subprocess.run(['javac','-proc:none','-d',out,'-XDshould-stop.at=PARSE','-Xmaxerrs','250',str(path)],cwd=ROOT,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=150)
    bad=[line for line in proc.stdout.splitlines() if any(m in line for m in MARKERS)]
    if bad:
        print(f'Targeted Java parse guard: FAIL ({path.name})')
        print('\n'.join(bad[:100])); raise SystemExit(1)
print(f'Targeted Java parse guard: PASS ({len(PURE)+len(FORGE)} changed Java files; pure contract compiled)')
