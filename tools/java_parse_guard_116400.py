#!/usr/bin/env python3
"""Targeted javac parse guard for v11.64.00 Research Note clear changes."""
from pathlib import Path
import subprocess,tempfile,textwrap
ROOT=Path(__file__).resolve().parents[1]
PARITY=ROOT/'src/main/java/com/darkifov/thaumcraft/research/TC4ResearchNoteClearParity.java'
EFF=ROOT/'src/main/java/com/darkifov/thaumcraft/research/TC4ResearchEfficiencyParity.java'
FORGE=[
 ROOT/'src/main/java/com/darkifov/thaumcraft/research/ResearchNoteState.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/research/ResearchTableInventoryRuntime.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/research/ResearchNoteSolver.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/network/RequestClearResearchNoteSlotPacket.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java',
]
MARKERS=('illegal start of','class, interface, enum, or record expected',"';' expected",'reached end of file while parsing','not a statement','identifier expected','unclosed string literal','unclosed character literal',"')' expected","'}' expected")
stub=textwrap.dedent('''
package com.darkifov.thaumcraft.research;
public final class ResearchNoteGrid { public static final int TYPE_PLACED=2; }
''')
with tempfile.TemporaryDirectory() as td:
    td=Path(td); pkg=td/'com/darkifov/thaumcraft/research'; pkg.mkdir(parents=True)
    sp=pkg/'ResearchNoteGrid.java'; sp.write_text(stub)
    pure=subprocess.run(['javac','-proc:none','-d',str(td),str(sp),str(EFF),str(PARITY)],cwd=ROOT,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=60)
if pure.returncode:
    print('Targeted Java parse guard v11.64.00: FAIL (pure contract compilation)')
    print(pure.stdout[:8000]); raise SystemExit(1)
for path in FORGE:
    with tempfile.TemporaryDirectory() as out:
        proc=subprocess.run(['javac','-proc:none','-d',out,'-XDshould-stop.at=PARSE','-Xmaxerrs','250',str(path)],cwd=ROOT,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=150)
    bad=[line for line in proc.stdout.splitlines() if any(m in line for m in MARKERS)]
    if bad:
        print(f'Targeted Java parse guard v11.64.00: FAIL ({path.name})')
        print('\n'.join(bad[:100])); raise SystemExit(1)
print(f'Targeted Java parse guard v11.64.00: PASS ({2+len(FORGE)} changed Java files; pure contract compiled)')
