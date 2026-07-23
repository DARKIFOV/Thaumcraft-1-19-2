#!/usr/bin/env python3
from pathlib import Path
import subprocess,tempfile,textwrap
ROOT=Path(__file__).resolve().parents[1]
source=ROOT/'src/main/java/com/darkifov/thaumcraft/research/TC4ResearchNoteCompletionParity.java'
test=textwrap.dedent("""
import com.darkifov.thaumcraft.research.TC4ResearchNoteCompletionParity;
public class CompletionSelfTest {
 public static void main(String[] args) {
  check(TC4ResearchNoteCompletionParity.acceptsCompletionContext(true,true,true));
  check(!TC4ResearchNoteCompletionParity.acceptsCompletionContext(true,false,true));
  check(TC4ResearchNoteCompletionParity.canCommitCompletion(false,true,true));
  check(!TC4ResearchNoteCompletionParity.canCommitCompletion(true,true,true));
  check(!TC4ResearchNoteCompletionParity.completionConsumesAdditionalInk());
  check(TC4ResearchNoteCompletionParity.completedDiscoveryConsumedInCreative());
  check(TC4ResearchNoteCompletionParity.completedDiscoveryConsumeCount()==1);
  check(TC4ResearchNoteCompletionParity.shouldUnlockSibling(true,false,true));
  check(!TC4ResearchNoteCompletionParity.shouldUnlockSibling(true,true,true));
  System.out.println("TC4 v11.64.01 Research Note completion self-test: PASS");
 }
 static void check(boolean v) { if(!v) throw new AssertionError(); }
}
""")
with tempfile.TemporaryDirectory() as td:
 td=Path(td); java=td/'CompletionSelfTest.java'; java.write_text(test)
 subprocess.run(['javac','-proc:none','-d',str(td),str(source),str(java)],check=True,cwd=ROOT)
 subprocess.run(['java','-cp',str(td),'CompletionSelfTest'],check=True,cwd=ROOT)
