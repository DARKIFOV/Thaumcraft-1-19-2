# GitHub upload notes — Stage342 audit hotfix

Stage342 version: `3.42.0`.

This hotfix addresses the GitHub Actions failure shown in `Static source/resource audit`:

- `Stage153 infusion lookup must check component pedestals before selecting a recipe`

Run before upload:

```bash
python3 scripts/java_syntax_guard.py
python3 scripts/github_ci_guard.py
python3 scripts/github_static_audit.py
python3 scripts/tc4_stage153_recipe_materialization_parity_audit.py
python3 scripts/tc4_stage155_recipe_resolver_audit.py
python3 scripts/tc4_stage211_runic_shield_runtime_audit.py
python3 scripts/tc4_stage212_fortress_mask_curios_audit.py
python3 scripts/tc4_stage213_fortress_champion_runtime_audit.py
python3 scripts/tc4_stage214_champion_generation_fx_audit.py
python3 scripts/tc4_stage215_eldritch_boss_champion_audit.py
python3 scripts/tc4_stage323_342_super_mega_original_parity_cleanup_audit.py
```

Important: upload this hotfix source as a clean replacement of the previous Stage342 source. Do not mix it with older generated files or old built jars in the repository/mods folder.
