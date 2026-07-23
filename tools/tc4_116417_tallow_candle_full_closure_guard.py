#!/usr/bin/env python3
"""v11.64.17 guard: complete Tallow Candle family source/resource closure."""
from pathlib import Path
import hashlib, json, re, zipfile

R = Path(__file__).resolve().parents[1]

def text(path): return (R / path).read_text(encoding='utf-8')
def req(ok, msg):
    if not ok: raise SystemExit('TC4 v11.64.17 Tallow Candle full-closure guard: FAIL: ' + msg)
def version(raw):
    m = re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']', raw)
    req(m is not None, 'version parse')
    return tuple(map(int, m.groups()))
def sha(path): return hashlib.sha256((R / path).read_bytes()).hexdigest()

req(version(text('build.gradle')) >= (11,64,17), 'build version >= 11.64.17')
req(version(text('src/main/resources/META-INF/mods.toml')) >= (11,64,17), 'mods version >= 11.64.17')

contract = text('src/main/java/com/darkifov/thaumcraft/block/TC4TallowCandleParity.java')
for token in (
    'CONTRACT_VERSION = "11.64.17"', 'COLOR_COUNT = 16', 'ARCANE_PECH_OFFER_COLOR_COUNT = 15',
    'BLOCK_HARDNESS = 0.1F', 'BLOCK_LIGHT_LEVEL = 14', 'BODY_MIN = 6.0D / 16.0D',
    'BODY_MAX = 10.0D / 16.0D', 'BODY_HEIGHT = 8.0D / 16.0D',
    'WICK_MIN = 0.475D', 'WICK_MAX = 0.525D', 'WICK_BOTTOM = 8.0D / 16.0D',
    'WICK_TOP = 10.0D / 16.0D', 'PARTICLE_Y_OFFSET = 0.7D',
    'DRIP_COUNT_BASE = 1', 'DRIP_COUNT_BOUND = 5', 'DRIP_LOCATION_BASE = 2',
    'DRIP_LOCATION_BOUND = 2', 'DRIP_HEIGHT_BASE = 1', 'DRIP_HEIGHT_BOUND = 3',
    'CRAFT_RESULT_COUNT = 3', 'OBJECT_LUX = 2', 'OBJECT_CORPUS = 1',
    'OBJECT_PRAECANTATIO = 1', 'RESEARCH_CORPUS = 3', 'RESEARCH_PRAECANTATIO = 1',
    'RESEARCH_X = -2', 'RESEARCH_Y = 0', 'RESEARCH_COMPLEXITY = 1',
    '0xF0F0F0', '0xEB8844', '0xC354CD', '0x6689D3', '0xDECF2A', '0x41CD34',
    '0xD88198', '0x434343', '0xA0A0A0', '0x287697', '0x7B2FBE', '0x253192',
    '0x51301A', '0x3B511A', '0xB3312C', '0x1E1B1B',
    'return x + y * z', 'new Random(worldSeed(x, y, z))',
    'DRIP_COUNT_BASE + random.nextInt(DRIP_COUNT_BOUND)', 'random.nextBoolean()',
    'DRIP_LOCATION_BASE + random.nextInt(DRIP_LOCATION_BOUND)',
    'DRIP_HEIGHT_BASE + random.nextInt(DRIP_HEIGHT_BOUND)'):
    req(token in contract, 'pure contract token missing: ' + token)

block = text('src/main/java/com/darkifov/thaumcraft/block/TallowCandleBlock.java')
for token in ('Block.box(', 'TC4TallowCandleParity.BODY_MIN', 'return Shapes.empty()',
              'canSupportCenter(level, pos.below(), Direction.UP)',
              'direction == Direction.DOWN && !state.canSurvive(level, pos)',
              'ParticleTypes.SMOKE', 'ParticleTypes.FLAME',
              'TC4TallowCandleParity.PARTICLE_Y_OFFSET', 'implements InfusionStabilizer'):
    req(token in block, 'block production path missing: ' + token)

mod = text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
for token in ('private static RegistryObject<Block> tallowCandle(String name)',
              '.strength(TC4TallowCandleParity.BLOCK_HARDNESS)', '.sound(SoundType.WOOL)',
              '.noCollission().noOcclusion()',
              '.lightLevel(state -> TC4TallowCandleParity.BLOCK_LIGHT_LEVEL)',
              'new TallowCandleBlockItem(block.get()',
              'BlockEntityType.Builder.of(TallowCandleBlockEntity::new'):
    req(token in mod, 'registration mismatch: ' + token)
