#!/usr/bin/env python3
"""v11.64.22 guard: complete ordinary Arcane Lamp closure and direct integrations."""
from pathlib import Path
import hashlib,json,re,zipfile
R=Path(__file__).resolve().parents[1]
def text(p): return (R/p).read_text(encoding='utf-8')
def req(ok,msg):
    if not ok: raise SystemExit('TC4 v11.64.22 Arcane Lamp full-closure guard: FAIL: '+msg)
def sha_bytes(b): return hashlib.sha256(b).hexdigest()
def sha(p): return hashlib.sha256((R/p).read_bytes()).hexdigest()
def version(p):
    s=text(p); m=re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']',s)
    req(m,'version parse '+p); return tuple(map(int,m.groups()))
req(version('build.gradle') >= (11,64,22),'build version')
req(version('src/main/resources/META-INF/mods.toml') >= (11,64,22),'mods version')

par=text('src/main/java/com/darkifov/thaumcraft/block/TC4ArcaneLampParity.java')
for t in ('CONTRACT_VERSION = "11.64.22"','LIGHT_RADIUS = 15','RANDOM_BOUND = 16','MINIMUM_Y = 5',
 'SURFACE_MARGIN = 4','LAMP_DARKNESS_THRESHOLD = 9','BORE_DARKNESS_THRESHOLD = 15',
 'BORE_DISTANCE_BOUND = 32','BORE_DISTANCE_STEP = 2','BORE_LATERAL_OFFSET = 3',
 'LIGHT_LEVEL = 15','BLOCK_HARDNESS = 3.0F','BLOCK_RESISTANCE = 17.0F',
 'SHAPE_MIN_XZ = 0.25D','SHAPE_MIN_Y = 0.125D','SHAPE_MAX_XZ = 0.75D','SHAPE_MAX_Y = 0.875D',
 'triangularOffset','clampSampledY','shouldPlaceLampLight','boreDistance','boreLateralOffset',
 'boreVerticalOffset','shouldPlaceBoreLight'):
    req(t in par,'parity token '+t)

block=text('src/main/java/com/darkifov/thaumcraft/block/ArcaneLampBlock.java')
for t in ('Block.box(4.0D, 2.0D, 4.0D, 12.0D, 14.0D, 12.0D)',
 'context.getClickedFace().getOpposite()','!level.getBlockState(pos.relative(state.getValue(FACING))).isAir()',
 'changedSide == state.getValue(FACING) && changedState.isAir()','lamp.removeLights()',
 'ArcaneLampBlockEntity::serverTick'):
    req(t in block,'block token '+t)
req('isFaceSturdy' not in block,'sturdy support drift')

light=text('src/main/java/com/darkifov/thaumcraft/block/ArcaneLampLightBlock.java')
req('extends Block' in light and 'extends BaseEntityBlock' not in light,'marker is plain Block')
for t in ('RenderShape.INVISIBLE','Shapes.empty()','propagatesSkylightDown','return 1.0F','isAir(BlockState state)','return true','PushReaction.DESTROY'):
    req(t in light,'marker token '+t)
for stale in ('newBlockEntity','getTicker','ArcaneLampLightBlockEntity::serverTick'):
    req(stale not in light,'active marker BlockEntity path '+stale)

migration=text('src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneLampLightBlockEntity.java')
for t in ('Migration-only carrier','@Deprecated','extends BlockEntity'):
    req(t in migration,'migration carrier '+t)
for stale in ('serverTick','sourceX','sourceY','sourceZ','setSource','private int counter'):
    req(stale not in migration,'stale self-cleanup '+stale)

be=text('src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneLampBlockEntity.java')
for t in ('TC4ArcaneLampParity.triangularOffset(random.nextInt(16), random.nextInt(16))',
 'Heightmap.Types.WORLD_SURFACE','TC4ArcaneLampParity.clampSampledY',
 'targetState != null && targetState.isAir()','targetState.is(ThaumcraftMod.ARCANE_LAMP_LIGHT.get())',
 'serverLevel.getMaxLocalRawBrightness(target)','serverLevel.setBlock(target, ThaumcraftMod.ARCANE_LAMP_LIGHT.get().defaultBlockState(), 3)',
 'for (int dx = -TC4ArcaneLampParity.LIGHT_RADIUS','for (int dy = -TC4ArcaneLampParity.LIGHT_RADIUS',
 'for (int dz = -TC4ArcaneLampParity.LIGHT_RADIUS','tag.putInt("orientation", facing.get3DDataValue())',
 'tag.contains("orientation", Tag.TAG_INT)','Direction.from3DDataValue','level.setBlock(worldPosition'):
    req(t in be,'block entity token '+t)
for stale in ('Mth.clamp','hasChunkAt','setSource(','ArcaneLampLightBlockEntity'):
    req(stale not in be,'non-original lamp path '+stale)

