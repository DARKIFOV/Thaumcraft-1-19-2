#!/usr/bin/env python3
from pathlib import Path
import json, sys, hashlib
R=Path(__file__).resolve().parents[1]

# Forward-compatible: v11.64.17 supersedes the earlier approximate pre-coloured
# texture and per-context item-transform contract with the complete original
# runtime-tint/UV/resource/research/aspect closure.
if "version = '11.64.17'" in (R / "build.gradle").read_text(encoding="utf-8"):
 import subprocess
 result = subprocess.run([sys.executable, str(R / "tools/tc4_116417_tallow_candle_full_closure_guard.py")], cwd=R)
 if result.returncode:
  raise SystemExit(result.returncode)
 print("TC4 v11.63.20 Tallow Candle historical guard: PASS via superseding v11.64.17 full-closure contract")
 raise SystemExit(0)
checks=[]
def text(p):
 q=R/p; return q.read_text(encoding='utf-8',errors='ignore') if q.is_file() else ''
def ok(n,v): checks.append((n,bool(v)))
def need(p,t): ok(f'{p}:{t[:92]}',t in text(p))
def sha(p): return hashlib.sha256(Path(p).read_bytes()).hexdigest()
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json')); ids={x.get('id') for x in manifest.get('tests',[])}
ok('build_version_116320',"version = '11.63.23'" in text('build.gradle'))
ok('mods_version_116320','version="11.63.23"' in text('src/main/resources/META-INF/mods.toml'))
ok('manifest_version_116320',manifest.get('version') in ('11.63.23','11.63.24','11.63.26','11.63.27','11.63.28','11.63.29','11.63.30','11.63.31','11.63.32','11.63.33','11.63.36','11.63.37','11.63.38','11.63.39','11.63.40', '11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58','11.63.59', '11.63.60', '11.63.61'))
ok('manifest_count_at_least_190',len(manifest.get('tests',[]))>=190)
for p,tokens in {
'src/main/java/com/darkifov/thaumcraft/block/TallowCandleBlock.java':[
 'extends BaseEntityBlock implements InfusionStabilizer','Block.box(6.0D, 0.0D, 6.0D, 10.0D, 8.0D, 10.0D)',
 'new TallowCandleBlockEntity(pos, state)','return RenderShape.ENTITYBLOCK_ANIMATED','return Shapes.empty()',
 'canSupportCenter(level, pos.below(), Direction.UP)','direction == Direction.DOWN','Blocks.AIR.defaultBlockState()',
 'pos.getY() + 0.7D','ParticleTypes.SMOKE','ParticleTypes.FLAME'],
'src/main/java/com/darkifov/thaumcraft/blockentity/TallowCandleBlockEntity.java':[
 'extends BlockEntity','TALLOW_CANDLE_BLOCK_ENTITY.get()','Render anchor for TC4'],
'src/main/java/com/darkifov/thaumcraft/block/TallowCandleBlockItem.java':[
 'extends BlockItem','initializeClient(Consumer<IClientItemExtensions> consumer)','getCustomRenderer()',
 'TallowCandleItemRenderer.instance()'],
'src/main/java/com/darkifov/thaumcraft/client/render/TallowCandleRenderer.java':[
 'implements BlockEntityRenderer<TallowCandleBlockEntity>','renderItem(ItemStack stack','BlockPos.ZERO, false',
 'renderCandle(candle.getBlockState().getBlock(), pos, true','Sheets.cutoutBlockSheet()',
 'int seed = pos.getX() + pos.getY() * pos.getZ()','new Random(seed)','int count = 1 + random.nextInt(5)',
 'boolean side = random.nextBoolean()','int location = 2 + random.nextInt(2)',
 'float height = (1 + random.nextInt(3)) / 16.0F','if ((index & 1) == 0)',
 '(5.0F + location) / 16.0F','(side ? 5.0F : 10.0F) / 16.0F','(side ? 6.0F : 11.0F) / 16.0F',
 '6.0F / 16.0F, 0.0F, 6.0F / 16.0F','10.0F / 16.0F, 8.0F / 16.0F, 10.0F / 16.0F',
 '0.475F, 8.0F / 16.0F, 0.475F','0.525F, 10.0F / 16.0F, 0.525F',
 'block/tc4/candlestub','"block/" + path','InventoryMenu.BLOCK_ATLAS'],
'src/main/java/com/darkifov/thaumcraft/client/render/TallowCandleItemRenderer.java':[
 'extends BlockEntityWithoutLevelRenderer','renderByItem(ItemStack stack','applyTransform(transformType, poseStack)',
 'TallowCandleRenderer.renderItem','type == ItemTransforms.TransformType.GUI','type.firstPerson()'],
'src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java':[
 'TALLOW_CANDLE_BLOCK_ENTITY','BlockEntityType.Builder.of(TallowCandleBlockEntity::new',
 'new TallowCandleBlock(','new TallowCandleBlockItem(','lightLevel(state -> 14)'],
'src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java':[
 'TallowCandleRenderer','TALLOW_CANDLE_BLOCK_ENTITY.get()','blockEntityRenderer(TallowCandleRenderer::new)'],
}.items():
 for t in tokens: need(p,t)
