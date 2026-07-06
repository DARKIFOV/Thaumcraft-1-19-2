from pathlib import Path
import json, sys
ROOT = Path(__file__).resolve().parents[1]
checks = []

def has(path, needle):
    p = ROOT / path
    ok = p.exists() and needle in p.read_text(encoding='utf-8', errors='ignore')
    checks.append({'file': path, 'needle': needle, 'ok': ok})
    return ok

has('src/main/java/com/darkifov/thaumcraft/research/ResearchNoteState.java', 'clearSlot(ItemStack stack, int index)')
has('src/main/java/com/darkifov/thaumcraft/research/ResearchNoteState.java', 'touchesCompatibleNeighbor')
has('src/main/java/com/darkifov/thaumcraft/research/ResearchAspectGraph.java', 'shortestPath(Aspect start, Aspect target)')
has('src/main/java/com/darkifov/thaumcraft/network/RequestClearResearchNoteSlotPacket.java', 'class RequestClearResearchNoteSlotPacket')
has('src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java', 'requestClearResearchNoteSlotFromClient')
has('src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java', 'Path hint')
has('src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java', 'matchesRecipeGrid')
has('src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java', 'consumePatternIngredients')
has('src/main/java/com/darkifov/thaumcraft/network/ThaumcraftNetwork.java', 'RequestClearResearchNoteSlotPacket::handle')

report = {
    'stage': 137,
    'name': 'TC4 core loop precision pass',
    'checks': checks,
    'ok': all(c['ok'] for c in checks),
}
(ROOT / 'STAGE137_CORE_LOOP_AUDIT.json').write_text(json.dumps(report, indent=2), encoding='utf-8')
if not report['ok']:
    for c in checks:
        if not c['ok']:
            print('::error::missing', c)
    sys.exit(1)
print('Stage137 core loop audit: OK')
