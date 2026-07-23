#!/usr/bin/env python3
"""v11.63.69 Arcane Workbench vis parity guard."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
def read(r): return (ROOT/r).read_text(encoding="utf-8")
def req(c,m):
    if not c: raise SystemExit(f"TC4 v11.63.69 arcane vis guard: FAIL: {m}")
def main():
    build=read('build.gradle')
    mods=read('src/main/resources/META-INF/mods.toml')
    req("version = '11.63." in build, 'build version marker')
    req('version="11.63.' in mods, 'mods version marker')
    parity=read('src/main/java/com/darkifov/thaumcraft/arcane/TC4ArcaneWorkbenchVisCostParity.java')
    tests=read('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
    for token in ('CONTRACT_VERSION','ordoCostMatchesOriginal','centivisScalingMatchesOriginal','recipeBookEntryCountMatchesOriginal','allRecipeBookEntriesUseOrdo2'):
        req(token in parity,f'missing {token}')
    for method in ('arcaneWorkbenchOrdoVisCostMatchesOriginal','arcaneWorkbenchRecipeBookMatchesOriginal','arcaneWorkbenchGuiCoordinatesMatchTc4Original'):
        req(method in tests,f'missing {method}')
    req(tests.count('@GameTest(')>=80,'expected at least 80 GameTests')
    manifest=json.loads(read('runtime_artifacts/runtime_test_manifest.template.json'))
    req(str(manifest.get('version','')).startswith('11.63.') and len(manifest.get('tests',[]))>=408,'manifest version/count')
    print('TC4 v11.63.69 arcane vis guard: PASS (baseline 80/408; forward-compatible current release)')
if __name__=='__main__': main()
