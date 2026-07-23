#!/usr/bin/env python3
"""v11.63.60 original Infusion Matrix lifecycle guard."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
def read(r): return (ROOT/r).read_text(encoding="utf-8")

def version_tuple(text):
    import re
    found = [(11, int(a), int(b)) for a, b in re.findall(r"11\.(\d+)\.(\d+)", text)]
    return max(found, default=(0, 0, 0))
def req(c,m):
    if not c: raise SystemExit(f"TC4 v11.63.60 infusion lifecycle guard: FAIL: {m}")
def main():
    req(version_tuple(read('build.gradle')) >= (11, 63, 60), 'build version')
    req(version_tuple(read('src/main/resources/META-INF/mods.toml')) >= (11, 63, 60), 'mods version')
    matrix=read('src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java')
    lifecycle=read('src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionLifecycleParity.java')
    tests=read('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
    req('INVALID_CATALYST_EVENT_ROLLS = 1' in lifecycle,'single invalid-catalyst event')
    req('cancelAfterInvalidCatalyst' in matrix,'dedicated invalid-catalyst cancellation')
    req('PlayerThaumData.addWarp(owner, Math.max(1, currentInstability / 2))' not in matrix,'success warp removed')
    branch=matrix.split('private void cancelAfterInvalidCatalyst',1)[1].split('private void failInfusion',1)[0]
    req('PlayerThaumData.addWarp' not in branch,'invalid-catalyst terminal warp removed')
    for method in ('infusionChangedCatalystRollsExactlyOneInstabilityBranch','infusionChangedCatalystAddsNoAutomaticFailureWarp','infusionSuccessAddsNoAutomaticWarp','infusionMissingComponentWaitsInsteadOfCancelling'):
        req(method in tests,f'missing {method}')
    req(tests.count('@GameTest(') >= 52, 'expected at least 52 cumulative GameTests')
    manifest=json.loads(read('runtime_artifacts/runtime_test_manifest.template.json'))
    req(version_tuple(str(manifest.get('version'))) >= (11, 63, 60) and len(manifest.get('tests', [])) >= 380, 'manifest version/count')
    ids={x.get('id') for x in manifest['tests']}
    expected={'gametest.infusion_changed_catalyst_single_event_contract','gametest.infusion_changed_catalyst_no_terminal_warp_contract','gametest.infusion_success_no_automatic_warp_contract','gametest.infusion_missing_component_wait_contract'}
    req(expected<=ids,'manifest scenarios')
    print('TC4 v11.63.60 infusion lifecycle guard: PASS (52 GameTests, 380 scenarios)')
if __name__=='__main__': main()
