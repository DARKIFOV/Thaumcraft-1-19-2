#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
read = lambda p: (ROOT / p).read_text(encoding='utf-8') if (ROOT / p).exists() else ''

files = {
    'manager': read('src/main/java/com/darkifov/thaumcraft/wand/WandManagerRuntime.java'),
    'packet': read('src/main/java/com/darkifov/thaumcraft/network/RequestFocusChangePacket.java'),
    'network': read('src/main/java/com/darkifov/thaumcraft/network/ThaumcraftNetwork.java'),
    'keys': read('src/main/java/com/darkifov/thaumcraft/client/ClientWandArchitectKeybinds.java'),
    'events': read('src/main/java/com/darkifov/thaumcraft/client/ClientWandArchitectEvents.java'),
    'pouch': read('src/main/java/com/darkifov/thaumcraft/block/FocusPouchItem.java'),
    'build': read('build.gradle'),
    'mods': read('src/main/resources/META-INF/mods.toml'),
    'workflow': read('.github/workflows/main.yml'),
    'guard': read('scripts/github_ci_guard.py'),
}

checks = {
    'manager_runtime_exists': bool(files['manager']),
    'original_wandmanager_source_named': 'WandManager.changeFocus' in files['manager'],
    'packet_focus_change_exists': 'RequestFocusChangePacket' in files['packet'] and 'PacketFocusChangeToServer' in files['packet'],
    'packet_sceptre_guard': 'WandComponentData.isSceptre' in files['packet'],
    'network_registration': 'RequestFocusChangePacket.class' in files['network'] and 'requestFocusChangeFromClient' in files['network'],
    'key_f_registered': 'KEY_CHANGE_WAND_FOCUS' in files['keys'] and 'GLFW.GLFW_KEY_F' in files['keys'],
    'shift_f_remove_semantics': 'WandManagerRuntime.REMOVE' in files['events'] and 'isShiftKeyDown' in files['events'],
    'tree_map_higher_wrap': all(token in files['manager'] for token in ['TreeMap', 'ceilingEntry', 'higherEntry', 'firstEntry']),
    'scan_order_adapter': all(token in files['manager'] for token in ['Baubles', 'off-hand pouch', 'inventory pouches', 'pouchcount*1000']),
    'pouch_exact_stack_helpers': all(token in files['pouch'] for token in ['addExactFocusStack', 'removeFocusAt', 'sortingHelper']),
    'camera_tick_sound': 'cameraticks' in files['manager'] and '0.3F' in files['manager'] and '0.9F' in files['manager'] and '1.0F' in files['manager'],
    'version_188': ("version = '2.04.0'" in files['build'] or "version = '1.98.0'" in files['build'] or "version = '2.00.0'" in files['build']) and ('version="2.04.0"' in files['mods'] or 'version="1.98.0"' in files['mods'] or 'version="2.00.0"' in files['mods']),
    'workflow_and_guard': 'tc4_stage188_focus_selection_packet_audit.py' in files['workflow'] and 'tc4_stage188_focus_selection_packet_audit.py' in files['guard'],
    'stage188_artifact': 'thaumcraft-legacy-rebuild-stage204-jars' in files['workflow'] and 'thaumcraft-legacy-rebuild-stage204-jars' in files['guard'],
}

errors = [name for name, ok in checks.items() if not ok]
if errors:
    for error in errors:
        print(f'::error::Stage190 focus selection packet audit failed: {error}')
    sys.exit(1)
print('Stage190 focus selection packet audit: OK')
