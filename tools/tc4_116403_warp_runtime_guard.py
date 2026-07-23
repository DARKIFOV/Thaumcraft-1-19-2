#!/usr/bin/env python3
from pathlib import Path
import json,re
ROOT=Path(__file__).resolve().parents[1]
def req(cond,msg):
    if not cond: raise SystemExit('TC4 v11.64.03 warp runtime guard: FAIL: '+msg)
def text(rel): return (ROOT/rel).read_text(encoding='utf-8')
def version_tuple(raw):
    m=re.search(r'(?m)^version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']',raw)
    req(m is not None,'version parse'); return tuple(map(int,m.groups()))
req(version_tuple(text('build.gradle')) >= (11,64,3),'build version')
req(version_tuple(text('src/main/resources/META-INF/mods.toml')) >= (11,64,3),'mods version')
parity=text('src/main/java/com/darkifov/thaumcraft/warp/TC4WarpRuntimeParity.java')
cm=re.search(r'CONTRACT_VERSION = "([0-9]+)\.([0-9]+)\.([0-9]+)"',parity)
req(cm is not None and tuple(map(int,cm.groups()))>=(11,64,3),'contract version >=11.64.03')
for token in ('INFECTIOUS_SPREAD_INTERVAL_TICKS = 40','INFECTIOUS_SPREAD_RADIUS = 4.0D','INFECTIOUS_SPREAD_DURATION_TICKS = 6000','SANITY_SOAP_USE_TICKS = 200','SANITY_SOAP_COMPLETION_THRESHOLD = 195','SANITY_SOAP_BASE_STICKY_CHANCE = 0.33F','SANITY_SOAP_WARD_BONUS = 0.25F','SANITY_SOAP_PURE_FLUID_BONUS = 0.25F','sanitySoapConsumption','return 1;','purifyingFluidWardDuration'):
    req(token in parity,'missing parity token '+token)
effect=text('src/main/java/com/darkifov/thaumcraft/effect/TC4WarpMobEffect.java')
for token in ('TC4WarpRuntimeParity.infectiousSpread(amplifier)','TC4WarpRuntimeParity.INFECTIOUS_SPREAD_RADIUS','TC4WarpRuntimeParity.INFECTIOUS_SPREAD_DURATION_TICKS','duration % TC4WarpRuntimeParity.INFECTIOUS_SPREAD_INTERVAL_TICKS == 0'):
    req(token in effect,'effect production wiring '+token)
spread=effect[effect.index('private static void spreadVisExhaustion'):effect.index('private static void tickSunScorned')]
req('configureCuratives(new MobEffectInstance' not in spread,'propagated effects still clear curatives')
req('target.addEffect(new MobEffectInstance' in spread,'propagated effects are not plain instances')
soap=text('src/main/java/com/darkifov/thaumcraft/block/SanitySoapItem.java')
for token in ('TC4WarpRuntimeParity.SANITY_SOAP_USE_TICKS','TC4WarpRuntimeParity.SANITY_SOAP_COMPLETION_THRESHOLD','TC4WarpRuntimeParity.sanitySoapStickyChance','stack.shrink(TC4WarpRuntimeParity.sanitySoapConsumption'):
    req(token in soap,'soap production wiring '+token)
req('if (!player.getAbilities().instabuild)' not in soap,'creative bypass remains in Sanity Soap')
fluid=text('src/main/java/com/darkifov/thaumcraft/block/PurifyingFluidBlock.java')
req(('TC4WarpRuntimeParity.purifyingFluidWardDuration(permanentWarp)' in fluid) or
    ('TC4BathSaltsParity.wardDurationTicks(permanentWarp)' in fluid
     and 'WARD_DURATION_CAP_TICKS = 32000' in text('src/main/java/com/darkifov/thaumcraft/warp/TC4BathSaltsParity.java')
     and 'WARD_DURATION_NUMERATOR = 200000' in text('src/main/java/com/darkifov/thaumcraft/warp/TC4BathSaltsParity.java')),
    'pure fluid duration not source-linked')
warp=text('src/main/java/com/darkifov/thaumcraft/event/WarpEvents.java')
req('TC4WarpRuntimeParity.WARP_CHECK_INTERVAL_TICKS' in warp,'warp scheduler interval not source-linked')
gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=136,f'expected at least 136 annotated GameTests, got {len(methods)}')
req(len(methods)==len(set(methods)),'GameTest names unique')
for name in ('infectiousWarpSpreadDowngradesLikeOriginal','propagatedWarpExhaustKeepsDefaultMilkCure','initialWarpEventExhaustRemainsUncurable','sanitySoapConsumesOneItemInCreativeAndSurvival','warpCleansingChanceAndWardDurationMatchOriginal'):
    req(name in methods,'missing GameTest '+name)
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids=[x['id'] for x in manifest['tests']]
req(tuple(map(int,manifest['version'].split('.'))) >= (11,64,3),'manifest version')
req(len(ids)>=464,f'expected at least 464 manifest scenarios, got {len(ids)}')
req(len(ids)==len(set(ids)),'manifest ids unique')
for id_ in ('gametest.warp_infectious_spread_amplifier','gametest.warp_infectious_spread_default_curative','gametest.warp_event_source_no_curative','gametest.sanity_soap_creative_consumption','gametest.warp_cleansing_chance_ward_duration'):
    req(id_ in ids,'missing manifest '+id_)
evidence=json.loads(text('tools/data/tc4_warp_runtime_source_evidence_v11.64.03.json'))
for key,value in evidence['claims'].items(): req(value is True,'source evidence '+key)
req(evidence['runtime_status']=='NOT_VERIFIED','runtime status honesty')
print('TC4 v11.64.03 warp runtime guard: PASS (cumulative GameTests/scenarios; spread curatives and cleansing source-linked)')
