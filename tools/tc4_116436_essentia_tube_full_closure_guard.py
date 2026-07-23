#!/usr/bin/env python3
from pathlib import Path
import hashlib,json,re
R=Path(__file__).resolve().parents[1]
O=R/'reference/original_source/Thaumcraft4-1.7.10-master'
def text(p): return (R/p).read_text(encoding='utf-8',errors='replace')
def original(p): return (O/p).read_text(encoding='utf-8',errors='replace')
def req(ok,msg):
    if not ok: raise SystemExit('TC4 v11.64.36 essentia tube full-closure guard: FAIL: '+msg)
def sha(p): return hashlib.sha256(Path(p).read_bytes()).digest()
req("version = '11.64.36'" in text('build.gradle') or "version = '11.64.37'" in text('build.gradle') or "version = '11.64.38'" in text('build.gradle'),'build version')
req('version="11.64.36"' in text('src/main/resources/META-INF/mods.toml') or 'version="11.64.37"' in text('src/main/resources/META-INF/mods.toml') or 'version="11.64.38"' in text('src/main/resources/META-INF/mods.toml'),'mods version')

parity=text('src/main/java/com/darkifov/thaumcraft/essentia/TC4EssentiaTubeParity.java')
for t in ('CONTRACT_VERSION = "11.64.36"','SUCTION_RECALC_INTERVAL_TICKS = 2','TRANSFER_INTERVAL_TICKS = 5','BUFFER_BELLOWS_REFRESH_TICKS = 20','TRANSFER_AMOUNT = 1','BUFFER_CAPACITY = 8','MINIMUM_SUCTION = 0','VENTING_SERVER_TICKS = 40','VENTING_CLIENT_TICKS = 50','VALVE_ROTATION_STEP = 20','VALVE_ROTATION_MAX = 360','VALVE_RENDER_ROTATION_MULTIPLIER = 1.5F','VALVE_RENDER_TRAVEL = 0.12F','CORE_MIN = 0.34375D','CORE_MAX = 0.65625D','ARM_MIN = 0.42D','ARM_MAX = 0.58D','propagatedSuction','bufferSuction','bufferComparator','nextValveRotation','nextChoke'):
    req(t in parity,'parity '+t)

sub=text('src/main/java/com/darkifov/thaumcraft/essentia/EssentiaTubeSubtype.java')
req('return TC4EssentiaTubeParity.MINIMUM_SUCTION' in sub,'minimum suction production binding')
req('TC4EssentiaTubeParity.propagatedSuction' in sub,'suction transform production binding')
be=text('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java')
for t in ('ORIGINAL_SUCTION_RECALC_INTERVAL_TICKS = TC4EssentiaTubeParity.SUCTION_RECALC_INTERVAL_TICKS','ORIGINAL_EQUALIZE_INTERVAL_TICKS = TC4EssentiaTubeParity.TRANSFER_INTERVAL_TICKS','ORIGINAL_BUFFER_BELLOWS_REFRESH_TICKS = TC4EssentiaTubeParity.BUFFER_BELLOWS_REFRESH_TICKS','TC4EssentiaTubeParity.nextChoke','TC4EssentiaTubeParity.nextValveRotation','toggleManualFlowLikeTC4','refreshConnectionBlockState','writeOriginalAspectList','readOriginalAspectList','tag.put("Aspects", list)','tag.putByteArray("choke"','tag.putBoolean("flow"','tag.putBoolean("hadpower"','tag.contains("buffer", Tag.TAG_COMPOUND)','ClientboundBlockEntityDataPacket.create(this)'):
    req(t in be,'block entity '+t)
for forbidden in ('tag.putInt("venting"','tag.putInt("ventColor"','tag.putInt("tc4Count"','tag.putString("tc4Subtype"','tag.putInt("bellows"','tag.put("buffer"'):
    req(forbidden not in be,'writes temporary/legacy NBT '+forbidden)

block=text('src/main/java/com/darkifov/thaumcraft/block/EssentiaTubeBlock.java')
for t in ('TC4EssentiaTubeParity.CORE_MIN * 16.0D','TC4EssentiaTubeParity.CORE_MAX * 16.0D','EssentiaTubeBlockEntity::clientTick','player.isShiftKeyDown() && tube.aspectFilter() != null','held.getItem() instanceof JarLabelItem','tube.subtype().storesBufferEssentia() && player.isShiftKeyDown()','tube.toggleSideWithNeighbour','hasAnalogOutputSignal','TC4EssentiaTubeParity.bufferComparator','onRemove','ThaumcraftMod.JAR_LABEL'):
    req(t in block,'block '+t)
req('EssentiaPhialItem' not in block,'phial still configures filtered tube')
req('displayClientMessage' not in block,'debug diagnostics remain')
valve=text('src/main/java/com/darkifov/thaumcraft/block/EssentiaValveBlock.java')
for t in ('TC4ResonatorItem','player.getItemInHand(hand).is(asItem())','tube.toggleManualFlowLikeTC4','TC4Sounds.event("squeek")'):
    req(t in valve,'valve '+t)
