#!/usr/bin/env python3
"""v11.64.15 guard: complete Brain in a Jar source/resource closure."""
from pathlib import Path
import hashlib
import json
import re
import zipfile

R = Path(__file__).resolve().parents[1]

def text(path):
    return (R / path).read_text(encoding='utf-8')

def req(condition, message):
    if not condition:
        raise SystemExit('TC4 v11.64.15 Brain Jar full-closure guard: FAIL: ' + message)

def version_tuple(raw):
    m = re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']', raw)
    req(m is not None, 'version parse')
    return tuple(map(int, m.groups()))

def sha(path):
    return hashlib.sha256((R / path).read_bytes()).hexdigest()

req(version_tuple(text('build.gradle')) >= (11, 64, 15), 'build version >= 11.64.15')
req(version_tuple(text('src/main/resources/META-INF/mods.toml')) >= (11, 64, 15), 'mods version >= 11.64.15')

contract = text('src/main/java/com/darkifov/thaumcraft/blockentity/TC4BrainJarParity.java')
for token in (
    'CONTRACT_VERSION = "11.64.15"', 'MAX_XP = 2000', 'INTERACTION_EAT_DELAY_TICKS = 40',
    'RANDOM_RELEASE_MAX_EXCLUSIVE = 64', 'SEARCH_RADIUS = 6.0D', 'PULL_DIVISOR = 7.0D',
    'PULL_HORIZONTAL = 0.15D', 'PULL_VERTICAL = 0.33D', 'TOUCH_MIN = -0.1D', 'TOUCH_MAX = 1.1D',
    'AMBIENT_INITIAL_DELAY_MILLIS = 1500', 'AMBIENT_BASE_DELAY_MILLIS = 5000',
    'AMBIENT_RANDOM_BOUND_MILLIS = 25000', 'BLOCK_HARDNESS = 0.3F', 'BLOCK_LIGHT_LEVEL = 9',
    'BLOCK_EXPLOSION_RESISTANCE = 0.0F', 'ENCHANT_POWER_BONUS = 2.0F', 'ITEM_MAX_STACK = 64', 'ITEM_WARP = 1', 'RESEARCH_WARP = 3',
    'RECIPE_INSTABILITY = 4', 'RECIPE_COGNITIO = 10', 'RECIPE_SENSUS = 10', 'RECIPE_EXANIMIS = 20',
    'FULL_PARTICLE_ALPHA = 0.5F', 'BRAIN_BOB_DIVISOR = 14.0F', 'BRAIN_RENDER_SCALE = 0.4F',
    'clampAtTickStart(', 'randomReleaseBound(', 'comparatorOutput(', 'Pull pull(', 'bob('):
    req(token in contract, 'missing pure contract token: ' + token)

be = text('src/main/java/com/darkifov/thaumcraft/blockentity/BrainJarBlockEntity.java')
for token in (
    'TC4BrainJarParity.clampAtTickStart(brain.xp)',
    'TC4BrainJarParity.mayAttract(brain.xp, brain.eatDelay)',
    'TC4BrainJarParity.pull(', 'TC4BrainJarParity.mayAbsorb(brain.xp, brain.eatDelay)',
    'brain.xp += orb.getValue()', 'The next tick-start clamp fixes it',
    'TC4BrainJarParity.INTERACTION_EAT_DELAY_TICKS',
    'level.random.nextInt(TC4BrainJarParity.randomReleaseBound(xp))',
    'ExperienceOrb.getExperienceValue(remaining)',
    'new Vec3(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ())',
    'TC4BrainJarParity.comparatorOutput(xp)', 'tag.putInt("XP", xp)',
    'eatDelay = 0', 'TC4BrainJarParity.AMBIENT_INITIAL_DELAY_MILLIS',
    'TC4BrainJarParity.AMBIENT_BASE_DELAY_MILLIS',
    'TC4BrainJarParity.AMBIENT_RANDOM_BOUND_MILLIS',
    'TC4BrainJarParity.IDLE_ROTATION_STEP', 'TC4BrainJarParity.ROTATION_LERP'):
    req(token in be, 'block-entity production path missing: ' + token)
