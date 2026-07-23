#!/usr/bin/env python3
from pathlib import Path
import hashlib, json, re, subprocess, zipfile

R = Path(__file__).resolve().parents[1]
checks = []

def req(name, ok, detail=''):
    checks.append((name, bool(ok), detail))
    if not ok:
        raise SystemExit(f'FINAL RECHECK v11.64.33: FAIL: {name}: {detail}')

def txt(path):
    return (R / path).read_text(encoding='utf-8', errors='replace')

# Version and mandatory evidence files.
req('build version', "version = '11.64.33'" in txt('build.gradle'))
req('mods version', 'version="11.64.33"' in txt('src/main/resources/META-INF/mods.toml'))
required = (
    'TC4_11.64.33_WAND_FOCI_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md',
    'TC4_11.64.33_FULL_CLOSURE_STATUS_AND_PLAN_RU.md',
    'TC4_11.64.33_WAND_FOCI_SOURCE_EVIDENCE.json',
    'tools/data/tc4_wand_foci_full_source_evidence_v11.64.33.json',
    'TC4_11.64.33_FOCUSED_STATIC_CI_FINAL.log',
    'TC4_11.64.33_JAVA17_SELF_TEST.log',
    'TC4_11.64.33_GRADLE_BUILD_ATTEMPT.log',
    'TC4_11.64.33_BUILD_STATUS.txt',
    'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md',
)
for path in required:
    req('file ' + path, (R / path).is_file())
req('prompt identical', (R/'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md').read_bytes() == (R/'PROMPT_FOR_FUTURE_CHAT_RU.md').read_bytes())
req('prompt mandatory wording', 'Файл `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md` обязателен' in txt('UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md'))

# Dependency-free contract.
contract = txt('src/main/java/com/darkifov/thaumcraft/wand/TC4WandFocusContract.java')
for token in (
    'CONTRACT_VERSION = "11.64.33"', 'FOCUS_STACK_NBT = "focus"',
    'LEGACY_FOCUS_ID_NBT = "Focus"', 'POUCH_INVENTORY_NBT = "Inventory"',
    'POUCH_SLOT_NBT = "Slot"', 'UPGRADE_LIST_NBT = "upgrade"',
    'REMOVE_SENTINEL = "REMOVE"', 'ORIGINAL_FOCUS_TYPES = 10',
    'FOCUS_POUCH_SLOTS = 18', 'FOCUS_UPGRADE_RANKS = 5',
    'REMOVE_SOUND_PITCH = 0.9F', 'CHANGE_SOUND_PITCH = 1.0F',
    'CAMERA_TICKS_VOLUME = 0.3F', 'radialRadius', 'radialSliceDegrees',
    'radialAngleDegrees', 'clamp01'):
    req('contract ' + token, token in contract)

# Canonical installed-focus stack and one-shot legacy migration.
focus = txt('src/main/java/com/darkifov/thaumcraft/wand/WandFocusRuntime.java')
for token in (
    'TAG_FOCUS_STACK = "focus"', 'LEGACY_TAG_FOCUS_ID = "Focus"',
    'ItemStack.of(tag.getCompound(TAG_FOCUS_STACK))',
    'tag.put(TAG_FOCUS_STACK, copy.save(new CompoundTag()))',
    'tag.remove(LEGACY_TAG_FOCUS_ID)', 'case FIRE ->', 'case FROST ->',
    'case SHOCK ->', 'case EXCAVATION ->', 'case PORTABLE_HOLE ->',
    'case EQUAL_TRADE ->', 'case WARDING ->', 'case HELLBAT ->',
    'case PECH_CURSE ->', 'case PRIMAL ->'):
    req('focus runtime ' + token, token in focus)
req('no upper-case focus write', 'putString(TAG_FOCUS' not in focus)

# No right-click bypass; pouch is only the original 18-slot inventory.
wand = txt('src/main/java/com/darkifov/thaumcraft/block/WandItem.java')
for forbidden in ('equipNextFocusFromPouch', 'offhand.getItem() instanceof WandFocusItem', 'Removed " + oldFocus', 'Equipped " + focusItem'):
    req('right-click bypass removed ' + forbidden, forbidden not in wand)