bore=text('src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneBoreBlockEntity.java')
for t in ('seedTunnelLightFromAdjacentLamp(server, worldPosition, basePos',
 'TC4ArcaneLampParity.boreDistance(random.nextInt(TC4ArcaneLampParity.BORE_DISTANCE_BOUND))',
 'new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}',
 'instanceof ArcaneLampBlockEntity','headPos.relative(facing, 1 + distance)',
 'TC4ArcaneLampParity.boreLateralOffset(distance)','TC4ArcaneLampParity.boreVerticalOffset(distance',
 'TC4ArcaneLampParity.shouldPlaceBoreLight','ARCANE_LAMP_LIGHT.get().defaultBlockState()'):
    req(t in bore,'bore integration '+t)

renderer=text('src/main/java/com/darkifov/thaumcraft/client/render/ArcaneLampRenderer.java')
for t in ('textures/models/Bore.png','TC4ArcaneBoreModel.BASE_LAYER','model.renderNozzle',
 'instanceof ArcaneBoreBaseBlockEntity','facing.getOpposite()','Vector3f.ZP.rotationDegrees(-90)',
 'Vector3f.ZP.rotationDegrees(90)','Vector3f.YP.rotationDegrees(90)','Vector3f.YP.rotationDegrees(-90)',
 'Vector3f.YP.rotationDegrees(180)','shouldRenderOffScreen'):
    req(t in renderer,'renderer token '+t)
model_java=text('src/main/java/com/darkifov/thaumcraft/client/render/model/TC4ArcaneBoreModel.java')
for t in ('texOffs(106, 42)','addBox(2.5F, -2, -2, 5, 4, 4)','texOffs(106, 51)',
 'addBox(7, -2.5F, -2.5F, 1, 5, 5)','public void renderNozzle','root.getChild("nozzle1")','root.getChild("nozzle2")'):
    req(t in model_java,'nozzle model '+t)
client=text('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java')
req('ARCANE_LAMP_BLOCK_ENTITY.get(), blockEntityRenderer(ArcaneLampRenderer::new)' in client,'renderer registration')

mod=text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
for t in ('TC4ArcaneLampParity.BLOCK_HARDNESS','TC4ArcaneLampParity.BLOCK_RESISTANCE',
 'new BlockItem(ARCANE_LAMP.get(), new Item.Properties().tab(THAUMCRAFT_TAB))',
 'BlockBehaviour.Properties.of(Material.AIR)','.lightLevel(state -> TC4ArcaneLampParity.LIGHT_LEVEL)',
 'ArcaneLampLightBlockEntity::new).build(null)'):
    req(t in mod,'registration '+t)
req('ARCANE_LAMP.get(), new Item.Properties().tab(THAUMCRAFT_TAB)\n                    .rarity' not in mod,'non-original lamp rarity')
req('ArcaneLampLightBlockEntity::new, ARCANE_LAMP_LIGHT.get()' not in mod,'marker BE still bound')

recipe=json.loads(text('src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_arcanelamp.json'))
req(recipe['research']=='ARCANELAMP' and recipe['pattern']==[' S ','IAI',' N '],'recipe pattern/research')
req(recipe['key']=={'A':'thaumcraft:arcane_stone','S':'minecraft:glowstone','N':'thaumcraft:nitor','I':'minecraft:iron_ingot'},'recipe key')
req(recipe['ingredients']==['thaumcraft:arcane_stone','thaumcraft:nitor','minecraft:iron_ingot'],'recipe ingredients')
req(recipe['aspects']=={'IGNIS':8,'AER':8,'AQUA':4,'PERDITIO':4},'recipe vis')
state=json.loads(text('src/main/resources/assets/thaumcraft/blockstates/tc4_block_arcane_lamp.json'))
req(len(state['variants'])==6,'six blockstates')
for k,v in state['variants'].items():
    req(v=={'model':'thaumcraft:block/tc4_block_arcane_lamp'},'fixed body variant '+k)
model=json.loads(text('src/main/resources/assets/thaumcraft/models/block/tc4_block_arcane_lamp.json'))
req(len(model['elements'])==1 and model['elements'][0]['from']==[4,2,4] and model['elements'][0]['to']==[12,14,12],'body-only exact model')
req(model['textures']['side']=='thaumcraft:block/tc4/lamp_side' and model['textures']['top']=='thaumcraft:block/tc4/lamp_top','model textures')

with zipfile.ZipFile(R/'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip') as z:
    names=z.namelist()
    def original(suffix):
        n=next((n for n in names if n.endswith('/'+suffix)),None); req(n,'original missing '+suffix); return z.read(n).decode(errors='replace')
    ol=original('thaumcraft/common/tiles/TileArcaneLamp.java')
    oa=original('thaumcraft/common/blocks/BlockAiry.java')
    om=original('thaumcraft/common/blocks/BlockMetalDevice.java')
    oi=original('thaumcraft/common/blocks/BlockMetalDeviceItem.java')
    ob=original('thaumcraft/common/tiles/TileArcaneBore.java')
    orend=original('thaumcraft/client/renderers/tile/TileArcaneLampRenderer.java')
    omodel=original('thaumcraft/client/renderers/models/ModelBoreBase.java')
    orecipe=original('thaumcraft/common/config/ConfigRecipes.java')
    all_java='\n'.join(z.read(n).decode(errors='replace') for n in names if n.endswith('.java'))
    for cur,orig_path in [('src/main/resources/assets/thaumcraft/textures/block/tc4/lamp_side.png','assets/thaumcraft/textures/blocks/lamp_side.png'),
                          ('src/main/resources/assets/thaumcraft/textures/block/tc4/lamp_top.png','assets/thaumcraft/textures/blocks/lamp_top.png'),
                          ('src/main/resources/assets/thaumcraft/textures/models/Bore.png','assets/thaumcraft/textures/models/Bore.png')]:
        n=next(n for n in names if n.endswith('/'+orig_path)); req(sha(cur)==sha_bytes(z.read(n)),'texture hash '+cur)
