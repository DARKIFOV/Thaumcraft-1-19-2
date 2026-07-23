#!/usr/bin/env python3
"""v11.64.16 guard: complete Hungry Chest source/resource closure."""
from pathlib import Path
import hashlib
import json
import re
import zipfile

R = Path(__file__).resolve().parents[1]

def text(path):
    return (R / path).read_text(encoding='utf-8')

def req(condition, message):
    if not condition:
        raise SystemExit('TC4 v11.64.16 Hungry Chest full-closure guard: FAIL: ' + message)

def version_tuple(raw):
    m = re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']', raw)
    req(m is not None, 'version parse')
    return tuple(map(int, m.groups()))

def sha(path):
    return hashlib.sha256((R / path).read_bytes()).hexdigest()

req(version_tuple(text('build.gradle')) >= (11, 64, 16), 'build version >= 11.64.16')
req(version_tuple(text('src/main/resources/META-INF/mods.toml')) >= (11, 64, 16), 'mods version >= 11.64.16')

contract = text('src/main/java/com/darkifov/thaumcraft/blockentity/TC4HungryChestParity.java')
for token in (
    'CONTRACT_VERSION = "11.64.16"', 'INVENTORY_SIZE = 27', 'MAX_STACK_SIZE = 64',
    'HORIZONTAL_MIN = 1.0D / 16.0D', 'HORIZONTAL_MAX = 15.0D / 16.0D',
    'OUTLINE_Y_MAX = 14.0D / 16.0D', 'COLLISION_Y_MAX = 15.0D / 16.0D',
    'BLOCK_HARDNESS = 2.5F', 'BLOCK_EXPLOSION_RESISTANCE = 12.5F',
    'LID_STEP = 0.1F', 'LID_CLOSE_SOUND_THRESHOLD = 0.5F',
    'OPENERS_EVENT_ID = 1', 'EAT_EVENT_ID = 2', 'EAT_EVENT_DATA = 2', 'EAT_LID_NUDGE = 0.2F',
    'CHEST_SOUND_VOLUME = 0.5F', 'EAT_SOUND_VOLUME = 0.25F',
    'DROP_MIN = 10', 'DROP_RANDOM_BOUND = 21', 'DROP_POSITION_MIN = 0.1F',
    'DROP_POSITION_RANGE = 0.8F', 'DROP_MOTION_SIGMA = 0.05F', 'DROP_MOTION_Y_BIAS = 0.2F',
    'RECIPE_AER = 5', 'RECIPE_ORDO = 3', 'RECIPE_PERDITIO = 3',
    'RESEARCH_FAMES = 3', 'RESEARCH_VACUOS = 3', 'RESEARCH_X = -1', 'RESEARCH_Y = 0',
    'nextLidAngle(', 'shouldPlayOpenSound(', 'shouldPlayCloseSound(', 'eatSoundPitch(',
    'easedLid(', 'nextDropCount(', 'intersectsCollision('):
    req(token in contract, 'missing pure contract token: ' + token)

block = text('src/main/java/com/darkifov/thaumcraft/block/HungryChestBlock.java')
for token in (
    'OUTLINE_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D)',
    'COLLISION_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D)',
    'return RenderShape.ENTITYBLOCK_ANIMATED', 'player.openMenu(chest)',
    'intersectsOriginalCollision(itemEntity.getBoundingBox(), pos)', 'chest.eat(itemEntity)',
    'TC4HungryChestParity.intersectsCollision(', 'dropContentsOriginal(level, pos, chest, level.random)',
    'TC4HungryChestParity.nextDropCount(', 'random.nextInt(TC4HungryChestParity.DROP_RANDOM_BOUND)',
    'droppedStack.setCount(count)', 'stored.shrink(count)', 'random.nextGaussian()',
    'AbstractContainerMenu.getRedstoneSignalFromContainer(chest)', 'return false;'):
    req(token in block, 'block production path missing: ' + token)
req('Containers.dropContents' not in block, 'vanilla break-drop shortcut must not replace original 10..30 splitting')

