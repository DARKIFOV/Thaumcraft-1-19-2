#!/usr/bin/env python3
"""v11.63.57/v11.63.58 alchemical processing runtime contract guard."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
def read(r): return (ROOT/r).read_text(encoding='utf-8')
def req(c,m):
    if not c: raise SystemExit(f'TC4 alchemical processing runtime guard: FAIL: {m}')
def main():
    req(any(v in read('build.gradle') for v in ("version = '11.63.58'", "version = '11.63.59'")),'build version')
    runtime=read('src/main/java/com/darkifov/thaumcraft/essentia/TC4DistillationRuntime.java')
    furnace=read('src/main/java/com/darkifov/thaumcraft/blockentity/AlchemicalFurnaceBlockEntity.java')
    tests=read('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
    for token in ('ORIGINAL_MAX_ALEMBICS_ABOVE_FURNACE = 4','furnace.distillationCounter() % interval','offset <= ORIGINAL_MAX_ALEMBICS_ABOVE_FURNACE'):
        req(token in runtime,f'missing {token}')
    req('public int distillationCounter()' in furnace,'persisted local counter accessor')
    methods=('alchemicalFurnaceProcessesPersistsAndRespectsCapacity','furnaceFeedsOnlyFourAlembicsOnLocalFortiethTick','alembicKeepsCapacityFilterSidesAndNbt','centrifugeOnlySplitsCompoundFromBelowWithRedstonePause')
    for method in methods: req(method in tests,f'missing {method}')
    manifest=json.loads(read('runtime_artifacts/runtime_test_manifest.template.json'))
    req(manifest.get('version') in ('11.63.58','11.63.59', '11.63.60', '11.63.61','11.63.62','11.63.63','11.63.64','11.63.65') and len(manifest.get('tests',[]))>=372,'manifest version/count')
    ids={x.get('id') for x in manifest['tests']}
    expected={'gametest.alchemical_furnace_processing_capacity_nbt_contract','gametest.furnace_four_alembic_local_phase_contract','gametest.alembic_capacity_filter_faces_nbt_contract','gametest.centrifuge_compound_redstone_output_contract'}
    req(expected<=ids,'manifest scenarios')
    print('TC4 alchemical processing runtime guard: PASS (four alembics, local phase, 4 contracts)')
if __name__=='__main__': main()
