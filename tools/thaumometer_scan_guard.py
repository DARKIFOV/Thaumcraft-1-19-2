#!/usr/bin/env python3
from pathlib import Path
root=Path(__file__).resolve().parents[1]
def t(p):return (root/p).read_text(encoding='utf-8',errors='replace')
item=t('src/main/java/com/darkifov/thaumcraft/block/ThaumometerItem.java')
events=t('src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java')
network=t('src/main/java/com/darkifov/thaumcraft/network/ThaumcraftNetwork.java')
packet=t('src/main/java/com/darkifov/thaumcraft/network/RequestThaumometerScanPacket.java')
parity=t('src/main/java/com/darkifov/thaumcraft/aura/TC4ThaumometerParity.java')
target=t('src/main/java/com/darkifov/thaumcraft/aura/TC4ThaumometerTargeting.java')
problems=[]
for token in ('RequestThaumometerScanPacket.class','syncScanKnowledge'): 
 if token not in network:problems.append('network '+token)
for token in ('beginBlockScan','beginEntityScan','context.setPacketHandled(true)'):
 if token not in packet:problems.append('packet '+token)
for token in ('RequestThaumometerScanPacket.block','RequestThaumometerScanPacket.entity','sendToServer','TickEvent.PlayerTickEvent'):
 if token not in events:problems.append('event '+token)
for token in ('PENDING_SCANS','ConcurrentHashMap','PendingScan','TC4ThaumometerTargeting.find(player,1.0F)',
              'TC4ThaumometerParity.shouldCompleteAfterElapsed(elapsed)','TC4ThaumometerParity.shouldPlayCameraTickAfterElapsed(elapsed)',
              'migrateLegacyItemLedger','TC4ThaumometerParity.cappedAspectReward','PlayerThaumData.markScannedPhenomenon'):
 if token not in item:problems.append('production '+token)
for forbidden in ('putLong(TAG_PENDING_SCAN_START','put(TAG_PENDING_BLOCK_SCAN','put(TAG_PENDING_ENTITY_SCAN'):
 if forbidden in item:problems.append('persistent pending state '+forbidden)
for token in ('USE_DURATION_TICKS = 25','REQUIRED_STABLE_TICKS = USE_DURATION_TICKS - COMPLETION_REMAINING_TICKS','ENTITY_SCAN_RANGE = 10.0D','ENTITY_TARGET_EXPAND = 0.5D','ASPECT_TOTAL_CAP = 100','ASPECT_HARD_CAP = 125'):
 if token not in parity:problems.append('parity '+token)
if 'nearestBlockDistance' not in target:problems.append('wall occlusion')
if problems:
 print('Thaumometer scan guard: FAILED');[print(' -',x) for x in problems];raise SystemExit(1)
print('Thaumometer scan guard: OK (transient server scan state, exact 25/20 targeting, stable ledgers and aspect caps)')
