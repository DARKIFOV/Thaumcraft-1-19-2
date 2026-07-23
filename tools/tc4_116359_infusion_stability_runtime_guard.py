#!/usr/bin/env python3
"""v11.63.59 original infusion stability/runtime contract guard."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
def read(r): return (ROOT/r).read_text(encoding='utf-8')

def version_tuple(text):
    import re
    found = [(11, int(a), int(b)) for a, b in re.findall(r"11\.(\d+)\.(\d+)", text)]
    return max(found, default=(0, 0, 0))
def req(c,m):
    if not c: raise SystemExit(f'TC4 v11.63.59 infusion stability guard: FAIL: {m}')
def main():
    req(version_tuple(read('build.gradle')) >= (11, 63, 59), 'build version')
    req(version_tuple(read('src/main/resources/META-INF/mods.toml')) >= (11, 63, 59), 'mods version')
    runtime=read('src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionRuntime.java')
    stability=read('src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionStabilityParity.java')
    matrix=read('src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java')
    crystal=read('src/main/java/com/darkifov/thaumcraft/block/CrystalClusterBlock.java')
    tests=read('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
    req('implements InfusionStabilizer' in crystal,'crystal cluster marker')
    for token in ('Blocks.WITHER_SKELETON_SKULL','Blocks.SKELETON_SKULL','Blocks.ZOMBIE_HEAD','Blocks.PLAYER_HEAD','Blocks.CREEPER_HEAD'):
        req(token in stability,f'missing original head {token}')
    for forbidden in ('Blocks.DRAGON_HEAD','ThaumcraftMod.MATRIX_STABILIZER','ThaumcraftMod.NODE_STABILIZER','ThaumcraftMod.ARCANE_STONE_BRICKS','ThaumcraftMod.INFUSION_PILLAR'):
        req(forbidden not in stability,f'non-original stabilizer {forbidden}')
    req('return symmetry + recipeInstability;' in runtime,'single original formula')
    req('TC4InfusionRuntime.initialInstability(symmetry, recipeInstability)' in matrix,'matrix start formula wiring')
    req('return current;' in runtime.split('runningInstability',1)[1].split('}',1)[0], 'running instability stays locked')
    req('currentInstability = TC4InfusionRuntime.runningInstability(' not in matrix, 'running craft must not recompute locked instability')
    req('auxiliary.unpairedInstabilityPenalty() - auxiliary.effectiveStabilizers()' not in matrix,'double application removed')
    for method in ('infusionUsesOnlyOriginalStabilizerWhitelist','infusionStabilizerScanPairsAroundMatrixBlock','infusionInitialInstabilityUsesSingleOriginalSymmetryPass','runningInfusionKeepsCraftingStartInstability'):
        req(method in tests,f'missing {method}')
    req(tests.count('@GameTest(') >= 48, 'expected at least 48 cumulative GameTests')
    manifest=json.loads(read('runtime_artifacts/runtime_test_manifest.template.json'))
    req(version_tuple(str(manifest.get('version'))) >= (11, 63, 59) and len(manifest.get('tests', [])) >= 376, 'manifest version/count')
    ids={x.get('id') for x in manifest['tests']}
    expected={'gametest.infusion_original_stabilizer_whitelist_contract','gametest.infusion_stabilizer_mirror_scan_contract','gametest.infusion_single_symmetry_instability_contract','gametest.infusion_running_instability_monotonic_contract'}
    req(expected<=ids,'manifest scenarios')
    print('TC4 v11.63.59 infusion stability guard: PASS (48 GameTests, 376 scenarios)')
if __name__=='__main__': main()
