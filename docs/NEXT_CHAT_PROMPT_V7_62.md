Продолжай перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2 строго от архива v7.62.

Важно по нумерации: больше не используй огромные публичные названия Stage743–762. Публичный номер текущего архива — v7.62. Старый диапазон 743–762 оставлен только как внутренний audit/history alias.

Главное правило: это parity-порт, не добавляй новые механики, предметы, GUI, рецепты, прогрессию или текстуры, если они не являются переносом оригинального TC4 или явно помеченным Forge 1.19.2 adapter.

Текущее состояние v7.62:
- Infusion Matrix failure path теперь использует ported TC4 weighted instability table через TC4InfusionFailureParity, а не один smoke placeholder.
- Matrix сохраняет recipefailure / recipefailinstability NBT для диагностики TC4 craftCycle drift.
- Arcane Workbench shift-click output теперь обязан пройти SlotCraftingArcaneWorkbench-style mayPickup/canTakeStack, чтобы stale preview не обходил оплату vis.
- Arcane Workbench output preview сравнивается с текущим результатом по item+NBT+count.
- Aura Node HUD использует общий ledger sortedAspectsForHud/ringIconX/ringIconY, чтобы HUD/renderer/scan не дрейфовали.

Следующий публичный batch: v7.82.
1. Продолжай Infusion Matrix: failure/stabilizer/symmetry edge cases, pedestal item loss, catalyst/result NBT, enchantment/runtime special recipes.
2. Доводи Arcane Workbench: точные звуки/hover/click/pickup/shift-click, vis consume order, wand slot behavior, container item edge cases.
3. Доводи Aura Node/Thaumometer/Goggles: pixel/alpha/order, scan screen/HUD, type/modifier mapping, original assets and transform.
4. Проверь drift по рецептам/GUI/текстурам и не добавляй мусорные дубли.
