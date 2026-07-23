#!/usr/bin/env python3
from pathlib import Path
import subprocess,tempfile,textwrap
ROOT=Path(__file__).resolve().parents[1]
SRC=[
 ROOT/'src/main/java/com/darkifov/thaumcraft/warp/TC4UnnaturalHungerParity.java',
 ROOT/'src/main/java/com/darkifov/thaumcraft/warp/TC4WarpRuntimeParity.java',
]
HARNESS='''
import com.darkifov.thaumcraft.warp.TC4WarpRuntimeParity;
public class WarpRuntimeHarness {
  static void req(boolean x){ if(!x) throw new AssertionError(); }
  public static void main(String[] args){
    var a=TC4WarpRuntimeParity.infectiousSpread(3);
    req(a.infectious() && a.amplifier()==2 && a.keepsDefaultCuratives());
    var z=TC4WarpRuntimeParity.infectiousSpread(0);
    req(!z.infectious() && z.amplifier()==0 && z.keepsDefaultCuratives());
    req(Math.abs(TC4WarpRuntimeParity.sanitySoapStickyChance(false,false)-0.33F)<0.0001F);
    req(Math.abs(TC4WarpRuntimeParity.sanitySoapStickyChance(true,true)-0.83F)<0.0001F);
    req(TC4WarpRuntimeParity.sanitySoapConsumption(true)==1);
    req(TC4WarpRuntimeParity.sanitySoapConsumption(false)==1);
    req(TC4WarpRuntimeParity.purifyingFluidWardDuration(-1)==32000);
    req(TC4WarpRuntimeParity.purifyingFluidWardDuration(0)==32000);
    req(TC4WarpRuntimeParity.purifyingFluidWardDuration(1)==32000);
    req(TC4WarpRuntimeParity.purifyingFluidWardDuration(100)==20000);
    req(TC4WarpRuntimeParity.purifyingFluidWardDuration(10000)==2000);
    for(int n=7;n<=24;n++){
      req(TC4WarpRuntimeParity.signedSpawnOffset(n,-1)==-n);
      req(TC4WarpRuntimeParity.signedSpawnOffset(n,0)==0);
      req(TC4WarpRuntimeParity.signedSpawnOffset(n,1)==n);
    }
    req(TC4WarpRuntimeParity.signedSpawnOffset(7,-99)==-7);
    req(TC4WarpRuntimeParity.signedSpawnOffset(7,99)==7);
    req(TC4WarpRuntimeParity.acceptsEntitySpawnCandidate(true,true,false));
    req(!TC4WarpRuntimeParity.acceptsEntitySpawnCandidate(false,true,false));
    req(!TC4WarpRuntimeParity.acceptsEntitySpawnCandidate(true,false,false));
    req(!TC4WarpRuntimeParity.acceptsEntitySpawnCandidate(true,true,true));
    req("warp.text.8".equals(TC4WarpRuntimeParity.BATHSALTS_MILESTONE_MESSAGE_KEY));
    req(TC4WarpRuntimeParity.sunScornedBurns(0.51F,0.0F,true));
    req(!TC4WarpRuntimeParity.sunScornedBurns(0.50F,0.0F,true));
    req(!TC4WarpRuntimeParity.sunScornedBurns(1.0F,0.0F,false));
    req(TC4WarpRuntimeParity.sunScornedHeals(0.24F,0.99F));
    req(!TC4WarpRuntimeParity.sunScornedHeals(0.25F,0.99F));
    req(!TC4WarpRuntimeParity.sunScornedHeals(0.10F,0.20F));
    var hunger=TC4WarpRuntimeParity.unnaturalHungerAfterCurative(5000,3);
    req(hunger.remainsActive() && hunger.duration()==4400 && hunger.amplifier()==2);
    var hunger0=TC4WarpRuntimeParity.unnaturalHungerAfterCurative(5000,0);
    req(!hunger0.remainsActive() && hunger0.duration()==4400 && hunger0.amplifier()==-1);
    var hungerShort=TC4WarpRuntimeParity.unnaturalHungerAfterCurative(600,3);
    req(!hungerShort.remainsActive() && hungerShort.duration()==0 && hungerShort.amplifier()==2);
    double range=8.0D;
    req(TC4WarpRuntimeParity.deathGazeConeContains(0,0,7,0,0,1,range));
    double a20=Math.toRadians(20.0D);
    req(TC4WarpRuntimeParity.deathGazeConeContains(Math.sin(a20)*7,0,Math.cos(a20)*7,0,0,1,range));
    double a30=Math.toRadians(30.0D);
    req(!TC4WarpRuntimeParity.deathGazeConeContains(Math.sin(a30)*7,0,Math.cos(a30)*7,0,0,1,range));
    double axial=7.9D, lateral=Math.tan(a20)*axial;
    req(Math.sqrt(axial*axial+lateral*lateral)>range);
    req(TC4WarpRuntimeParity.deathGazeConeContains(lateral,0,axial,0,0,1,range));
    req(!TC4WarpRuntimeParity.deathGazeConeContains(0,0,range,0,0,1,range));
    req(!TC4WarpRuntimeParity.deathGazeConeContains(0,0,-2,0,0,1,range));
  }
}
'''
with tempfile.TemporaryDirectory() as td:
    td=Path(td); (td/'WarpRuntimeHarness.java').write_text(textwrap.dedent(HARNESS),encoding='utf-8')
    r=subprocess.run(['javac','--release','17','-proc:none','-d',str(td),*map(str,SRC),str(td/'WarpRuntimeHarness.java')],cwd=ROOT,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
    if r.returncode: print(r.stdout); raise SystemExit(r.returncode)
    r=subprocess.run(['java','-cp',str(td),'WarpRuntimeHarness'],cwd=ROOT,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
    if r.returncode: print(r.stdout); raise SystemExit(r.returncode)
print('Warp runtime pure-Java self-test: PASS (spread downgrade, curative contract, soap chance/consumption, ward duration, original tri-state (-1/0/+1) offsets, entity-aware collision/liquid admission, BATHSALTS milestone message key, Unnatural Hunger 600-tick/one-amplifier food reduction, Sun Scorned brightness-table thresholds/rolls, and exact Death Gaze aperture/round-cap geometry)')