pouch = txt('src/main/java/com/darkifov/thaumcraft/block/FocusPouchItem.java')
for token in (
    'TAG_INVENTORY = "Inventory"', 'LEGACY_TAG_SELECTED = "SelectedFocus"',
    'MAX_FOCI = 18', 'FocusPouchContainer.readInventoryList',
    'FocusPouchContainer.writeInventoryList', 'Component.translatable("container.focuspouch")'):
    req('pouch ' + token, token in pouch)
for forbidden in ('equipNextFocusFromPouch', 'public static WandFocusType selected', 'appendHoverText'):
    req('pouch invention removed ' + forbidden, forbidden not in pouch)

# Server-authoritative main-hand switching and client radial.
manager = txt('src/main/java/com/darkifov/thaumcraft/wand/WandManagerRuntime.java')
for token in (
    'REMOVE = TC4WandFocusContract.REMOVE_SENTINEL', 'availableFoci(Player player)',
    'TreeMap<String, FocusLocation>', 'higherEntry(key)', 'FocusPouchItem.putFocusAt',
    'TC4WandFocusContract.CAMERA_TICKS_VOLUME'):
    req('manager ' + token, token in manager)
packet = txt('src/main/java/com/darkifov/thaumcraft/network/RequestFocusChangePacket.java')
req('packet main-hand', 'player.getMainHandItem()' in packet)
req('packet no off-hand', 'player.getOffhandItem()' not in packet)
radial = txt('src/main/java/com/darkifov/thaumcraft/client/ClientFocusRadialEvents.java')
for token in (
    'textures/misc/radial.png', 'textures/misc/radial2.png',
    'KEY_CHANGE_WAND_FOCUS.isDown()',
    'requestFocusChangeFromClient(WandManagerRuntime.REMOVE)',
    'WandManagerRuntime.availableFoci', 'mouseHandler.releaseMouse()',
    'mouseHandler.grabMouse()', 'TC4WandFocusContract.radialRadius',
    'TC4WandFocusContract.radialSliceDegrees'):
    req('radial ' + token, token in radial)
req('old F click cycle removed', 'KEY_CHANGE_WAND_FOCUS.consumeClick()' not in txt('src/main/java/com/darkifov/thaumcraft/client/ClientWandArchitectEvents.java'))

# Upgrade list and aggregate production contract.
upgrade = txt('src/main/java/com/darkifov/thaumcraft/wand/FocusUpgradeRuntime.java')
for token in ('TAG_UPGRADE = "upgrade"', 'MAX_RANK = 5', 'entry.putShort("id"', 'originalSortingHelper'):
    req('upgrade ' + token, token in upgrade)
parity = txt('src/main/java/com/darkifov/thaumcraft/wand/TC4WandFociFullClosureParity.java')
for token in ('cataloguesMatchOriginal', 'nbtKeysMatchOriginal', 'radialGeometryMatchesOriginal', 'soundContractMatchesOriginal'):
    req('parity ' + token, token in parity)

# Localization.
for lang in ('en_us.json', 'ru_ru.json'):
    values = json.loads(txt('src/main/resources/assets/thaumcraft/lang/' + lang))
    req('focus key ' + lang, 'key.thaumcraft.change_wand_focus' in values)
    req('pouch title ' + lang, 'container.focuspouch' in values)

