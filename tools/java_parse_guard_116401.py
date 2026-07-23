#!/usr/bin/env python3
from pathlib import Path
import subprocess,tempfile
ROOT=Path(__file__).resolve().parents[1]
PURE=ROOT/'src/main/java/com/darkifov/thaumcraft/research/TC4ResearchNoteCompletionParity.java'
FORGE=[
 ROOT/'src/main/java/com/darkifov/thaumcraft/research/ResearchTableInventoryRuntime.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/research/ResearchNoteSolver.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/research/OriginalResearchBridge.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/network/RequestSolveResearchNotePacket.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java',
]
MARKERS=('illegal start of','class, interface, enum, or record expected',"';' expected",'reached end of file while parsing','not a statement','identifier expected','unclosed string literal','unclosed character literal',"')' expected","'}' expected",'unreachable statement')
with tempfile.TemporaryDirectory() as td:
 proc=subprocess.run(['javac','-proc:none','-d',td,str(PURE)],cwd=ROOT,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=60)
 if proc.returncode:
  print('Targeted Java parse guard v11.64.01: FAIL (pure contract)'); print(proc.stdout); raise SystemExit(1)
for path in FORGE:
 with tempfile.TemporaryDirectory() as out:
  proc=subprocess.run(['javac','-proc:none','-d',out,'-XDshould-stop.at=PARSE','-Xmaxerrs','300',str(path)],cwd=ROOT,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=150)
 bad=[line for line in proc.stdout.splitlines() if any(m in line for m in MARKERS)]
 if bad:
  print(f'Targeted Java parse guard v11.64.01: FAIL ({path.name})'); print('\n'.join(bad[:120])); raise SystemExit(1)
print(f'Targeted Java parse guard v11.64.01: PASS ({1+len(FORGE)} changed Java files; pure contract compiled)')
