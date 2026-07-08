# GitHub upload notes — Stage209

Stage209 version: `2.09.0`.

Expected GitHub artifact name:

`thaumcraft_legacy_rebuild_1.19.2-2.09.0-github.jar`

Run before upload:

```bash
python scripts/java_syntax_guard.py
python scripts/github_static_audit.py
python scripts/github_ci_guard.py
python scripts/tc4_stage205_hard_parity_reset_audit.py
python scripts/tc4_stage206_original_parity_repair_audit.py
python scripts/tc4_stage207_infusion_matrix_parity_audit.py
python scripts/tc4_stage208_infusion_renderer_enchantment_audit.py
```

Stage209 keeps the Stage205/206 hard original-parity rules and continues Stage207 Infusion Matrix work with an animated matrix renderer, original infusion enchantment runtime adapter, XP-drain cycle, enchantment/NBT output handling and recipe-aware component pulling.

Important install note: remove old jars from the Minecraft `mods` folder, especially 1.94.0, 2.04.0, 2.05.0, 2.06.0 and 2.07.0 jars, before testing the new jar. If old jars remain, Minecraft can keep loading the old broken version.
