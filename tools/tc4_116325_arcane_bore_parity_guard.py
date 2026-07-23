#!/usr/bin/env python3
from pathlib import Path
import hashlib,sys
R=Path(__file__).resolve().parents[1]; checks=[]
def t(p):
 q=R/p; return q.read_text(encoding='utf-8',errors='ignore') if q.is_file() else ''
def ok(n,v): checks.append((n,bool(v)))
def sha(p): return hashlib.sha256((R/p).read_bytes()).hexdigest()
ok('historical report',(R/'TC4_11.63.25_ARCANE_BORE_PORT_REPORT_RU.md').is_file())
for p,tokens in {
 'src/main/java/com/darkifov/thaumcraft/block/ArcaneBoreBlock.java':['extends BaseEntityBlock','DirectionProperty FACING','ARCANE_BORE_BASE','NetworkHooks.openScreen','Shapes.box'],
 'src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneBoreBlockEntity.java':['new ItemStackHandler(2)','level.hasNeighborSignal(pos)','level.hasNeighborSignal(basePos)','TC4ArcaneBoreParity.nextLane','Block.getDrops','ItemHandlerHelper.insertItemStacked','invalidateCaps','reviveCaps'],
 'src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneBoreBaseBlockEntity.java':['SUCTION = 128','Aspect.PERDITIO','canInputFrom','takeEssentiaOriginal(Aspect.PERDITIO, 1'],
 'src/main/java/com/darkifov/thaumcraft/menu/ArcaneBoreMenu.java':['0, 26, 18','1, 74, 18','new SimpleContainerData(8)'],
 'src/main/java/com/darkifov/thaumcraft/client/screen/ArcaneBoreScreen.java':['gui_arcanebore.png','176','141']}.items():
 for token in tokens: ok(p+':'+token,token in t(p))
for cur,orig in [
 ('src/main/resources/assets/thaumcraft/textures/models/Bore.png','src/main/resources/assets/thaumcraft/original_tc4_1710/textures/models/Bore.png'),
 ('src/main/resources/assets/thaumcraft/textures/gui/gui_arcanebore.png','src/main/resources/assets/thaumcraft/original_tc4_1710/textures/gui/gui_arcanebore.png')]:
 ok('byte exact '+cur,(R/cur).is_file() and (R/orig).is_file() and sha(cur)==sha(orig))
failed=[n for n,v in checks if not v]
for n,v in checks: print(('PASS' if v else 'FAIL')+' | '+n)
print(f'SUMMARY | {len(checks)-len(failed)}/{len(checks)} passed')
sys.exit(1 if failed else 0)
