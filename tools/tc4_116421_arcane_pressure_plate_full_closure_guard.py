#!/usr/bin/env python3
"""v11.64.21 guard: complete Arcane Pressure Plate + integrated key access closure."""
from pathlib import Path
import hashlib, json, re, zipfile
R=Path(__file__).resolve().parents[1]
def text(p): return (R/p).read_text(encoding='utf-8')
def req(ok,msg):
    if not ok: raise SystemExit('TC4 v11.64.21 Arcane Pressure Plate full-closure guard: FAIL: '+msg)
def ver(s):
    m=re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']',s); req(m,'version parse'); return tuple(map(int,m.groups()))
def sha(p): return hashlib.sha256((R/p).read_bytes()).hexdigest()

req(ver(text('build.gradle')) >= (11,64,21),'build version')
req(ver(text('src/main/resources/META-INF/mods.toml')) >= (11,64,21),'mods version')

c=text('src/main/java/com/darkifov/thaumcraft/block/TC4ArcanePressurePlateParity.java')
for t in ('CONTRACT_VERSION = "11.64.21"','WARDED_HARDNESS = -1.0F','UNWARDED_HARDNESS = 2.0F',
 'EXPLOSION_RESISTANCE = 999.0F','CHECK_INTERVAL_TICKS = 20','SCAN_MIN_XZ = 0.125D',
 'SCAN_MAX_XZ = 0.875D','SCAN_MAX_Y = 0.25D','OUTLINE_MIN_XZ = 0.0625D',
 'OUTLINE_MAX_XZ = 0.9375D','OUTLINE_UNPRESSED_HEIGHT = 0.0625D',
 'OUTLINE_PRESSED_HEIGHT = 0.03125D','KEY_TARGET_DOOR = 0','KEY_TARGET_PLATE = 1',
 'shouldTrigger','mayBindKey','legacyLocation','tooltipLocation'):
    req(t in c,'parity token '+t)

b=text('src/main/java/com/darkifov/thaumcraft/block/ArcanePressurePlateBlock.java')
for t in ('TC4ArcanePressurePlateParity.OUTLINE_MIN_XZ','TC4ArcanePressurePlateParity.SCAN_MIN_XZ',
 'plate::shouldTrigger','TC4ArcanePressurePlateParity.CHECK_INTERVAL_TICKS',
 'removeWithOwnerWand','state.getValue(POWERED)','Block.popResource',
 'TC4ArcanePressurePlateParity.weakSignal','TC4ArcanePressurePlateParity.strongSignal',
 'return Shapes.empty()','canDropFromExplosion','onBlockExploded'):
    req(t in b,'block production binding '+t)
req('canSurvive(' not in b and 'Direction.DOWN && !canSurvive' not in b,'non-original support survival path remains')

be=text('src/main/java/com/darkifov/thaumcraft/blockentity/ArcanePressurePlateBlockEntity.java')
for t in ('LEGACY_OWNER_TAG = "owner"','LEGACY_ACCESS_TAG = "access"','LEGACY_ACCESS_NAME_TAG = "name"',
 'keyTargetType()','KEY_TARGET_PLATE','TC4ArcanePressurePlateParity.mayBindKey',
 'TC4ArcanePressurePlateParity.nextSetting','TC4ArcanePressurePlateParity.shouldTrigger',
 'tag.putString(LEGACY_OWNER_TAG, ownerName)','tag.put(LEGACY_ACCESS_TAG, legacy)',
 '"0" + name','"1" + name','tag.getList(LEGACY_ACCESS_TAG, Tag.TAG_COMPOUND)'):
    req(t in be,'block entity token '+t)

key=text('src/main/java/com/darkifov/thaumcraft/item/ArcaneKeyItem.java')
for t in ('TAG_LOCATION = "location"','TAG_TYPE = "type"','bind(bound, target.keyBindingPos(), target.keyTargetType())',
 'hasBindingContainer(held)','return stack.getTag() != null','deliverBoundCopy(player, held, bound)','if (!player.addItem(bound)) player.drop(bound, false)',
 'if (!player.getAbilities().instabuild) held.shrink(1)','TC4ArcanePressurePlateParity.legacyLocation',
 'TC4ArcanePressurePlateParity.locationMatches','TC4ArcanePressurePlateParity.tooltipLocation',
 '"tc.key1"','"tc.key2"','"tc.key7"','"tc.key8"','"tc.key9"','"tc.key10"','"tc.key11"'):
    req(t in key,'key production token '+t)
