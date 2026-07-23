#!/usr/bin/env python3
import subprocess,sys
checks = [['python3', 'tools/tc4_116437_alchemical_furnace_alembic_full_closure_guard.py'],
 ['python3', 'tools/java_parse_guard_116437.py'],
 ['python3', 'tools/tc4_116436_essentia_tube_full_closure_guard.py'],
 ['python3', 'tools/java_parse_guard_116436.py'],
 ['python3', 'tools/tc4_116435_essentia_jar_full_closure_guard.py'],
 ['python3', 'tools/java_parse_guard_116435.py'],
 ['python3', 'tools/tc4_116355_essentia_storage_mirror_runtime_guard.py'],
 ['python3', 'tools/tc4_116356_essentia_tube_transport_runtime_guard.py'],
 ['python3', 'tools/tc4_116372_essentia_parity_guard.py'],
 ['python3', 'tools/tc4_116434_infusion_altar_full_closure_guard.py'],
 ['python3', 'tools/java_parse_guard_116434.py'],
 ['python3', 'tools/tc4_116359_infusion_stability_runtime_guard.py'],
 ['python3', 'tools/tc4_116360_infusion_lifecycle_runtime_guard.py'],
 ['python3', 'tools/tc4_116361_infusion_pause_resume_runtime_guard.py'],
 ['python3', 'tools/tc4_116362_infusion_save_reload_runtime_guard.py'],
 ['python3', 'tools/tc4_116363_infusion_instability_event_table_guard.py'],
 ['python3', 'tools/tc4_116364_infusion_shortage_instability_guard.py'],
 ['python3', 'tools/tc4_116433_wand_foci_full_closure_guard.py'],
 ['python3', 'tools/java_parse_guard_116433.py'],
 ['python3', 'tools/tc4_116432_wand_rods_caps_full_closure_guard.py'],
 ['python3', 'tools/java_parse_guard_116432.py'],
 ['python3', 'tools/tc4_116371_wand_parity_guard.py'],
 ['python3', 'tools/tc4_116332_wand_component_families_parity_guard.py'],
 ['python3', 'tools/tc4_116431_arcane_workbench_full_closure_guard.py'],
 ['python3', 'tools/java_parse_guard_116431.py'],
 ['python3', 'tools/tc4_116430_fertility_lamp_full_closure_guard.py'],
 ['python3', 'tools/java_parse_guard_116430.py'],
 ['python3', 'tools/tc4_116429_growth_lamp_full_closure_guard.py'],
 ['python3', 'tools/java_parse_guard_116429.py'],
 ['python3', 'tools/tc4_116428_infernal_furnace_full_closure_guard.py'],
 ['python3', 'tools/java_parse_guard_116428.py'],
 ['python3', 'tools/tc4_116427_research_system_full_closure_guard.py'],
 ['python3', 'tools/tc4_116426_thaumonomicon_full_closure_guard.py'],
 ['python3', 'tools/tc4_116425_thaumometer_full_closure_guard.py'],
 ['python3', 'tools/tc4_116424_arcane_bore_full_closure_guard.py'],
 ['python3', 'tools/tc4_116423_arcane_spa_full_closure_guard.py'],
 ['python3', 'tools/tc4_116422_arcane_lamp_full_closure_guard.py'],
 ['python3', 'tools/tc4_116421_arcane_pressure_plate_full_closure_guard.py'],
 ['python3', 'tools/tc4_116420_arcane_levitator_full_closure_guard.py'],
 ['python3', 'tools/tc4_116419_arcane_ear_full_closure_guard.py'],
 ['python3', 'tools/tc4_116418_arcane_bellows_full_closure_guard.py'],
 ['python3', 'tools/tc4_116417_tallow_candle_full_closure_guard.py'],
 ['python3', 'tools/tc4_116416_hungry_chest_full_closure_guard.py'],
 ['python3', 'tools/tc4_116415_brain_jar_full_closure_guard.py'],
 ['python3', 'tools/tc4_116414_bath_salts_full_closure_guard.py'],
 ['python3', 'tools/tc4_116413_unnatural_hunger_full_closure_guard.py'],
 ['python3', 'tools/tc4_116412_unnatural_hunger_food_guard.py'],
 ['python3', 'tools/tc4_116411_sun_scorned_brightness_guard.py'],
 ['python3', 'tools/tc4_116410_death_gaze_cone_guard.py'],
 ['python3', 'tools/tc4_116409_warp_research_pool_sync_guard.py'],
 ['python3', 'tools/tc4_116408_eldritch_milestone_grant_guard.py'],
 ['python3', 'tools/tc4_116407_warp_message_parity_guard.py'],
 ['python3', 'tools/tc4_116405_warp_spawn_collision_guard.py'],
 ['python3', 'tools/tc4_116404_warp_spawn_offset_guard.py'],
 ['python3', 'tools/tc4_116403_warp_runtime_guard.py'],
 ['python3', 'tools/java_syntax_guard.py'],
 ['python3', 'tools/validate_json_resources.py'],
 ['python3', 'tools/validate_runtime_manifest.py', '--manifest', 'runtime_artifacts/runtime_test_manifest.template.json', '--version', '11.64.37', '--template'],
 ['python3', 'tools/tc4_recipe_registration_denominator_guard.py'],
 ['python3', 'tools/tc4_116394_source_inventory_guard.py'],
 ['python3', 'tools/tc4_116394_parity_consolidation_guard.py']]
checks = checks[26:]
for i,cmd in enumerate(checks,27):
    print(f"[{i}/{len(checks)}] {' '.join(cmd)}", flush=True)
    try:
        p=subprocess.run(cmd,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,timeout=150)
    except subprocess.TimeoutExpired as e:
        if e.stdout: print(e.stdout if isinstance(e.stdout,str) else e.stdout.decode(errors='replace'),flush=True)
        print(f"FOCUSED STATIC CI v11.64.37: TIMEOUT at {i}",file=sys.stderr,flush=True)
        raise SystemExit(124)
    print(p.stdout,end='' if p.stdout.endswith('\n') else '\n',flush=True)
    if p.returncode:
        print(f"FOCUSED STATIC CI v11.64.37: FAIL at {i}",file=sys.stderr,flush=True)
        raise SystemExit(p.returncode)
print("FOCUSED STATIC CI v11.64.37 CONTINUATION: PASS (35/35)")
