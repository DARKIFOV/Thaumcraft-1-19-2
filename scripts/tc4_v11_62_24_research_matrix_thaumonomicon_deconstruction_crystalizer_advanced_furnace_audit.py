#!/usr/bin/env python3
"""v11.62.24 Research Matrix/Thaumonomicon/machines parity regression audit."""
from pathlib import Path
import json,sys
ROOT=Path(__file__).resolve().parents[1]
errors=[]
def read(rel):
 p=ROOT/rel
 if not p.exists(): errors.append(f'missing {rel}'); return ''
 return p.read_text(encoding='utf-8',errors='replace')
def require(rel,*tokens):
 s=read(rel)
 for token in tokens:
  if token not in s: errors.append(f'{rel}: missing {token}')
def load(rel):
 try:return json.loads(read(rel))
 except Exception as e: errors.append(f'{rel}: invalid json {e}'); return {}

require('src/main/java/com/darkifov/thaumcraft/research/ResearchNoteState.java',
 'TAG_GRID_SEED = "GridSeed"','TAG_TC4_HEXGRID = "hexgrid"','importOriginalHexGrid',
 'syncOriginalTopLevel','hex.putByte("hexq"','hex.putByte("hexr"','hex.putByte("type"',
 'TC4 counts the candidate while testing','activeAroundAnchor < 2')
require('src/main/java/com/darkifov/thaumcraft/research/ResearchNoteGrid.java',
 'distributeRingRandomly','random.nextInt(ring.size())','consumes one random start value but never applies it','TYPE_RESEARCH_ANCHOR')
require('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchNoteCreator.java',
 'player.getRandom().nextLong()','paperSnapshot','toolsSnapshot')
require('src/main/java/com/darkifov/thaumcraft/blockentity/ResearchTableBlockEntity.java',
 'player.getRandom().nextLong()','paperSnapshot','toolsSnapshot')
require('src/main/java/com/darkifov/thaumcraft/client/screen/ThaumonomiconScreen.java',
 'RenderSystem.enableScissor','RenderSystem.disableScissor','categoryPanX','categoryPanY',
 'draggedBeyondClick','DRAG_THRESHOLD_SQUARED','proportionalBackgroundCoordinate')
require('src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java',
 'renderLinks','renderGrid','renderAspectPalette','drawSaggingThreadLikeTC4',
 'ResearchAspectGraph.canConnect','ClientResearchNoteData.activeAt')
require('src/main/java/com/darkifov/thaumcraft/blockentity/DeconstructionTableBlockEntity.java',
 'BREAK_TICKS = 40','level.random.nextInt(80) < primals.totalAmount()','PlayerAspectKnowledge.addPool',
 'side == Direction.UP ? NO_SLOTS : SLOT')
require('src/main/java/com/darkifov/thaumcraft/menu/DeconstructionTableMenu.java','claimAspect','SimpleContainerData(2)')
require('src/main/java/com/darkifov/thaumcraft/client/screen/DeconstructionTableScreen.java','gui_decontable.png','handleInventoryButtonClick')
require('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaCrystalizerBlockEntity.java',
 'EMPTY_SUCTION = 128','HOLDING_SUCTION = 64','PROCESS_MAX = 200','DRAW_INTERVAL = 5',
 'drainedPoints * 2','ForgeCapabilities.ITEM_HANDLER','ejectCrystal',
 'EssentiaJarBlockEntity','EssentiaReservoirBlockEntity','AlembicBlockEntity',
 'AlchemicalCentrifugeBlockEntity','AlchemicalFurnaceBlockEntity')
require('src/main/java/com/darkifov/thaumcraft/block/EssentiaCrystalItem.java','TAG_ASPECT = "Aspect"','static ItemStack create','static int tint')
matrix=read('src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java')
if 'does not\n        // require the full essentia bill' not in matrix: errors.append('infusion matrix still lacks start-without-full-essentia parity marker')
start=matrix.find('public boolean startInfusion')
end=matrix.find('private InfusionRecipe findMatchingOriginalInfusionRecipe')
if start >= 0 and end > start and 'InfusionProcessHelper.hasAspects' in matrix[start:end]: errors.append('infusion matrix still blocks craftingStart on full essentia preflight')
require('src/main/java/com/darkifov/thaumcraft/infusion/InfusionProcessHelper.java',
 'EssentiaHandler.drainEssentia parity adapter','EssentiaReservoirBlockEntity','AlembicBlockEntity',
 'EssentiaTubeBlockEntity','AlchemicalCentrifugeBlockEntity','furnace.isAdvanced()')
