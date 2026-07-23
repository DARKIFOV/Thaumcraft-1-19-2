#!/usr/bin/env python3
"""v11.64.13 guard: complete TC4 Unnatural Hunger source-level closure."""
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
        raise SystemExit('TC4 v11.64.13 Unnatural Hunger full-closure guard: FAIL: ' + message)

def version_tuple(raw):
    m = re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']', raw)
    req(m is not None, 'version parse')
    return tuple(map(int, m.groups()))

def sha(path):
    return hashlib.sha256((R / path).read_bytes()).hexdigest()

req(version_tuple(text('build.gradle')) >= (11, 64, 13), 'build version >= 11.64.13')
req(version_tuple(text('src/main/resources/META-INF/mods.toml')) >= (11, 64, 13), 'mods version >= 11.64.13')

contract = text('src/main/java/com/darkifov/thaumcraft/warp/TC4UnnaturalHungerParity.java')
for token in (
        'CONTRACT_VERSION = "11.64.13"', 'EFFECT_COLOR = 0x446633',
        'ICON_COLUMN = 7', 'ICON_ROW = 1',
        'EXHAUSTION_PER_LEVEL_PER_TICK = 0.025F',
        'FIRST_WARP_DURATION_TICKS = 5000', 'SECOND_WARP_DURATION_TICKS = 6000',
        'MAX_AMPLIFIER = 3', 'WARP_PER_AMPLIFIER_STEP = 15',
        'CURATIVE_DURATION_REDUCTION_TICKS = 600',
        'CURATIVE_AMPLIFIER_REDUCTION = 1',
        'COLOR_SCALE_RED = 1.00F', 'COLOR_SCALE_GREEN = 0.80F',
        'COLOR_SCALE_BLUE = 0.80F', 'SATURATION = 1.10F',
        'exhaustionPerTick(', 'warpAmplifier(', 'afterCurative(', 'transform(',
        'record Reduction', 'record Rgb'):
    req(token in contract, 'missing pure contract token: ' + token)

runtime = text('src/main/java/com/darkifov/thaumcraft/effect/TC4WarpMobEffect.java')
for token in (
        'case UNNATURAL_HUNGER ->',
        'player.causeFoodExhaustion(TC4UnnaturalHungerParity.exhaustionPerTick(amplifier))',
        'case UNNATURAL_HUNGER -> true',
        'cures.add(new ItemStack(Items.ROTTEN_FLESH))',
        'new ResourceLocation(ThaumcraftMod.MOD_ID, "tc4_brain")'):
    req(token in runtime, 'effect runtime/initial curative missing: ' + token)

production = text('src/main/java/com/darkifov/thaumcraft/event/WarpEvents.java')
for token in (
        'TC4UnnaturalHungerParity.afterCurative(',
        'replacement.setCurativeItems(List.of(new ItemStack(Items.ROTTEN_FLESH)))',
        'hungerMessage(player, "warp.text.hunger.2", ChatFormatting.DARK_GREEN)',
        'hungerMessage(player, "warp.text.hunger.1", ChatFormatting.DARK_RED)',
        'TC4UnnaturalHungerParity.FIRST_WARP_DURATION_TICKS',
        'TC4UnnaturalHungerParity.SECOND_WARP_DURATION_TICKS',
        'TC4UnnaturalHungerParity.warpAmplifier(warp)'):
    req(token in production, 'production event wiring missing: ' + token)
req(production.count('TC4UnnaturalHungerParity.warpAmplifier(warp)') == 2,
    'both original event-table paths must use the exact amplifier formula')

client = text('src/main/java/com/darkifov/thaumcraft/client/UnnaturalHungerPostEffect.java')
for token in (
        'new ResourceLocation(', '"shaders/post/unnatural_hunger.json"',
        'private static PostChain chain', 'RenderGuiEvent.Pre event',
        'chain.resize(framebufferWidth, framebufferHeight)',
        'chain.process(event.getPartialTick())',
        'minecraft.getMainRenderTarget()', 'mainTarget.bindWrite(true)',
        'invalidateResources()', 'reloadPending', 'chain.close()',
        '!minecraft.player.hasEffect(ThaumcraftMod.UNNATURAL_HUNGER.get())'):
    req(token in client, 'client post-chain lifecycle missing: ' + token)
req('GameRenderer.loadEffect' not in client and '.loadEffect(' not in client,
    'must not occupy Minecraft GameRenderer single global post-effect slot')

client_mod = text('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java')
req('RegisterClientReloadListenersEvent' in client_mod, 'client reload event registration missing')
req('UnnaturalHungerPostEffect.invalidateResources()' in client_mod,
    'post chain is not invalidated on resource reload')

overlay = text('src/main/java/com/darkifov/thaumcraft/client/WarpEffectOverlayEvents.java')
req('player.hasEffect(ThaumcraftMod.UNNATURAL_HUNGER.get())' not in overlay,
    'obsolete green rectangle visual is still active')
req('0x183A4D20' not in overlay, 'obsolete Unnatural Hunger overlay color remains')

post = json.loads(text('src/main/resources/assets/thaumcraft/shaders/post/unnatural_hunger.json'))
req(post.get('targets') == ['swap'], 'post target must be exactly swap')
req(len(post.get('passes', [])) == 2, 'post chain must contain color_convolve + blit')
first, second = post['passes']
req(first.get('name') == 'color_convolve' and first.get('intarget') == 'minecraft:main'
    and first.get('outtarget') == 'swap', 'first post pass mismatch')