for path in ('tallow_candle','tallow_candle_orange','tallow_candle_magenta','tallow_candle_light_blue',
             'tallow_candle_yellow','tallow_candle_lime','tallow_candle_pink','tallow_candle_gray',
             'tallow_candle_light_gray','tallow_candle_cyan','tallow_candle_purple','tallow_candle_blue',
             'tallow_candle_brown','tallow_candle_green','tallow_candle_red','tallow_candle_black'):
    req(f'tallowCandle("{path}")' in mod, 'registry colour missing: ' + path)

renderer = text('src/main/java/com/darkifov/thaumcraft/client/render/TallowCandleRenderer.java')
for token in ('"block/tc4/candle"', '"block/tc4/candlestub"',
              'TC4TallowCandleParity.legacyMetadata', 'TC4TallowCandleParity.red(metadata)',
              'TC4TallowCandleParity.drips(', 'if (world)', '1.0F, 1.0F, 1.0F',
              'sprite.getU(minX * 16.0D)', 'sprite.getV((1.0F - maxY) * 16.0D)',
              'Math.round(red * 255.0F)', 'Sheets.cutoutBlockSheet()'):
    req(token in renderer, 'renderer mismatch: ' + token)
req('block/tallow_candle_' not in renderer, 'renderer still uses approximate pre-coloured textures')

item_renderer = text('src/main/java/com/darkifov/thaumcraft/client/render/TallowCandleItemRenderer.java')
req('poseStack.translate(-0.5D, -0.5D, -0.5D)' in item_renderer, 'original inventory translation missing')
for forbidden in ('type.firstPerson()', 'TransformType.GUI', 'TransformType.GROUND', 'TransformType.FIXED', 'rotationDegrees('):
    req(forbidden not in item_renderer, 'approximate context transform remains: ' + forbidden)

colors = text('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java')
for token in ('RegisterColorHandlersEvent.Block', 'RegisterColorHandlersEvent.Item',
              'ForgeRegistries.BLOCKS.getKey(state.getBlock())', 'ForgeRegistries.ITEMS.getKey(stack.getItem())',
              'TC4TallowCandleParity.color(TC4TallowCandleParity.legacyMetadata('):
    req(token in colors, 'runtime tint handler missing: ' + token)
req(colors.count('ThaumcraftMod.TALLOW_CANDLE_BLACK.get()') >= 2, 'all 16 block/item colour registrations not covered')

aspects = text('src/main/java/com/darkifov/thaumcraft/source/TC4ObjectAspectRegistry.java')
for token in ('metadata < TC4TallowCandleParity.COLOR_COUNT',
              '"thaumcraft:" + TC4TallowCandleParity.registryPath(metadata)',
              'Aspect.LUX, TC4TallowCandleParity.OBJECT_LUX',
              'Aspect.CORPUS, TC4TallowCandleParity.OBJECT_CORPUS',
              'Aspect.PRAECANTATIO, TC4TallowCandleParity.OBJECT_PRAECANTATIO'):
    req(token in aspects, 'object aspect bridge missing: ' + token)

runtime_research = text('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchRuntimeBridge.java')
for token in ('"TALLOW", "Magic Tallow", "Rendered fat with a touch of magic"',
              '"ALCHEMY", -2, 0, 1', 'aspects("CORPUS", 3, "PRAECANTATIO", 1)',
              'new String[] {"CRUCIBLE"}', 'new String[] {"tc.research_page.TALLOW.1"}',
              'new String[] {"TEXT", "CRUCIBLE_CRAFTING", "NORMAL_CRAFTING"}',
              'new String[] {"Tallow", "TallowCandle"}'):
    req(token in runtime_research, 'research graph mismatch: ' + token)

# Exact original resources retained and active.
req(sha('src/main/resources/assets/thaumcraft/textures/block/tc4/candle.png') ==
    '62f717e298f57345a2d277d04ba72128cb3685494907671e4e5e338d9be86d5d', 'candle.png hash mismatch')
