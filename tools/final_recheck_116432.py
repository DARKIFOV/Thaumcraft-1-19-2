#!/usr/bin/env python3
from pathlib import Path
import hashlib, json, re, subprocess, zipfile
R = Path(__file__).resolve().parents[1]
checks = []

def req(name, ok, detail=''):
    checks.append((name, bool(ok), detail))
    if not ok:
        raise SystemExit(f'FINAL RECHECK v11.64.32: FAIL: {name}: {detail}')

def txt(path):
    return (R / path).read_text(encoding='utf-8', errors='replace')

req('build version', "version = '11.64.32'" in txt('build.gradle'))
req('mods version', 'version="11.64.32"' in txt('src/main/resources/META-INF/mods.toml'))
required = (
    'TC4_11.64.32_WAND_RODS_CAPS_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md',
    'TC4_11.64.32_FULL_CLOSURE_STATUS_AND_PLAN_RU.md',
    'TC4_11.64.32_WAND_RODS_CAPS_SOURCE_EVIDENCE.json',
    'tools/data/tc4_wand_rods_caps_full_source_evidence_v11.64.32.json',
    'TC4_11.64.32_FOCUSED_STATIC_CI_FINAL.log',
    'TC4_11.64.32_JAVA17_SELF_TEST.log',
    'TC4_11.64.32_GRADLE_BUILD_ATTEMPT.log',
    'TC4_11.64.32_BUILD_STATUS.txt',
    'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md',
)
for path in required:
    req('file ' + path, (R / path).is_file())
req('prompt identical', (R/'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md').read_bytes() == (R/'PROMPT_FOR_FUTURE_CHAT_RU.md').read_bytes())
req('prompt mandatory wording', 'Файл `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md` обязателен' in txt('UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md'))

math = txt('src/main/java/com/darkifov/thaumcraft/wand/TC4WandComponentMath.java')
for token in ('CONTRACT_VERSION = "11.64.32"', 'NORMAL_CAPACITY_MULTIPLIER = 100', 'SCEPTRE_CAPACITY_MULTIPLIER = 150', 'SCEPTRE_DISCOUNT = 0.1F', 'MINIMUM_COST_MODIFIER = 0.1F', 'REGEN_THRESHOLD_DIVISOR = 10', 'ONE_VIS_CENTIVIS = 100'):
    req('math ' + token, token in math)

data = txt('src/main/java/com/darkifov/thaumcraft/wand/WandComponentData.java')
for token in ('TC4WandComponentMath.capacityCentivis', 'TC4WandComponentMath.consumptionModifier', 'normalizeOriginalTags', 'root.putString(ORIGINAL_TAG_ROD', 'root.putString(ORIGINAL_TAG_CAP', 'root.remove(TAG_WAND)', 'displayNameComponent'):
    req('component data ' + token, token in data)
req('no nested NBT write', 'getOrCreateTagElement(TAG_WAND)' not in data)

variant = txt('src/main/java/com/darkifov/thaumcraft/wand/WandVariantRuntime.java')
for token in ('ORIGINAL_CREATIVE_VARIANT_COUNT = 4', 'create(WandRodType.WOOD, WandCapType.IRON, false, true)', 'create(WandRodType.GREATWOOD, WandCapType.GOLD, false, true)', 'create(WandRodType.SILVERWOOD, WandCapType.THAUMIUM, false, true)', 'create(WandRodType.SILVERWOOD, WandCapType.THAUMIUM, true, true)'):
    req('creative ' + token, token in variant)
req('expanded creative removed', 'for (WandRodType rod' not in variant and '216' not in variant)

rod = txt('src/main/java/com/darkifov/thaumcraft/wand/WandRodType.java')
for token in ('ELEMENTAL_REGEN_INTERVAL_TICKS = 200', 'PRIMAL_STAFF_REGEN_INTERVAL_TICKS = 50', 'REGEN_THRESHOLD_DIVISOR = 10', 'REGEN_AMOUNT_VIS = 1'):
    req('rod ' + token, token in rod)
req('misleading regen constant removed', 'REGEN_AMOUNT_CENTIVIS' not in rod)

wand = txt('src/main/java/com/darkifov/thaumcraft/block/WandItem.java')
for token in ('STAFF_ATTACK_DAMAGE = 6.0D', 'getAttributeModifiers(EquipmentSlot slot, ItemStack stack)', 'Attributes.ATTACK_DAMAGE', 'WandComponentData.normalizeOriginalTags(stack)', 'displayNameComponent(stack)', 'TC4WandComponentMath.regenerationThresholdCentivis', 'WandRodType.REGEN_AMOUNT_VIS'):
    req('wand ' + token, token in wand)
for forbidden in ('Rod: " + data.rod()', 'Caps: " + data.cap()', 'TC4 rod recharge:', 'Focus: none'):
    req('debug tooltip removed ' + forbidden, forbidden not in wand)