req('TAG_DIMENSION' not in key and 'level.dimension()' not in key,'non-original dimension binding remains')
req('player.setItemInHand' not in key,'single blank key is still replaced in hand')
req('tooltip.thaumcraft.arcane_key.blank' not in key,'non-original blank tooltip remains')

wand=text('src/main/java/com/darkifov/thaumcraft/block/WandItem.java')
for t in ('state.getBlock() instanceof ArcanePressurePlateBlock','!state.getValue(ArcanePressurePlateBlock.POWERED)',
 'ArcanePressurePlateBlock.removeWithOwnerWand(level, pos, player)'):
    req(t in wand,'wand removal wiring '+t)

mod=text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
for t in ('TC4ArcanePressurePlateParity.WARDED_HARDNESS','TC4ArcanePressurePlateParity.EXPLOSION_RESISTANCE',
 '.sound(SoundType.WOOD).noOcclusion().noCollission().noLootTable()',
 'new BlockItem(ARCANE_PRESSURE_PLATE.get(), new Item.Properties().tab(THAUMCRAFT_TAB))'):
    req(t in mod,'registration '+t)
req('ARCANE_PRESSURE_PLATE.get(), new Item.Properties().tab(THAUMCRAFT_TAB)\n                    .rarity' not in mod,'plate uncommon rarity remains')
req(not (R/'src/main/resources/data/thaumcraft/loot_tables/blocks/tc4_block_arcane_pressure_plate.json').exists(),'warded normal loot table remains')

plate=json.loads(text('src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_arcanepressureplate.json'))
req(plate['research']=='WARDEDARCANA' and plate['pattern']==['B','TDT'],'plate recipe research/pattern')
req(plate['key']['B']=='thaumcraft:tc4_brain' and plate['key']['T']=='thaumcraft:thaumium_ingot'
    and plate['key']['D']=='thaumcraft:greatwood_planks','plate T/D mapping')
req(plate['aspects']=={'AQUA':20,'ORDO':10,'IGNIS':5,'TERRA':10},'plate vis costs')
gold=json.loads(text('src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_goldkey.json'))
req(gold['result']=={'item':'thaumcraft:tc4_keygold','count':2},'gold key result')

research=text('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchRuntimeBridge.java')
for t in ('"WARDEDARCANA", "Warded Arcana", "Controlling access"','"ARTIFICE", -5, -4, 2',
 'aspects("INSTRUMENTUM", 6, "COGNITIO", 3, "MACHINA", 3, "TUTAMEN", 3)',
 'new String[] {"THAUMIUM"}','"ArcaneDoor", "IronKey", "GoldKey", "ArcanePressurePlate", "WardedGlass"'):
    req(t in research,'research '+t)

for tex in ('applate1','applate2','applate3'):
    req(sha('src/main/resources/assets/thaumcraft/original_tc4_1710/textures/blocks/'+tex+'.png')
        == sha('src/main/resources/assets/thaumcraft/textures/block/tc4/'+tex+'.png'),'block texture '+tex)
for tex in ('keyiron','keygold'):
    req(sha('src/main/resources/assets/thaumcraft/original_tc4_1710/textures/items/'+tex+'.png')
        == sha('src/main/resources/assets/thaumcraft/textures/item/tc4/'+tex+'.png'),'item texture '+tex)
state=json.loads(text('src/main/resources/assets/thaumcraft/blockstates/tc4_block_arcane_pressure_plate.json'))
req(len(state['variants'])==6,'six setting/power models')
for setting in range(3):
    for powered,suffix in ((False,'up'),(True,'down')):
        k=f'powered={str(powered).lower()},setting={setting}'
        req(state['variants'][k]['model']==f'thaumcraft:block/tc4_block_arcane_pressure_plate_{setting}_{suffix}','variant '+k)

with zipfile.ZipFile(R/'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip') as z:
    def orig(suffix):
        n=next((n for n in z.namelist() if n.endswith('/'+suffix)),None); req(n,'original missing '+suffix); return z.read(n).decode(errors='replace')
    ob=orig('thaumcraft/common/blocks/BlockWoodenDevice.java')
    ot=orig('thaumcraft/common/tiles/TileArcanePressurePlate.java')
    oo=orig('thaumcraft/common/tiles/TileOwned.java')
    ok=orig('thaumcraft/common/items/ItemKey.java')
    ow=orig('thaumcraft/common/items/wands/ItemWandCasting.java')
    orc=orig('thaumcraft/common/config/ConfigRecipes.java')
    ors=orig('thaumcraft/common/config/ConfigResearch.java')
