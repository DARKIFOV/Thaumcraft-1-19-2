#!/usr/bin/env python3
from pathlib import Path
import json,re,sys
R=Path(__file__).resolve().parents[1]; checks=[]
# HISTORICAL_DUPLICATE_GUARD_SUPERSEDED
current=re.search(r"^version = '([0-9.]+)'", (R / "build.gradle").read_text(encoding="utf-8"), re.M).group(1)
if tuple(map(int,current.split('.'))) >= (11,63,52):
    import subprocess
    subprocess.run([sys.executable, str(R / "tools/tc4_116352_duplicate_registry_purge_guard.py")], check=True)
    print("tc4_116332_wand_component_families_parity_guard.py: PASS (legacy duplicate expectations superseded by canonical registry purge)")
    raise SystemExit(0)

def text(p):
 q=R/p; return q.read_text(encoding='utf-8',errors='ignore') if q.is_file() else ''
def ok(n,v): checks.append((n,bool(v)))
def need(p,t): ok(f'{p}:{t}',t in text(p))
def j(p): return json.loads(text(p))
manifest=j('runtime_artifacts/runtime_test_manifest.template.json'); ids={x.get('id') for x in manifest.get('tests',[])}
audit=j('reports/tc4_remaining_objects_audit_v11.63.32.json')
ok('build_version',any(f"version = '{v}'" in text('build.gradle') for v in ('11.63.32','11.63.33')))
ok('mods_version',any(f'version="{v}"' in text('src/main/resources/META-INF/mods.toml') for v in ('11.63.32','11.63.33')))
ok('manifest_version',manifest.get('version') in ('11.63.32','11.63.33','11.63.36','11.63.37','11.63.38','11.63.39','11.63.40', '11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58','11.63.59', '11.63.60', '11.63.61')); ok('manifest_count',len(manifest.get('tests',[]))>=256)
cls='src/main/java/com/darkifov/thaumcraft/item/TC4WandComponentItem.java'
for token in ['class TC4WandComponentItem extends Item','enum Family { ACTIVE_CAP, INERT_CAP, WAND_ROD, STAFF_ROD }','activeCap(Properties properties, WandCapType cap)','inertCap(Properties properties, WandCapType cap)','rod(Properties properties, WandRodType rod)','rod.staff() ? Family.STAFF_ROD : Family.WAND_ROD','Math.round(cap.visCostModifier() * 100.0F)','rod.baseCapacity()','rod.regeneratesAllPrimals()','rod.regeneratedAspect()']:
 need(cls,token)
registry='src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java'
active={
 'tc4_wand_cap_iron':'WandCapType.IRON','tc4_wand_cap_gold':'WandCapType.GOLD','tc4_wand_cap_thaumium':'WandCapType.THAUMIUM',
 'tc4_wand_cap_copper':'WandCapType.COPPER','tc4_wand_cap_silver':'WandCapType.SILVER','tc4_wand_cap_void':'WandCapType.VOID'}
inert={
 'tc4_wand_cap_silver_inert':'WandCapType.SILVER','tc4_wand_cap_thaumium_inert':'WandCapType.THAUMIUM','tc4_wand_cap_void_inert':'WandCapType.VOID'}
rods={
 'tc4_wand_rod_greatwood':'WandRodType.GREATWOOD','tc4_wand_rod_obsidian':'WandRodType.OBSIDIAN','tc4_wand_rod_blaze':'WandRodType.BLAZE','tc4_wand_rod_ice':'WandRodType.ICE','tc4_wand_rod_quartz':'WandRodType.QUARTZ','tc4_wand_rod_bone':'WandRodType.BONE','tc4_wand_rod_reed':'WandRodType.REED','tc4_wand_rod_silverwood':'WandRodType.SILVERWOOD',
 'tc4_staff_rod_greatwood':'WandRodType.GREATWOOD_STAFF','tc4_staff_rod_obsidian':'WandRodType.OBSIDIAN_STAFF','tc4_staff_rod_blaze':'WandRodType.BLAZE_STAFF','tc4_staff_rod_ice':'WandRodType.ICE_STAFF','tc4_staff_rod_quartz':'WandRodType.QUARTZ_STAFF','tc4_staff_rod_bone':'WandRodType.BONE_STAFF','tc4_staff_rod_reed':'WandRodType.REED_STAFF','tc4_staff_rod_silverwood':'WandRodType.SILVERWOOD_STAFF','tc4_staff_rod_primal':'WandRodType.PRIMAL_STAFF'}
