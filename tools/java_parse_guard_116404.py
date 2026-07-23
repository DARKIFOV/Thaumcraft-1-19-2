#!/usr/bin/env python3
from pathlib import Path
import subprocess,tempfile
ROOT=Path(__file__).resolve().parents[1]
PURE=ROOT/'src/main/java/com/darkifov/thaumcraft/warp/TC4WarpRuntimeParity.java'
FORGE=[
 ROOT/'src/main/java/com/darkifov/thaumcraft/event/WarpEvents.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java',
]
MARKERS=('illegal start of','class, interface, enum, or record expected',"';' expected",'reached end of file while parsing','not a statement','identifier expected','unclosed string literal','unclosed character literal',"')' expected","'}' expected",'unreachable statement')
with tempfile.TemporaryDirectory() as td:
 r=subprocess.run(['javac','-proc:none','-d',td,str(PURE)],cwd=ROOT,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=60)
 if r.returncode:
  print('Targeted Java parse guard v11.64.04: FAIL (pure contract)'); print(r.stdout); raise SystemExit(1)
for path in FORGE:
 with tempfile.TemporaryDirectory() as out:
  r=subprocess.run(['javac','-proc:none','-d',out,'-XDshould-stop.at=PARSE','-Xmaxerrs','300',str(path)],cwd=ROOT,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=150)
 bad=[line for line in r.stdout.splitlines() if any(m in line for m in MARKERS)]
 if bad:
  print(f'Targeted Java parse guard v11.64.04: FAIL ({path.name})'); print('\n'.join(bad[:120])); raise SystemExit(1)
print('Targeted Java parse guard v11.64.04: PASS (3 changed Java files; pure contract compiled)')
