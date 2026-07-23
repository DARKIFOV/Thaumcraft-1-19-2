#!/usr/bin/env python3
"""v11.64.14 guard: Purifying Bath Salts complete source/resource closure."""
from pathlib import Path
from PIL import Image, ImageChops
import hashlib
import json
import re
import zipfile

R = Path(__file__).resolve().parents[1]

def text(path):
    return (R / path).read_text(encoding='utf-8')

def req(condition, message):
    if not condition:
        raise SystemExit('TC4 v11.64.14 Bath Salts full-closure guard: FAIL: ' + message)

def version_tuple(raw):
    m = re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']', raw)
    req(m is not None, 'version parse')
    return tuple(map(int, m.groups()))

def sha(path):
    return hashlib.sha256((R / path).read_bytes()).hexdigest()

req(version_tuple(text('build.gradle')) >= (11, 64, 14), 'build version >= 11.64.14')
req(version_tuple(text('src/main/resources/META-INF/mods.toml')) >= (11, 64, 14), 'mods version >= 11.64.14')

contract = text('src/main/java/com/darkifov/thaumcraft/warp/TC4BathSaltsParity.java')
for token in (
    'CONTRACT_VERSION = "11.64.14"', 'ITEM_ENTITY_LIFESPAN_TICKS = 200',
    'FLUID_LIGHT_LEVEL = 10', 'FLUID_VISCOSITY = 1000', 'FLUID_DENSITY = 1000',
    'WARD_DURATION_CAP_TICKS = 32000', 'WARD_DURATION_NUMERATOR = 200000',
    'WARD_AMPLIFIER = 0', 'WARD_ICON_COLUMN = 3', 'WARD_ICON_ROW = 2',
    'WARD_COLOR = 0xE0F2F7', 'WARD_EFFECTIVENESS = 0.25D',
    'BUBBLE_PARTICLE_FIRST_FRAME = 16', 'BUBBLE_PARTICLE_LAST_FRAME = 18',
    'BUBBLE_ALPHA = 0.25F', 'POP_SOUND_CHANCE_BOUND = 25',
    'RECIPE_COGNITIO = 6', 'RECIPE_AURAM = 6', 'RECIPE_ORDO = 6', 'RECIPE_SANO = 6',
    'RESEARCH_COGNITIO = 3', 'RESEARCH_AURAM = 3', 'RESEARCH_ORDO = 3', 'RESEARCH_SANO = 3',
    'RESEARCH_X = -4', 'RESEARCH_Y = -4', 'RESEARCH_COMPLEXITY = 2',
    'WIZARD_TRADE_EMERALD_MIN = 5', 'WIZARD_TRADE_RANDOM_BOUND = 3',
    'convertsExpiredItem(', 'wardDurationTicks(', 'bubbleYOffset(', 'bubbleLifetime(',
    'wizardEmeraldCost('):
    req(token in contract, 'missing pure contract token: ' + token)

item = text('src/main/java/com/darkifov/thaumcraft/block/BathSaltsItem.java')
req('TC4BathSaltsParity.ITEM_ENTITY_LIFESPAN_TICKS' in item, 'item lifespan not wired to contract')
req('getEntityLifespan' in item, 'item lifespan override missing')

events = text('src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java')
for token in ('onBathSaltsExpire', 'dissolveBathSalts(event.getEntity())',
              'fluidState.getType() == net.minecraft.world.level.material.Fluids.WATER',
              'TC4BathSaltsParity.convertsExpiredItem',
              'PURIFYING_FLUID_BLOCK.get().defaultBlockState()'):
    req(token in events, 'expiry production path missing: ' + token)

fluid = text('src/main/java/com/darkifov/thaumcraft/block/PurifyingFluidBlock.java')
for token in ('level.getFluidState(pos).isSource()', 'player.hasEffect(ThaumcraftMod.WARP_WARD.get())',
              'PlayerThaumData.getWarpPerm(player)', 'TC4BathSaltsParity.wardDurationTicks',
              'new MobEffectInstance(ThaumcraftMod.WARP_WARD.get(), duration, 0, true, true)',
              'level.removeBlock(pos, false)', 'PURIFYING_BUBBLE_PARTICLE.get()',
              'TC4BathSaltsParity.bubbleYOffset(legacyMetadata)',
              'random.nextInt(TC4BathSaltsParity.POP_SOUND_CHANCE_BOUND)',
              'TC4BathSaltsParity.POP_SOUND_Y_OFFSET', 'super.animateTick(state, level, pos, random)'):
    req(token in fluid, 'fluid production path missing: ' + token)