for t in ('func_149711_c(2.5F)','return Config.wardedStone ? -1.0F : 2.0F','return 999.0F',
 'float var7 = 0.125F','y + 0.25D','setting == 0','setting == 1','setting == 2',
 'world.func_147464_a(x, y, z, this, tickRate())','return (side == 1)','return true;'):
    req(t in ob,'original BlockWoodenDevice '+t)
for t in ('public byte setting = 0','func_74771_c("setting")','func_74774_a("setting", this.setting)'):
    req(t in ot,'original TileArcanePressurePlate '+t)
for t in ('public String owner = ""','public ArrayList<String> accessList','"owner"','"access"','"name"'):
    req(t in oo,'original TileOwned '+t)
for t in ('"location"','"type"','String loc = x + "," + (y + mod) + "," + z',
 'func_77942_o()','func_70441_a(st)','itemstack.field_77994_a -= 1','tc.key7','tc.key8',
 'location = "x " + ss[0] + ", z " + ss[2] + ", y " + ss[1]'):
    req(t in ok,'original ItemKey '+t)
req('bi == ConfigBlocks.blockWoodenDevice) && (md == 2)' in ow and 'safeToRemove = true' in ow,'original wand removal')
req('ConfigResearch.recipes.put("ArcanePressurePlate", ThaumcraftApi.addArcaneCraftingRecipe("WARDEDARCANA"' in orc,'original plate recipe registration')
req('ConfigResearch.recipes.put("GoldKey", ThaumcraftApi.addArcaneCraftingRecipe("WARDEDARCANA"' in orc,'original gold key recipe registration')
req('new ResearchItem("WARDEDARCANA", "ARTIFICE"' in ors,'original research registration')

gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=183 and len(methods)==len(set(methods)),f'GameTests {len(methods)}')
for m in ('arcanePressurePlateProductionTriggerModesMatchOriginal','arcanePressurePlateFloatingBoundsSignalsAndWardsMatchOriginal',
 'arcanePressurePlateLegacyOwnerAccessNbtRoundTrip','arcaneKeyOriginalLocationTypeAndMigrationContract',
 'arcanePressurePlateOwnerWandRemovalDropsOnePlate','arcanePressurePlateKeysResearchAndRecipeIndexMatchOriginal'):
    req(m in methods,'GameTest '+m)
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json')); ids=[x['id'] for x in manifest['tests']]
req(tuple(map(int,manifest['version'].split('.'))) >= (11,64,21) and len(ids)>=594 and len(ids)==len(set(ids)),f'manifest {manifest["version"]}/{len(ids)}')
for sid in ('gametest.arcane_pressure_plate_trigger_modes','gametest.arcane_pressure_plate_bounds_signals_wards',
 'gametest.arcane_pressure_plate_legacy_nbt','gametest.arcane_key_location_type_migration',
 'gametest.arcane_pressure_plate_owner_wand_removal','gametest.arcane_pressure_plate_research_recipe',
 'gameplay.arcane_key_binding_inventory_order','client.arcane_pressure_plate_models_tooltips',
 'jei.arcane_pressure_plate_keys_exact_recipes','dedicated.arcane_pressure_plate_multiplayer_access'):
    req(sid in ids,'scenario '+sid)
ev=json.loads(text('tools/data/tc4_arcane_pressure_plate_full_source_evidence_v11.64.21.json'))
req(ev['round']=='11.64.21' and ev['source_closure']=='CLOSED' and ev['resource_closure']=='CLOSED','evidence closure')
req(ev['build_status']=='NOT_OBTAINED' and ev['runtime_status']=='NOT_VERIFIED','evidence honesty')
prompt=text('UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md')
req(prompt==text('PROMPT_FOR_FUTURE_CHAT_RU.md'),'prompt copies differ')
for t in ('Один релиз — один предмет или одна цельная механика','SOURCE CLOSED','RESOURCE CLOSED',
 'BUILD VERIFIED','RUNTIME VERIFIED','Упаковка архива без этого файла запрещена'):
    req(t in prompt,'prompt token '+t)
print(f'TC4 v11.64.21 Arcane Pressure Plate full-closure guard: PASS ({len(methods)} GameTests; {len(ids)} scenarios; source/resource/prompt)')
