#!/usr/bin/env python3
from pathlib import Path
import hashlib, json, re, zipfile
R=Path(__file__).resolve().parents[1]
def text(p): return (R/p).read_text(encoding='utf-8',errors='replace')
def req(ok,msg):
    if not ok: raise SystemExit('TC4 v11.64.33 wand foci full-closure guard: FAIL: '+msg)
def ver(p):
    m=re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']',text(p)); req(m,'version '+p); return tuple(map(int,m.groups()))
def sha(b): return hashlib.sha256(b).digest()
req(ver('build.gradle')>=(11,64,33),'build version')
req(ver('src/main/resources/META-INF/mods.toml')>=(11,64,33),'mods version')
contract=text('src/main/java/com/darkifov/thaumcraft/wand/TC4WandFocusContract.java')
for token in ('CONTRACT_VERSION = "11.64.33"','FOCUS_STACK_NBT = "focus"','LEGACY_FOCUS_ID_NBT = "Focus"','POUCH_INVENTORY_NBT = "Inventory"','POUCH_SLOT_NBT = "Slot"','UPGRADE_LIST_NBT = "upgrade"','REMOVE_SENTINEL = "REMOVE"','ORIGINAL_FOCUS_TYPES = 10','FOCUS_POUCH_SLOTS = 18','FOCUS_UPGRADE_RANKS = 5','radialRadius','radialSliceDegrees','radialAngleDegrees'):
    req(token in contract,'contract '+token)
focus=text('src/main/java/com/darkifov/thaumcraft/wand/WandFocusRuntime.java')
for token in ('TAG_FOCUS_STACK = "focus"','LEGACY_TAG_FOCUS_ID = "Focus"','tag.put(TAG_FOCUS_STACK, copy.save(new CompoundTag()))','tag.remove(LEGACY_TAG_FOCUS_ID)','ItemStack.of(tag.getCompound(TAG_FOCUS_STACK))','case FIRE ->','case FROST ->','case SHOCK ->','case EXCAVATION ->','case PORTABLE_HOLE ->','case EQUAL_TRADE ->','case WARDING ->','case HELLBAT ->','case PECH_CURSE ->','case PRIMAL ->'):
    req(token in focus,'focus runtime '+token)
req('putString(TAG_FOCUS' not in focus,'upper-case id must not be written')
wand=text('src/main/java/com/darkifov/thaumcraft/block/WandItem.java')
for forbidden in ('equipNextFocusFromPouch','offhand.getItem() instanceof WandFocusItem','Removed " + oldFocus','Equipped " + focusItem'):
    req(forbidden not in wand,'right-click bypass '+forbidden)
pouch=text('src/main/java/com/darkifov/thaumcraft/block/FocusPouchItem.java')
for token in ('TAG_INVENTORY = "Inventory"','LEGACY_TAG_SELECTED = "SelectedFocus"','MAX_FOCI = 18','FocusPouchContainer.readInventoryList','FocusPouchContainer.writeInventoryList','Component.translatable("container.focuspouch")'):
    req(token in pouch,'pouch '+token)
for forbidden in ('equipNextFocusFromPouch','public static WandFocusType selected','appendHoverText'):
    req(forbidden not in pouch,'pouch invention '+forbidden)
manager=text('src/main/java/com/darkifov/thaumcraft/wand/WandManagerRuntime.java')
for token in ('REMOVE = TC4WandFocusContract.REMOVE_SENTINEL','availableFoci(Player player)','TreeMap<String, FocusLocation>','higherEntry(key)','FocusPouchItem.putFocusAt','TC4WandFocusContract.CAMERA_TICKS_VOLUME'):
    req(token in manager,'manager '+token)
packet=text('src/main/java/com/darkifov/thaumcraft/network/RequestFocusChangePacket.java')
req('player.getMainHandItem()' in packet and 'player.getOffhandItem()' not in packet,'packet main-hand only')
radial=text('src/main/java/com/darkifov/thaumcraft/client/ClientFocusRadialEvents.java')
for token in ('textures/misc/radial.png','textures/misc/radial2.png','KEY_CHANGE_WAND_FOCUS.isDown()','requestFocusChangeFromClient(WandManagerRuntime.REMOVE)','WandManagerRuntime.availableFoci','mouseHandler.releaseMouse()','mouseHandler.grabMouse()','TC4WandFocusContract.radialRadius','TC4WandFocusContract.radialSliceDegrees'):
    req(token in radial,'radial '+token)
architect=text('src/main/java/com/darkifov/thaumcraft/client/ClientWandArchitectEvents.java')
req('KEY_CHANGE_WAND_FOCUS.consumeClick()' not in architect,'old F cycle removed')
upgrade=text('src/main/java/com/darkifov/thaumcraft/wand/FocusUpgradeRuntime.java')
for token in ('TAG_UPGRADE = "upgrade"','MAX_RANK = 5','entry.putShort("id"','originalSortingHelper'):
    req(token in upgrade,'upgrade '+token)
