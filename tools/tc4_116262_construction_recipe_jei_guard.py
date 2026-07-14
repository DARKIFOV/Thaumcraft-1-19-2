#!/usr/bin/env python3
"""Static release guard for the 11.62.62 TC4 construction/candle recipe batch."""
from __future__ import annotations
import argparse, json, re, sys
from pathlib import Path

EXPECTED_BLOCK_ITEMS = {
    'greatwood_stairs','silverwood_stairs','greatwood_slab','silverwood_slab',
    'tc4_block_thaumium','tc4_block_tallow','amber_block','amber_bricks',
    'item_grate','tc4_block_crystal_cluster',
    'tallow_candle','tallow_candle_orange','tallow_candle_magenta','tallow_candle_light_blue',
    'tallow_candle_yellow','tallow_candle_lime','tallow_candle_pink','tallow_candle_gray',
    'tallow_candle_light_gray','tallow_candle_cyan','tallow_candle_purple','tallow_candle_blue',
    'tallow_candle_brown','tallow_candle_green','tallow_candle_red','tallow_candle_black',
}
EXPECTED_RECIPES = {
    'greatwood_stairs_original_tc4','silverwood_stairs_original_tc4',
    'greatwood_slab_original_tc4','silverwood_slab_original_tc4',
    'tc4_thaumium_block','tc4_thaumium_from_block','tc4_tallow_block','tc4_tallow_from_block',
    'aer_crystal','ignis_crystal','aqua_crystal','terra_crystal','ordo_crystal','perditio_crystal',
    'balanced_crystal_original_tc4','amber_block_original_tc4','amber_bricks_original_tc4',
    'amber_from_block_original_tc4','amber_from_bricks_original_tc4','item_grate_original_tc4',
    'essentia_phial_original_style','table_original_tc4_style',
    'scribing_tools_from_phial_original_tc4','tallow_candle_original_tc4',
    'tallow_candle_whitewash_original_tc4',
}
COLORS = ['orange','magenta','light_blue','yellow','lime','pink','gray','light_gray','cyan','purple','blue','brown','green','red','black']
EXPECTED_RECIPES |= {f'tallow_candle_{c}_from_white_original_tc4' for c in COLORS}


def load(path: Path):
    return json.loads(path.read_text(encoding='utf-8'))

def fail(errors, text):
    errors.append(text)

