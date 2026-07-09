Продолжай перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2 строго от архива v7.82.

Важно по нумерации: публичный номер текущего архива — v7.82. Старый диапазон 763–782 оставлен только как внутренний audit/history alias. Дальше продолжай компактными публичными batch-номерами, следующий публичный batch: v8.02.

Главное правило: это parity-порт, без новых предметов. Не добавляй новые предметы, блоки, рецепты, GUI, прогрессию или текстуры, если они не являются переносом оригинального TC4 или явно помеченным Forge 1.19.2 adapter.

Текущее состояние v7.82:
- Infusion Matrix terminal failure теперь вызывает уже портированную TC4 weighted instability table напрямую через triggerWeightedEvent(...), без второго probability gate.
- Обычный craftCycle instability roll остался через maybeTrigger(...), то есть вероятность active-craft событий не изменилась.
- TC4 stabilizer/symmetry scan исправлен: центр — Runic Matrix, диапазон Y — matrix-10..matrix+5, зеркальность считается вокруг matrix block.
- Structure/runtime auxiliary summary больше не считает stabilizers от catalyst pedestal и не даёт одностороннему MATRIX_STABILIZER синтетический +10 бонус.
- Thaumometer больше не записывает aspectless block targets в player scan knowledge / legacy NBT, чтобы не было fake scan/progression.
- Arcane Workbench v7.62 stale-preview / shift-click guards сохранены без расширения GUI/рецептов.
- Aura Node/Goggles HUD остаётся reveal-only: общий sortedAspectsForHud/ringIconX/ringIconY ledger, без scan side effects.

Следующий публичный batch: v8.02.
1. Продолжай Infusion Matrix: pedestal item-loss parity, ingredient restoration/cancellation edge cases, catalyst/result NBT equality, enchantment/special recipe runtime cases.
2. Доводи Arcane Workbench: container item return edge cases, ванильный craft vs arcane priority, wand slot click/drag/pickup details, sounds/hover parity.
3. Доводи Aura Node/Thaumometer/Goggles: node scan screen vs HUD parity, visibility distance/raycast edge cases, type/modifier sprite/alpha mapping against original assets.
4. Проверь drift по рецептам/GUI/текстурам и не добавляй мусорные дубли.
