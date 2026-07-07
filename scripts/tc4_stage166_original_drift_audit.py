#!/usr/bin/env python3
from pathlib import Path
import json, sys
ROOT=Path(__file__).resolve().parents[1]
errors=[]
def read(p): return (ROOT/p).read_text(encoding='utf-8')
for rel in ['src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_original_drift_check_stage166.json','STAGE166_ORIGINAL_DRIFT_CHECK_REPORT.json','docs/ORIGINAL_TC4_PORTING_STATUS.md']:
    if not (ROOT/rel).exists(): errors.append(f'missing {rel}')
if not errors:
    data=json.loads(read(Path('src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_original_drift_check_stage166.json')))
    if data.get('stage') != 166: errors.append('drift json stage mismatch')
    answer=(data.get('answer') or '').lower()
    if 'corrected' not in ' '.join(data.get('corrected_deviation',[])).lower() and 'corrected' not in answer:
        errors.append('drift report must explicitly state correction')
    status=read(Path('docs/ORIGINAL_TC4_PORTING_STATUS.md'))
    for token in ['Stage168', 'Drift check', 'persistent ResearchTableBlockEntity', 'compatibility fallback']:
        if token not in status: errors.append(f'status missing {token}')
    block=read(Path('src/main/java/com/darkifov/thaumcraft/block/ResearchTableBlock.java'))
    if 'NetworkHooks.openScreen' not in block or 'openResearchNote(serverPlayer, held)' in block:
        errors.append('ResearchTableBlock must use real container, not held-note direct screen primary path')
for token in ['new original', 'fake replacement', 'approximate aspect']:
    # docs may mention forbidden phrasing; only check Java source for dangerous wording.
    for path in (ROOT/'src/main/java/com/darkifov/thaumcraft/research').glob('*.java'):
        if token in path.read_text(encoding='utf-8', errors='ignore').lower():
            errors.append(f'possible drift wording in {path.relative_to(ROOT)}: {token}')
if errors:
    for e in errors: print('::error::'+e)
    sys.exit(1)
print('Stage168 original drift audit: OK')
