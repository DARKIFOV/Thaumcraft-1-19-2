#!/usr/bin/env python3
from pathlib import Path
import hashlib,json,re,zipfile
R=Path(__file__).resolve().parents[1]
def text(p): return (R/p).read_text(encoding='utf-8',errors='replace')
def req(ok,msg):
    if not ok: raise SystemExit('TC4 v11.64.26 Thaumonomicon full-closure guard: FAIL: '+msg)
def ver(p):
    m=re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']',text(p));req(m,'version '+p);return tuple(map(int,m.groups()))
def sha(p): return hashlib.sha256((R/p).read_bytes()).hexdigest()
req(ver('build.gradle')>=(11,64,26),'build version')
req(ver('src/main/resources/META-INF/mods.toml')>=(11,64,26),'mods version')
par=text('src/main/java/com/darkifov/thaumcraft/research/TC4ThaumonomiconParity.java')
for t in ('CONTRACT_VERSION = "11.64.26"','MAX_STACK_SIZE = 1','CHEAT_ASPECT_POOL = 50','BROWSER_WIDTH = 256','BROWSER_HEIGHT = 230','MAP_WIDTH = 224','MAP_HEIGHT = 196','RESEARCH_CELL = 24','DEFAULT_LAST_X = -5','DEFAULT_LAST_Y = -6','OPEN_PAGE_VOLUME = 1.0F','PAGE_TURN_VOLUME = 0.66F','CATEGORY_CLICK_VOLUME = 0.4F','spreadStart','previousSpread','nextSpread','maxFirstPage'):
    req(t in par,'parity '+t)
loot=text('src/main/java/com/darkifov/thaumcraft/research/TC4ThaumonomiconLootParity.java')
for t in ('CONTRACT_VERSION = "11.64.26"','RARE_CHEST_WEIGHT = 1','WIZARD_TOWER_WEIGHT = 20','MIN_COUNT = 1','MAX_COUNT = 1','dungeonChest','pyramidJungleChest','pyramidDesertyChest','mineshaftCorridor','strongholdCorridor','strongholdCrossing','strongholdLibrary','requiresOriginalWizardTowerIntegration','requiresLegacyGlobalChestIntegration'):
    req(t in loot,'loot contract '+t)
normal=text('src/main/java/com/darkifov/thaumcraft/block/ThaumonomiconItem.java')
for t in ('OriginalResearchProgression.seedAutoUnlocks','repairCompletedSiblingsOnBookOpen','ThaumcraftNetwork.syncResearch','ThaumcraftNetwork.syncAspectKnowledge','ClientHooks::openThaumonomicon'):
    req(t in normal,'normal item '+t)
req('Opening the Thaumonomicon' not in normal and 'displayClientMessage' not in normal,'invented normal-book chat')
cheat=text('src/main/java/com/darkifov/thaumcraft/block/CreativeThaumonomiconItem.java')
for t in ('ResearchRegistry.originalEntries()','applyUnlockSideEffects','!PlayerAspectKnowledge.knows','PlayerAspectKnowledge.discover','setPoolAmount','CHEAT_ASPECT_POOL','Cheat Sheet','DARK_PURPLE'):
    req(t in cheat,'cheat item '+t)
req('displayClientMessage' not in cheat,'invented Cheat Sheet summary chat')
progress=text('src/main/java/com/darkifov/thaumcraft/research/OriginalResearchProgression.java')
for t in ('repairCompletedSiblingsOnBookOpen','ResearchRegistry.originalEntries()','entry.siblings()','PlayerThaumData.unlockResearch','applyUnlockSideEffects'):
    req(t in progress,'sibling repair '+t)
bridge=text('src/main/java/com/darkifov/thaumcraft/research/OriginalResearchBridge.java')
for t in ('completeSecondaryFromThaumonomicon','OriginalAspectWallet.consume','PlayerThaumData.unlockResearch','entry.siblings()'):
    req(t in bridge,'secondary path '+t)
packet=text('src/main/java/com/darkifov/thaumcraft/network/RequestCompleteSelectedResearchPacket.java')
for t in ('researchKey','OriginalResearchBridge.byKey','completeSecondaryFromThaumonomicon','TC4ResearchNoteCreator.create','syncResearch','syncAspectKnowledge'):
    req(t in packet,'packet '+t)
req('displayClientMessage' not in packet and 'Not enough' not in packet,'invented packet feedback')
note=text('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchNoteCreator.java')
for t in ('findPaper','findScribingTools','consumeInk','paper.shrink(1)','ResearchNoteState.initialize','TC4Sounds.event("learn")','0.75F'):
    req(t in note,'note path '+t)
