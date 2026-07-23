#!/usr/bin/env python3
from pathlib import Path
import hashlib,json,re,zipfile
R=Path(__file__).resolve().parents[1]
def text(p): return (R/p).read_text(encoding='utf-8',errors='replace')
def req(ok,msg):
    if not ok: raise SystemExit('TC4 v11.64.27 research-system full-closure guard: FAIL: '+msg)
def version(p):
    m=re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']',text(p));req(m,'version '+p);return tuple(map(int,m.groups()))
def sha_bytes(b): return hashlib.sha256(b).hexdigest()
def sha(p): return sha_bytes((R/p).read_bytes())
req(version('build.gradle')>=(11,64,27),'build version')
req(version('src/main/resources/META-INF/mods.toml')>=(11,64,27),'mods version')
par=text('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchSystemFullClosureParity.java')
for token in ('CONTRACT_VERSION = "11.64.27"','ORIGINAL_RESEARCH_COUNT = 201','PRIMAL_DISCOVERY_POOL = 0','RESEARCH_TABLE_SLOT_COUNT = 2','SCRIBING_TOOLS_SLOT = 0','RESEARCH_NOTE_SLOT = 1','NOTE_PAPER_COST = 1','NOTE_INK_COST = 1','DUPLICATE_PAPER_COST = 1','DUPLICATE_INK_SAC_COST = 1','BONUS_RECALCULATE_THRESHOLD = 600','BONUS_SCAN_RADIUS = 8','CREATIVE_BYPASSES_RESEARCH_COSTS = false','TABLE_BUTTON_CREATES_NOTE = false','TABLE_BUTTON_COMPLETES_NOTE = false','DUPLICATE_RESPECTS_ITEM_MAX_STACK = false'):
    req(token in par,'parity '+token)
knowledge=text('src/main/java/com/darkifov/thaumcraft/research/PlayerAspectKnowledge.java')
for token in ('LEGACY_WALLET_ROOT = "ThaumcraftOriginalAspectWallet"','Math.max(pool.get(aspect)','root.putBoolean(LEGACY_WALLET_MIGRATED, true)','persistent.remove(LEGACY_WALLET_ROOT)','known.putBoolean(aspect.id(), true)'):
    req(token in knowledge,'knowledge '+token)
req('list.add(aspect, 10)' not in knowledge and 'list.add(aspect, 5)' not in knowledge,'free starter aspect points')
wallet=text('src/main/java/com/darkifov/thaumcraft/research/OriginalAspectWallet.java')
for token in ('PlayerAspectKnowledge.pool','PlayerAspectKnowledge.consumePool','PlayerAspectKnowledge.addPool'):
    req(token in wallet,'unified wallet '+token)
req('ThaumcraftOriginalAspectWallet' not in wallet and 'SEED_AMOUNT' not in wallet,'parallel wallet retained')
registry=text('src/main/java/com/darkifov/thaumcraft/research/ResearchRegistry.java')
req('ORIGINAL_TC4_ENTRY_COUNT = TC4ResearchRuntimeBridge.size()' in registry,'original denominator bridge')
req('ENTRIES.subList(0, Math.min(ORIGINAL_TC4_ENTRY_COUNT' in registry,'original-only registry view')
bridge=text('src/main/java/com/darkifov/thaumcraft/research/OriginalResearchBridge.java')
for token in ('entry == null || byKey(entry.key()).isEmpty()','ResearchRegistry.originalEntries()','costs.put(aspect.getKey().toLowerCase(), Math.max(0, aspect.getValue()))','OriginalAspectWallet.consume(player, costsFor(entry))','unlockEligibleSiblings'):
    req(token in bridge,'bridge '+token)
