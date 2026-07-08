Продолжай перенос Thaumcraft Legacy Rebuild строго на Minecraft/Forge 1.19.2, не отходя от оригинального Thaumcraft 4.2.3.5 для Minecraft 1.7.10.

Базовый архив для продолжения: `thaumcraft_legacy_rebuild_STAGE219_TC4_GUARDIAN_MODEL_PORTAL_1192_PARITY.zip`.
Оригинал для сверки: `Thaumcraft4-1.7.10-master.zip`.

Что уже сделано к Stage219:
- Stage207–209: Infusion Matrix parity, enchantment recipes, instability/failure table, FX packets, damage/NBT matching.
- Stage210–212: Runic Augmentation, runtime runic shielding, fortress masks, Curios/Baubles reflective adapter.
- Stage213–214: fortress armor runtime, champion modifier runtime, champion generation/showFX/display-name parity.
- Stage215–218: Eldritch Warden/Golem boss entities, Eldritch/Golem orbs, boss bar/death drops, permanent key-room item, key-room guardian spawns.
- Stage219: normal Eldritch Guardian now has a dedicated renderer using original `eldritch_guardian.png`; guardian distance-alpha fade mirrors `RenderEldritchGuardian`; `TC4EldritchBossLayerDefinitions` registers 1.19.2 ModelPart/LayerDefinition anchors for Guardian/Warden/Golem; `TC4OuterLandsGenCommonAdapter` starts the direct `GenCommon`/`GenPortal` palette and portal-room bridge; `EldritchPortalBlockEntity` calls the portal room adapter before boss/key rooms.

Important constraints:
- Target stays Minecraft/Forge 1.19.2.
- Do not introduce old 1.7.10 API calls like `func_*`, `NBTTag*`, `ForgeDirection`, `WorldProvider`, or direct TC4 classes in runtime code.
- Compare every port block against `Thaumcraft4-1.7.10-master.zip` before changing behavior.
- Keep adding a new `docs/NEXT_CHAT_PROMPT_STAGE###.md` in every archive.
- Keep the version bump and add docs/report/audit script for every stage.

Recommended Stage220:
1. Convert Guardian/Warden/Golem renderers to consume the registered `ModelLayerLocation` / baked `ModelPart` contracts instead of the temporary low-level cuboid bridge.
2. Continue copying remaining high-impact `ModelEldritchGuardian` and `ModelEldritchGolem` parts from the 1.7.10 constructors.
3. Add dedicated `EldritchCrabEntity` and crab-spawner adapter because original `GenCommon` queues crab spawners/decorations.
4. Start direct `Gen2x2`, `GenPassage`, `GenLibraryRoom`, and `GenNestRoom` adapters using 1.19.2-safe code.
5. Run at minimum:
   - `python scripts/java_syntax_guard.py`
   - `python scripts/github_static_audit.py`
   - `python scripts/github_ci_guard.py`
   - all Stage205–Stage219 parity audits available in `scripts/`.