req('displayClientMessage' not in note,'invented note feedback')
client=text('src/main/java/com/darkifov/thaumcraft/client/ClientHooks.java')
for t in ('TC4ThaumonomiconPageHistory.clear()','TC4Sounds.event("page")','OPEN_PAGE_VOLUME','GUI_SOUND_PITCH','new ThaumonomiconScreen()'):
    req(t in client,'open client '+t)
state=text('src/main/java/com/darkifov/thaumcraft/client/screen/TC4ThaumonomiconClientState.java')
for t in ('selectedCategory','DEFAULT_PAN_X','DEFAULT_PAN_Y','save(','panX','panY'):
    req(t in state,'client state '+t)
req('Map<' not in state and 'EnumMap' not in state,'per-category pan must not return')
history=text('src/main/java/com/darkifov/thaumcraft/client/screen/TC4ThaumonomiconPageHistory.java')
for t in ('ArrayDeque','push(','pop()','isEmpty()','clear()'):
    req(t in history,'page history '+t)
browser=text('src/main/java/com/darkifov/thaumcraft/client/screen/ThaumonomiconScreen.java')
for t in ('PANE_WIDTH = 256','PANE_HEIGHT = 230','TC4ThaumonomiconClientState.category()','TC4ThaumonomiconClientState.panX()','TC4ThaumonomiconClientState.save','CATEGORY_CLICK_VOLUME','popupUntil = System.currentTimeMillis() + 3000L','return super.mouseScrolled','TC4ThaumonomiconPageHistory.clear()','requestCompleteSelectedResearchFromClient'):
    req(t in browser,'browser '+t)
for t in ('hasResearchSupplies(','stack.is(Items.PAPER)','ScribingToolsItem.hasInk(stack)','ResearchNoteState.target(stack)','!ResearchNoteState.solved(stack)'): req(t in browser,'primary precondition '+t)
page=text('src/main/java/com/darkifov/thaumcraft/client/screen/TC4ResearchPageScreen.java')
for t in ('spreadStart(initialPage)','TC4ThaumonomiconPageHistory.push','TC4ThaumonomiconPageHistory.pop','PAGE_TURN_VOLUME','return super.mouseScrolled','TC4ThaumonomiconPageHistory.clear()','minecraft.setScreen(parent)'):
    req(t in page,'page GUI '+t)
wand=text('src/main/java/com/darkifov/thaumcraft/block/WandItem.java')
for t in ('state.is(Blocks.BOOKSHELF)','WandFocusRuntime.hasFocus(wandStack)','level.removeBlock(pos, false)','new SpecialItemEntity','book.setDeltaMovement(Vec3.ZERO)','ParticleTypes.ENCHANT','TC4Sounds.event("wand")'):
    req(t in wand,'bookshelf transform '+t)
bookshelf=wand[wand.index('if (state.is(Blocks.BOOKSHELF))'):wand.index('if (state.is(ThaumcraftMod.TABLE.get()))')]
req('consumeTransformationCost' not in bookshelf and 'Aspect.ORDO' not in bookshelf,'bookshelf transformation must cost no vis')
recipe=text('src/main/java/com/darkifov/thaumcraft/recipe/TC4RecipeRuntimeBridge.java')
req('r("Thaumonomicon", Kind.COMPOUND' in recipe and 'new String[] {"1", "2", "1"}' in recipe,'compound recipe')
mod=text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
req('.stacksTo(1).rarity(Rarity.UNCOMMON)' in mod and '.stacksTo(1).rarity(Rarity.EPIC)' in mod,'normal/Cheat properties')
aspects=text('src/main/java/com/darkifov/thaumcraft/source/TC4ObjectAspectRegistry.java')
req('exact("thaumcraft:thaumonomicon", aspects(Aspect.COGNITIO, 10, Aspect.PRAECANTATIO, 2, Aspect.ARBOR, 1))' in aspects,'object aspects')
orig=R/'reference/tc4_source/Thaumcraft4-1.7.10-master'
source_zip=R/'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip'
def original_bytes(rel):
    disk=orig/rel
    if disk.is_file(): return disk.read_bytes()
    with zipfile.ZipFile(source_zip) as z: return z.read('Thaumcraft4-1.7.10-master/'+rel)
