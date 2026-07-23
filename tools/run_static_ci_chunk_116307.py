#!/usr/bin/env python3
from pathlib import Path
import ast, argparse, subprocess, sys, json
ROOT=Path(__file__).resolve().parents[1]
RUNNER=ROOT/'tools/run_full_static_ci_116307.py'
LOG=Path('/mnt/data/TC4_11.63.10_FULL_STATIC_CI.log')
STATUS=Path('/mnt/data/TC4_11.63.10_FULL_STATIC_CI.status.json')
mod=ast.parse(RUNNER.read_text())
cmds=None
for n in mod.body:
    if isinstance(n,ast.Assign) and any(isinstance(t,ast.Name) and t.id=='CMDS' for t in n.targets):
        cmds=ast.literal_eval(n.value); break
if cmds is None: raise SystemExit('CMDS not found')
ap=argparse.ArgumentParser(); ap.add_argument('--start',type=int,required=True); ap.add_argument('--end',type=int,required=True); ap.add_argument('--reset',action='store_true'); a=ap.parse_args()
if a.reset:
    LOG.write_text('',encoding='utf-8'); STATUS.write_text('{}',encoding='utf-8')
try: status=json.loads(STATUS.read_text())
except Exception: status={}
with LOG.open('a',encoding='utf-8') as log:
    for idx in range(a.start,min(a.end,len(cmds))):
        cmd=cmds[idx]
        header=f'===== [{idx+1}] {cmd} =====\n'; log.write(header); log.flush()
        proc=subprocess.run(cmd,cwd=ROOT,shell=True,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=110)
        log.write(proc.stdout or ''); log.write(f'RC={proc.returncode}\n'); log.flush()
        status[str(idx)]=proc.returncode
        STATUS.write_text(json.dumps(status,indent=2),encoding='utf-8')
        print(f'[{idx+1}/{len(cmds)}] RC={proc.returncode} {cmd}',flush=True)
failed=[i for i in range(a.start,min(a.end,len(cmds))) if status.get(str(i))!=0]
print(f'CHUNK_SUMMARY start={a.start} end={min(a.end,len(cmds))} fail={len(failed)}')
sys.exit(1 if failed else 0)