req(sha('src/main/resources/assets/thaumcraft/textures/block/tc4/candlestub.png') ==
    'c767a0627638f4dd371b059c1d20ebb345fa5bc55095a34405da362c6ea15c11', 'candlestub.png hash mismatch')
req(json.loads(text('src/main/resources/assets/thaumcraft/textures/block/tc4/candle.png.mcmeta')) == {'animation': {}},
    'candle animation metadata mismatch')

paths = [TC for TC in re.findall(r'"(tallow_candle(?:_[a-z]+(?:_[a-z]+)?)?)"', contract)]
paths = list(dict.fromkeys(paths))
req(len(paths) == 16, f'expected 16 registry paths in contract, got {len(paths)}')
for path in paths:
    model_path = f'src/main/resources/assets/thaumcraft/models/block/{path}.json'
    item_path = f'src/main/resources/assets/thaumcraft/models/item/{path}.json'
    state_path = f'src/main/resources/assets/thaumcraft/blockstates/{path}.json'
    loot_path = f'src/main/resources/data/thaumcraft/loot_tables/blocks/{path}.json'
    for resource in (model_path,item_path,state_path,loot_path): req((R/resource).is_file(), 'resource missing: ' + resource)
    model=json.loads(text(model_path))
    req(model['textures']['wax']=='thaumcraft:block/tc4/candle' and model['textures']['particle']=='thaumcraft:block/tc4/candle',
        'model does not use common original candle texture: ' + path)
    wax_faces=[face for e in model.get('elements',[]) for face in e.get('faces',{}).values() if face.get('texture')=='#wax']
    req(wax_faces and all(face.get('tintindex')==0 for face in wax_faces), 'wax tintindex missing: ' + path)
    req(json.loads(text(item_path)).get('parent')=='builtin/entity', 'item model must use BEWLR: ' + path)
    loot=json.loads(text(loot_path))
    req(path in json.dumps(loot), 'loot does not return itself: ' + path)

recipe=json.loads(text('src/main/resources/data/thaumcraft/recipes/tallow_candle_original_tc4.json'))
req(recipe['pattern']==[' S ',' T ',' T '] and recipe['result']=={'item':'thaumcraft:tallow_candle','count':3}, 'base recipe mismatch')
req(recipe['key']=={'S':{'item':'minecraft:string'},'T':{'item':'thaumcraft:tc4_tallow'}}, 'base recipe ingredients mismatch')
tag=json.loads(text('src/main/resources/data/thaumcraft/tags/items/tallow_candles.json'))
req(len(tag['values'])==16 and set(tag['values'])=={'thaumcraft:'+p for p in paths}, 'candle family tag mismatch')
whitewash=json.loads(text('src/main/resources/data/thaumcraft/recipes/tallow_candle_whitewash_original_tc4.json'))
req(whitewash['ingredients']==[{'tag':'forge:dyes/white'},{'tag':'thaumcraft:tallow_candles'}], 'whitewash mismatch')
for path in paths[1:]:
    f=R/f'src/main/resources/data/thaumcraft/recipes/{path}_from_white_original_tc4.json'
    req(f.is_file(), 'recolour recipe missing: ' + path)
    data=json.loads(f.read_text())
    req(data['ingredients'][1]=={'item':'thaumcraft:tallow_candle'} and data['result']=={'item':'thaumcraft:'+path,'count':1},
        'recolour recipe mismatch: ' + path)

with zipfile.ZipFile(R/'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip') as z:
    def member(suffix):
        name=next((n for n in z.namelist() if n.endswith('/'+suffix) or n==suffix),None)
        req(name is not None,'original source missing: '+suffix)
        return z.read(name).decode('utf-8',errors='replace')
    ob=member('thaumcraft/common/blocks/BlockCandle.java')
    oi=member('thaumcraft/common/blocks/BlockCandleItem.java')
    orender=member('thaumcraft/client/renderers/block/BlockCandleRenderer.java')
    orec=member('thaumcraft/common/config/ConfigRecipes.java')
    ores=member('thaumcraft/common/config/ConfigResearch.java')
    oasp=member('thaumcraft/common/config/ConfigAspects.java')
    opech=member('thaumcraft/common/entities/monster/EntityPech.java')