# Original source tokens and exact original resources.
source_zip = R/'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip'
prefix = 'Thaumcraft4-1.7.10-master/'
with zipfile.ZipFile(source_zip) as zf:
    originals = {
        'thaumcraft/common/items/wands/ItemWandCasting.java': ('func_74775_l("focus")', 'func_82580_o("focus")', 'func_77983_a("focus"'),
        'thaumcraft/common/items/wands/WandManager.java': ('TreeMap', 'higherKey', 'thaumcraft:cameraticks", 0.3F, 0.9F', 'thaumcraft:cameraticks", 0.3F, 1.0F'),
        'thaumcraft/common/items/wands/ItemFocusPouch.java': ('new ItemStack[18]', 'func_150295_c("Inventory", 10)', 'func_74774_a("Slot", (byte)var3)'),
        'thaumcraft/api/wands/ItemFocusBasic.java': ('func_150295_c("upgrade", 10)', 'short[] l = { -1, -1, -1, -1, -1 }'),
        'thaumcraft/common/lib/events/KeyHandler.java': ('radialActive = true', 'PacketFocusChangeToServer(player, "REMOVE")'),
        'thaumcraft/client/lib/REHWandHandler.java': ('textures/misc/radial.png', 'textures/misc/radial2.png', '16.0F + this.fociItem.size() * 2.5F', '360.0F / this.fociItem.size()'),
    }
    for rel, tokens in originals.items():
        src = zf.read(prefix + rel).decode('utf-8', errors='replace')
        for token in tokens:
            req('original ' + rel + ' ' + token, token in src)
    item_prefix = prefix + 'assets/thaumcraft/textures/items/'
    original_focus = [n for n in zf.namelist() if n.startswith(item_prefix) and Path(n).name.startswith('focus') and (n.endswith('.png') or n.endswith('.mcmeta'))]
    req('original focus resource count', len(original_focus) == 30, str(len(original_focus)))
    for n in original_focus:
        name = Path(n).name
        candidates = [R/'src/main/resources/assets/thaumcraft/textures/item/tc4'/name, R/'src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/items'/name]
        expected = hashlib.sha256(zf.read(n)).digest()
        req('focus resource hash ' + name, any(p.is_file() and hashlib.sha256(p.read_bytes()).digest() == expected for p in candidates))
    for rel, current in (
        ('assets/thaumcraft/textures/misc/radial.png', 'src/main/resources/assets/thaumcraft/textures/misc/radial.png'),
        ('assets/thaumcraft/textures/misc/radial2.png', 'src/main/resources/assets/thaumcraft/textures/misc/radial2.png'),
        ('assets/thaumcraft/textures/gui/gui_focuspouch.png', 'src/main/resources/assets/thaumcraft/textures/gui/gui_focuspouch.png')):
        req('resource hash ' + rel, hashlib.sha256((R/current).read_bytes()).digest() == hashlib.sha256(zf.read(prefix+rel)).digest())

# Evidence matrices.
for path in ('TC4_11.64.33_WAND_FOCI_SOURCE_EVIDENCE.json', 'tools/data/tc4_wand_foci_full_source_evidence_v11.64.33.json'):
    evidence = json.loads(txt(path))
    req('evidence version ' + path, evidence.get('version') == '11.64.33')
    req('evidence original count ' + path, len(evidence.get('original_sources', [])) == 18)
    req('evidence production count ' + path, len(evidence.get('production_contracts', [])) == 12)

# GameTests and runtime manifest are unique and cumulative.
gt = txt('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods = re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(', gt, re.S)
req('GameTest count', len(methods) == 259, str(len(methods)))
req('GameTest unique', len(set(methods)) == 259, str(len(set(methods))))
for name in ('wandFociCatalogueAndKeysMatchTc4', 'wandFocusCanonicalStackNbtRoundTripMatchesTc4', 'wandFocusLegacyIdMigratesOnce', 'focusPouchInventorySlotNbtMatchesTc4', 'wandFocusUpgradeListMatchesTc4', 'wandFocusRadialGeometryMatchesTc4'):
    req('GameTest ' + name, name in methods)
manifest = json.loads(txt('runtime_artifacts/runtime_test_manifest.template.json'))
ids = [x['id'] for x in manifest['tests']]
req('manifest version', manifest.get('version') == '11.64.33', str(manifest.get('version')))
req('manifest count', len(ids) == 775, str(len(ids)))
req('manifest unique', len(set(ids)) == 775, str(len(set(ids))))
for sid in ('gameplay.wand_focus_canonical_stack_nbt', 'world_migration.wand_focus_uppercase_id', 'gameplay.focus_pouch_inventory_slot_nbt', 'client.wand_focus_hold_f_radial', 'client.wand_focus_release_selection', 'client.wand_focus_shift_f_remove', 'gameplay.wand_focus_main_hand_only', 'gameplay.wand_focus_no_right_click_bypass', 'multiplayer.wand_focus_server_authority'):
    req('scenario ' + sid, sid in ids)

