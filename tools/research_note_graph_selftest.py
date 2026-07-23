#!/usr/bin/env python3
from pathlib import Path
import subprocess,tempfile,textwrap
ROOT=Path(__file__).resolve().parents[1]
source=ROOT/'src/main/java/com/darkifov/thaumcraft/research/TC4ResearchNoteGraphParity.java'
main=textwrap.dedent('''
import com.darkifov.thaumcraft.research.TC4ResearchNoteGraphParity;
public class ResearchNoteGraphSelfTest {
  public static void main(String[] args) {
    if (!TC4ResearchNoteGraphParity.canPlaceIntoHex(12, 0, true)) throw new AssertionError("empty hex");
    if (TC4ResearchNoteGraphParity.canPlaceIntoHex(12, 1, false)) throw new AssertionError("anchor overwrite");
    if (TC4ResearchNoteGraphParity.canPlaceIntoHex(61, 0, true)) throw new AssertionError("slot range");
    if (TC4ResearchNoteGraphParity.placementRequiresCompatibleNeighbour()) throw new AssertionError("neighbour gate");
    if (!TC4ResearchNoteGraphParity.acceptsServerTableContext(true, true)) throw new AssertionError("live table");
    if (TC4ResearchNoteGraphParity.acceptsServerTableContext(true, false)) throw new AssertionError("stale table");
    if (!TC4ResearchNoteGraphParity.sourceContractMatchesTc4()) throw new AssertionError("source contract");
    System.out.println("TC4 v11.63.99 Research Note graph self-test: PASS");
  }
}
''')
with tempfile.TemporaryDirectory() as td:
    td=Path(td); (td/'ResearchNoteGraphSelfTest.java').write_text(main)
    subprocess.run(['javac','-d',str(td),str(source),str(td/'ResearchNoteGraphSelfTest.java')],check=True,cwd=ROOT)
    subprocess.run(['java','-cp',str(td),'ResearchNoteGraphSelfTest'],check=True,cwd=ROOT)
