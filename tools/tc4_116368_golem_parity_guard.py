#!/usr/bin/env python3
"""v11.63.68 TC4 golem parity guard."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
def read(r): return (ROOT/r).read_text(encoding="utf-8")
def req(c,m):
    if not c: raise SystemExit(f"TC4 v11.63.68 golem parity guard: FAIL: {m}")
def main():
    build=read('build.gradle')
    mods=read('src/main/resources/META-INF/mods.toml')
    req("version = '11.63." in build, 'build version marker')
    req('version="11.63.' in mods, 'mods version marker')
    parity=read('src/main/java/com/darkifov/thaumcraft/golem/TC4GolemParity.java')
    tests=read('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
    for token in ('CONTRACT_VERSION','everyCoreHasCreeperAvoidance','fishCoreHasFishingTask','useCoreHasUseItemTask','materialStatsMatchTc4','upgradeStackingMatchesTc4'):
        req(token in parity,f'missing {token}')
    for method in ('golemEveryCoreHasCreeperAvoidanceAtHighestPriority','golemFishCoreHasAIFishWithOriginalPriority','golemUseCoreHasAIUseItem','golemMaterialStatsMatchTc4EnumGolemType'):
        req(method in tests,f'missing {method}')
    req(tests.count('@GameTest(')>=77,'expected at least 77 GameTests')
    manifest=json.loads(read('runtime_artifacts/runtime_test_manifest.template.json'))
    req(str(manifest.get('version','')).startswith('11.63.') and len(manifest.get('tests',[]))>=405,'manifest version/count')
    ids={x.get('id') for x in manifest['tests']}
    for eid in ('gametest.golem_every_core_has_creeper_avoidance_contract','gametest.golem_fish_core_has_aifish_contract','gametest.golem_material_stats_match_tc4_enumgolemtype_contract'):
        req(eid in ids,f'missing {eid}')
    print('TC4 v11.63.68 golem parity guard: PASS (baseline 77/405; forward-compatible current release)')
if __name__=='__main__': main()