component = txt('src/main/java/com/darkifov/thaumcraft/item/TC4WandComponentItem.java')
req('component invented tooltip removed', 'appendHoverText' not in component)
mod = txt('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
req('wand stack/rarity registrations', mod.count('stacksTo(1).rarity(Rarity.UNCOMMON)') >= 3)
for token in ('IRON_WAND_CAP = specialItem', 'GREATWOOD_WAND_CORE = specialItem', 'TC4WandComponentItem.activeCap', 'TC4WandComponentItem.rod(new Item.Properties().tab(THAUMCRAFT_TAB), WandRodType.GREATWOOD)', 'TC4WandComponentItem.rod(new Item.Properties(), WandRodType.WOOD)'):
    req('canonical component ' + token, token in mod)

research = txt('src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java')
for old in ('tc4_wand_cap_iron', 'tc4_wand_cap_gold', 'tc4_wand_cap_thaumium', 'tc4_wand_rod_greatwood', 'tc4_wand_rod_silverwood'):
    line = next((x for x in research.splitlines() if f'case "{old}"' in x), '')
    req('hidden migration alias ' + old, 'properties,' in line and 'functionalProperties,' not in line, line)

parity = txt('src/main/java/com/darkifov/thaumcraft/wand/TC4WandComponentsFullClosureParity.java')
for token in ('CONTRACT_VERSION = "11.64.32"', 'ORIGINAL_WAND_CREATIVE_VARIANTS = 4', 'ORIGINAL_ROD_COMPONENT_VARIANTS = 17', 'ORIGINAL_CAP_COMPONENT_VARIANTS = 9', 'ORIGINAL_STAFF_ATTACK_DAMAGE = 6', 'capacitiesAndCraftCostsMatchOriginal', 'capCostsAndModifiersMatchOriginal', 'regenerationMatchesOriginal', 'catalogueCountsMatchOriginal'):
    req('parity ' + token, token in parity)

for lang in ('en_us.json', 'ru_ru.json'):
    values = json.loads(txt('src/main/resources/assets/thaumcraft/lang/' + lang))
    req('name format ' + lang, values.get('item.thaumcraft.wand_name_format') == '%1$s %2$s %3$s')
    for key in ('item.Wand.name', 'item.Wand.wand.obj', 'item.Wand.sceptre.obj', 'item.Wand.staff.obj', 'item.Wand.wood.rod', 'item.Wand.iron.cap'):
        req('localization ' + lang + ' ' + key, key in values)

source_zip = R/'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip'
prefix = 'Thaumcraft4-1.7.10-master/'
textures = ['wand_cap_copper','wand_cap_gold','wand_cap_iron','wand_cap_silver','wand_cap_silver_inert','wand_cap_thaumium','wand_cap_thaumium_inert','wand_cap_void','wand_cap_void_inert','wand_rod_blaze','wand_rod_bone','wand_rod_greatwood','wand_rod_ice','wand_rod_obsidian','wand_rod_quartz','wand_rod_reed','wand_rod_silverwood','staff_rod_blaze','staff_rod_bone','staff_rod_greatwood','staff_rod_ice','staff_rod_obsidian','staff_rod_primal','staff_rod_quartz','staff_rod_reed','staff_rod_silverwood']
with zipfile.ZipFile(source_zip) as zf:
    for name in textures:
        current = R/f'src/main/resources/assets/thaumcraft/textures/item/tc4/{name}.png'
        req('texture exists ' + name, current.is_file())
        req('texture hash ' + name, hashlib.sha256(current.read_bytes()).digest() == hashlib.sha256(zf.read(prefix+f'assets/thaumcraft/textures/items/{name}.png')).digest())

for path in ('TC4_11.64.32_WAND_RODS_CAPS_SOURCE_EVIDENCE.json', 'tools/data/tc4_wand_rods_caps_full_source_evidence_v11.64.32.json'):
    evidence = json.loads(txt(path))
    req('evidence version ' + path, evidence.get('version') == '11.64.32')
    req('evidence original sources ' + path, len(evidence.get('original_sources', [])) == 5)
    req('evidence production contracts ' + path, len(evidence.get('production_contracts', [])) == 5)

gt = txt('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods = re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(', gt, re.S)
req('GameTest count', len(methods) == 253, str(len(methods)))
req('GameTest unique', len(set(methods)) == 253, str(len(set(methods))))
for name in ('wandOriginalCreativeVariantsMatchTc4', 'wandRootNbtMigrationMatchesTc4', 'wandStaffAttackDamageMatchesTc4', 'wandStackRarityAndNamesMatchTc4', 'wandComponentCatalogueFullClosureMatchesTc4'):
    req('GameTest ' + name, name in methods)

manifest = json.loads(txt('runtime_artifacts/runtime_test_manifest.template.json'))
ids = [x['id'] for x in manifest['tests']]
req('manifest version', manifest['version'] == '11.64.32', manifest['version'])
req('manifest count', len(ids) == 761, str(len(ids)))
req('manifest unique', len(set(ids)) == 761, str(len(set(ids))))
for sid in ('gameplay.wand_creative_four_variants', 'gameplay.wand_original_root_component_nbt', 'world_migration.wand_nested_component_nbt', 'gameplay.wand_staff_attack_damage', 'visual.wand_localized_component_name', 'gameplay.wand_stack_and_rarity', 'gameplay.wand_elemental_regeneration_amount', 'gameplay.wand_primal_staff_regeneration', 'gameplay.wand_cap_special_modifiers', 'gameplay.wand_sceptre_capacity_discount', 'visual.wand_component_catalogue', 'jei.wand_component_subtype_identity', 'dedicated.wand_component_server_sync'):
    req('scenario ' + sid, sid in ids)

focused = txt('TC4_11.64.32_FOCUSED_STATIC_CI_FINAL.log')
req('focused CI', 'FOCUSED STATIC CI v11.64.32: PASS (42/42)' in focused)
req('JSON count', 'JSON resource validation: OK (2190 files)' in focused)
req('recipe denominator', '258/258 STATICALLY MAPPED' in focused)
req('manifest focused count', 'runtime manifest: PASS (761 tests; template=True)' in focused)
selftest = txt('TC4_11.64.32_JAVA17_SELF_TEST.log')
req('Java self-test', 'TC4WandComponentMathSelfTest: PASS' in selftest and 'EXIT_CODE=0' in selftest)
gradle = txt('TC4_11.64.32_GRADLE_BUILD_ATTEMPT.log')
req('Gradle command', './gradlew build --no-daemon --console=plain' in gradle)
req('Gradle honest failure', 'UnknownHostException: services.gradle.org' in gradle and 'EXIT_CODE=1' in gradle)
req('Java environment', '21.0.10' in gradle)
status = txt('TC4_11.64.32_BUILD_STATUS.txt')
req('build status honest', 'BUILD VERIFIED: NO' in status and 'RUNTIME VERIFIED: NO' in status and 'JAR CREATED: NO' in status)
req('start current', 'АКТУАЛЬНАЯ ВЕРСИЯ: 11.64.32' in txt('00_START_HERE_RU.txt'))
req('status current', '# TC4 PORT STATUS V3 — v11.64.32' in txt('TC4_PORT_STATUS_V3.md'))
req('README current', txt('README.md').startswith('# Thaumcraft Legacy Rebuild — v11.64.32'))
req('known boundary', '## 11.64.32 Wand rods/caps runtime boundary' in txt('KNOWN_DEVIATIONS.md'))
plan = txt('TC4_11.64.32_FULL_CLOSURE_STATUS_AND_PLAN_RU.md')
req('strict closed 20', '20. **v11.64.32' in plan)
req('next foci', 'Система wand foci' in plan)
report = txt('TC4_11.64.32_WAND_RODS_CAPS_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md')
for token in ('SOURCE CLOSED: YES', 'RESOURCE CLOSED: YES', 'BUILD VERIFIED: NO', 'RUNTIME VERIFIED: NO', 'JAR CREATED: NO', 'не новый gameplay-фикс'):
    req('report ' + token, token in report)

forbidden = []
for path in R.rglob('*'):
    if any(part in ('build', '.gradle', '__pycache__') for part in path.parts) or (path.is_file() and path.suffix in ('.class', '.pyc')):
        forbidden.append(str(path.relative_to(R)))
req('clean tree', not forbidden, ','.join(forbidden[:10]))

commands = [
    ('full guard', ['python3', 'tools/tc4_116432_wand_rods_caps_full_closure_guard.py']),
    ('parse guard', ['python3', 'tools/java_parse_guard_116432.py']),
    ('JSON validator', ['python3', 'tools/validate_json_resources.py']),
    ('manifest validator', ['python3', 'tools/validate_runtime_manifest.py', '--manifest', 'runtime_artifacts/runtime_test_manifest.template.json', '--version', '11.64.32', '--template']),
    ('recipe guard', ['python3', 'tools/tc4_recipe_registration_denominator_guard.py']),
    ('workbench regression', ['python3', 'tools/tc4_116431_arcane_workbench_full_closure_guard.py']),
    ('fertility regression', ['python3', 'tools/tc4_116430_fertility_lamp_full_closure_guard.py']),
    ('research regression', ['python3', 'tools/tc4_116427_research_system_full_closure_guard.py']),
]
for name, cmd in commands:
    result = subprocess.run(cmd, cwd=R, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=300)
    req(name, result.returncode == 0, result.stdout[-700:])

print(f'FINAL RECHECK v11.64.32: PASS ({len(checks)}/{len(checks)})')