be = text('src/main/java/com/darkifov/thaumcraft/blockentity/HungryChestBlockEntity.java')
for token in (
    'SIZE = TC4HungryChestParity.INVENTORY_SIZE', 'NonNullList.withSize(SIZE, ItemStack.EMPTY)',
    'private final InvWrapper forgeItemHandler = new InvWrapper(this)', 'ForgeCapabilities.ITEM_HANDLER',
    'ItemHandlerHelper.insertItemStacked(forgeItemHandler, original.copy(), false)',
    'int moved = original.getCount() - remainder.getCount()', 'if (moved <= 0)',
    'TC4HungryChestParity.eatSoundPitch(level.random.nextFloat(), level.random.nextFloat())',
    'TC4HungryChestParity.EAT_SOUND_VOLUME', 'itemEntity.discard()', 'itemEntity.setItem(remainder)',
    'TC4HungryChestParity.EAT_LID_NUDGE', 'TC4HungryChestParity.EAT_EVENT_ID',
    'TC4HungryChestParity.EAT_EVENT_DATA', 'openCount = data', 'float eventAngle = data / 10.0F',
    'ChestMenu.threeRows(containerId, playerInventory, this)', 'return TC4HungryChestParity.MAX_STACK_SIZE',
    'return level != null && level.getBlockEntity(worldPosition) == this',
    'openCount += 1', 'openCount -= 1', 'ContainerHelper.saveAllItems(tag, items)',
    'ContainerHelper.loadAllItems(tag, items)', 'level.updateNeighbourForOutputSignal',
    'TC4HungryChestParity.nextLidAngle(', 'TC4HungryChestParity.shouldPlayOpenSound(',
    'TC4HungryChestParity.shouldPlayCloseSound('):
    req(token in be, 'block-entity production path missing: ' + token)
req('recountOpeners' not in be and 'ticksSinceSync' not in be, 'invented periodic opener recount remains')
req('distanceToSqr' not in be and '<= 64.0D' not in be, 'invented menu distance gate remains')
req('Math.max(0, openCount' not in be and 'Math.max(0, data)' not in be, 'original opener/event values are still clamped')
req('tag.putInt("OpenCount"' not in be and 'tag.putFloat("LidAngle"' not in be,
    'transient opener/lid state must not be persisted')

renderer = text('src/main/java/com/darkifov/thaumcraft/client/render/HungryChestRenderer.java')
for token in (
    'textures/original/thaumcraft4/models/chesthungry.png',
    'TC4HungryChestParity.easedLid(lidAngle)', '-(open * ((float) Math.PI / 2.0F))',
    'poseStack.translate(0.0D, 1.0D, 1.0D)', 'poseStack.scale(1.0F, -1.0F, -1.0F)',
    'case NORTH -> 180.0F', 'case WEST -> 90.0F', 'case EAST -> -90.0F'):
    req(token in renderer, 'world renderer mismatch: ' + token)
model = text('src/main/java/com/darkifov/thaumcraft/client/render/model/TC4HungryChestModel.java')
for token in (
    'texOffs(0, 19)', 'addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F)',
    'addBox(0.0F, -5.0F, -14.0F, 14.0F, 5.0F, 14.0F)',
    'PartPose.offset(1.0F, 7.0F, 15.0F)',
    'addBox(-1.0F, -2.0F, -15.0F, 2.0F, 4.0F, 1.0F)',
    'PartPose.offset(8.0F, 7.0F, 15.0F)', 'LayerDefinition.create(mesh, 64, 64)'):
    req(token in model, 'ModelChest geometry/UV mismatch: ' + token)
item_renderer = text('src/main/java/com/darkifov/thaumcraft/client/render/HungryChestItemRenderer.java')
for token in (
    'poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F))',
    'poseStack.translate(-0.5D, -0.5D, -0.5D)',
    'HungryChestRenderer.renderModel(model, Direction.SOUTH, 0.0F'):
    req(token in item_renderer, 'item renderer mismatch: ' + token)
for forbidden in ('type.firstPerson()', 'TransformType.GUI', 'TransformType.GROUND', 'TransformType.FIXED'):
    req(forbidden not in item_renderer, 'approximate per-context item transform remains: ' + forbidden)

mod = text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
for token in (
    'HUNGRY_CHEST = hungryChestBlock("hungry_chest"',
    'TC4HungryChestParity.BLOCK_HARDNESS, TC4HungryChestParity.BLOCK_EXPLOSION_RESISTANCE',
    '.sound(SoundType.WOOD).noOcclusion()', 'new HungryChestBlockItem(block.get()',
    'BlockEntityType.Builder.of(HungryChestBlockEntity::new, HUNGRY_CHEST.get())'):
    req(token in mod, 'registration mismatch: ' + token)

for path in (
    'src/main/resources/assets/thaumcraft/models/item/hungry_chest.json',
    'src/main/resources/assets/thaumcraft/models/block/hungry_chest.json',
    'src/main/resources/assets/thaumcraft/blockstates/hungry_chest.json',
    'src/main/resources/data/thaumcraft/loot_tables/blocks/hungry_chest.json',
    'src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_hungrychest.json'):
    req((R / path).is_file(), 'resource missing: ' + path)
