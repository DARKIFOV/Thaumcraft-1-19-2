Продолжи портирование Thaumcraft Legacy Rebuild на Minecraft/Forge 1.19.2 строго по оригиналу TC4 1.7.10.

Базовый архив: `thaumcraft_legacy_rebuild_STAGE222_TC4_OUTER_LANDS_ROOM_SELECTOR_LOOTBAG_1192_PARITY.zip`.
Сверочный оригинал: `Thaumcraft4-1.7.10-master.zip`.

Уже перенесено до Stage222:
- Infusion Matrix parity: двухфазный active/crafting, enchantment recipes, instability/failure FX, runic augmentation.
- Runic shield runtime, fortress armor/masks, champion generation/FX.
- Eldritch Guardian/Warden/Golem/Orb/Crab, boss AI/render adapters, key-room permanent item, crab spawner.
- Outer Lands adapters: GenPortal, Gen2x2, GenPassage, GenLibraryRoom, GenNestRoom, loot urn/crate.
- Stage222 подключил live room selector ring вокруг portal adapter и добавил TC4-style lootbag opening (`8 + rand(5)`) + weighted loot tables.

Следующий Stage223:
1. Не отходи от оригинала TC4 1.7.10.
2. Продолжи прямой перенос Outer Lands worldgen: `MazeHandler`, `Cell`, `GenCommon.PAT_CONNECT`, feature/room graph, boss/key-room placement in chunk coordinates.
3. Перенеси `ItemLootBag` глубже: potion metadata equivalents, enchanted book generation, exact rare bauble/gear mappings where items уже существуют в 1.19.2-порту.
4. Продолжи baked model parity для Eldritch Guardian/Warden/Golem/Crab.
5. Сохрани 1.19.2-safe API: не использовать `NBTTag*`, `func_*`, `World`, `ForgeDirection` из 1.7.10.
6. В архив обязательно добавь `docs/NEXT_CHAT_PROMPT_STAGE223.md`, Stage223 report и audit script.
7. Прогони `java_syntax_guard.py`, `github_static_audit.py`, `github_ci_guard.py` и Stage222/Stage223 audits.
