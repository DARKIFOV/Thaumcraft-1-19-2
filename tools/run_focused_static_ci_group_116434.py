#!/usr/bin/env python3
from pathlib import Path
import ast,subprocess,sys
R=Path(__file__).resolve().parents[1]
tree=ast.parse((R/'tools/run_focused_static_ci_116434.py').read_text())
assign=next(n for n in tree.body if isinstance(n,ast.Assign) and any(isinstance(t,ast.Name) and t.id=='checks' for t in n.targets))
checks=ast.literal_eval(assign.value)
start=int(sys.argv[1]); end=int(sys.argv[2])
for index in range(start-1,end):
    command=checks[index]
    try:
        result=subprocess.run(command,cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=300)
    except subprocess.TimeoutExpired as exc:
        print(f'[{index+1}/{len(checks)}] {" ".join(command)}')
        if exc.stdout: print(exc.stdout if isinstance(exc.stdout,str) else exc.stdout.decode(errors='replace'))
        print(f'FOCUSED STATIC CI v11.64.34: TIMEOUT at {index+1}',file=sys.stderr)
        raise SystemExit(124)
    print(f'[{index+1}/{len(checks)}] {" ".join(command)}')
    if result.stdout: print(result.stdout,end='' if result.stdout.endswith('\n') else '\n')
    if result.returncode:
        print(f'FOCUSED STATIC CI v11.64.34: FAIL at {index+1}',file=sys.stderr)
        raise SystemExit(result.returncode)
print(f'GROUP {start}-{end}: PASS')
