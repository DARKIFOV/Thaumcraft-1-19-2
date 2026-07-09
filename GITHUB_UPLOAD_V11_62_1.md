# GitHub upload — v11.62.1

Upload the archive contents to the repository root and push to `main`.

Hotfix scope:
- Fixes integrated-server startup crash caused by early `RecipeManager` lookup from Arcane Workbench vanilla preview.
- Keeps v11.62 infusion source ordering unchanged.
- No new gameplay content was added.

Key changed files:
- `src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java`
- `build.gradle`
- `src/main/resources/META-INF/mods.toml`
- `README.md`
- `docs/ORIGINAL_TC4_PORTING_STATUS.md`
- `.github/workflows/main.yml`
- `scripts/tc4_v11_62_1_integrated_server_startup_hotfix_audit.py`
