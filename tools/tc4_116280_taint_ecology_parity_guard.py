#!/usr/bin/env python3
"""Static source/resource contract guard for v11.62.89 TC4 taint ecology parity."""
from __future__ import annotations
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
def text(path: str) -> str: return (ROOT / path).read_text(encoding="utf-8")
checks=[]
def check(name, ok): checks.append((name,bool(ok)))

build=text('build.gradle'); mods=text('src/main/resources/META-INF/mods.toml')
spread=text('src/main/java/com/darkifov/thaumcraft/taint/TaintSpreadRuntime.java')
regions=text('src/main/java/com/darkifov/thaumcraft/taint/TaintRegionSavedData.java')
fibres=text('src/main/java/com/darkifov/thaumcraft/block/TaintFibresBlock.java')
taint=text('src/main/java/com/darkifov/thaumcraft/block/TaintBlock.java')
seed=text('src/main/java/com/darkifov/thaumcraft/block/TaintSeedItem.java')
spider=text('src/main/java/com/darkifov/thaumcraft/entity/TaintCrawlerEntity.java')
spore=text('src/main/java/com/darkifov/thaumcraft/entity/TaintSporeEntity.java')
renderer=text('src/main/java/com/darkifov/thaumcraft/client/render/TaintCrawlerRenderer.java')
spore_renderer=text('src/main/java/com/darkifov/thaumcraft/client/render/TaintSporeRenderer.java')
spore_model=text('src/main/java/com/darkifov/thaumcraft/client/render/model/TC4TaintSporeModel.java')
mod=text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
client=text('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java')
state=json.loads(text('src/main/resources/assets/thaumcraft/blockstates/taint_fibres.json'))
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))

check('build version', "version = '11.62.89'" in build)
check('mods version', 'version="11.62.89"' in mods)
check('taint config rate', 'TAINT_SPREAD_RATE' in text('src/main/java/com/darkifov/thaumcraft/config/ThaumcraftConfig.java'))
check('taint config spores', 'SPAWN_TAINT_SPORES' in text('src/main/java/com/darkifov/thaumcraft/config/ThaumcraftConfig.java'))
check('persistent taint columns', 'putLongArray' in regions and 'setDirty()' in regions)
check('seed marks taint columns', 'markTaintedColumn' in seed and 'TaintCrawlerEntity' not in seed)
check('crust threshold two', 'adjacent >= 2 && isCrustTarget' in spread)
check('soil threshold three', 'adjacent >= 3 && !targetState.isAir() && isSoilTarget' in spread)
check('tc4 target offsets', 'random.nextInt(3) - 1' in spread and 'random.nextInt(5) - 3' in spread)
check('growth only one in ten', 'random.nextInt(10) == 0' in spread)
check('growth 90 percent grass', 'random.nextInt(10) < 9' in spread)
check('mature spore stage', 'setValue(TaintFibresBlock.AGE, 4)' in spread and 'TAINT_SPORE.get().create' in spread)
check('orphan mature stalk regresses', 'setValue(TaintFibresBlock.AGE, 3)' in spread)
check('crust outside decay 1/20', 'Variant.CRUST && random.nextInt(20) == 0' in spread)
check('soil outside decay 1/10', 'Variant.SOIL && random.nextInt(10) == 0' in spread)
check('crust falling entity', 'FallingBlockEntity.fall' in spread and 'setHurtsEntities(2.0F, 40)' in spread)
check('log support prevents falling', 'BlockTags.LOGS' in spread)
check('five metadata ages', 'IntegerProperty.create("age", 0, 4)' in fibres)
for face in ('DOWN','UP','NORTH','SOUTH','WEST','EAST'):
    check(f'face attachment {face.lower()}', f'BooleanProperty {face}' in fibres)
