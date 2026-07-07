#!/usr/bin/env python3
from __future__ import annotations
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(rel: str) -> str:
    p = ROOT / rel
    return p.read_text(encoding='utf-8') if p.exists() else ''

menu = read('src/main/java/com/darkifov/thaumcraft/menu/GolemMenu.java')
screen = read('src/main/java/com/darkifov/thaumcraft/client/screen/GolemScreen.java')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')
report = json.loads(read('STAGE203_GOLEM_GHOST_SLOT_REPORT.json') or '{}')

checks = {
    'version_stage203': "version = '2.04.0'" in build and 'version="2.04.0"' in mods,
    'report_stage_203': report.get('stage') == 203 and report.get('version') == '2.04.0',
    'container_ghost_slots_adapter': 'ContainerGhostSlots parity adapter' in menu and 'GolemVisibleGhostSlot' in menu,
    'slotghost_pickup_blocked': 'public boolean mayPickup(Player player)' in menu and 'return false;' in menu,
    'slotghost_fluid_adapter': 'ForgeCapabilities.FLUID_HANDLER_ITEM' in menu and 'originalLiquidGhost' in menu,
    'fill_core_limit_256': 'GolemCoreType.FILL ? 256 : 1' in menu,
    'clicked_intercepts_ghost': 'public void clicked(int slotId, int button, ClickType clickType, Player player)' in menu and 'applyGhostClick' in menu,
    'ghost_copy_semantics': 'copyToFirstGhostSlot' in menu and 'never consume' in menu,
    'shift_left_clear_shift_right_16': 'ClickType.QUICK_MOVE' in menu and '+ 16' in menu,
    'color_button_semantics_preserved': 'cycleGolemColor(button, false)' in menu and 'cycleGolemColor(button - slots, true)' in menu,
    'gui_original_texture_still_used': 'guigolem.png' in screen,
}

errors = [name for name, ok in checks.items() if not ok]
if errors:
    for name in errors:
        print(f'::error::Stage203 audit failed: {name}')
    sys.exit(1)
print('Stage203 golem ghost-slot parity audit: OK')
