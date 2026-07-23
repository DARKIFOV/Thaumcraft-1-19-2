#!/usr/bin/env python3
from pathlib import Path
import hashlib,json,re,zipfile
R=Path(__file__).resolve().parents[1]
def text(p): return (R/p).read_text(encoding='utf-8',errors='replace')
def req(ok,msg):
    if not ok: raise SystemExit('TC4 v11.64.32 Wand rods/caps full-closure guard: FAIL: '+msg)
def ver(p):
    m=re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']',text(p)); req(m,'version '+p); return tuple(map(int,m.groups()))
def sha(b): return hashlib.sha256(b).hexdigest()
req(ver('build.gradle')>=(11,64,32),'build version')
req(ver('src/main/resources/META-INF/mods.toml')>=(11,64,32),'mods version')
math=text('src/main/java/com/darkifov/thaumcraft/wand/TC4WandComponentMath.java')
for token in ('CONTRACT_VERSION = "11.64.32"','NORMAL_CAPACITY_MULTIPLIER = 100','SCEPTRE_CAPACITY_MULTIPLIER = 150','SCEPTRE_DISCOUNT = 0.1F','MINIMUM_COST_MODIFIER = 0.1F','REGEN_THRESHOLD_DIVISOR = 10','ONE_VIS_CENTIVIS = 100'):
    req(token in math,'math '+token)
data=text('src/main/java/com/darkifov/thaumcraft/wand/WandComponentData.java')
for token in ('TC4WandComponentMath.capacityCentivis','TC4WandComponentMath.consumptionModifier','normalizeOriginalTags','root.putString(ORIGINAL_TAG_ROD','root.putString(ORIGINAL_TAG_CAP','root.remove(TAG_WAND)','displayNameComponent'):
    req(token in data,'component data '+token)
req('getOrCreateTagElement(TAG_WAND)' not in data,'nested adapter must not be written')
variant=text('src/main/java/com/darkifov/thaumcraft/wand/WandVariantRuntime.java')
for token in ('ORIGINAL_CREATIVE_VARIANT_COUNT = 4','create(WandRodType.WOOD, WandCapType.IRON, false, true)','create(WandRodType.GREATWOOD, WandCapType.GOLD, false, true)','create(WandRodType.SILVERWOOD, WandCapType.THAUMIUM, false, true)','create(WandRodType.SILVERWOOD, WandCapType.THAUMIUM, true, true)'):
    req(token in variant,'creative '+token)
req('for (WandRodType rod' not in variant and '216' not in variant,'expanded creative catalogue removed')
rod=text('src/main/java/com/darkifov/thaumcraft/wand/WandRodType.java')
for token in ('ELEMENTAL_REGEN_INTERVAL_TICKS = 200','PRIMAL_STAFF_REGEN_INTERVAL_TICKS = 50','REGEN_THRESHOLD_DIVISOR = 10','REGEN_AMOUNT_VIS = 1'):
    req(token in rod,'rod '+token)
wand=text('src/main/java/com/darkifov/thaumcraft/block/WandItem.java')
for token in ('STAFF_ATTACK_DAMAGE = 6.0D','getAttributeModifiers(EquipmentSlot slot, ItemStack stack)','Attributes.ATTACK_DAMAGE','WandComponentData.normalizeOriginalTags(stack)','displayNameComponent(stack)','TC4WandComponentMath.regenerationThresholdCentivis','WandRodType.REGEN_AMOUNT_VIS'):
    req(token in wand,'wand '+token)
for forbidden in ('Rod: " + data.rod()','Caps: " + data.cap()','TC4 rod recharge:','Focus: none'):
    req(forbidden not in wand,'debug tooltip '+forbidden)
component=text('src/main/java/com/darkifov/thaumcraft/item/TC4WandComponentItem.java')
req('appendHoverText' not in component,'component item invented tooltip removed')
mod=text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
req(mod.count('stacksTo(1).rarity(Rarity.UNCOMMON)')>=3,'wand stack/rarity registrations')
for token in ('IRON_WAND_CAP = specialItem','GREATWOOD_WAND_CORE = specialItem','TC4WandComponentItem.activeCap','TC4WandComponentItem.rod(new Item.Properties().tab(THAUMCRAFT_TAB), WandRodType.GREATWOOD)','TC4WandComponentItem.rod(new Item.Properties(), WandRodType.WOOD)'):
    req(token in mod,'canonical component '+token)
research=text('src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java')
for old in ('tc4_wand_cap_iron','tc4_wand_cap_gold','tc4_wand_cap_thaumium','tc4_wand_rod_greatwood','tc4_wand_rod_silverwood'):
    segment=next((line for line in research.splitlines() if f'case "{old}"' in line), '')
    req('properties,' in segment and 'functionalProperties,' not in segment,'hidden migration alias '+old)