req('Research completed:' not in bridge and 'displayClientMessage' not in bridge,'invented research completion chat')
requirements=text('src/main/java/com/darkifov/thaumcraft/research/ResearchNoteRequirements.java')
req('for (String id : original.get().aspects().keySet())' in requirements,'exact note tag set')
req('return exact;' in requirements,'exact original note requirements return')
creator=text('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchNoteCreator.java')
for token in ('ResearchRegistry.originalEntries().contains(entry)','findResearchNote(player, entry.key())','return existing;','findPaper(player)','findScribingTools(player)','ScribingToolsItem.consumeInk(tools, INK_COST)','paper.shrink(1)','ResearchNoteState.initialize(note, entry.key()','TC4Sounds.event("learn")','0.75F'):
    req(token in creator,'creator '+token)
req('instabuild' not in creator and 'displayClientMessage' not in creator,'creative bypass or invented creator chat')
state=text('src/main/java/com/darkifov/thaumcraft/research/ResearchNoteState.java')
req('anchors.isEmpty() && !isOriginalResearch(target)' in state,'original empty-tag compatibility boundary')
req('if (anchors.size() == 1)' not in state and 'anchors.add(Aspect.PRAECANTATIO)' not in state,'invented one-tag anchor')
req('int radius = ResearchNoteGrid.radiusForResearch(target)' in state,'note radius production path')
inv=text('src/main/java/com/darkifov/thaumcraft/research/ResearchTableInventoryRuntime.java')
for token in ('checkInkForCreate','findInkedScribingTools(player).isPresent()','checkInkForEdit','consumeInkForEdit','rollbackResearchNotePlacementDebit'):
    req(token in inv,'inventory '+token)
req('instabuild ||' not in inv and '|| player.getAbilities().instabuild' not in inv,'creative ink bypass')
solver=text('src/main/java/com/darkifov/thaumcraft/research/ResearchNoteSolver.java')
req('Component.translatable("tc.researcherror")' in solver,'original prerequisite error')
for invented in ('Research completed:','Research solved:','Aspect placed:','Aspect removed:','Discovery completed:'):
    req(invented not in solver,'invented solver chat '+invented)
be=text('src/main/java/com/darkifov/thaumcraft/blockentity/ResearchTableBlockEntity.java')
for token in ('public boolean createResearchNote(ServerPlayer player) {\n        return false;','public boolean completeResearchNote(ServerPlayer player) {\n        return false;','note.grow(1)','consumeInventoryItem(player, Items.PAPER)','consumeInventoryItem(player, Items.INK_SAC)','playOriginalResearchSound("learn", 1.0F, 1.0F)','Math.max(0, aspectEntry.getValue()) + Math.max(0, copies)'):
    req(token in be,'table '+token)
copy_segment=be[be.index('public boolean copyCompletedResearchNote'):be.index('private boolean hasCopyAspectCost')]
req('getMaxStackSize' not in copy_segment,'duplicate max-stack clamp')
for invented in ('Research Table slot 1 already contains','Research note prepared','bonus aspects:','Research note creation failed'):
    req(invented not in be,'invented table chat '+invented)
action=text('src/main/java/com/darkifov/thaumcraft/network/RequestResearchTableActionPacket.java')
req('ACTION_OPEN_NOTE' in action and 'ACTION_SYNC_NOTE' in action and 'copyCompletedResearchNote' in action,'table action routing')
req('createResearchNote(' not in action and 'completeResearchNote(' not in action and 'displayClientMessage' not in action,'invented table action path/chat')
for packet in ('RequestSolveResearchNotePacket.java','RequestPlaceResearchNoteAspectPacket.java','RequestClearResearchNoteSlotPacket.java','RequestCombineAspectsPacket.java','RequestSelectResearchPacket.java'):
    s=text('src/main/java/com/darkifov/thaumcraft/network/'+packet)
    req('displayClientMessage' not in s,'invented packet chat '+packet)
