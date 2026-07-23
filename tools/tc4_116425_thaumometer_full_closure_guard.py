#!/usr/bin/env python3
from pathlib import Path
import hashlib,json,re,zipfile
R=Path(__file__).resolve().parents[1]
def text(p):return (R/p).read_text(encoding='utf-8',errors='replace')
def req(ok,msg):
 if not ok:raise SystemExit('TC4 v11.64.25 Thaumometer full-closure guard: FAIL: '+msg)
def ver(p):
 m=re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']',text(p));req(m,'version '+p);return tuple(map(int,m.groups()))
def sha(p):return hashlib.sha256((R/p).read_bytes()).hexdigest()
req(ver('build.gradle')>=(11,64,26),'current build version')
req(ver('src/main/resources/META-INF/mods.toml')>=(11,64,26),'current mods version')
par=text('src/main/java/com/darkifov/thaumcraft/aura/TC4ThaumometerParity.java')
for t in ('CONTRACT_VERSION = "11.64.25"','MAX_STACK_SIZE = 1','USE_DURATION_TICKS = 25','COMPLETION_REMAINING_TICKS = 5','ENTITY_SCAN_RANGE = 10.0D','ENTITY_TARGET_EXPAND = 0.5D','ASPECT_TOTAL_CAP = 100','ASPECT_HARD_CAP = 125','NODE_VIEW_DISTANCE = 48.0D','NODE_VIEW_CONE_DOT = 0.44F','NODE_TYPE_TEXT_COLOR = 15642134','TITLE_BASE_SCALE = 0.005F','TITLE_SHRINK_PER_PIXEL = 0.000025F','MAX_RENDERED_ASPECTS = 15','cappedAspectReward','Math.sqrt'):
 req(t in par,'parity '+t)
keys=text('src/main/java/com/darkifov/thaumcraft/aura/TC4ThaumometerScanKeys.java')
for t in ('CONTRACT_VERSION = "11.64.25"','NODE_PREFIX = "NODE"','itemKey(','blockKey(','entityKey(','player_','CHILD','VILLAGER','FLASHING','POWERED','getGolemMaterial().id()','node.nodeId()'):
 req(t in keys,'scan key '+t)
item=text('src/main/java/com/darkifov/thaumcraft/block/ThaumometerItem.java')
for t in ('ConcurrentHashMap','PENDING_SCANS','record PendingScan','migrateLegacyItemLedger','TC4ThaumometerTargeting.find(player,1.0F)','matchesPending','cappedAspectReward','markScannedPhenomenon','migrateLegacyPosition','applyScanTriggers','readStrings','stack.setTag(null)'):
 req(t in item,'production '+t)
for t in ('putLong(TAG_PENDING_SCAN_START','put(TAG_PENDING_BLOCK_SCAN','put(TAG_PENDING_ENTITY_SCAN','addScannedBlock(','addScannedEntity('):
 req(t not in item,'forbidden persistent/mirror path '+t)
target=text('src/main/java/com/darkifov/thaumcraft/aura/TC4ThaumometerTargeting.java')
for t in ('TC4ThaumometerScanKeys.itemKey','TC4ThaumometerScanKeys.entityKey','TC4ThaumometerScanKeys.nodeKey','TC4ThaumometerScanKeys.blockKey','nearestBlockDistance'):
 req(t in target,'targeting '+t)
registry=text('src/main/java/com/darkifov/thaumcraft/aura/TC4ThaumometerPhenomenaRegistry.java')
for t in ('CONTRACT_VERSION = "11.64.25"','interface Handler','scanPhenomena','CopyOnWriteArrayList','register(Handler handler)','unregister(Handler handler)','first non-null phenomenon wins'):
 req(t in registry,'phenomena registry '+t)
req('TC4ThaumometerPhenomenaRegistry.find(player, scanner, partialTick)' in target,'phenomena registry production link')
node=text('src/main/java/com/darkifov/thaumcraft/data/NodeScanData.java')
for t in ('hasScanned(Player player, String nodeKey)','markScanned(Player player, String nodeKey)','migrateLegacyPosition','foundLegacy'):
 req(t in node,'node data '+t)
player=text('src/main/java/com/darkifov/thaumcraft/data/PlayerThaumData.java')
for t in ('SCANNED_PHENOMENA','markScannedPhenomenon','hasScannedPhenomenon','getScannedPhenomena','importScannedObjects','importScannedEntities'):
 req(t in player,'player data '+t)
packet=text('src/main/java/com/darkifov/thaumcraft/network/ScanKnowledgeSyncPacket.java')
req('objects, entities, nodes, phenomena' in packet and 'ClientScanData.set(p.objects,p.entities,p.nodes,p.phenomena)' in packet,'four-ledger sync')
progress=text('src/main/java/com/darkifov/thaumcraft/research/OriginalResearchProgression.java')
for t in ('choose exactly one','new LinkedHashSet','new ArrayList','player.getRandom().nextInt','"@"+chosen.key()','return 1;'):
 req(t in progress,'research clue '+t)
req('applyUnlockSideEffects(player,chosen)' not in progress,'clue must not grant warp/full completion')
req('Research discovered:' not in progress and 'displayClientMessage' not in progress,'clue path must not fabricate chat')
policy=text('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchFlagPolicy.java')
req('PlayerThaumData.hasResearch(player, "@" + entry.key())' in policy and 'return clueRevealed;' in policy,'clue visibility/note gate')
render=text('src/main/java/com/darkifov/thaumcraft/client/render/ThaumometerItemRenderer.java')
for t in ('if (target == null || !target.isPresent()) return;','if (!scanned) { poseStack.popPose(); return; }','MAX_RENDERED_ASPECTS','titleScale(width)','NODE_TYPE_TEXT_COLOR','ClientScanData.hasPhenomenon'):
 req(t in render,'renderer '+t)
