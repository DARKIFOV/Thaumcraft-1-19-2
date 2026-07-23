#!/usr/bin/env python3
"""v11.63.67 dispatcher parity wiring guard."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
def read(r): return (ROOT/r).read_text(encoding="utf-8")
def req(c,m):
    if not c: raise SystemExit(f"TC4 v11.63.67 dispatcher parity guard: FAIL: {m}")
def main():
    req("version = '11.63." in read('build.gradle'),'build version marker')
    req('version="11.63.' in read('src/main/resources/META-INF/mods.toml'),'mods version marker')
    events=read('src/main/java/com/darkifov/thaumcraft/infusion/InfusionInstabilityEvents.java')
    tests=read('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
    req('TC4InfusionInstabilityEventTableParity.EVENT_ROLL_BOUND' in events,'EVENT_ROLL_BOUND uses parity')
    req('TC4InfusionInstabilityEventTableParity.gateAllows' in events,'gateAllows called from production')
    req('infusionInstabilityEventDispatcherUsesParityGate' in tests,'dispatcher gate GameTest')
    req(tests.count('@GameTest(')>=73,'expected at least 73 GameTests')
    manifest=json.loads(read('runtime_artifacts/runtime_test_manifest.template.json'))
    req(str(manifest.get('version','')).startswith('11.63.') and len(manifest.get('tests',[]))>=401,'manifest version/count')
    print('TC4 v11.63.67 dispatcher parity wiring guard: PASS (baseline 73/401; forward-compatible current release)')
if __name__=='__main__': main()
