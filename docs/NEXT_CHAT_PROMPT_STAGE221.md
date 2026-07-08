Продолжи портирование Thaumcraft 4 на Minecraft/Forge 1.19.2 строго от архива Stage221:
`thaumcraft_legacy_rebuild_STAGE221_TC4_OUTER_LANDS_LOOT_ROOMS_1192_PARITY.zip`.

Обязательные правила:
1. Сверяться с оригиналом `Thaumcraft4-1.7.10-master.zip` и не выдумывать новые механики.
2. Оставаться совместимым с Forge/Minecraft 1.19.2; не переносить напрямую 1.7.10 API (`NBTTag*`, `func_*`, старые world/block/entity классы).
3. В конце нового stage добавить `docs/NEXT_CHAT_PROMPT_STAGE222.md`.
4. Прогнать существующие audit scripts и новый audit текущего stage.

Текущее состояние Stage221:
- Eldritch Crab портирован, включая helm bit, latch/ride attack, sounds и теперь original-style `ConfigItems.itemChestCultistPlate` drop через `CRIMSON_PLATE_CHEST`.
- Добавлены crimson/cultist plate armor items с original texture ids.
- Добавлены `TC4LootBlock` urn/crate, `variant=0..2` как metadata rarity и `1 + md + rand(3)` loot-drop contract.
- `GenNestRoom` adapter теперь ставит urn/crate loot blocks вместо stone placeholder.
- Добавлен `TC4OuterLandsRoomAdapter` с direct adapters для `Gen2x2` и `GenPassage`.

Следующий точный блок Stage222:
- Подключить `TC4OuterLandsRoomAdapter.generate2x2/generatePassage` к live Outer Lands room selection/worldgen path.
- Перенести exact `WeightedRandomLoot` common/uncommon/rare таблицы из TC4 API registration, насколько возможно через текущие 1.19.2 items/proxies.
- Добрать lootbag item-use/opening behaviour (`ItemLootBag#onItemRightClick`) вместо только static item proxies.
- Продолжить baked model tree для Guardian/Golem и crab vent renderer from `crabvent.obj`.
