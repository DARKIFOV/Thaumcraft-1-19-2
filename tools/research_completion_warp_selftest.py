#!/usr/bin/env python3
from pathlib import Path
import subprocess,tempfile,textwrap
ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/'src/main/java/com/darkifov/thaumcraft/research/TC4ResearchCompletionWarpParity.java'
HARNESS='''
import com.darkifov.thaumcraft.research.TC4ResearchCompletionWarpParity;
public class WarpHarness {
  static void check(int w,int p,int s){
    var x=TC4ResearchCompletionWarpParity.splitResearchWarp(w);
    if(x.permanent()!=p || x.sticky()!=s || x.total()!=p+s) throw new AssertionError(w+":"+x);
  }
  public static void main(String[] args){
    check(-1,0,0); check(0,0,0); check(1,1,0); check(2,1,1);
    check(3,2,1); check(4,2,2); check(5,3,2); check(11,6,5);
  }
}
'''
with tempfile.TemporaryDirectory() as td:
    td=Path(td); (td/'WarpHarness.java').write_text(textwrap.dedent(HARNESS),encoding='utf-8')
    r=subprocess.run(['javac','-proc:none','-d',str(td),str(SRC),str(td/'WarpHarness.java')],cwd=ROOT,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
    if r.returncode: print(r.stdout); raise SystemExit(r.returncode)
    r=subprocess.run(['java','-cp',str(td),'WarpHarness'],cwd=ROOT,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
    if r.returncode: print(r.stdout); raise SystemExit(r.returncode)
print('Research completion warp pure-Java self-test: PASS (negative/0/1/even/odd split boundaries)')
