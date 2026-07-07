#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(rel):
    p = ROOT / rel
    return p.read_text(encoding='utf-8') if p.exists() else ''

item = read('src/main/java/com/darkifov/thaumcraft/block/FocusPouchItem.java')
bauble = read('src/main/java/com/darkifov/thaumcraft/block/FocusPouchBaubleItem.java')
container = read('src/main/java/com/darkifov/thaumcraft/menu/FocusPouchContainer.java')
menu = read('src/main/java/com/darkifov/thaumcraft/menu/FocusPouchMenu.java')
screen = read('src/main/java/com/darkifov/thaumcraft/client/screen/FocusPouchScreen.java')
mod = read('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
client = read('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java')
workflow = read('.github/workflows/main.yml')
guard = read('scripts/github_ci_guard.py')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')

for rel in [
    'src/main/java/com/darkifov/thaumcraft/block/FocusPouchBaubleItem.java',
    'src/main/java/com/darkifov/thaumcraft/menu/FocusPouchContainer.java',
    'src/main/java/com/darkifov/thaumcraft/menu/FocusPouchMenu.java',
    'src/main/java/com/darkifov/thaumcraft/client/screen/FocusPouchScreen.java',
    'src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/gui/gui_focuspouch.png',
]:
    if not (ROOT / rel).exists():
        errors.append(f'missing Stage186 file {rel}')

for token in [
    'TAG_INVENTORY = "Inventory"',
    'getInventory(ItemStack item)',
    'setInventory(ItemStack item, ItemStack[] stackList)',
    'migrateLegacyCounts',
    'equipNextFocusFromPouch',
    'WandManager.changeFocus adapter',
    'NetworkHooks.openScreen',
    'new FocusPouchMenu(id, inventory, pouch)'
]:
    if token not in item:
        errors.append(f'FocusPouchItem missing Stage186 token {token}')

for token in ['FocusPouchBaubleItem extends FocusPouchItem', 'getBaubleType', '"BELT"', 'canEquip', 'canUnequip']:
    if token not in bauble:
        errors.append(f'FocusPouchBaubleItem missing original bauble token {token}')

for token in [
    'SIZE = 18',
    'ItemStack[] stackList = new ItemStack[18]',
    'getMaxStackSize() == 1',
    'readInventoryList',
    'writeInventoryList',
    'var4.putByte("Slot", (byte)var3)',
    'item.getOrCreateTag().put(FocusPouchItem.TAG_INVENTORY, var2)',
    'stack.getItem() instanceof WandFocusItem'
]:
    if token not in container:
        errors.append(f'FocusPouchContainer missing Stage186 inventory token {token}')

for token in [
    '37 + a % 6 * 18, 51 + a / 6 * 18',
    '8 + j * 18, 151 + i * 18',
    '8 + i * 18, 209',
    'blockSlot = inventory.selected + 45',
    'slot == blockSlot',
    'SlotLimitedByClass(ItemFocusBasic.class)'
]:
    if token not in menu:
        errors.append(f'FocusPouchMenu missing original slot-layout token {token}')

for token in [
    'textures/original/thaumcraft4/gui/gui_focuspouch.png',
    'imageWidth = 175',
    'imageHeight = 232',
    '8 + blockSlot * 18',
    '209',
    '240, 0, 16, 16'
]:
    if token not in screen:
        errors.append(f'FocusPouchScreen missing GuiFocusPouch token {token}')

if 'new FocusPouchBaubleItem' not in mod or 'FOCUS_POUCH_MENU' not in mod:
    errors.append('ThaumcraftMod missing FocusPouchBaubleItem registration or menu')
if 'MenuScreens.register(ThaumcraftMod.FOCUS_POUCH_MENU.get(), FocusPouchScreen::new)' not in client:
    errors.append('ClientModEvents missing FocusPouchScreen registration')

for token in ['tc4_stage186_focus_pouch_gui_audit.py', 'python scripts/tc4_stage186_focus_pouch_gui_audit.py', 'thaumcraft-legacy-rebuild-stage194-jars']:
    if token not in workflow or token not in guard:
        errors.append(f'workflow/guard missing Stage186 token {token}')

if "version = '1.94.0'" not in build or 'version="1.94.0"' not in mods:
    errors.append('project version must be 1.94.0')

if errors:
    for e in errors:
        print('::error::' + e)
    sys.exit(1)
print('Stage186 focus pouch GUI audit: OK')