req(fluid.index('PURIFYING_BUBBLE_PARTICLE.get()') < fluid.index('super.animateTick(state, level, pos, random)'),
    'TC4 bubble/sound RNG sequence must occur before superclass animateTick')

particle = text('src/main/java/com/darkifov/thaumcraft/client/fx/TC4PurifyingBubbleParticle.java')
for token in ('extends TextureSheetParticle', 'this.alpha = TC4BathSaltsParity.BUBBLE_ALPHA',
              'this.hasPhysics = false', 'this.setSize(0.02F, 0.02F)',
              'this.quadSize *= this.random.nextFloat() * 0.3F + 0.2F',
              'TC4BathSaltsParity.bubbleLifetime', 'UPWARD_ACCELERATION = 0.002D',
              'VELOCITY_DAMPING = 0.8500000238418579D', 'return 0xF000F0',
              'PARTICLE_SHEET_TRANSLUCENT', 'remaining <= 1 ? 2 : remaining <= 2 ? 1 : 0'):
    req(token in particle, 'FXBubble production parity missing: ' + token)
client = text('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java')
req('RegisterParticleProvidersEvent' in client and 'PURIFYING_BUBBLE_PARTICLE.get()' in client
    and 'TC4PurifyingBubbleParticle.Provider::new' in client,
    'client particle provider registration missing')

mod = text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
for token in ('PARTICLE_TYPES.register("purifying_bubble"', 'PARTICLE_TYPES.register(modBus)',
              '.lightLevel(TC4BathSaltsParity.FLUID_LIGHT_LEVEL)',
              '.density(TC4BathSaltsParity.FLUID_DENSITY)',
              '.viscosity(TC4BathSaltsParity.FLUID_VISCOSITY).rarity(Rarity.RARE)',
              '.slopeFindDistance(TC4BathSaltsParity.FLUID_SLOPE_FIND_DISTANCE)',
              '.levelDecreasePerBlock(TC4BathSaltsParity.FLUID_LEVEL_DECREASE_PER_BLOCK)',
              '.tickRate(TC4BathSaltsParity.FLUID_TICK_RATE)',
              'DispenserBlock.registerBehavior(PURIFYING_FLUID_BUCKET.get()'):
    req(token in mod, 'registration/property path missing: ' + token)
pure_bucket = re.search(r'PURIFYING_FLUID_BUCKET =.*?;\n', mod, re.S)
req(pure_bucket is not None and '.rarity(' not in pure_bucket.group(0), 'pure bucket must have default item rarity')
bath_item = re.search(r'BATH_SALTS =.*?;\n', mod, re.S)
req(bath_item is not None and '.rarity(' not in bath_item.group(0), 'Bath Salts must have default item rarity')
death_bucket = re.search(r'LIQUID_DEATH_BUCKET =.*?;\n', mod, re.S)
req(death_bucket is not None and '.rarity(Rarity.RARE)' in death_bucket.group(0),
    'unrelated Liquid Death bucket rarity was accidentally changed')

recipe = json.loads(text('src/main/resources/data/thaumcraft/thaumcraft_alchemy/tc4_bathsalts.json'))
req(recipe['research'] == 'BATHSALTS' and recipe['catalyst'] == 'thaumcraft:tc4_dust', 'recipe research/catalyst mismatch')
req(recipe['result'] == {'item':'thaumcraft:tc4_bath_salts','count':1}, 'recipe result mismatch')
req(recipe['aspects'] == {'COGNITIO':6,'AURAM':6,'ORDO':6,'SANO':6}, 'recipe aspects mismatch')

research = text('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchRuntimeBridge.java')
for token in ('"BATHSALTS", "Purifying Bath Salts"', '"ALCHEMY", -4, -4, 2',
              'aspects("COGNITIO", 3, "AURAM", 3, "ORDO", 3, "SANO", 3)',
              'new String[] {"hidden"}', 'new String[] {"BathSalts"}',
              '"SANESOAP"', 'new String[] {"BATHSALTS"}', '"ARCANESPA"'):
    req(token in research, 'research graph/page mismatch: ' + token)

spa = text('src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneSpaBlockEntity.java')
for token in ('CAPACITY = TC4ArcaneSpaParity.CAPACITY_MB', 'CHECK_INTERVAL = TC4ArcaneSpaParity.CHECK_INTERVAL_TICKS', 'BUCKET = TC4ArcaneSpaParity.BUCKET_MB',
              'stack.is(ThaumcraftMod.BATH_SALTS.get())',
              'targetFluid = ThaumcraftMod.PURIFYING_FLUID.get()',
              'tank.drain(BUCKET', 'salts.extractItem(0, 1, false)',
              'for (int x = -TC4ArcaneSpaParity.OUTPUT_RADIUS', 'for (int z = -TC4ArcaneSpaParity.OUTPUT_RADIUS'):
    req(token in spa, 'Arcane Spa integration mismatch: ' + token)