resolver=text('src/main/java/com/darkifov/thaumcraft/essentia/EssentiaSuctionResolver.java')
req('!EssentiaValveBlock.isOpen(level, tubePos)' not in resolver,'closed valve deletes topology')
req('return level.getBlockEntity(pos) instanceof EssentiaTubeBlockEntity' in resolver,'closed valve tube-like topology')
renderer=text('src/main/java/com/darkifov/thaumcraft/client/render/EssentiaTubeRenderer.java')
for t in ('ORIGINAL_VALVE_TEXTURE','renderBufferChokes','renderValve','tube.valveRotation(partialTick)','VALVE_RENDER_ROTATION_MULTIPLIER','VALVE_RENDER_TRAVEL','choke == 2 ? 0xFFFF4D4D : 0xFF4D4DFF','orientLikeOriginalValve'):
    req(t in renderer,'renderer '+t)

original_checks={
 'thaumcraft/common/tiles/TileTube.java':('static final int freq = 5','++this.count % 2 == 0','this.count % 5 == 0','restrict ? suck / 2 : suck - 1','return 0','this.venting = 40','this.venting = 50','func_74778_a("type"','func_74768_a("amount"','func_74768_a("side"','func_74773_a("open"','func_74778_a("stype"','func_74768_a("samount"','0.34375D','0.65625D','float min = 0.42F','float max = 0.58F'),
 'thaumcraft/common/tiles/TileTubeFilter.java':('AspectFilter','super.calculateSuction(this.aspectFilter','new AspectList().add(this.aspectFilter, -1)'),
 'thaumcraft/common/tiles/TileTubeRestrict.java':('super.calculateSuction(filter, true, dir)'),
 'thaumcraft/common/tiles/TileTubeOneway.java':('super.calculateSuction(filter, restrict, true)','super.equalizeWithNeighbours(true)'),
 'thaumcraft/common/tiles/TileTubeValve.java':('allowFlow = true','rotation += 20.0F','rotation -= 20.0F','flow','hadpower','face != this.facing','if (this.allowFlow) super.setSuction'),
 'thaumcraft/common/tiles/TileTubeBuffer.java':('MAXAMOUNT = 8','chokedSides','this.aspects.writeToNBT','this.aspects.readFromNBT','this.bellows * 32','this.count % 20 == 0','this.count % 5 == 0','tmp118_110[tmp118_115] + 1'),
 'thaumcraft/common/blocks/BlockTube.java':('aspects.visSize() / 8.0F','MathHelper.func_76141_d(r * 14.0F)','aspectFilter = null','itemResource, 1, 13','allowFlow = (!((TileTubeValve)te).allowFlow)')
}
for path,tokens in original_checks.items():
    src=original(path)
    for t in tokens: req(t in src,'original '+path+' '+t)

assets=['pipe_1.png','pipe_2.png','pipe_3.png','pipe_buffer.png','pipe_filter.png','pipe_filter_core.png','pipe_oneway.png','pipe_restrict.png','pipe_valve.png']
for name in assets:
    req(sha(O/'assets/thaumcraft/textures/blocks'/name)==sha(R/'src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/blocks'/name),'resource '+name)
req(sha(O/'assets/thaumcraft/textures/models/valve.png')==sha(R/'src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/models/valve.png'),'resource valve.png')

gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=278 and len(methods)==len(set(methods)),f'GameTests {len(methods)}')
for name in ('normalAndRestrictedTubesPropagateOriginalJarSuction','filteredTubeLocksSuctionAndTransferToLabelAspect','oneWayTubePropagatesSuctionOnlyAlongOriginalFacing','bufferTubeCapsPersistsAndSynchronizesRollbackState','tubeSubtypeNbtMatchesOriginalContracts','bufferComparatorAndMinimumSuctionMatchOriginal','closedValveKeepsTopologyAndManualState','tubeSideToggleUpdatesBakedConnectionState'):
    req(name in methods,'GameTest '+name)
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids=[x['id'] for x in manifest['tests']]
req(manifest.get('version') in {'11.64.36','11.64.37','11.64.38'},'manifest version')
req(len(ids)>=821 and len(ids)==len(set(ids)),'manifest count/unique')
for sid in ('gameplay.tube_direct_suction_propagation','gameplay.restricted_tube_halves_suction','gameplay.filtered_tube_label_only','gameplay.filtered_tube_shift_remove','gameplay.oneway_tube_direction','gameplay.valve_manual_and_redstone','gameplay.valve_trapped_unit_drain','gameplay.buffer_capacity_mixed_aspects','gameplay.buffer_choke_cycle','gameplay.buffer_comparator','persistence.tube_subtype_nbt','client.valve_and_buffer_animation','client.tube_connection_geometry','integration.tube_transport_chain_restart'):
    req(sid in ids,'scenario '+sid)
for evidence_path in ('TC4_11.64.36_ESSENTIA_TUBE_SOURCE_EVIDENCE.json','tools/data/tc4_essentia_tube_full_source_evidence_v11.64.36.json'):
    evidence=json.loads(text(evidence_path))
    req(evidence.get('version')=='11.64.36','evidence version '+evidence_path)
    req(len(evidence.get('original_sources',[]))==10,'evidence original sources '+evidence_path)
    req(len(evidence.get('production_contracts',[]))>=13,'evidence production contracts '+evidence_path)
req((R/'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md').is_file(),'universal prompt')
req('Файл `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md` обязателен' in text('UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md'),'mandatory prompt wording')
print('TC4 v11.64.36 essentia tube full-closure guard: PASS (278 GameTests, 821 scenarios, 10 exact assets)')
