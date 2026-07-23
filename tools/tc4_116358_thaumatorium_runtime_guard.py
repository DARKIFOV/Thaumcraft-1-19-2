#!/usr/bin/env python3
"""v11.63.58 Thaumatorium runtime contract guard."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
def read(r): return (ROOT/r).read_text(encoding='utf-8')
def req(c,m):
    if not c: raise SystemExit(f'TC4 v11.63.58 Thaumatorium runtime guard: FAIL: {m}')
def main():
    req(any(v in read('build.gradle') for v in ("version = '11.63.58'", "version = '11.63.59'", "version = '11.63.60'", "version = '11.63.61'", "version = '11.63.62'")),'build version')
    req(any(v in read('src/main/resources/META-INF/mods.toml') for v in ('version="11.63.58"','version="11.63.59"','version="11.63.60"','version="11.63.61"','version="11.63.62"')),'mods version')
    tile=read('src/main/java/com/darkifov/thaumcraft/blockentity/ThaumatoriumBlockEntity.java')
    tests=read('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
    req('essentia.clear();\n        catalyst.shrink(1);' in tile,'complete recipe must clear entire buffer')
    for method in ('thaumatoriumRequiresHeatAndRedstoneStopsSuction','thaumatoriumCraftClearsEntireLegacyEssentiaBuffer','mnemonicMatricesExpandThaumatoriumFormulaMemoryByTwo','blockedThaumatoriumOutputPreservesCatalystAndEssentia'):
        req(method in tests,f'missing {method}')
    req(tests.count('@GameTest(')>=44,'expected at least 44 GameTests')
    manifest=json.loads(read('runtime_artifacts/runtime_test_manifest.template.json'))
    req(manifest.get('version') in ('11.63.58','11.63.59', '11.63.60', '11.63.61','11.63.62','11.63.63','11.63.64','11.63.65') and len(manifest.get('tests',[]))>=372,'manifest version/count')
    ids={x.get('id') for x in manifest['tests']}
    expected={'gametest.thaumatorium_heat_redstone_suction_contract','gametest.thaumatorium_complete_buffer_clear_contract','gametest.thaumatorium_mnemonic_formula_memory_contract','gametest.thaumatorium_blocked_output_rollback_contract'}
    req(expected<=ids,'manifest scenarios')
    for workflow in ('.github/workflows/build.yml','.github/workflows/release.yml'):
        text=read(workflow); req(any(f'--version {v}' in text for v in ('11.63.58','11.63.59','11.63.60','11.63.61','11.63.62')),f'{workflow} version'); req('tc4_116358_thaumatorium_runtime_guard.py' in text,f'{workflow} wiring')
    print('TC4 v11.63.58 Thaumatorium runtime guard: PASS (44 GameTests, 372 scenarios)')
if __name__=='__main__': main()