check('age zero attachment survival', 'hasAttachment(withAttachments' in fibres)
check('growth requires sturdy floor', 'isFaceSturdy(level, pos.below(), Direction.UP)' in fibres)
check('fibre player poison 1/1000', 'living instanceof Player ? 1000 : 500' in fibres)
check('taint player poison 1/100', 'living instanceof Player ? 100 : 20' in taint)
check('flesh skips poison', 'variant == Variant.FLESH' in taint)
check('spider is real Spider', 'extends Spider' in spider)
check('spider original health', 'Attributes.MAX_HEALTH, 5.0D' in spider)
check('spider original damage', 'Attributes.ATTACK_DAMAGE, 2.0D' in spider)
check('spider one in six drop', 'random.nextInt(6) == 0' in spider)
check('spider original textures', 'taint_spider.png' in renderer and 'taint_spider_eyes.png' in renderer)
check('spider vanilla geometry', 'SpiderModel' in renderer and 'ModelLayers.SPIDER' in renderer)
check('spore synced size', 'EntityDataAccessor<Integer> SIZE' in spore and 'Math.min(10' in spore)
check('spore grows at 1200 ticks', '++growth >= 1200' in spore)
check('spore anchors to mature fibre', 'TaintFibresBlock.AGE) != 4' in spore)
check('spore bursts spiders', 'TAINT_CRAWLER.get().create' in spore and 'spiderBurst' in spore)
check('spore shrinks stalk', 'setValue(TaintFibresBlock.AGE, 3)' in spore)
check('original two cube spore model', '12.0F, 12.0F, 12.0F' in spore_model and '16.0F, 16.0F, 16.0F' in spore_model)
check('spore pulse renderer', 'displaySize' in spore_renderer and 'Mth.sin' in spore_renderer)
check('spore registered', 'ENTITY_TYPES.register("taint_spore"' in mod and 'TaintSporeEntity.createAttributes' in mod)
check('spore client registered', 'TAINT_SPORE.get()' in client and 'TC4TaintSporeModel.LAYER' in client)
check('fibres cutout layer', 'TAINT_FIBRES.get(), RenderType.cutout()' in client)
parts=state.get('multipart',[])
check('multipart has ten visual parts', len(parts)==10)
for model_name in ('taintgrass1','taintgrass2','taint_spore_stalk_1','taint_spore_stalk_2'):
    check(f'model {model_name}', (ROOT/f'src/main/resources/assets/thaumcraft/models/block/{model_name}.json').is_file())
for face in ('down','up','north','south','west','east'):
    check(f'fibre face model {face}', (ROOT/f'src/main/resources/assets/thaumcraft/models/block/taint_fibres_{face}.json').is_file())
check('crust empty loot', json.loads(text('src/main/resources/data/thaumcraft/loot_tables/blocks/taint_crust.json')).get('pools') == [])
check('soil drops dirt', 'minecraft:dirt' in text('src/main/resources/data/thaumcraft/loot_tables/blocks/taint_soil.json'))
check('flesh nine rotten flesh and silk', 'minecraft:rotten_flesh' in text('src/main/resources/data/thaumcraft/loot_tables/blocks/flesh_block.json') and 'minecraft:silk_touch' in text('src/main/resources/data/thaumcraft/loot_tables/blocks/flesh_block.json'))
required={
 'taint.column_spread_thresholds_decay_persistence',
 'taint.fibre_five_states_spore_growth_burst',
 'taint.spider_geometry_eyes_poison_drops',
}
ids={x.get('id') for x in manifest.get('tests',[])}
check('manifest version', manifest.get('version')=='11.62.89')
check('manifest retains at least the 56 taint-era cases', len(manifest.get('tests',[])) >= 56)
for i in required: check(f'runtime case {i}', i in ids)

failed=[n for n,o in checks if not o]
payload={'version':'11.62.89','scope':'TC4 taint blocks/fibres, persistent spread, taint spider and stationary spore source contracts','checks_total':len(checks),'checks_passed':len(checks)-len(failed),'failed':failed,'runtime_verified':False,'known_deviations':['modern biome palette/colour/weather mutation not implemented','flying EntityTaintSporeSwarmer not implemented']}
out=ROOT/'reports/tc4_116280_taint_ecology_parity_audit.json'; out.parent.mkdir(exist_ok=True); out.write_text(json.dumps(payload,ensure_ascii=False,indent=2)+'\n')
print(f"Taint ecology parity guard: {payload['checks_passed']}/{payload['checks_total']}")
if failed:
    for n in failed: print('FAIL:',n)
    raise SystemExit(1)
print('STATIC SOURCE CONTRACT PASS; runtime remains NOT TESTED')