colors=['','orange','magenta','light_blue','yellow','lime','pink','gray','light_gray','cyan','purple','blue','brown','green','red','black']
mod=text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
for color in colors:
 name='tallow_candle'+(('_'+color) if color else '')
 ok('registry:'+name,f'tallowCandle("{name}")' in mod)
 item=R/f'src/main/resources/assets/thaumcraft/models/item/{name}.json'
 block=R/f'src/main/resources/assets/thaumcraft/models/block/{name}.json'
 tex=R/f'src/main/resources/assets/thaumcraft/textures/block/{name}.png'
 ok('item_model_exists:'+name,item.is_file())
 if item.is_file():
  d=json.loads(item.read_text()); ok('item_builtin:'+name,d.get('parent')=='builtin/entity' and d.get('gui_light')=='front')
 ok('block_model_texture_anchor:'+name,block.is_file() and f'thaumcraft:block/{name}' in block.read_text())
 ok('wax_texture:'+name,tex.is_file())
# The source wick is retained byte-exact; coloured wax atlases are pre-tinted modern assets.
orig=R/'src/main/resources/assets/thaumcraft/original_tc4_1710/textures/blocks/candlestub.png'
modern=R/'src/main/resources/assets/thaumcraft/textures/block/tc4/candlestub.png'
ok('candlestub_source_retained',orig.is_file())
ok('candlestub_renderer_copy',modern.is_file())
ok('candlestub_byte_exact',orig.is_file() and modern.is_file() and sha(orig)==sha(modern))
for tid in [
 'blocks.tallow_candle_world_body_wick_particles_collision_and_support',
 'blocks.tallow_candle_coordinate_seeded_drips_exact_range',
 'blocks.tallow_candle_seed_determinism_save_reload_and_chunk_reload',
 'blocks.tallow_candle_all_sixteen_colours_and_atlas_textures',
 'blocks.tallow_candle_inventory_geometry_has_no_world_drips',
 'blocks.tallow_candle_infusion_stabilizer_multiplayer_and_resource_reload']:
 ok('manifest:'+tid,tid in ids)
for wf in ['.github/workflows/build.yml','.github/workflows/release.yml']:
 need(wf,'tc4_116320_tallow_candle_parity_guard.py')
 need(wf,'Validate v11.63.23 Tallow Candle visual parity')
need('README.md','11.63.20 — Tallow Candle coordinate-seeded visual parity')
need('KNOWN_DEVIATIONS.md','11.63.20 Tallow Candle dynamic-render runtime notes')
failed=[n for n,v in checks if not v]
for n,v in checks: print(('PASS' if v else 'FAIL')+' | '+n)
print(f'SUMMARY | {len(checks)-len(failed)}/{len(checks)} passed')
sys.exit(1 if failed else 0)