item_model = json.loads(text('src/main/resources/assets/thaumcraft/models/item/hungry_chest.json'))
req(item_model.get('parent') == 'builtin/entity' and item_model.get('gui_light') == 'front',
    'item model must use BEWLR builtin/entity')
block_model = json.loads(text('src/main/resources/assets/thaumcraft/models/block/hungry_chest.json'))
req(block_model['textures']['particle'] == 'thaumcraft:block/woodplain', 'original woodplain particle icon mismatch')

original_texture = 'src/main/resources/assets/thaumcraft/original_tc4_1710/textures/models/chesthungry.png'
for runtime in (
    'src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/models/chesthungry.png',
    'src/main/resources/assets/thaumcraft/textures/models/chesthungry.png'):
    req(sha(runtime) == sha(original_texture), 'runtime chest texture differs from retained original: ' + runtime)

recipe = json.loads(text('src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_hungrychest.json'))
req(recipe['research'] == 'HUNGRYCHEST', 'arcane recipe research mismatch')
req(recipe['pattern'] == ['WTW','W W','WWW'], 'arcane pattern mismatch')
req(recipe['key'] == {'W':'#minecraft:planks','T':'minecraft:oak_trapdoor'}, 'arcane key mismatch')
req(recipe['aspects'] == {'AER':5,'ORDO':3,'PERDITIO':3}, 'arcane aspects mismatch')
req(recipe['result'] == {'item':'thaumcraft:hungry_chest','count':1}, 'arcane result mismatch')
research = text('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchRuntimeBridge.java')
for token in (
    '"HUNGRYCHEST", "Hungry Chest", "A chest that doesn\'t wait to be opened"',
    '"GOLEMANCY", -1, 0, 1', 'aspects("FAMES", 3, "VACUOS", 3)',
    'new String[] {"secondary"}', 'new String[] {"tc.research_page.HUNGRYCHEST.1"}',
    'new String[] {"TEXT", "ARCANE_CRAFTING"}', 'new String[] {"HungryChest"}',
    'new String[] {"HUNGRYCHEST"}'):
    req(token in research, 'research graph mismatch: ' + token)

with zipfile.ZipFile(R / 'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip') as z:
    def member(suffix):
        name = next((n for n in z.namelist() if n.endswith('/' + suffix)), None)
        req(name is not None, 'original source member missing: ' + suffix)
        return z.read(name).decode('utf-8', errors='replace')
    source_block = member('thaumcraft/common/blocks/BlockChestHungry.java')
    source_tile = member('thaumcraft/common/tiles/TileChestHungry.java')
    source_world_renderer = member('thaumcraft/client/renderers/tile/TileChestHungryRenderer.java')
    source_item_renderer = member('thaumcraft/client/renderers/block/BlockChestHungryRenderer.java')
    source_recipe = member('thaumcraft/common/config/ConfigRecipes.java')
    source_research = member('thaumcraft/common/config/ConfigResearch.java')
for token in (
    'func_149711_c(2.5F)', 'func_149672_a(field_149766_f)',
    'par2 + var5, par3, par4 + var5, par2 + 1 - var5, par3 + 1 - var5, par4 + 1 - var5',
    'func_149676_a(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F)',
    'nextInt(21) + 10', '* 0.8F + 0.1F', 'nextGaussian() * var15',
    'InventoryUtils.placeItemStackIntoInventory', '"random.eat", 0.25F',
    'func_147452_c(x, y, z, ConfigBlocks.blockChestHungry, 2, 2)'):
    req(token in source_block, 'original BlockChestHungry oracle mismatch: ' + token)
for token in (
    'return 27', 'return 64', 'return this.field_145850_b.func_147438_o',
    'this.prevLidAngle = this.lidAngle', 'float var1 = 0.1F',
    'random.chestopen', 'random.chestclosed', 'this.numUsingPlayers += 1',
    'this.numUsingPlayers -= 1', 'par1NBTTagCompound.func_74782_a("Items", var2)',
    'if (this.lidAngle < par2 / 10.0F)'):
    req(token in source_tile, 'original TileChestHungry oracle mismatch: ' + token)
for token in (
    'textures/models/chesthungry.png', 'var12 = 1.0F - var12',
    'var12 = 1.0F - var12 * var12 * var12', 'var12 * 3.1415927F / 2.0F'):
    req(token in source_world_renderer, 'original world renderer oracle mismatch: ' + token)
for token in ('GL11.glRotatef(90.0F', 'GL11.glTranslatef(-0.5F, -0.5F, -0.5F)'):
    req(token in source_item_renderer, 'original item renderer oracle mismatch: ' + token)
