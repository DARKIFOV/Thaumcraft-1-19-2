#!/usr/bin/env python3
from pathlib import Path
import json, hashlib, sys
R=Path(__file__).resolve().parents[1]; checks=[]
def text(p):
 q=R/p; return q.read_text(encoding='utf-8',errors='ignore') if q.is_file() else ''
def need(p,t): checks.append((f'{p}:{t[:74]}',t in text(p)))
def ok(n,v): checks.append((n,bool(v)))
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json')); ids={x.get('id') for x in manifest['tests']}
for n,v in [('build',"version = '11.63.23'" in text('build.gradle')),('mods','version="11.63.23"' in text('src/main/resources/META-INF/mods.toml')),('manifest',manifest['version'] in ('11.63.23','11.63.24','11.63.26','11.63.27','11.63.28','11.63.29','11.63.30','11.63.31','11.63.32','11.63.33','11.63.36','11.63.37','11.63.38','11.63.39','11.63.40', '11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58','11.63.59', '11.63.60', '11.63.61')),('count_at_least_166',len(manifest['tests'])>=166)]: ok(n,v)
for p,tokens in {
'src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java':['import com.darkifov.thaumcraft.item.PrimalCrusherItem;','case "tc4_primal_crusher" -> new PrimalCrusherItem(functionalProperties)'],
'src/main/java/com/darkifov/thaumcraft/item/gear/TC4PrimalCrusherTier.java':['implements Tier','return 500;','return 8.0F;','return 4.0F;','return 5;','return 20;','return Ingredient.EMPTY;'],
'src/main/java/com/darkifov/thaumcraft/item/PrimalCrusherItem.java':['extends PickaxeItem','TC4PrimalCrusherTier.INSTANCE, 3, -3.0F','return Rarity.EPIC;','ToolActions.PICKAXE_DIG','ToolActions.SHOVEL_DIG','state.is(BlockTags.MINEABLE_WITH_PICKAXE)','state.is(BlockTags.MINEABLE_WITH_SHOVEL)','return !state.isAir() && state.getFluidState().isEmpty();','registered("tc4_charm")','player.isShiftKeyDown()','ThreadLocal<Boolean> INTERNAL_HARVEST','player.pick(6.0D, 0.0F, false)','face.getAxis() == Direction.Axis.Y','face.getAxis() == Direction.Axis.Z','serverPlayer.gameMode.destroyBlock(target)','state.getDestroySpeed(serverLevel, target) < 0.0F','stack.isEmpty()','entity.tickCount % 20 == 0','stack.setDamageValue(Math.max(0, stack.getDamageValue() - 1))','tooltip.thaumcraft.primal_crusher.self_repair','TC4WarpingGearAdapter.appendTooltip'],
'src/main/java/com/darkifov/thaumcraft/item/ElementalPickaxeRuntime.java':['instanceof PrimalCrusherItem','0.2F + pending.fortune * 0.075F','!pending.existing.contains(entity.getUUID())'],
'src/main/java/com/darkifov/thaumcraft/runic/TC4WarpingGearAdapter.java':['Map.entry("thaumcraft:tc4_primal_crusher", 2)'],
}.items():
 for t in tokens: need(p,t)
for tid in ['tools.primal_crusher_material_actions_rarity_repair_and_warp','tools.primal_crusher_three_by_three_all_face_planes_and_sneak','tools.primal_crusher_hardness_protection_durability_and_break','tools.primal_crusher_cluster_conversion_fortune_and_fresh_drops','tools.primal_crusher_passive_repair_inventory_save_reload','tools.primal_crusher_multiplayer_combat_modded_tags_and_tool_actions']: ok('manifest:'+tid,tid in ids)
for lang,name,tip in [('en_us','Primal Crusher','Repairs itself over time'),('ru_ru','Сингулярный крушитель','Постепенно восстанавливает прочность')]:
 d=json.loads(text(f'src/main/resources/assets/thaumcraft/lang/{lang}.json')); ok('lang_name:'+lang,d.get('item.thaumcraft.tc4_primal_crusher')==name); ok('lang_tip:'+lang,d.get('tooltip.thaumcraft.primal_crusher.self_repair')==tip)
tex=R/'src/main/resources/assets/thaumcraft/textures/item/tc4/primal_crusher.png'; ok('texture_exists',tex.is_file())
model=json.loads(text('src/main/resources/assets/thaumcraft/models/item/tc4_primal_crusher.json')); ok('model_texture',model.get('textures',{}).get('layer0')=='thaumcraft:item/tc4/primal_crusher')
recipe=json.loads(text('src/main/resources/data/thaumcraft/thaumcraft_infusion/tc4_primal_crusher.json')); ok('infusion_recipe',bool(recipe))
for wf in ['.github/workflows/build.yml','.github/workflows/release.yml']: ok('workflow:'+wf,'tc4_116316_primal_crusher_parity_guard.py' in text(wf))
fail=[n for n,v in checks if not v]
for n,v in checks: print(('PASS' if v else 'FAIL')+' | '+n)
print(f'SUMMARY | {len(checks)-len(fail)}/{len(checks)} passed'); sys.exit(bool(fail))
