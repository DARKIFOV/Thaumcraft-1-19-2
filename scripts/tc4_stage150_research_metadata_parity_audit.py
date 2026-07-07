#!/usr/bin/env python3
from __future__ import annotations

import json
import re
import sys
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[1]

def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding='utf-8', errors='ignore')

def exists(rel: str) -> bool:
    return (ROOT / rel).exists()

def split_top_level(value: str) -> list[str]:
    out: list[str] = []
    start = 0
    depth = 0
    in_string = False
    escape = False
    for i, char in enumerate(value):
        if in_string:
            if escape:
                escape = False
            elif char == '\\':
                escape = True
            elif char == '"':
                in_string = False
        else:
            if char == '"':
                in_string = True
            elif char in '({[':
                depth += 1
            elif char in ')}]':
                depth -= 1
            elif char == ',' and depth == 0:
                out.append(value[start:i].strip())
                start = i + 1
    out.append(value[start:].strip())
    return out

def string_array(arg: str) -> list[str]:
    if arg.strip() == 'new String[0]':
        return []
    return re.findall(r'"([^"\\]*(?:\\.[^"\\]*)*)"', arg)

def parse_int(arg: str) -> int:
    m = re.search(r'-?\d+', arg)
    return int(m.group(0)) if m else 0

def parse_aspects(arg: str) -> dict[str, int]:
    if 'Map.of()' in arg:
        return {}
    vals = string_array(arg)
    nums = [int(x) for x in re.findall(r'(?<![A-Za-z_])-?\d+(?![A-Za-z_])', arg)]
    return {k: nums[i] for i, k in enumerate(vals) if i < len(nums)}

def research_entry_bodies(source: str) -> list[str]:
    bodies: list[str] = []
    idx = 0
    while True:
        match = re.search(r'new ResearchEntry\(', source[idx:])
        if not match:
            return bodies
        start = idx + match.start()
        pos = source.find('(', start) + 1
        depth = 1
        i = pos
        in_string = False
        escape = False
        while i < len(source) and depth:
            char = source[i]
            if in_string:
                if escape:
                    escape = False
                elif char == '\\':
                    escape = True
                elif char == '"':
                    in_string = False
            else:
                if char == '"':
                    in_string = True
                elif char in '({[':
                    depth += 1
                elif char in ')}]':
                    depth -= 1
            i += 1
        bodies.append(source[pos:i - 1])
        idx = i

def current_metadata() -> dict[str, dict[str, Any]]:
    source = read('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchRuntimeBridge.java')
    result: dict[str, dict[str, Any]] = {}
    for body in research_entry_bodies(source):
        args = split_top_level(body)
        if len(args) < 18:
            continue
        key_match = re.match(r'"([^"]+)"', args[0])
        if not key_match:
            continue
        key = key_match.group(1)
        result[key] = {
            'key': key,
            'category': string_array(args[3])[0] if string_array(args[3]) else '',
            'display_column': parse_int(args[4]),
            'display_row': parse_int(args[5]),
            'complexity': parse_int(args[6]),
            'aspects': parse_aspects(args[7]),
            'parents': string_array(args[8]),
            'parents_hidden': string_array(args[9]),
            'siblings': string_array(args[10]),
            'flags': string_array(args[11]),
            'warp': parse_int(args[17]),
            'entity_triggers': string_array(args[15]),
            'aspect_triggers': string_array(args[16]),
        }
    return result

source_path = ROOT / 'src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_original_research_items.json'
metadata_path = ROOT / 'src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_original_research_metadata_stage150.json'
source_items = json.loads(source_path.read_text(encoding='utf-8')) if source_path.exists() else []
expected_items = json.loads(metadata_path.read_text(encoding='utf-8')) if metadata_path.exists() else []
expected = {e['key']: e for e in expected_items}
current = current_metadata()

metadata_mismatches: list[dict[str, Any]] = []
for key, exp in expected.items():
    cur = current.get(key)
    if not cur:
        metadata_mismatches.append({'key': key, 'missing': True})
        continue
    for field in ['category','display_column','display_row','complexity','aspects','parents','parents_hidden','siblings','flags','warp','entity_triggers','aspect_triggers']:
        if cur.get(field) != exp.get(field):
            metadata_mismatches.append({'key': key, 'field': field, 'expected': exp.get(field), 'actual': cur.get(field)})

index = read('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchMetadataIndex.java') if exists('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchMetadataIndex.java') else ''
registry = read('src/main/java/com/darkifov/thaumcraft/research/ResearchRegistry.java')
layout = read('src/main/java/com/darkifov/thaumcraft/client/screen/OriginalResearchLayout.java')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')
workflow = read('.github/workflows/main.yml')
guard = read('scripts/github_ci_guard.py')

trigger_keys = [e['key'] for e in source_items if e.get('item_triggers_raw') or e.get('entity_triggers') or e.get('aspect_triggers')]
missing_trigger_literals = []
for e in source_items:
    for field in ['item_triggers_raw','entity_triggers','aspect_triggers']:
        for trigger in e.get(field, []):
            if trigger not in index:
                missing_trigger_literals.append({'key': e['key'], 'field': field, 'trigger': trigger})

checks = {
    'version_stage150_or_later': any((f"version = '{v}'" in build and f'version="{v}"' in mods) for v in ['2.04.0', '2.02.0', '2.00.0', '1.98.0', '1.78.0', '1.76.0', '1.70.0', '1.65.0', '1.64.0', '1.63.0', '1.62.0', '1.61.0', '1.60.0', '1.59.0', '1.58.0', '1.57.0', '1.56.0', '1.55.0', '1.54.0', '1.51.0', '1.53.0', '1.52.0']),
    'stage150_metadata_spec_present': len(expected_items) == 201,
    'stage150_metadata_matches_original_source': expected_items == [{k:e.get(k) for k in ['key','category','display_column','display_row','complexity','aspects','parents','parents_hidden','siblings','flags','warp','item_triggers_raw','entity_triggers','aspect_triggers']} for e in source_items],
    'runtime_key_count_201': len(current) == 201,
    'metadata_fields_match_runtime_bridge': not metadata_mismatches,
    'metadata_index_java_present': 'class TC4ResearchMetadataIndex' in index,
    'all_trigger_literals_preserved_in_index': not missing_trigger_literals,
    'original_registry_slice_added': 'originalEntries()' in registry and 'ORIGINAL_TC4_ENTRY_COUNT' in registry,
    'thaumonomicon_layout_uses_original_entries': 'ResearchRegistry.originalEntries()' in layout,
    'workflow_runs_stage150_audit': 'tc4_stage150_research_metadata_parity_audit.py' in workflow,
    'github_guard_runs_stage150_audit': 'tc4_stage150_research_metadata_parity_audit.py' in guard,
}

report = {
    'stage': 150,
    'goal': 'strict original TC4 ResearchItem metadata parity',
    'expected_research_keys': len(expected),
    'current_research_keys': len(current),
    'triggered_research_keys': len(trigger_keys),
    'metadata_mismatches': metadata_mismatches[:20],
    'missing_trigger_literals': missing_trigger_literals[:20],
    'remaining_stage_estimate_after_stage150': '42-67 stages before Stage151; 41-66 after Stage151',
    'checks': checks,
    'passed': all(checks.values()),
}
print(json.dumps(report, indent=2, ensure_ascii=False))
if not report['passed']:
    for name, ok in checks.items():
        if not ok:
            print(f'::error::Stage150 research metadata parity audit failed: {name}')
    sys.exit(1)
