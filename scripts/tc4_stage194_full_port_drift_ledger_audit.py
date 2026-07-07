#!/usr/bin/env python3
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
read = lambda p: (ROOT / p).read_text(encoding='utf-8') if (ROOT / p).exists() else ''
exists = lambda p: (ROOT / p).exists()

ledger_path = ROOT / 'src/main/resources/data/thaumcraft/tc4_drift/full_port_drift_ledger_stage194.json'
ledger = json.loads(ledger_path.read_text(encoding='utf-8')) if ledger_path.exists() else {}
systems = ledger.get('systems', [])
by_system = {entry.get('system'): entry for entry in systems}
expected = {
    'golems', 'wands_foci', 'aura_nodes', 'crucible', 'infusion', 'taint', 'eldritch',
    'worldgen', 'thaumonomicon_research', 'research_table', 'arcane_workbench'
}
java = read('src/main/java/com/darkifov/thaumcraft/porting/TC4FullPortDriftLedger.java')
doc = read('docs/TC4_FULL_PORT_DRIFT_LEDGER_STAGE194.md')
status = read('docs/ORIGINAL_TC4_PORTING_STATUS.md')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')
workflow = read('.github/workflows/main.yml')
guard = read('scripts/github_ci_guard.py')
report = read('STAGE194_FULL_PORT_DRIFT_LEDGER_REPORT.json')

checks = {
    'version_194': "version = '1.94.0'" in build and 'version="1.94.0"' in mods,
    'ledger_resource_exists': ledger_path.exists() and ledger.get('stage') == 194 and ledger.get('version') == '1.94.0',
    'all_major_systems_present': expected.issubset(by_system.keys()) and len(systems) >= 11,
    'entries_have_tc4_source_and_adapter_notes': all(entry.get('tc4_source') and entry.get('parity_locked') and entry.get('remaining_drift') and entry.get('adapter_notes') for entry in systems),
    'arcane_workbench_documents_stage193_cleanup': 'arcane_workbench' in by_system and 'NetworkHooks.openScreen replaces legacy standalone packet screen' in ' '.join(by_system['arcane_workbench'].get('adapter_notes', [])),
    'wands_foci_has_original_focus_sources': 'wands_foci' in by_system and all(token in ' '.join(by_system['wands_foci'].get('tc4_source', [])) for token in ['ItemWandCasting', 'WandManager', 'ItemFocusPrimal']),
    'doc_exists_and_covers_partial_systems': exists('docs/TC4_FULL_PORT_DRIFT_LEDGER_STAGE194.md') and all(token in doc for token in ['Golems', 'Eldritch', 'Worldgen', 'Essentia transport']),
    'java_runtime_ledger_exists': exists('src/main/java/com/darkifov/thaumcraft/porting/TC4FullPortDriftLedger.java') and 'public static final int STAGE = 194' in java and 'public static final String VERSION = "1.94.0"' in java,
    'report_exists': 'systems_covered' in report and 'golems' in report and 'arcane_workbench' in report,
    'status_mentions_stage194': 'Stage194' in status and 'TC4_FULL_PORT_DRIFT_LEDGER_STAGE194.md' in status,
    'workflow_and_guard': 'tc4_stage194_full_port_drift_ledger_audit.py' in workflow and 'tc4_stage194_full_port_drift_ledger_audit.py' in guard,
}

errors = [name for name, ok in checks.items() if not ok]
if errors:
    for error in errors:
        print(f'::error::Stage194 full-port drift ledger audit failed: {error}')
    print(json.dumps({'systems': sorted(by_system.keys()), 'stage': ledger.get('stage'), 'version': ledger.get('version')}, indent=2))
    sys.exit(1)
print('Stage194 full-port drift ledger audit: OK')
print(json.dumps({'systems': sorted(by_system.keys()), 'partial_count_hint': java.count('partial')}, indent=2))