uniforms = {u['name']: u['values'] for u in first.get('uniforms', [])}
req(uniforms.get('ColorScale') == [1.0, 0.8, 0.8], 'ColorScale drifted from original hunger.json')
req(uniforms.get('Saturation') == [1.1], 'Saturation drifted from original hunger.json')
req(second.get('name') == 'blit' and second.get('intarget') == 'swap'
    and second.get('outtarget') == 'minecraft:main', 'final blit pass mismatch')

original_post = json.loads(text('reference/original_tc4_1710_assets/minecraft/shaders/post/hunger.json'))
original_uniforms = {u['name']: u['values'] for u in original_post['passes'][0]['uniforms']}
req(original_uniforms['ColorScale'] == uniforms['ColorScale'], 'ported ColorScale differs from embedded original')
req(original_uniforms['Saturation'] == uniforms['Saturation'], 'ported Saturation differs from embedded original')

sheet = Image.open(R / 'src/main/resources/assets/thaumcraft/textures/misc/potions.png').convert('RGBA')
icon = Image.open(R / 'src/main/resources/assets/thaumcraft/textures/mob_effect/unnatural_hunger.png').convert('RGBA')
req(sheet.size == (256, 256) and icon.size == (18, 18), 'potion sheet/icon dimensions mismatch')
req(ImageChops.difference(sheet.crop((126, 216, 144, 234)), icon).getbbox() is None,
    'effect icon is not the exact TC4 index (7,1) crop')
req(sha('src/main/resources/assets/thaumcraft/textures/misc/potions.png') ==
    sha('src/main/resources/assets/thaumcraft/original_tc4_1710/textures/misc/potions.png'),
    'runtime potion sheet is not byte-identical to embedded TC4 sheet')

with zipfile.ZipFile(R / 'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip') as z:
    def member(suffix):
        name = next((n for n in z.namelist() if n.endswith(suffix)), None)
        req(name is not None, 'original source member missing: ' + suffix)
        return z.read(name).decode('utf-8', errors='replace')
    potion = member('/thaumcraft/common/lib/potions/PotionUnnaturalHunger.java')
    food = member('/thaumcraft/common/lib/events/EventHandlerEntity.java')
    warp = member('/thaumcraft/common/lib/WarpEvents.java')
    config = member('/thaumcraft/common/config/Config.java')
    tick = member('/thaumcraft/client/lib/ClientTickEventsFML.java')
    render = member('/thaumcraft/client/lib/RenderEventHandler.java')
for token in ('func_76390_b("potion.unhunger")', 'func_76399_b(7, 1)',
              'func_76404_a(0.25D)', '0.025F * (par2 + 1)'):
    req(token in potion, 'original potion oracle mismatch: ' + token)
req('new PotionUnnaturalHunger(potionUnHungerID, true, 4482611)' in config,
    'original harmful/color registration mismatch')
for token in ('int amp = pe.func_76458_c() - 1', 'int duration = pe.func_76459_b() - 600',
              'warp.text.hunger.1', 'warp.text.hunger.2'):
    req(token in food, 'original food oracle mismatch: ' + token)
for token in ('Config.potionUnHungerID, 5000', 'Config.potionUnHungerID, 6000',
              'Math.min(3, warp / 15)'):
    req(token in warp, 'original event table oracle mismatch: ' + token)
req('shaders/post/hunger.json' in tick, 'original shader activation oracle mismatch')
req('RenderGameOverlayEvent.Pre' in render and 'ElementType.ALL' in render,
    'original pre-HUD shader processing oracle mismatch')

lang_en = json.loads(text('src/main/resources/assets/thaumcraft/lang/en_us.json'))
lang_ru = json.loads(text('src/main/resources/assets/thaumcraft/lang/ru_ru.json'))
for lang, name in ((lang_en, 'en_us'), (lang_ru, 'ru_ru')):
    for key in ('effect.thaumcraft.unnatural_hunger', 'potion.unhunger',
                'warp.text.2', 'warp.text.hunger.1', 'warp.text.hunger.2'):
        req(key in lang and lang[key], f'{name} missing {key}')

gametest = text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods = re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(', gametest, re.S)
req(len(methods) >= 146 and len(methods) == len(set(methods)),
    f'expected at least 146 unique GameTests, got {len(methods)}')
req('unnaturalHungerFullRuntimeAndShaderContractMatchesOriginal' in methods,
    'full Unnatural Hunger GameTest missing')

manifest = json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids = [case['id'] for case in manifest['tests']]
req(tuple(map(int, manifest['version'].split('.'))) >= (11, 64, 13), 'manifest version >= 11.64.13')
req(len(ids) >= 478 and len(ids) == len(set(ids)),
    f'expected at least 478 unique scenarios, got {len(ids)}')
for scenario in (
        'gametest.unnatural_hunger_curative_food_reduction',
        'gametest.unnatural_hunger_full_mechanic_contract',
        'client.unnatural_hunger_exact_post_chain',
        'client.unnatural_hunger_post_chain_lifecycle',
        'multiplayer.unnatural_hunger_effect_and_food_sync',
        'resource.unnatural_hunger_icon_exact_crop'):
    req(scenario in ids, 'manifest scenario missing: ' + scenario)

evidence = json.loads(text('tools/data/tc4_unnatural_hunger_full_source_evidence_v11.64.13.json'))
req(evidence['round'] == '11.64.13', 'evidence round mismatch')
req(evidence['runtime_status'] == 'NOT_VERIFIED', 'runtime honesty drifted')
req(evidence['build_status'] == 'NOT_OBTAINED', 'build honesty drifted')

print(f'TC4 v11.64.13 Unnatural Hunger full-closure guard: PASS '
      f'({len(methods)} GameTests; {len(ids)} scenarios; exact runtime + food + icon + post chain)')
