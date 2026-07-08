Continue the Thaumcraft Legacy Rebuild port from **Stage220**.

Target: **Minecraft/Forge 1.19.2** only. Do not reintroduce 1.7.10 APIs such as `func_*`, `NBTTag*`, `DataWatcher`, `ForgeDirection`, or old FML classes in active source. Use the original source archive `Thaumcraft4-1.7.10-master.zip` strictly as the parity reference, and continue by adapting behaviour to modern 1.19.2 APIs.

Current base archive to use:
`thaumcraft_legacy_rebuild_STAGE220_TC4_CRAB_MODEL_ROOMS_1192_PARITY.zip`

Previous stage included:
- Stage219 Guardian renderer and first `GenPortal` / `GenCommon` bridge.
- Stage220 added `EldritchCrabEntity`, `EldritchCrabSpawnerBlockEntity`, crab renderer/model layer, baked `ModelPart` bridge for Guardian/Warden/Golem renderers, and direct adapters for `GenLibraryRoom` and `GenNestRoom`.
- Stage220 includes audit script `scripts/tc4_stage220_crab_model_rooms_audit.py`.

For Stage221, continue without drifting from TC4 1.7.10. Recommended next block:
1. Port dedicated crimson plate armor items so Eldritch Crab helm break can drop original `itemChestCultistPlate` equivalent instead of the temporary iron chestplate fallback.
2. Continue full `Gen2x2`, `GenPassage`, `GenLibraryRoom`, `GenNestRoom` block palette parity, especially loot urn/crate, crystal orientation and `blockEldritchNothing` behaviour.
3. Complete baked `ModelPart` tree for `ModelEldritchGuardian` and `ModelEldritchGolem` with all named panels/limbs, replacing remaining manual halo/box fallback where possible.
4. Add stage docs, report JSON, and a new `docs/NEXT_CHAT_PROMPT_STAGE221.md` in the output archive.

Expected checks:
```bash
python scripts/java_syntax_guard.py
python scripts/github_static_audit.py
python scripts/github_ci_guard.py
python scripts/tc4_stage220_crab_model_rooms_audit.py
```

Gradle build may fail in the sandbox if there is no internet to download the Gradle wrapper; mention that honestly and do not treat it as source failure.