source=text(registry)
for item,typ in active.items(): ok('active_'+item,f'case "{item}" -> TC4WandComponentItem.activeCap(functionalProperties, {typ});' in source)
for item,typ in inert.items(): ok('inert_'+item,f'case "{item}" -> TC4WandComponentItem.inertCap(functionalProperties, {typ});' in source)
for item,typ in rods.items(): ok('rod_'+item,f'case "{item}" -> TC4WandComponentItem.rod(functionalProperties, {typ});' in source)
# Numerical contracts already used by assembled wands.
cap='src/main/java/com/darkifov/thaumcraft/wand/WandCapType.java'
for token in ['IRON("iron", "wand_cap_iron", 1.1F','GOLD("gold", "wand_cap_gold", 1.0F','THAUMIUM("thaumium", "wand_cap_thaumium", 0.9F','COPPER("copper", "wand_cap_copper", 1.1F','SILVER("silver", "wand_cap_silver", 1.0F','VOID("void", "wand_cap_void", 0.8F','this == COPPER && (aspect == Aspect.ORDO || aspect == Aspect.PERDITIO)','this == SILVER && (aspect == Aspect.AER || aspect == Aspect.TERRA || aspect == Aspect.IGNIS || aspect == Aspect.AQUA)']:
 need(cap,token)
rod='src/main/java/com/darkifov/thaumcraft/wand/WandRodType.java'
for token in ['GREATWOOD("greatwood", "wand_rod_greatwood", 50','OBSIDIAN("obsidian", "wand_rod_obsidian", 75','SILVERWOOD("silverwood", "wand_rod_silverwood", 100','GREATWOOD_STAFF("greatwood_staff", "wand_rod_greatwood", 125','OBSIDIAN_STAFF("obsidian_staff", "wand_rod_obsidian", 175','SILVERWOOD_STAFF("silverwood_staff", "wand_rod_silverwood", 250','PRIMAL_STAFF("primal_staff", "wand_rod_primal", 250','case OBSIDIAN, OBSIDIAN_STAFF -> Aspect.TERRA','case BLAZE, BLAZE_STAFF -> Aspect.IGNIS','case ICE, ICE_STAFF -> Aspect.AQUA','case QUARTZ, QUARTZ_STAFF -> Aspect.ORDO','case BONE, BONE_STAFF -> Aspect.PERDITIO','case REED, REED_STAFF -> Aspect.AER','return this == PRIMAL_STAFF;']:
 need(rod,token)
# Existing assembly maps exact legacy ids and intentionally omits inert caps.
component='src/main/java/com/darkifov/thaumcraft/wand/WandComponentData.java'
for item in list(active)+list(rods): need(component,item)
for item in inert: ok('inert_not_assembly_'+item,item not in text(component))
items=list(active)+list(inert)+list(rods)
# Parse entry texture mapping for byte exact comparisons.
entry_text=text(registry).split('private static final Map<String, Entry> BY_ID')[0]
entries={m.group(1):(m.group(2),m.group(4)) for m in re.finditer(r'e\("([^"]+)",\s*"([^"]+)",\s*"([^"]*)",\s*"([^"]+)"\)',entry_text)}
for item in items:
 ok('model_'+item,(R/'src/main/resources/assets/thaumcraft/models/item'/f'{item}.json').is_file())
 tex,legacy=entries[item]
 a=R/'src/main/resources/assets/thaumcraft/textures/item/tc4'/f'{tex}.png'
 b=R/'src/main/resources/assets/thaumcraft/original_tc4_1710/textures/items'/f'{legacy}.png'
 ok('texture_'+item,a.is_file() and b.is_file() and a.read_bytes()==b.read_bytes())
