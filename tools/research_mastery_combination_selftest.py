#!/usr/bin/env python3
from pathlib import Path
import subprocess,tempfile,textwrap
ROOT=Path(__file__).resolve().parents[1]
source=ROOT/'src/main/java/com/darkifov/thaumcraft/research/TC4ResearchMasteryCombinationParity.java'
main=textwrap.dedent('''
import com.darkifov.thaumcraft.research.TC4ResearchMasteryCombinationParity;
public class CombinationSelfTest {
  public static void main(String[] args) {
    var mixed=TC4ResearchMasteryCombinationParity.plan(0,1,1,0,false).orElseThrow();
    if (mixed.first().tableBonus()!=1 || mixed.second().playerPool()!=1) throw new AssertionError("mixed sources");
    if (TC4ResearchMasteryCombinationParity.plan(1,0,0,0,false).isPresent()) throw new AssertionError("partial pair");
    var same=TC4ResearchMasteryCombinationParity.plan(1,1,0,0,true).orElseThrow();
    if (same.firstPoolTotal()!=1 || same.firstBonusTotal()!=1) throw new AssertionError("same aspect allocation");
    if (!TC4ResearchMasteryCombinationParity.sourcePriorityMatchesTc4()) throw new AssertionError("source priority");
    if (!TC4ResearchMasteryCombinationParity.atomicPreflightMatchesTc4()) throw new AssertionError("atomic preflight");
    System.out.println("TC4 v11.63.98 combination parity self-test: PASS");
  }
}
''')
with tempfile.TemporaryDirectory() as td:
    td=Path(td); (td/'CombinationSelfTest.java').write_text(main)
    subprocess.run(['javac','-d',str(td),str(source),str(td/'CombinationSelfTest.java')],check=True,cwd=ROOT)
    subprocess.run(['java','-cp',str(td),'CombinationSelfTest'],check=True,cwd=ROOT)
