#!/usr/bin/env python3
"""Forward-compatible retained guard for the original v11.63.23 Levitator milestone."""
from pathlib import Path
import hashlib,json,re
R=Path(__file__).resolve().parents[1]
def text(p): return (R/p).read_text(encoding='utf-8')
def req(v,m):
    if not v: raise SystemExit('TC4 retained Arcane Levitator parity guard: FAIL: '+m)
def sha(p): return hashlib.sha256((R/p).read_bytes()).hexdigest()
m=re.search(r"(?m)^version = '(\d+)\.(\d+)\.(\d+)'",text('build.gradle')); req(m,'version')
req(tuple(map(int,m.groups())) >= (11,63,23),'version too old')
block=text('src/main/java/com/darkifov/thaumcraft/block/ArcaneLevitatorBlock.java')
be=text('src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneLevitatorBlockEntity.java')
for t in ('extends BaseEntityBlock','ArcaneLevitatorBlockEntity::serverTick','getBlockSupportShape',
          'direction.getAxis().isHorizontal()','markStackBelow(level, pos)','TC4ArcaneLevitatorEffectsBridge.spawn'):
    req(t in block,'block '+t)
for t in ('counter % TC4ArcaneLevitatorParity.REFRESH_INTERVAL_TICKS','new AABB(pos.getX(), pos.getY() + 1.0D',
          'entity instanceof ItemEntity','entity.isPushable()','entity instanceof AbstractHorse',
          'nextVerticalVelocity','entity.fallDistance = 0.0F','level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above())',
          'TC4ArcaneLevitatorParity.lowerSegmentContributes(level.hasNeighborSignal(below))'):
    req(t in be,'block entity '+t)
for tex in ('liftertop','lifterside','arcaneearbottom','animatedglow'):
    req(sha('src/main/resources/assets/thaumcraft/original_tc4_1710/textures/blocks/'+tex+'.png') ==
        sha('src/main/resources/assets/thaumcraft/textures/block/tc4/'+tex+'.png'),'texture '+tex)
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json')); ids={x['id'] for x in manifest['tests']}
for tid in ('blocks.arcane_levitator_single_range_velocity_and_cap','blocks.arcane_levitator_sneak_descent_and_entity_filter',
 'blocks.arcane_levitator_stack_range_and_powered_segment','blocks.arcane_levitator_obstruction_and_build_height',
 'blocks.arcane_levitator_redstone_block_and_above','blocks.arcane_levitator_visual_save_reload_multiplayer'):
    req(tid in ids,'manifest '+tid)
print('TC4 retained Arcane Levitator parity guard: PASS (v11.63.23 milestone preserved by v11.64.20 full closure)')