if 'consumeOneAspectSource(level, worldPosition, aspect)' not in matrix: errors.append('infusion matrix does not use generalized nearby essentia endpoint drain')
require('src/main/java/com/darkifov/thaumcraft/blockentity/AlchemicalFurnaceBlockEntity.java',
 'ADVANCED_CAPACITY = 500','ADVANCED_MAX_POWER = 500','ADVANCED_RECHARGE_CV = 50',
 'processAdvancedItem','advancedHeat < visSize * 2','advancedEntropy < visSize','advancedWater < visSize',
 '5.0F + Math.max','ignisCreditCv','perditioCreditCv','aquaCreditCv','tickAdvanced')
require('src/main/java/com/darkifov/thaumcraft/block/AlchemicalFurnaceBlock.java',
 'void entityInside','ADVANCED_ALCHEMICAL_FURNACE','processAdvancedItem','ItemEntity')
require('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java','AdvancedFurnaceSource','takeAdvancedOutput')
require('src/main/java/com/darkifov/thaumcraft/recipe/TC4RecipeItemResolver.java',
 'blockTable:14','thaumcraft:deconstruction_table','blockTube:2','blockTube:7','thaumcraft:essentia_crystalizer')
for rel in [
 'src/main/resources/assets/thaumcraft/blockstates/deconstruction_table.json',
 'src/main/resources/assets/thaumcraft/blockstates/essentia_crystalizer.json',
 'src/main/resources/assets/thaumcraft/models/block/deconstruction_table.json',
 'src/main/resources/assets/thaumcraft/models/block/essentia_crystalizer.json',
 'src/main/resources/assets/thaumcraft/textures/gui/gui_decontable.png',
 'src/main/resources/assets/thaumcraft/textures/models/crystalizer.obj',
 'src/main/resources/data/thaumcraft/loot_tables/blocks/deconstruction_table.json',
 'src/main/resources/data/thaumcraft/loot_tables/blocks/essentia_crystalizer.json']:
 if not (ROOT/rel).exists(): errors.append(f'missing {rel}')

decon=load('src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_deconstructor.json')
if decon.get('pattern')!=[' S ','ATP'] or decon.get('result',{}).get('item')!='thaumcraft:deconstruction_table': errors.append('deconstructor recipe mismatch')
if decon.get('key',{}).get('A')!='minecraft:golden_axe' or decon.get('key',{}).get('P')!='minecraft:golden_pickaxe': errors.append('deconstructor tools mismatch')
crys=load('src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_essentia_crystalizer.json')
if crys.get('pattern')!=['IDI','QCQ','WTW'] or crys.get('aspects')!={'AQUA':5,'TERRA':15,'ORDO':5}: errors.append('crystalizer recipe mismatch')
require('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java','DECONSTRUCTION_TABLE','ESSENTIA_CRYSTALIZER','ESSENTIA_CRYSTAL','DECONSTRUCTION_TABLE_MENU')
require('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java','DeconstructionTableScreen','EssentiaCrystalizerRenderer','EssentiaCrystalItem.tint')
require('build.gradle',"version = '11.62.24'","version = '11.62.23'")
require('src/main/resources/META-INF/mods.toml','version="11.62.24"','version="11.62.23"')
require('.github/workflows/main.yml','tc4_v11_62_24_research_matrix_thaumonomicon_deconstruction_crystalizer_advanced_furnace_audit.py','v11.62.24-github-jar','v11.62.24-build-reports')
m=load('src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_v11_62_24_research_matrix_thaumonomicon_deconstruction_crystalizer_advanced_furnace.json')
if m.get('version')!='11.62.24' or not m.get('strict_original'): errors.append('v11.62.24 mapping metadata mismatch')
if errors:
 print('TC4 v11.62.24 Research Matrix/Thaumonomicon/machines audit FAILED:')
 for e in errors: print(' -',e)
 sys.exit(1)
print('TC4 v11.62.24 Research Matrix + Thaumonomicon + machines audit: OK')