parity=text('src/main/java/com/darkifov/thaumcraft/wand/TC4WandFociFullClosureParity.java')
for token in ('cataloguesMatchOriginal','nbtKeysMatchOriginal','radialGeometryMatchesOriginal','soundContractMatchesOriginal'):
    req(token in parity,'parity '+token)
for lang in ('en_us.json','ru_ru.json'):
    j=json.loads(text('src/main/resources/assets/thaumcraft/lang/'+lang))
    req('key.thaumcraft.change_wand_focus' in j,'focus key '+lang)
    req('container.focuspouch' in j,'pouch title '+lang)
source_zip=R/'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip'; prefix='Thaumcraft4-1.7.10-master/'
with zipfile.ZipFile(source_zip) as z:
    originals={
      'thaumcraft/common/items/wands/ItemWandCasting.java':('func_74775_l("focus")','func_82580_o("focus")','func_77983_a("focus"'),
      'thaumcraft/common/items/wands/WandManager.java':('TreeMap','higherKey','thaumcraft:cameraticks", 0.3F, 0.9F','thaumcraft:cameraticks", 0.3F, 1.0F'),
      'thaumcraft/common/items/wands/ItemFocusPouch.java':('new ItemStack[18]','func_150295_c("Inventory", 10)','func_74774_a("Slot", (byte)var3)'),
      'thaumcraft/api/wands/ItemFocusBasic.java':('func_150295_c("upgrade", 10)','short[] l = { -1, -1, -1, -1, -1 }'),
      'thaumcraft/common/lib/events/KeyHandler.java':('radialActive = true','PacketFocusChangeToServer(player, "REMOVE")'),
      'thaumcraft/client/lib/REHWandHandler.java':('textures/misc/radial.png','textures/misc/radial2.png','16.0F + this.fociItem.size() * 2.5F','360.0F / this.fociItem.size()')}
    for rel,tokens in originals.items():
        src=z.read(prefix+rel).decode('utf-8',errors='replace')
        for token in tokens: req(token in src,'original '+rel+' '+token)
    item_prefix=prefix+'assets/thaumcraft/textures/items/'
    original_focus=[n for n in z.namelist() if n.startswith(item_prefix) and Path(n).name.startswith('focus') and (n.endswith('.png') or n.endswith('.mcmeta'))]
    req(len(original_focus)==30,f'original focus resources {len(original_focus)}')
    for n in original_focus:
        name=Path(n).name
        candidates=[R/'src/main/resources/assets/thaumcraft/textures/item/tc4'/name,R/'src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/items'/name]
        req(any(p.is_file() and sha(p.read_bytes())==sha(z.read(n)) for p in candidates),'resource hash '+name)
    for rel,current in [('assets/thaumcraft/textures/misc/radial.png','src/main/resources/assets/thaumcraft/textures/misc/radial.png'),('assets/thaumcraft/textures/misc/radial2.png','src/main/resources/assets/thaumcraft/textures/misc/radial2.png'),('assets/thaumcraft/textures/gui/gui_focuspouch.png','src/main/resources/assets/thaumcraft/textures/gui/gui_focuspouch.png')]:
        req(sha((R/current).read_bytes())==sha(z.read(prefix+rel)),'resource hash '+rel)
gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=259 and len(methods)==len(set(methods)),f'GameTests {len(methods)}')
for name in ('wandFociCatalogueAndKeysMatchTc4','wandFocusCanonicalStackNbtRoundTripMatchesTc4','wandFocusLegacyIdMigratesOnce','focusPouchInventorySlotNbtMatchesTc4','wandFocusUpgradeListMatchesTc4','wandFocusRadialGeometryMatchesTc4'):
    req(name in methods,'GameTest '+name)
man=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json')); ids=[x['id'] for x in man['tests']]
req(tuple(map(int,man['version'].split('.')))>=(11,64,33),'manifest version'); req(len(ids)>=775 and len(ids)==len(set(ids)),f'manifest {len(ids)}')
for sid in ('gameplay.wand_focus_canonical_stack_nbt','world_migration.wand_focus_uppercase_id','gameplay.focus_pouch_inventory_slot_nbt','client.wand_focus_hold_f_radial','client.wand_focus_release_selection','client.wand_focus_shift_f_remove','gameplay.wand_focus_main_hand_only','gameplay.wand_focus_no_right_click_bypass','multiplayer.wand_focus_server_authority'):
    req(sid in ids,'scenario '+sid)
for p in ('TC4_11.64.33_WAND_FOCI_SOURCE_EVIDENCE.json','tools/data/tc4_wand_foci_full_source_evidence_v11.64.33.json'):
    e=json.loads(text(p)); req(e.get('version')=='11.64.33' and len(e.get('original_sources',[]))==18,'evidence '+p)
prompt=R/'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md'
req(prompt.is_file(),'mandatory prompt missing')
req(prompt.read_bytes()==(R/'PROMPT_FOR_FUTURE_CHAT_RU.md').read_bytes(),'prompt copies differ')
req('Файл `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md` обязателен' in prompt.read_text(encoding='utf-8'),'prompt wording')
print('TC4 v11.64.33 wand foci full-closure guard: PASS')