def main() -> int:
    ap=argparse.ArgumentParser(); ap.add_argument('root', nargs='?', default='.')
    args=ap.parse_args(); root=Path(args.root).resolve()
    errors=[]
    build=(root/'build.gradle').read_text(encoding='utf-8')
    if "version = '11.62.62'" not in build: fail(errors,'project version is not 11.62.62')

    java=(root/'src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java').read_text(encoding='utf-8')
    research=(root/'src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java').read_text(encoding='utf-8')
    for token in ['GREATWOOD_STAIRS','SILVERWOOD_STAIRS','GREATWOOD_SLAB','SILVERWOOD_SLAB',
                  'THAUMIUM_STORAGE_BLOCK','TALLOW_BLOCK','AMBER_BLOCK','AMBER_BRICKS',
                  'ITEM_GRATE','BALANCED_CRYSTAL','TALLOW_CANDLE_BLACK']:
        if token not in java: fail(errors,f'missing Java registration {token}')
    for rid in ['tc4_block_thaumium','tc4_block_tallow','tc4_block_crystal_cluster']:
        if f'entry.id().equals("{rid}")' not in research: fail(errors,f'legacy flat item {rid} is not skipped')

    recdir=root/'src/main/resources/data/thaumcraft/recipes'
    for name in sorted(EXPECTED_RECIPES):
        p=recdir/f'{name}.json'
        if not p.exists(): fail(errors,f'missing recipe {name}')
        elif load(p).get('type') not in {'minecraft:crafting_shaped','minecraft:crafting_shapeless'}:
            fail(errors,f'{name} is not a vanilla crafting recipe (JEI visibility risk)')

    # Exact high-risk recipe assertions.
    phial=load(recdir/'essentia_phial_original_style.json')
    if phial.get('pattern') != [' C ','G G',' G '] or phial.get('result',{}).get('count') != 8:
        fail(errors,'essentia phial recipe is not the original 8-output clay/glass pattern')
    table=load(recdir/'table_original_tc4_style.json')
    if table.get('key',{}).get('S',{}).get('tag') != 'minecraft:wooden_slabs' or table.get('key',{}).get('W',{}).get('tag') != 'minecraft:planks':
        fail(errors,'table recipe does not accept vanilla wood tags')
    for aspect in ['aer','ignis','aqua','terra','ordo','perditio']:
        d=load(recdir/f'{aspect}_crystal.json')
        ingredients=d.get('ingredients',[])
        if len(ingredients)!=6 or any(x.get('item')!=f'thaumcraft:{aspect}_shard' for x in ingredients):
            fail(errors,f'{aspect} cluster is not six identical shards')
        if (recdir/f'{aspect}_shard_from_crystal_original_style.json').exists():
            fail(errors,f'non-original reverse {aspect} cluster recipe still exists')
    balanced=load(recdir/'balanced_crystal_original_tc4.json')
    if len(balanced.get('ingredients',[])) != 6:
        fail(errors,'balanced cluster does not use all six primal shards')

    # Every new placeable has blockstate, item model and self-drop loot.
    assets=root/'src/main/resources/assets/thaumcraft'
    data=root/'src/main/resources/data/thaumcraft'
    for rid in sorted(EXPECTED_BLOCK_ITEMS):
        for p in [assets/f'blockstates/{rid}.json',assets/f'models/item/{rid}.json',data/f'loot_tables/blocks/{rid}.json']:
            if not p.exists(): fail(errors,f'missing resource {p.relative_to(root)}')
    tag=load(data/'tags/items/tallow_candles.json')
    if len(tag.get('values',[])) != 16: fail(errors,'tallow_candles tag must contain 16 variants')

    # Standard recipes are consumed by JEI's crafting category; custom TC4 categories must remain registered.
    jei=(root/'src/main/java/com/darkifov/thaumcraft/compat/jei/TC4JeiPlugin.java').read_text(encoding='utf-8')
    for token in ['ArcaneWorkbenchRecipe','AlchemyRecipe','InfusionRecipe']:
        if token not in jei: fail(errors,f'JEI custom category registration lost: {token}')
    counts={}
    for p in recdir.glob('*.json'):
        try: t=load(p).get('type','unknown')
        except Exception as exc: fail(errors,f'invalid recipe JSON {p.name}: {exc}'); continue
        counts[t]=counts.get(t,0)+1
    standard=counts.get('minecraft:crafting_shaped',0)+counts.get('minecraft:crafting_shapeless',0)
    if standard < 125: fail(errors,f'only {standard} standard crafting recipes, expected at least 125')

    report={'version':'11.62.62','status':'PASS' if not errors else 'FAIL','errors':errors,
            'checked_release_recipes':len(EXPECTED_RECIPES),'new_block_items':len(EXPECTED_BLOCK_ITEMS),
            'standard_jei_crafting_recipes':standard,'recipe_type_counts':counts}
    out=root/'reports/tc4_116262_construction_recipe_jei_guard.json'; out.parent.mkdir(exist_ok=True)
    out.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    if errors:
        print('TC4 11.62.62 construction/recipe/JEI guard: FAIL')
        for e in errors: print(' -',e)
        return 1
    print('TC4 11.62.62 construction/recipe/JEI guard: PASS')
    print(f"Release recipes checked: {len(EXPECTED_RECIPES)}; new functional block items: {len(EXPECTED_BLOCK_ITEMS)}; standard JEI crafting recipes: {standard}")
    return 0

if __name__=='__main__': raise SystemExit(main())
