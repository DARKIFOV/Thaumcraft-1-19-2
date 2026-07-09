#!/usr/bin/env python3
from pathlib import Path
import json, sys
ROOT = Path(__file__).resolve().parents[1]
errors=[]

def read(rel):
    return (ROOT/rel).read_text(encoding='utf-8')

def exists(rel):
    return (ROOT/rel).exists()

# Version/document markers
if "version = '5.02.0'" not in read('build.gradle'):
    errors.append('build.gradle was not advanced to 5.02.0')
if 'Stage483-502' not in read('src/main/java/com/darkifov/thaumcraft/essentia/TC4DistillationRuntime.java'):
    errors.append('TC4DistillationRuntime missing Stage483-502 marker')

# Original TC4 dataflow: furnace -> alembics -> tubes -> jars, not furnace -> tubes.
conn = read('src/main/java/com/darkifov/thaumcraft/essentia/EssentiaTubeConnections.java')
tube = read('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java')
furnace = read('src/main/java/com/darkifov/thaumcraft/blockentity/AlchemicalFurnaceBlockEntity.java')
if 'AlchemicalFurnaceBlockEntity' in conn:
    errors.append('EssentiaTubeConnections still treats AlchemicalFurnaceBlockEntity as a tube endpoint')
if 'instanceof AlchemicalFurnaceBlockEntity' in tube or 'FurnaceSource' in tube:
    errors.append('EssentiaTubeBlockEntity still pulls directly from the furnace instead of alembics')
if 'TC4DistillationRuntime.tickFurnaceToAlembics(level, pos, furnace)' not in furnace:
    errors.append('AlchemicalFurnaceBlockEntity does not tick the TC4 furnace->alembic adapter')

runtime = read('src/main/java/com/darkifov/thaumcraft/essentia/TC4DistillationRuntime.java')
for token in ['ORIGINAL_MAX_ALEMBICS_ABOVE_FURNACE = 5', 'ORIGINAL_DISTILLATION_INTERVAL_TICKS = 5', 'ORIGINAL_DISTILLATION_STEP = 1', 'findAcceptingAlembic']:
    if token not in runtime:
        errors.append(f'TC4DistillationRuntime missing {token}')

# Jar parity: label facing is stored with original facing NBT and rendered from the clicked side.
jar_be = read('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaJarBlockEntity.java')
jar_rt = read('src/main/java/com/darkifov/thaumcraft/jar/JarTubeInteractionRuntime.java')
jar_renderer = read('src/main/java/com/darkifov/thaumcraft/client/render/EssentiaJarRenderer.java')
for token in ['labelFacing', 'tag.putByte("facing"', 'Direction.from3DDataValue']:
    if token not in jar_be:
        errors.append(f'EssentiaJarBlockEntity missing original facing/label token {token}')
if 'applyLabelToJar(EssentiaJarBlockEntity jar, Player player, ItemStack held, ItemStack otherHand, Direction clickedFace)' not in jar_rt:
    errors.append('Jar label interaction does not preserve clicked face')
if 'renderJarLabel(jar.filterAspect(), jar.labelFacing()' not in jar_renderer:
    errors.append('Jar renderer does not use saved label facing')

# Original active assets and models.
for rel in [
    'src/main/resources/assets/thaumcraft/textures/block/pipe_1.png',
    'src/main/resources/assets/thaumcraft/textures/block/pipe_2.png',
    'src/main/resources/assets/thaumcraft/textures/block/pipe_filter.png',
    'src/main/resources/assets/thaumcraft/textures/block/pipe_filter_core.png',
    'src/main/resources/assets/thaumcraft/textures/block/pipe_oneway.png',
    'src/main/resources/assets/thaumcraft/textures/block/pipe_restrict.png',
    'src/main/resources/assets/thaumcraft/textures/block/pipe_buffer.png',
    'src/main/resources/assets/thaumcraft/textures/block/pipe_valve.png',
    'src/main/resources/assets/thaumcraft/textures/models/alembic.obj',
    'src/main/resources/assets/thaumcraft/textures/models/alembic.png',
    'src/main/resources/assets/thaumcraft/textures/models/jar.png',
    'src/main/resources/assets/thaumcraft/textures/models/jar_void.png',
    'src/main/resources/assets/thaumcraft/textures/models/label.png',
]:
    if not exists(rel):
        errors.append(f'Missing original TC4 essentia asset: {rel}')

if 'minecraft:block/cube_all' in read('src/main/resources/assets/thaumcraft/models/block/alembic.json'):
    errors.append('Alembic block model is still cube_all placeholder')
if 'thaumcraft:block/pipe_filter_core' not in read('src/main/resources/assets/thaumcraft/models/block/essentia_tube_center_filter.json'):
    errors.append('Filter tube center does not use original pipe_filter_core texture')
if 'thaumcraft:block/pipe_valve' not in read('src/main/resources/assets/thaumcraft/models/block/essentia_valve.json'):
    errors.append('Essentia valve does not use original pipe_valve texture')

# Duplicate recipe cleanup and resolver mapping away from tc4_* mirrors for real TC4 blocks.
if exists('src/main/resources/data/thaumcraft/recipes/essentia_jar_original_style.json'):
    errors.append('Duplicate essentia_jar_original_style recipe still present')
if exists('src/main/resources/data/thaumcraft/recipes/alembic_original_style.json'):
    errors.append('Duplicate alembic_original_style recipe still present')
jar_recipe = json.loads(read('src/main/resources/data/thaumcraft/recipes/essentia_jar.json'))
if jar_recipe.get('result', {}).get('item') != 'thaumcraft:essentia_jar' or jar_recipe.get('key', {}).get('P', {}).get('tag') != 'minecraft:wooden_slabs':
    errors.append('Essentia jar recipe is not the cleaned original-style 1.19.2 adapter')
resolver = read('src/main/java/com/darkifov/thaumcraft/recipe/TC4RecipeItemResolver.java')
for token in ['BLOCK_META.put("blockJar:0", "thaumcraft:essentia_jar")', 'BLOCK_META.put("blockMetalDevice:1", "thaumcraft:alembic")', 'BLOCK_META.put("blockTube:0", "thaumcraft:essentia_tube")']:
    if token not in resolver:
        errors.append(f'Resolver missing real core block mapping: {token}')

if errors:
    print('Stage483-502 audit FAILED:')
    for e in errors:
        print(' -', e)
    sys.exit(1)
print('Stage483-502 audit OK')
