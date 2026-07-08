Continue from `thaumcraft_legacy_rebuild_STAGE216_TC4_ELDRITCH_ORB_BOSS_AI_1192_PARITY.zip`.

The port target is still Minecraft Forge 1.19.2. Compare against the uploaded original `Thaumcraft4-1.7.10-master.zip` and keep all new code 1.19.2-safe; do not reintroduce 1.7.10 APIs like `func_*`, `NBTTag*`, `DataWatcher`, `ForgeDirection`, or direct FML old networking.

Stage216 completed:

- real `TC4EldritchOrbEntity`;
- real `TC4GolemOrbEntity`;
- Warden ranged orb branch and sonic branch;
- Warden arm-lift/status FX bridge;
- Warden field-frenzy/home-teleport adapter;
- Golem headless beam now spawning `GolemOrb`;
- Golem arc/status FX bridge;
- `TC4OuterLandsBossRoomMetadata` with original `GenBossRoom.PAT_DOORWAY`.

Recommended Stage217 focus:

1. Dedicated Eldritch Warden/Golem model renderers instead of block placeholder renderers.
2. Port original `ModelEldritchGuardian`, `ModelEldritchGolem`, and renderer animation state for Warden arm lift and Golem headless body.
3. Add exact boss idle/hurt/death sound hooks (`egidle`, `egscreech`, `egdeath`) and client particle cadence.
4. Start Outer Lands structure realization from the Stage216 metadata: boss room, lock blocks, boss/key room placement, and exact feature ids.
5. Add an audit that checks renderer/model resource presence and verifies no 1.7.10 render API leaks.
