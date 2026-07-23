#!/usr/bin/env python3
"""v11.64.12 guard: TC4 Unnatural Hunger food-finish reduction parity."""
from pathlib import Path
import json
import re
import zipfile
import subprocess
import sys

R = Path(__file__).resolve().parents[1]

def text(path):
    return (R / path).read_text(encoding='utf-8')

def req(condition, message):
    if not condition:
        raise SystemExit('TC4 v11.64.12 Unnatural Hunger food guard: FAIL: ' + message)

def version_tuple(raw):
    m = re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']', raw)
    req(m is not None, 'version parse')
    return tuple(map(int, m.groups()))

current_version = version_tuple(text('build.gradle'))
if current_version >= (11, 64, 13):
    result = subprocess.run([sys.executable, str(R / 'tools/tc4_116413_unnatural_hunger_full_closure_guard.py')], cwd=R)
    if result.returncode:
        raise SystemExit(result.returncode)
    print('TC4 v11.64.12 Unnatural Hunger food guard: PASS (forward-compatible via v11.64.13 full closure)')
    raise SystemExit(0)

req(current_version >= (11, 64, 12), 'build version >= 11.64.12')
req(version_tuple(text('src/main/resources/META-INF/mods.toml')) >= (11, 64, 12), 'mods version >= 11.64.12')

parity = text('src/main/java/com/darkifov/thaumcraft/warp/TC4WarpRuntimeParity.java')
contract = re.search(r'CONTRACT_VERSION = "(\d+)\.(\d+)\.(\d+)"', parity)
req(contract and tuple(map(int, contract.groups())) >= (11, 64, 12), 'contract version >= 11.64.12')
for token in (
        'UNNATURAL_HUNGER_CURATIVE_DURATION_REDUCTION_TICKS = 600',
        'UNNATURAL_HUNGER_CURATIVE_AMPLIFIER_REDUCTION = 1',
        'unnaturalHungerAfterCurative(',
        'currentDuration - UNNATURAL_HUNGER_CURATIVE_DURATION_REDUCTION_TICKS',
        'currentAmplifier - UNNATURAL_HUNGER_CURATIVE_AMPLIFIER_REDUCTION',
        'duration > 0 && amplifier >= 0',
        'record UnnaturalHungerReduction'):
    req(token in parity, 'missing exact reduction contract token: ' + token)

production = text('src/main/java/com/darkifov/thaumcraft/event/WarpEvents.java')
start = production.index('public static void onItemUseFinish')
end = production.index('public static void onPlayerTick', start)
segment = production[start:end]
for token in (
        'LivingEntityUseItemEvent.Finish event',
        'event.getEntity() instanceof ServerPlayer player',
        'player.getEffect(ThaumcraftMod.UNNATURAL_HUNGER.get())',
        'ItemStack consumed = event.getItem();',
        'consumed.is(Items.ROTTEN_FLESH)',
        'TC4_RESEARCH_ITEMS.get("tc4_brain").get()',
        'TC4WarpRuntimeParity.unnaturalHungerAfterCurative(',
        'player.removeEffect(ThaumcraftMod.UNNATURAL_HUNGER.get())',
        'if (reduced.remainsActive())',
        'replacement.setCurativeItems(List.of(new ItemStack(Items.ROTTEN_FLESH)))',
        'hungerMessage(player, "warp.text.hunger.2", ChatFormatting.DARK_GREEN)',
        'else if (consumed.isEdible())',
        'hungerMessage(player, "warp.text.hunger.1", ChatFormatting.DARK_RED)'):
    req(token in segment, 'production food-finish wiring missing: ' + token)
req(segment.index('player.removeEffect') < segment.index('player.addEffect(replacement)'),
    'effect must be removed before the reduced instance is re-added')
req('setResultStack' not in segment and 'setCanceled' not in segment,
    'ordinary food nutrition must not be canceled or replaced')

with zipfile.ZipFile(R / 'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip') as z:
    name = next((n for n in z.namelist() if n.endswith('/thaumcraft/common/lib/events/EventHandlerEntity.java')), None)
    req(name is not None, 'original EventHandlerEntity oracle missing')
    original = z.read(name).decode('utf-8', errors='replace')
for token in (
        'public void finishedUsingItem(PlayerUseItemEvent.Finish event)',
        'event.entityPlayer.func_82165_m(Config.potionUnHungerID)',
        'event.item.func_77969_a(new ItemStack(Items.field_151078_bh))',
        'event.item.func_77969_a(new ItemStack(ConfigItems.itemZombieBrain))',
        'int amp = pe.func_76458_c() - 1',
        'int duration = pe.func_76459_b() - 600',
        'event.entityPlayer.func_82170_o(Config.potionUnHungerID)',
        '(duration > 0) && (amp >= 0)',
        'pe.getCurativeItems().clear()',
        'pe.addCurativeItem(new ItemStack(Items.field_151078_bh))',
        'warp.text.hunger.2',
        'event.item.func_77973_b() instanceof ItemFood',
        'warp.text.hunger.1'):
    req(token in original, 'original source evidence mismatch: ' + token)

# Initial effect still exposes both TC4 special foods before the first reduction.
effects = text('src/main/java/com/darkifov/thaumcraft/effect/TC4WarpMobEffect.java')
req('case UNNATURAL_HUNGER ->' in effects, 'initial Unnatural Hunger curative configuration missing')
req('cures.add(new ItemStack(Items.ROTTEN_FLESH))' in effects, 'initial rotten-flesh cure missing')
req('new ResourceLocation(ThaumcraftMod.MOD_ID, "tc4_brain")' in effects, 'initial zombie-brain cure missing')

gametest = text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods = re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(', gametest, re.S)
req(len(methods) >= 145, f'expected at least 145 GameTests, got {len(methods)}')
req(len(methods) == len(set(methods)), 'duplicate GameTest method names')
req('unnaturalHungerCurativeFoodReducesDurationAndAmplifier' in methods,
    'Unnatural Hunger food GameTest missing')
for token in ('unnaturalHungerAfterCurative(5000, 3)', 'duration() == 4400',
              'amplifier() == 2', 'unnaturalHungerAfterCurative(5000, 0)',
              'amplifier() == -1', 'unnaturalHungerAfterCurative(600, 3)',
              'duration() == 0'):
    req(token in gametest, 'Unnatural Hunger fixture missing: ' + token)

manifest = json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids = [case['id'] for case in manifest['tests']]
req(tuple(map(int, manifest['version'].split('.'))) >= (11, 64, 12), 'manifest version >= 11.64.12')
req(len(ids) >= 473 and len(ids) == len(set(ids)), 'expected at least 473 unique manifest scenarios')
req('gametest.unnatural_hunger_curative_food_reduction' in ids,
    'Unnatural Hunger manifest scenario missing')

evidence = json.loads(text('tools/data/tc4_unnatural_hunger_food_source_evidence_v11.64.12.json'))
req(evidence['round'] == '11.64.12', 'evidence round mismatch')
req(evidence['runtime_status'] == 'NOT_VERIFIED', 'runtime honesty drifted')
req(evidence['build_status'] == 'NOT_OBTAINED', 'build honesty drifted')

print(f'TC4 v11.64.12 Unnatural Hunger food guard: PASS '
      f'({len(methods)} GameTests; {len(ids)} scenarios; 600 ticks + one amplifier + exact messages)')