req('addArcaneCraftingRecipe("HUNGRYCHEST"' in source_recipe and 'Aspect.AIR, 5' in source_recipe
    and 'Aspect.ORDER, 3' in source_recipe and 'Aspect.ENTROPY, 3' in source_recipe,
    'original recipe oracle mismatch')
req('new ResearchItem("HUNGRYCHEST", "GOLEMANCY"' in source_research
    and 'Aspect.HUNGER, 3' in source_research and 'Aspect.VOID, 3' in source_research
    and 'setSecondary()' in source_research and 'setParents(new String[] { "HUNGRYCHEST" })' in source_research,
    'original research oracle mismatch')

langs = [json.loads(text('src/main/resources/assets/thaumcraft/lang/en_us.json')),
         json.loads(text('src/main/resources/assets/thaumcraft/lang/ru_ru.json'))]
for lang in langs:
    for key in ('block.thaumcraft.hungry_chest','container.thaumcraft.hungry_chest',
                'tc.research_name.HUNGRYCHEST','tc.research_text.HUNGRYCHEST',
                'tc.research_page.HUNGRYCHEST.1'):
        req(key in lang and lang[key], 'language key missing: ' + key)

gametest = text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods = re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(', gametest, re.S)
req(len(methods) >= 157 and len(methods) == len(set(methods)),
    f'expected at least 157 unique GameTests, got {len(methods)}')
for method in (
    'hungryChestEatsPersistsAndExposesSingleInventory', 'hungryChestFullContractMatchesOriginal',
    'hungryChestCollisionFilterPartialAndRejectionMatchOriginal',
    'hungryChestLidEventsAndRemoteValidityMatchOriginal',
    'hungryChestBreakDropsPreserveOriginalChunksAndNbt',
    'hungryChestAutomationComparatorAndNbtMatchOriginal'):
    req(method in methods, 'Hungry Chest GameTest missing: ' + method)

manifest = json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids = [case['id'] for case in manifest['tests']]
req(tuple(map(int, manifest['version'].split('.'))) >= (11,64,16), 'manifest version >= 11.64.16')
req(len(ids) >= 518 and len(ids) == len(set(ids)), f'expected at least 518 unique scenarios, got {len(ids)}')
for scenario in (
    'gametest.hungry_chest_full_contract','gametest.hungry_chest_collision_partial_rejection',
    'gametest.hungry_chest_lid_remote_validity','gametest.hungry_chest_break_chunks_nbt',
    'gametest.hungry_chest_automation_comparator_nbt','gameplay.hungry_chest_original_collision_body',
    'gameplay.hungry_chest_insertion_order_and_eat_event','gameplay.hungry_chest_original_break_distribution',
    'client.hungry_chest_exact_model_texture','client.hungry_chest_lid_easing_and_sounds',
    'client.hungry_chest_item_original_transform','persistence.hungry_chest_items_only_nbt',
    'automation.hungry_chest_all_sides_and_comparator','research.hungry_chest_recipe_and_graph',
    'multiplayer.hungry_chest_server_owned_inventory_events'):
    req(scenario in ids, 'manifest scenario missing: ' + scenario)

evidence = json.loads(text('tools/data/tc4_hungry_chest_full_source_evidence_v11.64.16.json'))
req(evidence['round'] == '11.64.16', 'evidence round mismatch')
req(evidence['source_closure'] == 'CLOSED' and evidence['resource_closure'] == 'CLOSED', 'closure status drifted')
req(evidence['build_status'] == 'NOT_OBTAINED' and evidence['runtime_status'] == 'NOT_VERIFIED',
    'build/runtime honesty drifted')

prompt = text('UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md')
prompt2 = text('PROMPT_FOR_FUTURE_CHAT_RU.md')
req(prompt == prompt2, 'future-chat prompt must match universal prompt exactly')
for token in ('Один релиз — один предмет или одна цельная механика', 'SOURCE CLOSED', 'RESOURCE CLOSED',
              'BUILD VERIFIED', 'RUNTIME VERIFIED', 'Нельзя писать «портировано на 100%»',
              'обязателен в корне каждого следующего полного исходного архива',
              'Упаковка архива без этого файла запрещена'):
    req(token in prompt, 'universal prompt rule missing: ' + token)

print(f'TC4 v11.64.16 Hungry Chest full-closure guard: PASS '
      f'({len(methods)} GameTests; {len(ids)} scenarios; collision/insertion/lid/drops/NBT/model/recipe/research/prompt)')