req('tag.putInt("EatDelay"' not in be, 'transient EatDelay must not be saved')
req('Mth.clamp(tag.getInt("XP")' not in be, 'load must not pre-clamp XP before original tick-start')
# Overflow must not be clamped again after the absorption loop.
absorb_tail = be[be.index('for (ExperienceOrb orb : touching)'):be.index('public static void clientTick')]
req('Math.min(brain.xp' not in absorb_tail and 'clampAtTickStart' not in absorb_tail,
    'absorption tick must preserve temporary overflow')

block = text('src/main/java/com/darkifov/thaumcraft/block/BrainJarBlock.java')
for token in (
    'Block.box(3.0D, 0.0D, 3.0D, 13.0D, 12.0D, 13.0D)', 'return Shapes.block()',
    'TC4Sounds.event("jar")', 'new SoundType(1.0F, 1.0F, jar, jar, jar, jar, jar)',
    'BRAIN_JAR_FULL_PARTICLE.get()', 'TC4BrainJarParity.fullParticleGreen',
    'TC4BrainJarParity.fullParticleBlue', 'brain.releaseRandomExperience',
    'brain.releaseAllExperience', 'brain.comparatorOutput()', 'TC4BrainJarParity.ENCHANT_POWER_BONUS'):
    req(token in block, 'block production path missing: ' + token)

item = text('src/main/java/com/darkifov/thaumcraft/block/BrainJarBlockItem.java')
req('properties.stacksTo(1)' not in item, 'Brain Jar must retain original stack size 64')
req('TC4WarpingGearAdapter.appendTooltip(stack, null, tooltip)' in item, 'item warp tooltip not wired')
req('BrainJarItemRenderer.instance()' in item, 'custom item renderer missing')
warp = text('src/main/java/com/darkifov/thaumcraft/runic/TC4WarpingGearAdapter.java')
req('Map.entry("thaumcraft:tc4_jar_brain", 1)' in warp, 'original item warp 1 missing')

mod = text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
for token in (
    'PARTICLE_TYPES.register("brain_jar_full"',
    '.strength(TC4BrainJarParity.BLOCK_HARDNESS, TC4BrainJarParity.BLOCK_EXPLOSION_RESISTANCE)',
    '.lightLevel(state -> TC4BrainJarParity.BLOCK_LIGHT_LEVEL)',
    'new BrainJarBlockItem(BRAIN_JAR.get(), new Item.Properties().tab(THAUMCRAFT_TAB))'):
    req(token in mod, 'registration/property path missing: ' + token)
brain_reg = re.search(r'BRAIN_JAR =.*?BRAIN_JAR_ITEM =.*?;\n', mod, re.S)
req(brain_reg is not None, 'Brain Jar registration parse')
for forbidden in ('.rarity(', '.requiresCorrectToolForDrops()', '.strength(2.0F'):
    req(forbidden not in brain_reg.group(0), 'wrong Brain Jar registration property retained: ' + forbidden)

particle = text('src/main/java/com/darkifov/thaumcraft/client/fx/TC4BrainJarFullParticle.java')
for token in ('extends TextureSheetParticle', 'this.alpha = TC4BrainJarParity.FULL_PARTICLE_ALPHA',
              'this.hasPhysics = false', 'PARTICLE_SHEET_TRANSLUCENT', 'setSpriteFromAge'):
    req(token in particle, 'full-jar particle path missing: ' + token)
client = text('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java')
for token in ('BRAIN_JAR_FULL_PARTICLE.get()', 'TC4BrainJarFullParticle.Provider::new',
              'TC4BrainJarModel.LAYER', 'TC4BrainJarBrineModel.LAYER'):
    req(token in client, 'client registration missing: ' + token)

