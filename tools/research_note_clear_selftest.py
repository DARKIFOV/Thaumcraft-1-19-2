#!/usr/bin/env python3
from pathlib import Path
import subprocess,tempfile,textwrap
ROOT=Path(__file__).resolve().parents[1]
parity=ROOT/'src/main/java/com/darkifov/thaumcraft/research/TC4ResearchNoteClearParity.java'
eff=ROOT/'src/main/java/com/darkifov/thaumcraft/research/TC4ResearchEfficiencyParity.java'
stub=textwrap.dedent('''
package com.darkifov.thaumcraft.research;
public final class ResearchNoteGrid {
  public static final int TYPE_PLACED = 2;
}
''')
main=textwrap.dedent('''
import com.darkifov.thaumcraft.research.TC4ResearchNoteClearParity;
public class ResearchNoteClearSelfTest {
  public static void main(String[] args) {
    if (!TC4ResearchNoteClearParity.canClearHex(true,2,true)) throw new AssertionError("placed hex");
    if (TC4ResearchNoteClearParity.canClearHex(true,0,true)) throw new AssertionError("empty hex");
    if (TC4ResearchNoteClearParity.canClearHex(true,1,true)) throw new AssertionError("anchor hex");
    if (TC4ResearchNoteClearParity.canClearHex(false,2,true)) throw new AssertionError("inactive hex");
    if (!TC4ResearchNoteClearParity.boundariesMatchTc4()) throw new AssertionError("refund boundaries");
    if (TC4ResearchNoteClearParity.creativeHasImplicitRefund()) throw new AssertionError("creative refund");
    if (TC4ResearchNoteClearParity.INK_PER_ACCEPTED_CLEAR != 1) throw new AssertionError("ink cost");
    System.out.println("TC4 v11.64.00 Research Note clear self-test: PASS");
  }
}
''')
with tempfile.TemporaryDirectory() as td:
    td=Path(td)
    pkg=td/'com/darkifov/thaumcraft/research'; pkg.mkdir(parents=True)
    (pkg/'ResearchNoteGrid.java').write_text(stub)
    (td/'ResearchNoteClearSelfTest.java').write_text(main)
    subprocess.run(['javac','-d',str(td),str(pkg/'ResearchNoteGrid.java'),str(eff),str(parity),str(td/'ResearchNoteClearSelfTest.java')],check=True,cwd=ROOT)
    subprocess.run(['java','-cp',str(td),'ResearchNoteClearSelfTest'],check=True,cwd=ROOT)