def original_text(rel): return original_bytes(rel).decode('utf-8',errors='replace')
for p,tokens in [
('thaumcraft/common/items/relics/ItemThaumonomicon.java',('func_77625_d(1)','completeResearch(player, sib)','completeAspect(player, aspect, (short)50)','PacketSyncResearch','PacketSyncAspects','"thaumcraft:page", 1.0F, 1.0F','EnumRarity.uncommon','EnumRarity.epic','Cheat Sheet')),
('thaumcraft/common/items/wands/WandManager.java',('if (wand.getFocus(itemstack) != null) return false','EntitySpecialItem','field_70181_x = 0.0D','PacketFXBlockSparkle','"thaumcraft:wand", 1.0F, 1.0F')),
('thaumcraft/client/gui/GuiResearchBrowser.java',('lastX = -5','lastY = -6','selectedCategory = null','lastX * 24','System.currentTimeMillis() + 3000L')),
('thaumcraft/client/gui/GuiResearchRecipe.java',('static LinkedList<Object[]> history','history.clear()','"thaumcraft:page", 0.66F, 1.0F','history.pop()')),
('thaumcraft/common/config/Config.java',('new ItemStack(ConfigItems.itemThaumonomicon)','ChestGenHooks.addItem("dungeonChest"','ChestGenHooks.addItem("strongholdLibrary"','new WeightedRandomChestContent(is, 1, 1, 1)')),
('thaumcraft/common/lib/world/ComponentWizardTower.java',('ConfigItems.itemThaumonomicon, 0, 1, 1, 20',)),
('thaumcraft/common/config/ConfigResearch.java',('new ResearchItem("THAUMONOMICON", "BASICS"','setAutoUnlock().setStub().setRound()','setParents(new String[] { "RESEARCH" })')),
('thaumcraft/common/config/ConfigAspects.java',('ConfigItems.itemThaumonomicon','Aspect.MAGIC, 2','Aspect.MIND, 2'))]:
    s=original_text(p)
    for t in tokens:req(t in s,'original '+p+' '+t)
for cur,old in [
('src/main/resources/assets/thaumcraft/textures/item/thaumonomicon.png','assets/thaumcraft/textures/items/thaumonomicon.png'),
('src/main/resources/assets/thaumcraft/textures/item/tc4/thaumonomiconcheat.png','assets/thaumcraft/textures/items/thaumonomiconcheat.png'),
('src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/gui/gui_research.png','assets/thaumcraft/textures/gui/gui_research.png'),
('src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/gui/gui_researchbook.png','assets/thaumcraft/textures/gui/gui_researchbook.png')]:
    req(sha(cur)==hashlib.sha256(original_bytes(old)).hexdigest(),'resource hash '+cur)
gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java');methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=218 and len(methods)==len(set(methods)),f'GameTests {len(methods)}')
for m in ('thaumonomiconItemPropertiesMatchOriginal','thaumonomiconSpreadAndPanContractsMatchOriginal','thaumonomiconCompoundRecipeMatchesOriginal','thaumonomiconResearchEntryMatchesOriginal','thaumonomiconCheatSheetContractMatchesOriginal','thaumonomiconAcquisitionContractsMatchOriginal','thaumonomiconObjectAspectsMatchOriginal'):
    req(m in methods,'GameTest '+m)
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'));ids=[x['id'] for x in manifest['tests']]
req(tuple(map(int,manifest['version'].split('.')))>=(11,64,26) and len(ids)>=672 and len(ids)==len(set(ids)),f'manifest {manifest["version"]}/{len(ids)}')
for i in ('gameplay.thaumonomicon_open_repairs_siblings','gameplay.thaumonomicon_cheat_sheet_first_discovery','gameplay.thaumonomicon_bookshelf_transform','gameplay.thaumonomicon_focused_wand_rejected','gameplay.thaumonomicon_secondary_purchase','gameplay.thaumonomicon_research_note_preconditions','client.thaumonomicon_shared_map_state','client.thaumonomicon_recipe_reference_history','client.thaumonomicon_no_mousewheel_navigation','multiplayer.thaumonomicon_server_authority','resource.thaumonomicon_original_textures','integration.thaumonomicon_external_loot_contract'):
    req(i in ids,'scenario '+i)
ev=json.loads(text('TC4_11.64.26_THAUMONOMICON_SOURCE_EVIDENCE.json'))
req(ev==json.loads(text('tools/data/tc4_thaumonomicon_full_source_evidence_v11.64.26.json')),'evidence copies')
req(ev['source_closure']=='CLOSED_WITH_EXPLICIT_EXTERNAL_ACQUISITION_BOUNDARIES' and ev['resource_closure']=='CLOSED' and ev['build_status']=='NOT_OBTAINED' and ev['runtime_status']=='NOT_VERIFIED','evidence status')
prompt=text('UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md');req(prompt==text('PROMPT_FOR_FUTURE_CHAT_RU.md'),'prompt copies')
for t in ('Один релиз — один предмет или одна цельная механика','SOURCE CLOSED','RESOURCE CLOSED','BUILD VERIFIED','RUNTIME VERIFIED','Упаковка архива без этого файла запрещена'):
    req(t in prompt,'prompt '+t)
print(f'TC4 v11.64.26 Thaumonomicon full-closure guard: PASS ({len(methods)} GameTests; {len(ids)} scenarios; core source/resource/prompt; external acquisition boundaries explicit)')