brain_model = text('src/main/java/com/darkifov/thaumcraft/client/render/model/TC4BrainJarModel.java')
for token in ('LayerDefinition.create(mesh, 128, 64)', 'texOffs(0, 0)',
              '12.0F, 10.0F, 16.0F', 'PartPose.offset(-6.0F, 8.0F, -8.0F)',
              'texOffs(64, 0)', '8.0F, 3.0F, 7.0F', 'PartPose.offset(-4.0F, 18.0F, 0.0F)',
              'texOffs(0, 32)', '2.0F, 6.0F, 2.0F', '0.4089647F'):
    req(token in brain_model, 'exact ModelBrain geometry missing: ' + token)
brine_model = text('src/main/java/com/darkifov/thaumcraft/client/render/model/TC4BrainJarBrineModel.java')
for token in ('LayerDefinition.create(mesh, 64, 32)', 'addBox(-4.0F, -11.0F, -4.0F, 8.0F, 10.0F, 8.0F)'):
    req(token in brine_model, 'exact ModelJar.Brine geometry missing: ' + token)
for renderer_path in ('src/main/java/com/darkifov/thaumcraft/client/render/BrainJarRenderer.java',
                      'src/main/java/com/darkifov/thaumcraft/client/render/BrainJarItemRenderer.java'):
    renderer = text(renderer_path)
    for token in ('textures/original/thaumcraft4/models/brain2.png',
                  'textures/original/thaumcraft4/models/jarbrine.png',
                  'TC4BrainJarParity.BRAIN_Y_OFFSET', 'TC4BrainJarParity.BRAIN_RENDER_SCALE'):
        req(token in renderer, renderer_path + ' missing: ' + token)

model = json.loads(text('src/main/resources/assets/thaumcraft/models/block/tc4_jar_brain.json'))
req(len(model['elements']) == 2, 'block model must use exactly two original cuboids')
req(model['elements'][0]['from'] == [3,0,3] and model['elements'][0]['to'] == [13,12,13],
    'jar body cuboid mismatch')
req(model['elements'][1]['from'] == [5,12,5] and model['elements'][1]['to'] == [11,14,11],
    'jar lid cuboid mismatch')
particle_json = json.loads(text('src/main/resources/assets/thaumcraft/particles/brain_jar_full.json'))
req(len(particle_json.get('textures', [])) == 8, 'brain spell particle must register eight generic frames')

# Exact original textures used by runtime renderers and block model.
for runtime, original in (
    ('src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/models/brain2.png',
     'src/main/resources/assets/thaumcraft/textures/models/brain2.png'),
    ('src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/models/jarbrine.png',
     'src/main/resources/assets/thaumcraft/textures/models/jarbrine.png'),
    ('src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/blocks/jar_side.png',
     'src/main/resources/assets/thaumcraft/textures/block/jar_side.png'),
    ('src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/blocks/jar_top.png',
     'src/main/resources/assets/thaumcraft/textures/block/jar_top.png'),
    ('src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/blocks/jar_bottom.png',
     'src/main/resources/assets/thaumcraft/textures/block/jar_bottom.png')):
    req(sha(runtime) == sha(original), 'runtime texture differs from retained original: ' + runtime)

recipe = json.loads(text('src/main/resources/data/thaumcraft/thaumcraft_infusion/tc4_jarbrain.json'))
req(recipe['research'] == 'JARBRAIN' and recipe['instability'] == 4, 'infusion research/instability mismatch')
req(recipe['catalyst'] == 'thaumcraft:essentia_jar', 'warded jar catalyst mismatch')
req(recipe['components'] == ['thaumcraft:tc4_brain','minecraft:spider_eye','minecraft:water_bucket','minecraft:spider_eye'],
    'infusion components/order mismatch')
