#!/usr/bin/env python3
from pathlib import Path
import json,re
ROOT=Path(__file__).resolve().parents[1]

def req(cond,msg):
    if not cond: raise SystemExit('TC4 v11.63.94 parity consolidation guard: FAIL: '+msg)

def text(rel): return (ROOT/rel).read_text(encoding='utf-8')

build=text('build.gradle')
mods=text('src/main/resources/META-INF/mods.toml')
m=re.search(r"^version = '(\d+)\.(\d+)\.(\d+)'", build, re.M)
req(m is not None, 'build version parse')
current=tuple(map(int,m.groups()))
req(current >= (11,63,94), 'build version must be 11.63.94 or newer')
mm=re.search(r'(?m)^version="(\d+)\.(\d+)\.(\d+)"', mods)
req(mm is not None, 'mods.toml version parse')
req(tuple(map(int,mm.groups())) == current, 'mods.toml/build version agreement')
files={
 'taint':'src/main/java/com/darkifov/thaumcraft/taint/TC4TaintParity.java',
 'world':'src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenParity.java',
 'runic':'src/main/java/com/darkifov/thaumcraft/runic/TC4RunicParity.java',
 'eldritch':'src/main/java/com/darkifov/thaumcraft/eldritch/TC4EldritchParity.java',
 'aura':'src/main/java/com/darkifov/thaumcraft/aura/TC4AuraParity.java',
 'recipe':'src/main/java/com/darkifov/thaumcraft/recipe/TC4RecipeParity.java',
}
for name,rel in files.items():
    s=text(rel)
    req('CONTRACT_VERSION = "11.63.94"' in s,name+' contract version')

req('ORIGINAL_HORIZONTAL_OFFSET_BOUND = 3' in text('src/main/java/com/darkifov/thaumcraft/taint/TaintSpreadRuntime.java'),'taint horizontal bound')
req('ORIGINAL_SPORE_ROLL_BOUND = 200' in text('src/main/java/com/darkifov/thaumcraft/taint/TaintSpreadRuntime.java'),'taint spore bound')
req('ORIGINAL_MANA_POD_ATTEMPTS = 10' in text('src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenRuntime.java'),'mana pod attempts')
req('ORIGINAL_FORTRESS_PIECE_COUNT = 3' in text('src/main/java/com/darkifov/thaumcraft/runic/TC4FortressArmorRuntime.java'),'fortress piece count')
req('CRIMSON_RITES_STICKY_WARP = 1' in text('src/main/java/com/darkifov/thaumcraft/eldritch/TC4EldritchProgression.java'),'crimson warp')
req('ORIGINAL_PRESERVATION_PERCENT = 100' in text('src/main/java/com/darkifov/thaumcraft/aura/TC4NodeJarRuntime.java'),'node preservation')
req('crystalCountForRoll' in text('src/main/java/com/darkifov/thaumcraft/alchemy/LiquidDeathDropRuntime.java'),'liquid death formula helper')
req('focusCatalogueAndUpgradeIdsMatchTc4' in text('src/main/java/com/darkifov/thaumcraft/wand/TC4WandParity.java'),'wand focus contract')
req('decorationsBellModesAndCarryFormulaMatchTc4' in text('src/main/java/com/darkifov/thaumcraft/golem/TC4GolemParity.java'),'golem v2 contract')

gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=98,f'expected at least 98 annotated GameTests, got {len(methods)}')
req(len(set(methods))==len(methods),'GameTest names must be unique')
for name in (
 'taintDeathConversionAndSpreadConstantsMatchTc4','worldgenBiomesTreesAndManaPodsMatchTc4',
 'runicShieldDamageAndFortressConstantsMatchTc4','eldritchWarpCrimsonAndOuterLandsMatchTc4',
 'auraNodeTypesModifiersAndJarCaptureMatchTc4','recipeAspectAndSmeltingBonusLedgerMatchesTc4',
 'golemDecorationsBellModesAndCarryFormulaMatchTc4','wandFocusCatalogueAndUpgradeIdsMatchTc4',
 'alchemyLiquidDeathCrystalCountFormulaMatchesTc4'):
    req(name in methods,'missing GameTest '+name)

manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids=[x['id'] for x in manifest['tests']]
req(tuple(map(int,manifest['version'].split('.'))) >= (11,63,94),'manifest version')
req(len(ids)>=426,f'expected at least 426 manifest scenarios, got {len(ids)}')
req(len(ids)==len(set(ids)),'manifest ids unique')
req(not any((ROOT/'src/main/java/com/darkifov/thaumcraft'/p).exists() for p in (
    'entity/TC4EntityParity.java','menu/TC4MenuParity.java','item/TC4ItemParity.java',
    'network/TC4NetworkParity.java','blockentity/TC4BlockEntityParity.java')),
    'file-count pseudo-parity classes must not be present')
print(f'TC4 v11.63.94 parity consolidation guard: PASS ({len(methods)} GameTests; {len(ids)} manifest scenarios; source-linked contracts retained)')
