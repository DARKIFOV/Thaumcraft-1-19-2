Continue the Thaumcraft 4 -> Minecraft/Forge 1.19.2 port from mega-stage Stage253-262.

Use these files as the active base:
- Base ZIP: `thaumcraft_legacy_rebuild_STAGE253_262_TC4_FULL_STAGE_AUDIT_CLEANUP_BATCH_1192_PARITY.zip`
- Original reference: `Thaumcraft4-1.7.10-master.zip`

Hard rules:
- Keep the target on Minecraft/Forge 1.19.2.
- Do not drift from original TC4 core mechanics.
- Keep addon work quarantined unless explicitly asked to port that addon.
- Keep `docs/NEXT_CHAT_PROMPT_STAGE###.md` inside every resulting archive.
- Prefer mega-stage batches of 5-10 internal stages in one ZIP.

Stage253-262 summary:
- Version is `2.62.0`.
- Full-stage audit pass was added.
- `TC4RegistryGarbageGuard` hides accumulated debug/addon/non-core placeholder registry entries from the Thaumcraft creative tab.
- Outer Lands loot generation now replaces quarantined garbage stacks.
- Split 1.19.2 blocks were added for `TileEldritchCap`, `TileEldritchLock`, `TileEldritchTrap`, and `TileEldritchCrystal` equivalents.
- `TC4EldritchBlockVariantAdapter` now maps TC4 metadata 3/8/10/crystal anchor to those split blocks.

Recommended next mega-stage Stage263-272:
- Continue real `TileEldritchLock` parity: count animation, opening sequence, redstone/door removal behavior, particle cadence.
- Continue `TileEldritchTrap` parity: rune FX, trigger rules, reset/disable behavior.
- Port `TileEldritchCapRenderer`, `TileEldritchLockRenderer`, `TileEldritchCrystalRenderer` to 1.19.2 BER/layer adapters.
- Deepen `GenLibraryRoom`, `GenNestRoom`, `GenPassage`, and `Gen2x2` using original loop bodies and decoration queues.
- Keep quarantined addon registries out of creative/loot and do not reintroduce them into TC4 core progression.