req(recipe['aspects'] == {'COGNITIO':10,'SENSUS':10,'EXANIMIS':20}, 'infusion aspects mismatch')
req(recipe['result'] == {'item':'thaumcraft:tc4_jar_brain','count':1}, 'infusion result mismatch')
research = text('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchRuntimeBridge.java')
for token in ('"JARBRAIN", "Brain in a Jar"', '"ARTIFICE", -5, 9, 2',
              'aspects("FAMES", 3, "COGNITIO", 3, "EXANIMIS", 3, "LUCRUM", 3)',
              'new String[] {"INFUSION"}', 'new String[] {"hidden"}',
              'new String[] {"Thaumcraft.BrainyZombie", "Thaumcraft.GiantBrainyZombie"}',
              'new String[] {"JARBRAIN"}'):
    req(token in research, 'research graph mismatch: ' + token)
metadata = text('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchMetadataIndex.java')
for token in ('map.put("JARBRAIN", 3)', 'put(map, "JARBRAIN", "hidden")',
              'put(map, "JARBRAIN", "ConfigBlocks.blockJar, 1, 1", "ConfigItems.itemResource, 1, 3")'):
    req(token in metadata, 'research metadata missing: ' + token)

with zipfile.ZipFile(R / 'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip') as z:
    def member(suffix):
        name = next((n for n in z.namelist() if n.endswith('/' + suffix)), None)
        req(name is not None, 'original source member missing: ' + suffix)
        return z.read(name).decode('utf-8', errors='replace')
    source_tile = member('thaumcraft/common/tiles/TileJarBrain.java')
    source_block = member('thaumcraft/common/blocks/BlockJar.java')
    source_renderer = member('thaumcraft/client/renderers/tile/TileJarRenderer.java')
    source_brain = member('thaumcraft/client/renderers/models/ModelBrain.java')
    source_jar = member('thaumcraft/client/renderers/models/ModelJar.java')
    source_recipe = member('thaumcraft/common/config/ConfigRecipes.java')
    source_research = member('thaumcraft/common/config/ConfigResearch.java')
for token in ('xpMax = 2000', 'System.currentTimeMillis() + 1500L', '/ 7.0D', '* 0.15D', '* 0.33D',
              'eatDelay -= 1', 'xp += eo.func_70526_d()', 'nbttagcompound.func_74768_a("XP", this.xp)'):
    req(token in source_tile, 'original TileJarBrain oracle mismatch: ' + token)
for token in ('func_149711_c(0.3F)', 'new CustomStepSound("jar", 1.0F, 1.0F)', 'func_149715_a(0.66F)',
              'eatDelay = 40', 'nextInt(Math.min(((TileJarBrain)te).xp + 1, 64))',
              'func_70527_a(xp)', 'return 2.0F'):
    req(token in source_block, 'original BlockJar oracle mismatch: ' + token)
for token in ('/ 14.0F) * 0.03F + 0.03F', '-0.8F + bob', 'GL11.glRotatef(-90.0F',
              'textures/models/brain2.png', 'GL11.glScalef(0.4F', 'textures/models/jarbrine.png'):
    req(token in source_renderer, 'original TileJarRenderer oracle mismatch: ' + token)
for token in ('12, 10, 16', '-6.0F, 8.0F, -8.0F', '8, 3, 7', '-4.0F, 18.0F, 0.0F',
              '2, 6, 2', '0.4089647F'):
    req(token in source_brain, 'original ModelBrain oracle mismatch: ' + token)
req('this.Brine.func_78789_a(-4.0F, -11.0F, -4.0F, 8, 10, 8)' in source_jar,
    'original ModelJar.Brine oracle mismatch')
req('addInfusionCraftingRecipe("JARBRAIN"' in source_recipe and '.add(Aspect.MIND, 10)' in source_recipe
    and '.add(Aspect.SENSES, 10)' in source_recipe and '.add(Aspect.UNDEAD, 20)' in source_recipe,
    'original recipe oracle mismatch')