item=text('src/main/java/com/darkifov/thaumcraft/block/ResearchNoteItem.java')
req('tc.researcherror' in item or 'ResearchNoteSolver.convertSolvedNote' in item,'discovery use path')
req('Research completed:' not in item and 'Right-click' not in item,'invented note item text')
foundation=text('src/main/java/com/darkifov/thaumcraft/research/ResearchTableFoundation.java')
req('PlayerAspectKnowledge.seedPrimals(player)' in foundation,'primal discovery seed')
req('displayClientMessage' not in foundation and 'starter' not in foundation.lower(),'foundation invented feedback/starter')
# Original source proof.
source_zip=R/'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip'
with zipfile.ZipFile(source_zip) as z:
    prefix='Thaumcraft4-1.7.10-master/'
    def ob(rel): return z.read(prefix+rel)
    def ot(rel): return ob(rel).decode('utf-8',errors='replace')
    originals={
      'thaumcraft/common/lib/research/PlayerKnowledge.java':('known.add(Aspect.FIRE, 0)','known.add(aspect, 0)','addAspectPool'),
      'thaumcraft/common/lib/research/ResearchManager.java':('createResearchNoteForPlayer','consumeInkFromPlayer(player, false)','consumeInkFromPlayer(player, true)','int radius = 1 + Math.min(3, rr.getComplexity())','func_74768_a("copies", 0)'),
      'thaumcraft/common/tiles/TileResearchTable.java':('this.nextRecalc++ > 600','for (int x = -8; x <= 8; x++)','consumeInkFromTable(this.contents[0], false)','consumeInkFromTable(this.contents[0], true)'),
      'thaumcraft/common/items/ItemResearchNotes.java':('completeResearch(player, ResearchManager.getData(stack).key)','tc.researcherror','siblings'),
      'thaumcraft/common/container/ContainerResearchTable.java':('if (button == 1)','var8.field_77994_a = var9')}
    for rel,tokens in originals.items():
        s=ot(rel)
        for token in tokens:req(token in s,'original '+rel+' '+token)
    # All 50 original aspect textures plus table/note resources must be retained byte-identically.
    aspect_names=[n.split('/')[-1] for n in z.namelist() if '/assets/thaumcraft/textures/aspects/' in n and n.endswith('.png')]
    req(len(aspect_names)==50,'original aspect texture count')
    for name in aspect_names:
        port=R/'src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/aspects'/name
        req(port.is_file(),'missing aspect texture '+name)
        req(sha_bytes(port.read_bytes())==sha_bytes(ob('assets/thaumcraft/textures/aspects/'+name)),'aspect hash '+name)
    pairs=[
      ('src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/gui/guiresearchtable2.png','assets/thaumcraft/textures/gui/guiresearchtable2.png'),
      ('src/main/resources/assets/thaumcraft/textures/item/tc4/researchnotes.png','assets/thaumcraft/textures/items/researchnotes.png'),
      ('src/main/resources/assets/thaumcraft/textures/item/tc4/researchnotesoverlay.png','assets/thaumcraft/textures/items/researchnotesoverlay.png')]
    for cur,old in pairs:req(sha(cur)==sha_bytes(ob(old)),'resource hash '+cur)
# Counts and manifest.
gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=226 and len(methods)==len(set(methods)),f'GameTests {len(methods)}')
for name in ('researchSystemOriginalRegistryIsExactly201','researchSystemPrimalsStartKnownWithZeroPool','researchSystemLegacyWalletMigratesWithoutDuplication','researchSystemOneTagNotesKeepOneAnchor','researchSystemCreativeDoesNotBypassInk','researchSystemTableHasNoInventedCreateOrCompleteButtons','researchSystemDuplicateCanExceedVanillaMaxStack','researchSystemCostsAndRadiusUseExactResearchTags'):
    req(name in methods,'GameTest '+name)
man=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids=[x['id'] for x in man['tests']]
req(tuple(map(int,man['version'].split('.')))>=(11,64,27),'manifest version')
req(len(ids)>=688 and len(ids)==len(set(ids)),f'manifest scenarios {len(ids)}')
for sid in ('gameplay.research_system_zero_primal_pool','persistence.research_system_legacy_wallet_max_merge','gameplay.research_note_creation_creative_cost','gameplay.research_table_no_create_button','gameplay.research_note_duplicate_overstack','dedicated.research_system_multiplayer_authority','resource.research_system_original_assets'):
    req(sid in ids,'manifest scenario '+sid)
print('TC4 v11.64.27 research-system full-closure guard: PASS')