for token in ('func_149711_c(0.1F)', 'func_149672_a(field_149775_l)', 'func_149715_a(0.95F)',
              'Utils.colors[par1]', 'func_149676_a(0.375F, 0.0F, 0.375F, 0.625F, 0.5F, 0.625F)',
              'return null', 'par3 + 0.7F', 'canStabaliseInfusion'):
    req(token in ob, 'original BlockCandle oracle mismatch: ' + token)
req('func_77627_a(true)' in oi and 'return par1' in oi, 'original metadata item oracle mismatch')
for token in ('GL11.glColor3f(r, g, b)', 'new Random(x + y * z)', '1 + rr.nextInt(5)',
              '2 + rr.nextInt(2)', '1 + rr.nextInt(3)', 'iconStub', 'GL11.glColor3f(1.0F, 1.0F, 1.0F)'):
    req(token in orender, 'original renderer oracle mismatch: ' + token)
req('new ItemStack(ConfigBlocks.blockCandle, 3, 0)' in orec and 'for (int a = 1; a < 16; a++)' in orec
    and 'new ItemStack(ConfigBlocks.blockCandle, 1, 32767)' in orec, 'original recipe oracle mismatch')
req('new ResearchItem("TALLOW", "ALCHEMY"' in ores and 'Aspect.FLESH, 3' in ores and 'Aspect.MAGIC, 1' in ores
    and 'setParents(new String[] { "CRUCIBLE" })' in ores, 'original research oracle mismatch')
req('new ItemStack(ConfigBlocks.blockCandle)' in oasp and 'Aspect.LIGHT, 2' in oasp
    and 'Aspect.FLESH, 1' in oasp and 'Aspect.MAGIC, 1' in oasp, 'original aspects oracle mismatch')
req('for (int a = 0; a < 15; a++)' in opech and 'new ItemStack(ConfigBlocks.blockCandle, 1, a)' in opech,
    'original Arcane Pech offer oracle mismatch')

# Tests and runtime evidence.
gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=162 and len(methods)==len(set(methods)), f'expected >=162 unique GameTests, got {len(methods)}')
for method in ('tallowCandleFamilyMatchesOriginalMetadataAndColors','tallowCandleUsesExactShapeLightAndNoCollision',
               'tallowCandleDropsWhenSupportIsLost','tallowCandleDripsAndStabilizersUseProductionContract',
               'tallowResearchEntryMatchesOriginalContract'):
    req(method in methods, 'GameTest missing: '+method)
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids=[x['id'] for x in manifest['tests']]
req(tuple(map(int,manifest['version'].split('.'))) >= (11,64,17), 'manifest version')
req(len(ids)>=534 and len(ids)==len(set(ids)), f'expected >=534 unique scenarios, got {len(ids)}')
for sid in ('gametest.tallow_candle_metadata_colors','gametest.tallow_candle_shape_light_collision',
            'gametest.tallow_candle_support_loss','gametest.tallow_candle_drips_stabilizers',
            'gametest.tallow_research_contract','client.tallow_candle_runtime_tint_animation',
            'client.tallow_candle_geometry_uv_drips','client.tallow_candle_item_original_inventory_path',
            'research.tallow_candle_recipe_aspects','external.tallow_candle_arcane_pech_offer_contract'):
    req(sid in ids, 'manifest scenario missing: '+sid)

evidence=json.loads(text('tools/data/tc4_tallow_candle_full_source_evidence_v11.64.17.json'))
req(evidence['round']=='11.64.17' and evidence['resource_closure']=='CLOSED', 'evidence status mismatch')
req(evidence['build_status']=='NOT_OBTAINED' and evidence['runtime_status']=='NOT_VERIFIED', 'honesty status drifted')

prompt=text('UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md')
req(prompt==text('PROMPT_FOR_FUTURE_CHAT_RU.md'), 'universal and future-chat prompts differ')
for token in ('Один релиз — один предмет или одна цельная механика','SOURCE CLOSED','RESOURCE CLOSED',
              'BUILD VERIFIED','RUNTIME VERIFIED','обязателен в корне каждого следующего полного исходного архива',
              'Упаковка архива без этого файла запрещена'):
    req(token in prompt, 'universal prompt rule missing: '+token)

print(f'TC4 v11.64.17 Tallow Candle full-closure guard: PASS ({len(methods)} GameTests; {len(ids)} scenarios; 16 colours/geometry/UV/resources/recipes/research/aspects/prompt)')