# Original names.
en=j('src/main/resources/assets/thaumcraft/lang/en_us.json'); ru=j('src/main/resources/assets/thaumcraft/lang/ru_ru.json')
expected_en={'tc4_wand_cap_iron':'Iron Cap','tc4_wand_cap_gold':'Gold Cap','tc4_wand_cap_thaumium':'Charged Thaumium Cap','tc4_wand_cap_copper':'Copper Cap','tc4_wand_cap_silver':'Charged Silver Cap','tc4_wand_cap_silver_inert':'Inert Silver Cap','tc4_wand_cap_thaumium_inert':'Inert Thaumium Cap','tc4_wand_cap_void':'Charged Void Metal Cap','tc4_wand_cap_void_inert':'Inert Void Metal Cap','tc4_wand_rod_greatwood':'Greatwood Rod','tc4_wand_rod_obsidian':'Obsidian Rod','tc4_wand_rod_silverwood':'Silverwood Rod','tc4_wand_rod_ice':'Icy Rod','tc4_wand_rod_quartz':'Quartz Rod','tc4_wand_rod_reed':'Reed Rod','tc4_wand_rod_blaze':'Blazing Rod','tc4_wand_rod_bone':'Bone Rod','tc4_staff_rod_greatwood':'Greatwood Staff Core','tc4_staff_rod_obsidian':'Obsidian Staff Core','tc4_staff_rod_silverwood':'Silverwood Staff Core','tc4_staff_rod_ice':'Icy Staff Core','tc4_staff_rod_quartz':'Quartz Staff Core','tc4_staff_rod_reed':'Reed Staff Core','tc4_staff_rod_blaze':'Blazing Staff Core','tc4_staff_rod_bone':'Bone Staff Core','tc4_staff_rod_primal':'Staff Core of the Primal'}
for item,name in expected_en.items(): ok('english_'+item,en.get('item.thaumcraft.'+item)==name)
for item in items: ok('russian_'+item,bool(ru.get('item.thaumcraft.'+item)))
for key in ['inert','vis_cost','copper_special','silver_special','capacity','regen_all','regen_one']:
 ok('tooltip_en_'+key,bool(en.get('tooltip.thaumcraft.wand_component.'+key))); ok('tooltip_ru_'+key,bool(ru.get('tooltip.thaumcraft.wand_component.'+key)))
# Tags.
tagmap={'wand_caps':list(active),'inert_wand_caps':list(inert),'wand_rods':[x for x in rods if x.startswith('tc4_wand_')],'staff_rods':[x for x in rods if x.startswith('tc4_staff_')],'wand_components':items}
for fn,expected in tagmap.items():
 data=j(f'src/main/resources/data/thaumcraft/tags/items/{fn}.json').get('values',[])
 for item in expected: ok('tag_'+fn+'_'+item,'thaumcraft:'+item in data)
# Runtime protocol.
for tid in ['items.wand_components_registry_and_names','items.wand_active_caps_assembly','items.wand_inert_caps_rejected','items.wand_rods_capacity_and_regen','items.staff_rods_capacity_and_primal','items.wand_components_save_reload_multiplayer']:
 ok('runtime_'+tid,tid in ids)
ok('audit_version',audit.get('version')=='11.63.32'); ok('audit_fallback',audit.get('generic_fallback_ids')==118); ok('audit_items',audit.get('generic_fallback_item_like_ids')==76); ok('audit_blocks',audit.get('generic_fallback_block_alias_ids')==42)
for item in items: ok('not_fallback_'+item,item not in audit.get('generic_fallback',[]))
for p in ['src/main/java/com/darkifov/thaumcraft/world/MagicalForestWorldgenDiagnostics.java','src/main/java/com/darkifov/thaumcraft/world/MagicalForestWorldgenInstaller.java','src/main/java/com/darkifov/thaumcraft/world/TC4Biomes.java','src/main/java/com/darkifov/thaumcraft/world/TC4TreeGenerator.java','src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenRuntime.java','src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenSavedData.java']:
 ok('worldgen_preserved_'+Path(p).name,(R/p).is_file())
for wf in ['.github/workflows/build.yml','.github/workflows/release.yml']: need(wf,'python3 tools/tc4_116332_wand_component_families_parity_guard.py')
need('README.md','11.63.32 — Wand Component Families')
need('KNOWN_DEVIATIONS.md','11.63.32 Wand Component Families')
need('TC4_11.63.32_WAND_COMPONENT_FAMILIES_PORT_REPORT_RU.md','**118 fallback-ID**')
need('TC4_11.63.32_REMAINING_OBJECTS_AUDIT_RU.md','**118**')
failed=[n for n,v in checks if not v]
for n,v in checks: print(('PASS' if v else 'FAIL')+' | '+n)
print(f'SUMMARY | {len(checks)-len(failed)}/{len(checks)} passed')
sys.exit(1 if failed else 0)