for t in ('nextInt(16) - this.field_145850_b.field_73012_v.nextInt(16)','func_72976_f(x, z) + 4','if (y < 5) y = 5',
 'func_72957_l(x, y, z) < 9','ConfigBlocks.blockAiry, 3, 3','"orientation"','for (int x = -15; x <= 15; x++)'):
    req(t in ol,'original lamp '+t)
for t in ('if ((md == 1) || (md == 2) || (md == 3)) return 15', 'if ((md == 2) || (md == 3) || (md == 4) || (md == 10) || (md == 11)) return true', 'if ((md == 2) || (md == 3) || (md == 10) || (md == 11)) return true'):
    req(t in oa,'original airy '+t)
for t in ('func_149711_c(3.0F)','func_149752_b(17.0F)','metadata == 7) return new TileArcaneLamp()',
 'instanceof TileArcaneLamp','removeLights()','if (md == 7) return 15'):
    req(t in om,'original metal device '+t)
req('tile.facing = ForgeDirection.getOrientation(side).getOpposite()' in oi,'original six-face placement')
for t in ('nextInt(32) * 2','int p = d / 2 % 4','if ((p == 3) && (this.orientation.offsetY == 0))',
 'ConfigBlocks.blockAiry, 3, 3'):
    req(t in ob,'original bore '+t)
for t in ('textures/models/Bore.png','instanceof TileArcaneBoreBase','dir.getOpposite().ordinal()','this.model.renderNozzle()'):
    req(t in orend,'original renderer '+t)
for t in ('new ModelRenderer(this, 106, 42)','new ModelRenderer(this, 106, 51)','renderNozzle'):
    req(t in omodel,'original model '+t)
req('ConfigResearch.recipes.put("ArcaneLamp", ThaumcraftApi.addArcaneCraftingRecipe("ARCANELAMP"' in orecipe,'original recipe registration')
req(all_java.count('TileArcaneLampLight')==2,'dead TileArcaneLampLight must appear only in its own declaration/source-location comment')

gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=190 and len(methods)==len(set(methods)),f'GameTests {len(methods)}')
for m in ('arcaneLampParityMathMatchesOriginalBounds','arcaneLampAcceptsAnyNonAirSupportAndDropsOnAir',
 'arcaneLampLightMarkerIsAirAndHasNoBlockEntity','arcaneLampOrientationNbtRoundTripsOriginalOrdinal',
 'arcaneLampCleanupUsesOriginalSharedRadiusCube','arcaneBoreAdjacentLampSeedsOriginalTunnelMarker',
 'arcaneLampResearchRecipeAndRarityMatchOriginal'):
    req(m in methods,'GameTest '+m)
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json')); ids=[x['id'] for x in manifest['tests']]
req(tuple(map(int,manifest['version'].split('.'))) >= (11,64,22) and len(ids)>=606 and len(ids)==len(set(ids)),f'manifest {manifest["version"]}/{len(ids)}')
for sid in ('blocks.arcane_lamp_light_marker_air_persistence_no_block_entity','gametest.arcane_lamp_marker_no_block_entity',
 'gametest.arcane_bore_adjacent_lamp_tunnel_marker','client.arcane_lamp_bore_nozzle_all_faces',
 'client.arcane_lamp_arcane_bore_base_connector','jei.arcane_lamp_exact_arcane_recipe'):
    req(sid in ids,'scenario '+sid)
ev=json.loads(text('tools/data/tc4_arcane_lamp_full_source_evidence_v11.64.22.json'))
req(ev['round']=='11.64.22' and ev['source_closure']=='CLOSED' and ev['resource_closure']=='CLOSED','evidence closure')
req(ev['build_status']=='NOT_OBTAINED' and ev['runtime_status']=='NOT_VERIFIED','evidence honesty')
prompt=text('UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md')
req(prompt==text('PROMPT_FOR_FUTURE_CHAT_RU.md'),'prompt copies differ')
for t in ('Один релиз — один предмет или одна цельная механика','SOURCE CLOSED','RESOURCE CLOSED','BUILD VERIFIED','RUNTIME VERIFIED','Упаковка архива без этого файла запрещена'):
    req(t in prompt,'prompt token '+t)
print(f'TC4 v11.64.22 Arcane Lamp full-closure guard: PASS ({len(methods)} GameTests; {len(ids)} scenarios; source/resource/prompt)')