req('renderQuestionMark(' not in render and 'ThaumometerItem.hasScanned' not in render,'no invented question/NBT fallback')
aura=text('src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java')
req('viewer.getMainHandItem().is(ThaumcraftMod.THAUMOMETER.get())' in aura and 'viewer.getOffhandItem().is(ThaumcraftMod.THAUMOMETER.get())' not in aura,'main hand only')
req('NODE_VIEW_DISTANCE' in aura and 'NODE_VIEW_CONE_DOT' in aura,'node constants linked')
mod=text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
req('new ThaumometerItem(new Item.Properties().tab(THAUMCRAFT_TAB).stacksTo(1).rarity(Rarity.UNCOMMON))' in mod,'item properties')
recipe=json.loads(text('src/main/resources/data/thaumcraft/recipes/thaumometer.json'))
req(recipe['pattern']==[' 1 ','IGI',' 1 '],'recipe pattern')
orig=R/'reference/tc4_source/Thaumcraft4-1.7.10-master'
source_zip=R/'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip'
def original_bytes(rel):
 disk=orig/rel
 if disk.is_file(): return disk.read_bytes()
 with zipfile.ZipFile(source_zip) as z: return z.read('Thaumcraft4-1.7.10-master/'+rel)
def original_text(rel): return original_bytes(rel).decode('utf-8',errors='replace')
for p,tokens in [
 ('thaumcraft/api/research/IScanEventHandler.java',('scanPhenomena','ScanResult')),
 ('thaumcraft/common/items/relics/ItemThaumometer.java',('return 25','ScanManager.completeScan','"NODE" + ((INode)tile).getId()','count <= 5','"thaumcraft:cameraticks", 0.2F, 0.45F')),
 ('thaumcraft/common/lib/research/ScanManager.java',('hash = hash + "CHILD"','hash = hash + "VILLAGER"','hash = hash + "FLASHING"','hash = hash + "POWERED"','Math.sqrt(amount)','Config.aspectTotalCap * 1.25F')),
 ('thaumcraft/common/lib/research/ResearchManager.java',('world.field_73012_v.nextInt(keys.size())','"@" + key')),
 ('thaumcraft/client/renderers/item/ItemThaumometerRenderer.java',('float scale = 0.005F','if (sw > 90)','15642134')),
 ('thaumcraft/client/renderers/tile/TileNodeRenderer.java',('0.44F','viewDistance = 48.0D'))]:
 s=original_text(p);[req(t in s,'original '+p+' '+t) for t in tokens]
for cur,old in [
 ('src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/models/scanner.obj','assets/thaumcraft/textures/models/scanner.obj'),
 ('src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/models/scanner.png','assets/thaumcraft/textures/models/scanner.png'),
 ('src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/models/scanscreen.png','assets/thaumcraft/textures/models/scanscreen.png')]:
 req(sha(cur)==hashlib.sha256(original_bytes(old)).hexdigest(),'resource hash '+cur)
gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java');methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=211 and len(methods)==len(set(methods)),f'GameTests {len(methods)}')
for m in ('thaumometerAspectPoolCapsMatchTc4','thaumometerDiscoveryBonusPrecedesCaps','thaumometerNodeVisibilityConstantsMatchTc4','thaumometerReadoutLayoutMatchesTc4','thaumometerTitleShrinkFormulaMatchesTc4','thaumometerStableNodeIdentityMatchesTc4','thaumometerItemContractMatchesTc4'):req(m in methods,'GameTest '+m)
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'));ids=[x['id'] for x in manifest['tests']]
req(tuple(map(int,manifest['version'].split('.')))>=(11,64,26) and len(ids)>=655 and len(ids)==len(set(ids)),f'manifest {manifest["version"]}/{len(ids)}')
for i in ('gameplay.thaumometer_transient_scan_cancel','gameplay.thaumometer_entity_variant_identity','gameplay.thaumometer_hidden_research_clue','persistence.thaumometer_legacy_item_nbt_migration','persistence.thaumometer_node_uuid_survives_move','client.thaumometer_scanner_glass_and_node_overlay','dedicated.thaumometer_multiplayer_authority','integration.thaumometer_scan_event_handler'):req(i in ids,'scenario '+i)
ev=json.loads(text('TC4_11.64.25_THAUMOMETER_SOURCE_EVIDENCE.json'))
req(ev==json.loads(text('tools/data/tc4_thaumometer_full_source_evidence_v11.64.25.json')),'evidence copies')
req(ev['source_closure']=='CLOSED' and ev['resource_closure']=='CLOSED' and ev['build_status']=='NOT_OBTAINED' and ev['runtime_status']=='NOT_VERIFIED','evidence status')
prompt=text('UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md');req(prompt==text('PROMPT_FOR_FUTURE_CHAT_RU.md'),'prompt copies')
for t in ('Один релиз — один предмет или одна цельная механика','SOURCE CLOSED','RESOURCE CLOSED','BUILD VERIFIED','RUNTIME VERIFIED','Упаковка архива без этого файла запрещена'):req(t in prompt,'prompt '+t)
print(f'TC4 v11.64.25 Thaumometer full-closure guard: PASS ({len(methods)} GameTests; {len(ids)} scenarios; source/resource/prompt)')