# Runtime item/block textures remain byte-identical to embedded original assets.
for runtime, original in (
    ('src/main/resources/assets/thaumcraft/textures/item/tc4/bath_salts.png',
     'src/main/resources/assets/thaumcraft/original_tc4_1710/textures/items/bath_salts.png'),
    ('src/main/resources/assets/thaumcraft/textures/item/tc4/bucket_pure.png',
     'src/main/resources/assets/thaumcraft/original_tc4_1710/textures/items/bucket_pure.png'),
    ('src/main/resources/assets/thaumcraft/textures/block/tc4/fluidpure.png',
     'src/main/resources/assets/thaumcraft/original_tc4_1710/textures/blocks/fluidpure.png')):
    req(sha(runtime) == sha(original), 'runtime texture differs from original: ' + runtime)

sheet = Image.open(R / 'src/main/resources/assets/thaumcraft/original_tc4_1710/textures/misc/particles.png').convert('RGBA')
req(sheet.size == (256,256), 'original particle sheet dimensions')
for index, frame in enumerate((16,17,18)):
    expected = sheet.crop(((frame % 16)*16, (frame // 16)*16, (frame % 16)*16+16, (frame // 16)*16+16))
    actual = Image.open(R / f'src/main/resources/assets/thaumcraft/textures/particle/purifying_bubble_{index}.png').convert('RGBA')
    req(ImageChops.difference(expected, actual).getbbox() is None,
        f'purifying bubble frame {index} is not exact particle-sheet crop {frame}')
particle_json = json.loads(text('src/main/resources/assets/thaumcraft/particles/purifying_bubble.json'))
req(particle_json['textures'] == ['thaumcraft:purifying_bubble_0','thaumcraft:purifying_bubble_1','thaumcraft:purifying_bubble_2'],
    'particle JSON frame order mismatch')

ward_sheet = Image.open(R / 'src/main/resources/assets/thaumcraft/textures/misc/potions.png').convert('RGBA')
ward_icon = Image.open(R / 'src/main/resources/assets/thaumcraft/textures/mob_effect/warp_ward.png').convert('RGBA')
req(ImageChops.difference(ward_sheet.crop((54,234,72,252)), ward_icon).getbbox() is None,
    'Warp Ward icon is not exact TC4 icon (3,2) crop')

with zipfile.ZipFile(R / 'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip') as z:
    def member(suffix):
        name = next((n for n in z.namelist() if n.endswith(suffix)), None)
        req(name is not None, 'original source member missing: ' + suffix)
        return z.read(name).decode('utf-8', errors='replace')
    source_item = member('/thaumcraft/common/items/ItemBathSalts.java')
    source_event = member('/thaumcraft/common/lib/events/EventHandlerEntity.java')
    source_fluid = member('/thaumcraft/common/blocks/BlockFluidPure.java')
    source_bucket = member('/thaumcraft/common/items/ItemBucketPure.java')
    source_world = member('/thaumcraft/common/lib/events/EventHandlerWorld.java')
    source_ward = member('/thaumcraft/common/lib/potions/PotionWarpWard.java')
    source_recipe = member('/thaumcraft/common/config/ConfigRecipes.java')
    source_research = member('/thaumcraft/common/config/ConfigResearch.java')
    source_trade = member('/thaumcraft/common/lib/world/VillageWizardManager.java')
    source_bubble = member('/thaumcraft/client/fx/particles/FXBubble.java')
for token in ('return 200', 'thaumcraft:bath_salts'):
    req(token in source_item, 'original item oracle mismatch: ' + token)
for token in ('instanceof ItemBathSalts', 'Blocks.field_150355_j', 'func_72805_g(x, y, z) == 0', 'ConfigBlocks.blockFluidPure'):
    req(token in source_event, 'original expiry oracle mismatch: ' + token)
for token in ('Math.min(32000, 200000 / div)', 'world.func_147468_f(x, y, z)',
              'new FXBubble', '0.125F * (8 - meta)', 'func_82338_g(0.25F)',
              'rand.nextInt(25) == 0', '0.1F + rand.nextFloat() * 0.1F',
              '0.9F + rand.nextFloat() * 0.15F'):
    req(token in source_fluid, 'original fluid oracle mismatch: ' + token)
for token in ('func_77625_d(1)', 'ConfigBlocks.blockFluidPure, 0, 3', 'Items.field_151133_ar'):
    req(token in source_bucket, 'original bucket oracle mismatch: ' + token)
for token in ('ConfigBlocks.blockFluidPure', 'func_72805_g(event.target.field_72311_b',
              'new ItemStack(ConfigItems.itemBucketPure)', 'event.setResult(Event.Result.ALLOW)'):
    req(token in source_world, 'original bucket pickup oracle mismatch: ' + token)
for token in ('func_76399_b(3, 2)', 'func_76404_a(0.25D)', 'return false'):
    req(token in source_ward, 'original ward oracle mismatch: ' + token)
req('new AspectList().add(Aspect.MIND, 6).add(Aspect.AURA, 6).add(Aspect.ORDER, 6).add(Aspect.HEAL, 6)' in source_recipe,
    'original recipe oracle mismatch')
req('new ResearchItem("BATHSALTS", "ALCHEMY"' in source_research and '.setHidden()' in source_research,
    'original research oracle mismatch')
req('new ItemStack(Items.field_151166_bC, 5 + random.nextInt(3))' in source_trade
    and 'new ItemStack(ConfigItems.itemBathSalts, 1, 0)' in source_trade,
    'original wizard trade oracle mismatch')
for token in ('particle = 16', 'this.field_70547_e = ((int)(age + 2 + 8.0D /',
              'bubblespeed = 0.002D', '*= 0.8500000238418579D', 'tessellator.func_78380_c(240)'):
    req(token in source_bubble, 'original FXBubble oracle mismatch: ' + token)

langs = [json.loads(text('src/main/resources/assets/thaumcraft/lang/en_us.json')),
         json.loads(text('src/main/resources/assets/thaumcraft/lang/ru_ru.json'))]
for lang in langs:
    for key in ('item.thaumcraft.tc4_bath_salts','item.thaumcraft.tc4_bucket_pure',
                'block.thaumcraft.purifying_fluid','effect.thaumcraft.warp_ward'):
        req(key in lang and lang[key], 'language key missing: ' + key)

gametest = text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods = re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(', gametest, re.S)
req(len(methods) >= 149 and len(methods) == len(set(methods)),
    f'expected at least 149 unique GameTests, got {len(methods)}')
for method in ('bathSaltsFullContractMatchesOriginal','bathSaltsExpireConvertsOnlyExactWaterSource',
               'purifyingFluidSourceGrantsSingleUseWarpWard'):
    req(method in methods, 'Bath Salts GameTest missing: ' + method)

manifest = json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids = [case['id'] for case in manifest['tests']]
req(tuple(map(int, manifest['version'].split('.'))) >= (11,64,14), 'manifest version >= 11.64.14')
req(len(ids) >= 489 and len(ids) == len(set(ids)),
    f'expected at least 489 unique scenarios, got {len(ids)}')
for scenario in ('gametest.bath_salts_full_contract','gametest.bath_salts_expire_source_water_only',
                 'gametest.purifying_fluid_single_use_ward','client.purifying_fluid_fx_bubble_exact_frames',
                 'resource.bath_salts_bucket_fluid_exact_textures','gameplay.purifying_bucket_source_roundtrip',
                 'machine.arcane_spa_bath_salts_cycle','research.bath_salts_hidden_entry_and_recipe',
                 'multiplayer.purifying_fluid_warp_ward_sync','external.wizard_bath_salts_trade_contract'):
    req(scenario in ids, 'manifest scenario missing: ' + scenario)

evidence = json.loads(text('tools/data/tc4_bath_salts_full_source_evidence_v11.64.14.json'))
req(evidence['round'] == '11.64.14', 'evidence round mismatch')
req(evidence['build_status'] == 'NOT_OBTAINED' and evidence['runtime_status'] == 'NOT_VERIFIED',
    'build/runtime honesty drifted')
req(evidence['external_dependency']['production_activation'] == 'PENDING_SEPARATE_WIZARD_SYSTEM_CLOSURE',
    'wizard dependency honesty drifted')

prompt = text('UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md')
for token in ('Один релиз — один предмет или одна цельная механика',
              'SOURCE CLOSED', 'RESOURCE CLOSED', 'BUILD VERIFIED', 'RUNTIME VERIFIED',
              'Нельзя писать «портировано на 100%»'):
    req(token in prompt, 'universal prompt rule missing: ' + token)

print(f'TC4 v11.64.14 Bath Salts full-closure guard: PASS '
      f'({len(methods)} GameTests; {len(ids)} scenarios; item/fluid/ward/particle/bucket/recipe/research/spa)')