# Logs and honest status.
focused = txt('TC4_11.64.33_FOCUSED_STATIC_CI_FINAL.log')
req('focused CI', 'FOCUSED STATIC CI v11.64.33: PASS (44/44)' in focused)
req('JSON count', 'JSON resource validation: OK (2190 files)' in focused)
req('recipe denominator', '258/258 STATICALLY MAPPED' in focused)
req('manifest focused count', 'runtime manifest: PASS (775 tests; template=True)' in focused)
selftest = txt('TC4_11.64.33_JAVA17_SELF_TEST.log')
req('Java self-test output', 'TC4WandFocusContractSelfTest: PASS' in selftest)
req('Java self-test javac', 'JAVAC_EXIT_CODE=0' in selftest)
req('Java self-test java', 'JAVA_EXIT_CODE=0' in selftest)
gradle = txt('TC4_11.64.33_GRADLE_BUILD_ATTEMPT.log')
req('Gradle command', './gradlew build --no-daemon --console=plain' in gradle)
req('Gradle honest failure', 'UnknownHostException: services.gradle.org' in gradle and 'EXIT_CODE=1' in gradle)
req('Java environment', '21.0.10' in gradle)
status = txt('TC4_11.64.33_BUILD_STATUS.txt')
for token in ('SOURCE CLOSED: YES', 'RESOURCE CLOSED: YES', 'BUILD VERIFIED: NO', 'RUNTIME VERIFIED: NO', 'JAR CREATED: NO', 'EXIT_CODE=1'):
    req('build status ' + token, token in status)

# Current documentation and next single object.
req('start current', 'АКТУАЛЬНАЯ ВЕРСИЯ: 11.64.33' in txt('00_START_HERE_RU.txt'))
req('status current', '# TC4 PORT STATUS V3 — v11.64.33' in txt('TC4_PORT_STATUS_V3.md'))
req('README current', txt('README.md').startswith('# Thaumcraft Legacy Rebuild — v11.64.33'))
req('known boundary', '## 11.64.33 Wand foci runtime boundary' in txt('KNOWN_DEVIATIONS.md'))
plan = txt('TC4_11.64.33_FULL_CLOSURE_STATUS_AND_PLAN_RU.md')
req('strict closed 21', '21. **v11.64.33' in plan)
req('next infusion', '**Infusion Matrix**' in plan)
report = txt('TC4_11.64.33_WAND_FOCI_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md')
for token in ('SOURCE CLOSED: YES', 'RESOURCE CLOSED: YES', 'BUILD VERIFIED: NO', 'RUNTIME VERIFIED: NO', 'JAR CREATED: NO', 'UnknownHostException: services.gradle.org', 'exit code `1`'):
    req('report ' + token, token in report)

# No generated/cache artifacts in the source tree.
forbidden = []
for path in R.rglob('*'):
    rel = path.relative_to(R)
    if any(part in ('build', '.gradle', '__pycache__') for part in rel.parts) or (path.is_file() and path.suffix in ('.class', '.pyc')):
        forbidden.append(str(rel))
req('clean tree', not forbidden, ','.join(forbidden[:10]))

# Re-run the most load-bearing validators.
commands = [
    ('full guard', ['python3', 'tools/tc4_116433_wand_foci_full_closure_guard.py']),
    ('parse guard', ['python3', 'tools/java_parse_guard_116433.py']),
    ('JSON validator', ['python3', 'tools/validate_json_resources.py']),
    ('manifest validator', ['python3', 'tools/validate_runtime_manifest.py', '--manifest', 'runtime_artifacts/runtime_test_manifest.template.json', '--version', '11.64.33', '--template']),
    ('recipe guard', ['python3', 'tools/tc4_recipe_registration_denominator_guard.py']),
    ('rods/caps regression', ['python3', 'tools/tc4_116432_wand_rods_caps_full_closure_guard.py']),
    ('workbench regression', ['python3', 'tools/tc4_116431_arcane_workbench_full_closure_guard.py']),
    ('research regression', ['python3', 'tools/tc4_116427_research_system_full_closure_guard.py']),
]
for name, cmd in commands:
    result = subprocess.run(cmd, cwd=R, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=300)
    req(name, result.returncode == 0, result.stdout[-900:])

print(f'FINAL RECHECK v11.64.33: PASS ({len(checks)}/{len(checks)})')
