#!/usr/bin/env python3
"""v11.63.63 original Infusion Matrix weighted instability event table guard."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
def read(r): return (ROOT/r).read_text(encoding="utf-8")

def version_tuple(text):
    import re
    found = [(11, int(a), int(b)) for a, b in re.findall(r"11\.(\d+)\.(\d+)", text)]
    return max(found, default=(0, 0, 0))
def req(c,m):
    if not c: raise SystemExit(f"TC4 v11.63.63 infusion instability event table guard: FAIL: {m}")
def main():
    req(version_tuple(read('build.gradle')) >= (11, 63, 63), 'build version')
    req(version_tuple(read('src/main/resources/META-INF/mods.toml')) >= (11, 63, 63), 'mods version')
    parity=read('src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionInstabilityEventTableParity.java')
    tests=read('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
    req(any(v in parity for v in ('CONTRACT_VERSION = "11.63.63"','CONTRACT_VERSION = "11.63.65"')),'parity version')
    req('GATE_ROLL_BOUND = 500' in parity,'gate roll bound')
    req('EVENT_ROLL_BOUND = 21' in parity,'event roll bound')
    for token in ('EJECT_TYPE_0_ROLLS','EJECT_FLUX_GOO_ROLLS','EJECT_FLUX_GAS_ROLLS','ZAP_SINGLE_ROLLS','HARM_SINGLE_ROLLS','ZAP_ALL_ROLL','EJECT_TYPE_3_ROLL','EJECT_TYPE_4_ROLL','EXPLODE_PEDESTAL_ROLLS','HARM_ALL_ROLL','EXPLODE_MATRIX_ROLL','WARP_ROLL','countAssignedRolls','gateAllows'):
        req(token in parity,f'missing parity token {token}')
    req('EXPLODE_MATRIX_BASE_STRENGTH = 1.5F' in parity,'explosion base strength')
    req('WARP_STICKY_CHANCE = 0.25F' in parity,'warp sticky chance')
    req('WARP_PERMANENT_MIN = 1' in parity and 'WARP_PERMANENT_MAX = 5' in parity,'warp permanent range')
    for method in ('infusionInstabilityEventTableRollBoundMatchesOriginal','infusionInstabilityEventTableGateMatchesOriginal','infusionInstabilityEventTableIsTotalAndNonOverlapping','infusionInstabilityEventTableCosmeticSurgeIsDefaultBranch','infusionInstabilityEventTableExplosionAndWarpMatchOriginal'):
        req(method in tests,f'missing {method}')
    req('TC4InfusionInstabilityEventTableParity' in tests,'missing import usage in gametest file')
    req(tests.count('@GameTest(') >= 66, 'expected at least 66 cumulative GameTests')
    manifest=json.loads(read('runtime_artifacts/runtime_test_manifest.template.json'))
    req(version_tuple(str(manifest.get('version'))) >= (11, 63, 63) and len(manifest.get('tests', [])) >= 394, 'manifest version/count')
    expected={'gametest.infusion_instability_event_table_roll_bound_contract','gametest.infusion_instability_event_table_gate_contract','gametest.infusion_instability_event_table_total_contract','gametest.infusion_instability_event_table_branch_sizes_contract','gametest.infusion_instability_event_table_explosion_warp_contract'}
    req(expected <= {x.get('id') for x in manifest['tests']},'manifest scenarios')
    print('TC4 v11.63.63 infusion instability event table guard: PASS (66 GameTests, 394 scenarios)')
if __name__=='__main__': main()