parity=text('src/main/java/com/darkifov/thaumcraft/wand/TC4WandComponentsFullClosureParity.java')
for token in ('CONTRACT_VERSION = "11.64.32"','ORIGINAL_WAND_CREATIVE_VARIANTS = 4','ORIGINAL_ROD_COMPONENT_VARIANTS = 17','ORIGINAL_CAP_COMPONENT_VARIANTS = 9','ORIGINAL_STAFF_ATTACK_DAMAGE = 6','capacitiesAndCraftCostsMatchOriginal','capCostsAndModifiersMatchOriginal','regenerationMatchesOriginal','catalogueCountsMatchOriginal'):
    req(token in parity,'parity '+token)
for lang in ('en_us.json','ru_ru.json'):
    j=json.loads(text('src/main/resources/assets/thaumcraft/lang/'+lang))
    req(j.get('item.thaumcraft.wand_name_format')=='%1$s %2$s %3$s','name format '+lang)
    for key in ('item.Wand.name','item.Wand.wand.obj','item.Wand.sceptre.obj','item.Wand.staff.obj','item.Wand.wood.rod','item.Wand.iron.cap'):
        req(key in j,'original localization '+lang+' '+key)
source_zip=R/'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip'; prefix='Thaumcraft4-1.7.10-master/'
with zipfile.ZipFile(source_zip) as z:
    originals={
      'thaumcraft/common/items/wands/ItemWandCasting.java':('this.field_77777_bU = 1','return EnumRarity.uncommon','par3List.add(sceptre)','item.Wand.name','getRod(stack).getCapacity() * (isSceptre(stack) ? 150 : 100)','new AttributeModifier(field_111210_e, "Weapon modifier", 6.0D, 0)','stack.func_77983_a("rod"','stack.func_77983_a("cap"'),
      'thaumcraft/common/items/wands/WandRodPrimalOnUpdate.java':('player.field_70173_aa % 200 == 0','getMaxVis(itemstack) / 10','addVis(itemstack, this.aspect, 1, true)','player.field_70173_aa % 50 == 0'),
      'thaumcraft/common/items/wands/ItemWandRod.java':('new ItemStack(this, 1, 0)','new ItemStack(this, 1, 57)','new ItemStack(this, 1, 100)'),
      'thaumcraft/common/items/wands/ItemWandCap.java':('new ItemStack(this, 1, 0)','new ItemStack(this, 1, 8)')}
    for rel,tokens in originals.items():
        src=z.read(prefix+rel).decode('utf-8',errors='replace')
        for token in tokens: req(token in src,'original '+rel+' '+token)
    textures=['wand_cap_copper','wand_cap_gold','wand_cap_iron','wand_cap_silver','wand_cap_silver_inert','wand_cap_thaumium','wand_cap_thaumium_inert','wand_cap_void','wand_cap_void_inert','wand_rod_blaze','wand_rod_bone','wand_rod_greatwood','wand_rod_ice','wand_rod_obsidian','wand_rod_quartz','wand_rod_reed','wand_rod_silverwood','staff_rod_blaze','staff_rod_bone','staff_rod_greatwood','staff_rod_ice','staff_rod_obsidian','staff_rod_primal','staff_rod_quartz','staff_rod_reed','staff_rod_silverwood']
    for name in textures:
        current=R/f'src/main/resources/assets/thaumcraft/textures/item/tc4/{name}.png'
        req(current.is_file(),'texture '+name)
        req(sha(current.read_bytes())==sha(z.read(prefix+f'assets/thaumcraft/textures/items/{name}.png')),'texture hash '+name)
gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=253 and len(methods)==len(set(methods)),f'GameTests {len(methods)}')
for name in ('wandOriginalCreativeVariantsMatchTc4','wandRootNbtMigrationMatchesTc4','wandStaffAttackDamageMatchesTc4','wandStackRarityAndNamesMatchTc4','wandComponentCatalogueFullClosureMatchesTc4'):
    req(name in methods,'GameTest '+name)
man=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json')); ids=[x['id'] for x in man['tests']]
req(tuple(map(int,man['version'].split('.')))>=(11,64,32),'manifest version')
req(len(ids)>=761 and len(ids)==len(set(ids)),f'manifest {len(ids)}')
for sid in ('gameplay.wand_creative_four_variants','gameplay.wand_original_root_component_nbt','world_migration.wand_nested_component_nbt','gameplay.wand_staff_attack_damage','visual.wand_localized_component_name','gameplay.wand_elemental_regeneration_amount','visual.wand_component_catalogue','dedicated.wand_component_server_sync'):
    req(sid in ids,'scenario '+sid)
for p in ('TC4_11.64.32_WAND_RODS_CAPS_SOURCE_EVIDENCE.json','tools/data/tc4_wand_rods_caps_full_source_evidence_v11.64.32.json'):
    e=json.loads(text(p)); req(e.get('version')=='11.64.32' and len(e.get('original_sources',[]))==5,'evidence '+p)
prompt=R/'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md'
req(prompt.is_file(),'mandatory prompt missing')
req(prompt.read_bytes()==(R/'PROMPT_FOR_FUTURE_CHAT_RU.md').read_bytes(),'prompt copies differ')
req('Файл `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md` обязателен' in prompt.read_text(encoding='utf-8'),'prompt wording')
print('TC4 v11.64.32 Wand rods/caps full-closure guard: PASS')
