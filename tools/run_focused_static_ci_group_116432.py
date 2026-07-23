#!/usr/bin/env python3
from pathlib import Path
import ast,subprocess,sys
R=Path(__file__).resolve().parents[1]
source=(R/'tools/run_focused_static_ci_116432.py').read_text()
checks=ast.literal_eval(source.split('checks=',1)[1].split('\nfor i,c',1)[0])
start=int(sys.argv[1]); end=int(sys.argv[2])
for index in range(start-1,end):
 c=checks[index]
 try:
  r=subprocess.run(c,cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=300)
 except subprocess.TimeoutExpired as exc:
  print(f'[{index+1}/{len(checks)}] {" ".join(c)}')
  print((exc.stdout or '') if isinstance(exc.stdout,str) else '')
  print(f'FOCUSED STATIC CI v11.64.32: TIMEOUT at {index+1}',file=sys.stderr); raise SystemExit(124)
 print(f'[{index+1}/{len(checks)}] {" ".join(c)}')
 if r.stdout: print(r.stdout,end='' if r.stdout.endswith('\n') else '\n')
 if r.returncode:
  print(f'FOCUSED STATIC CI v11.64.32: FAIL at {index+1}',file=sys.stderr); raise SystemExit(r.returncode)
print(f'GROUP {start}-{end}: PASS')
