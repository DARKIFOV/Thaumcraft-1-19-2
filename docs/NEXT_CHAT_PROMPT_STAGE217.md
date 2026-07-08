Continue from `thaumcraft_legacy_rebuild_STAGE217_TC4_ELDRITCH_RENDER_ROOM_1192_PARITY.zip`.

Hard constraints:
1. Keep the project on Minecraft/Forge 1.19.2.
2. Compare against `Thaumcraft4-1.7.10-master.zip` before porting each block.
3. Do not import legacy 1.7.10 APIs (`GL11`, `func_*`, `NBTTag*`, `DataWatcher`, `ForgeDirection`, `IEntityAdditionalSpawnData`) into new 1.19.2 runtime classes.
4. Preserve original TC4 tags/sound keys/texture paths wherever safe.

Recommended Stage218 focus:
- Replace Stage217 cuboid Warden/Golem bridge models with fuller `ModelEldritchGuardian` / `ModelEldritchGolem` part parity.
- Add Warden/Golem boss health bar display parity and exact death/drop/achievement hooks.
- Expand `TC4OuterLandsBossRoomPlacer` into chunk/structure integration instead of portal-only placement.
- Add lock/key-room block metadata persistence and chest loot table parity.
- Audit `EntityEldritchGuardian`, `EntityEldritchWarden`, `EntityEldritchGolem`, `GenBossRoom`, `GenKeyRoom`, `RenderEldritchGuardian`, and `RenderEldritchGolem` together.
