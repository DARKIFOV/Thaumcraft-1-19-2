#!/usr/bin/env python3
from pathlib import Path
root=Path(__file__).resolve().parents[1]
checks=[
    root/'src/main/java/com/darkifov/thaumcraft/golem/GolemDecorationType.java',
    root/'src/main/java/com/darkifov/thaumcraft/block/GolemDecorationItem.java',
    root/'docs/porting/STAGE143_TC4_GOLEMANCY_WHOLE_SYSTEM_SWEEP.md',
    root/'STAGE143_VALIDATION_REPORT.json',
]
missing=[str(p.relative_to(root)) for p in checks if not p.exists()]
entity=(root/'src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java').read_text()
required=['handleLiquidCore','handleEssentiaCore','useWorkTarget','GolemDecorationType','GolemDecorationItem','decorationsToString']
missing += [f'ThaumGolemEntity missing {r}' for r in required if r not in entity]
mod=(root/'src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java').read_text()
for name in ['GOLEM_DECO_ARMOR','GOLEM_DECO_TOP_HAT','GOLEM_DECO_FEZ','GOLEM_WIRELESS_BACKPACK']:
    if name not in mod:
        missing.append(f'ThaumcraftMod missing {name}')
if missing:
    print('Stage143 golemancy audit failed:')
    for m in missing:
        print(' -',m)
    raise SystemExit(1)
print('Stage143 golemancy whole sweep audit OK')
