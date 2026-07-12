#!/usr/bin/env python3
from pathlib import Path

root = Path(__file__).resolve().parents[1]
item = (root / 'src/main/java/com/darkifov/thaumcraft/block/ThaumometerItem.java').read_text()
events = (root / 'src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java').read_text()
network = (root / 'src/main/java/com/darkifov/thaumcraft/network/ThaumcraftNetwork.java').read_text()
packet_path = root / 'src/main/java/com/darkifov/thaumcraft/network/RequestThaumometerScanPacket.java'
parity = (root / 'src/main/java/com/darkifov/thaumcraft/aura/TC4AuraNodeScanParity.java').read_text()
targeting = (root / 'src/main/java/com/darkifov/thaumcraft/aura/TC4ThaumometerTargeting.java').read_text()

problems = []
if not packet_path.exists():
    problems.append('missing explicit Forge client->server scan packet')
else:
    packet = packet_path.read_text()
    for token in ['beginBlockScan', 'beginEntityScan', 'context.setPacketHandled(true)', 'target validity and server-authoritative aspect availability']:
        if token not in packet:
            problems.append(f'scan packet missing {token}')
    if 'TC4ThaumometerTargeting.find(player, 1.0F)' in packet:
        problems.append('scan packet reintroduced the one-tick-late exact server ray that broke runtime scans')
if 'RequestThaumometerScanPacket.class' not in network:
    problems.append('scan packet is not registered')
for token in ['RequestThaumometerScanPacket.block', 'RequestThaumometerScanPacket.entity', 'sendToServer']:
    if token not in events:
        problems.append(f'interaction bridge missing {token}')
if 'if (!level.isClientSide && !target.hasAspects())' not in item:
    problems.append('client aspect table can still reject a scan before packet delivery')
for token in ['REQUIRED_STABLE_TICKS = 20', 'elapsed >= REQUIRED_STABLE_TICKS',
              'serverTickPendingScan', 'TAG_PENDING_SCAN_START']:
    if token not in item:
        problems.append(f'TC4 twenty-stable-tick completion path missing {token}')
if 'TickEvent.PlayerTickEvent' not in events:
    problems.append('cancelled Forge interactions have no authoritative scan tick fallback')
if 'THAUMOMETER_USE_DURATION_TICKS = 25' not in parity:
    problems.append('TC4 25 tick duration missing')
if 'THAUMOMETER_SCAN_RANGE = 10.0D' not in parity:
    problems.append('TC4 10 block scan range missing')
if 'nearestBlockDistance' not in targeting:
    problems.append('entity ray can pass through nearer blocks')

if problems:
    print('Thaumometer scan guard: FAILED')
    for problem in problems:
        print(' -', problem)
    raise SystemExit(1)
print('Thaumometer scan guard: OK (explicit Forge packet, latency-tolerant server target validation, 25/20 tick TC4 hold)')