for token in ('new ResearchItem("JARBRAIN", "ARTIFICE"', '.setHidden()', '.setItemTriggers(',
              'Thaumcraft.BrainyZombie', 'ThaumcraftApi.addWarpToResearch("JARBRAIN", 3)',
              'ThaumcraftApi.addWarpToItem(new ItemStack(ConfigBlocks.blockJar, 1, 1), 1)'):
    req(token in source_research, 'original research/warp oracle mismatch: ' + token)

langs = [json.loads(text('src/main/resources/assets/thaumcraft/lang/en_us.json')),
         json.loads(text('src/main/resources/assets/thaumcraft/lang/ru_ru.json'))]
for lang in langs:
    for key in ('item.thaumcraft.tc4_jar_brain','block.thaumcraft.tc4_jar_brain'):
        req(key in lang and lang[key], 'language key missing: ' + key)

gametest = text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods = re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(', gametest, re.S)
req(len(methods) >= 152 and len(methods) == len(set(methods)),
    f'expected at least 152 unique GameTests, got {len(methods)}')
for method in ('brainJarAbsorbsPersistsAndReportsComparatorExperience','brainJarFullContractMatchesOriginal',
               'brainJarAbsorptionPreservesOverflowUntilNextTick','brainJarShakeDelayAndBreakReleaseMatchOriginal'):
    req(method in methods, 'Brain Jar GameTest missing: ' + method)

manifest = json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids = [case['id'] for case in manifest['tests']]
req(tuple(map(int, manifest['version'].split('.'))) >= (11,64,15), 'manifest version >= 11.64.15')
req(len(ids) >= 503 and len(ids) == len(set(ids)), f'expected at least 503 unique scenarios, got {len(ids)}')
for scenario in (
    'gametest.brain_jar_full_contract','gametest.brain_jar_overflow_next_tick_clamp',
    'gametest.brain_jar_shake_delay_and_break_release','gameplay.brain_jar_orb_attraction_geometry',
    'gameplay.brain_jar_random_release_distribution','persistence.brain_jar_xp_only_nbt',
    'redstone.brain_jar_comparator_1_15','enchanting.brain_jar_bonus_two',
    'client.brain_jar_exact_brain_brine_model','client.brain_jar_full_spell_particle',
    'client.brain_jar_target_rotation_and_ambient_sound','research.brain_jar_hidden_recipe_warp',
    'multiplayer.brain_jar_server_owned_xp_sync','resource.brain_jar_exact_original_assets'):
    req(scenario in ids, 'manifest scenario missing: ' + scenario)

evidence = json.loads(text('tools/data/tc4_brain_jar_full_source_evidence_v11.64.15.json'))
req(evidence['round'] == '11.64.15', 'evidence round mismatch')
req(evidence['source_closure'] == 'CLOSED' and evidence['resource_closure'] == 'CLOSED', 'closure status drifted')
req(evidence['build_status'] == 'NOT_OBTAINED' and evidence['runtime_status'] == 'NOT_VERIFIED',
    'build/runtime honesty drifted')

prompt = text('UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md')
prompt2 = text('PROMPT_FOR_FUTURE_CHAT_RU.md')
req(prompt == prompt2, 'future-chat prompt must match universal prompt exactly')
for token in ('Один релиз — один предмет или одна цельная механика', 'SOURCE CLOSED', 'RESOURCE CLOSED',
              'BUILD VERIFIED', 'RUNTIME VERIFIED', 'Нельзя писать «портировано на 100%»',
              'обязателен в корне каждого следующего полного исходного архива',
              'Упаковка архива без этого файла запрещена'):
    req(token in prompt, 'universal prompt rule missing: ' + token)

print(f'TC4 v11.64.15 Brain Jar full-closure guard: PASS '
      f'({len(methods)} GameTests; {len(ids)} scenarios; XP/NBT/model/brine/particle/item-warp/recipe/research/prompt)')
