Продолжай перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2 строго от архива v8.02.

Важно по нумерации: публичный номер текущего архива — v8.02. Старый диапазон 783–802 оставлен только как внутренний audit/history alias. Следующий публичный batch: v8.22.

Главное правило: это parity-порт, без новых предметов. Не добавляй новые предметы, блоки, рецепты, GUI, прогрессию или текстуры, если они не являются переносом оригинального TC4 или явно помеченным Forge 1.19.2 adapter.

Текущее состояние v8.02:
- Infusion Matrix сохраняет exact `lockedCatalystSnapshot` ItemStack при старте craft, пишет `LockedCatalystSnapshot` и legacy-style `recipeinput`.
- Running catalyst validation теперь сравнивает item id, damage wildcard semantics и NBT против locked snapshot, чтобы mid-craft NBT/damage swap не завершал старый recipe.
- Arcane Workbench output slot остаётся virtual preview: не сохраняется в world NBT, очищается при load, не дропается при разрушении блока.
- v7.82 fixes сохранены: terminal failure через weighted table без второго probability gate; matrix-centered stabilizer/symmetry scan; aspectless thaumometer scans rejected.
- Прогресс портирования зафиксирован: примерно 72% готово, осталось примерно 28% до честного 100% TC4 parity.

Следующий публичный batch: v8.22.
1. Infusion Matrix: component source-lock должен хранить exact ItemStack/spec identity, а не только ResourceLocation id, для damage/NBT-sensitive components.
2. Arcane Workbench: vanilla remaining-items parity через Recipe#getRemainingItems, чтобы bucket/container edge-cases совпадали с 1.19.2 vanilla crafting path и не расходились с TC4 priority.
3. Aura Node/Thaumometer/Goggles: distance/raycast hardening, scan screen vs HUD parity, node type/modifier sprite/alpha mapping against original assets.
4. Research/GUI drift: проверить, что новые фиксы не вернули fake preview, дубль output, stub progression или мусорные рецепты.
5. В каждом следующем ответе пользователю писать план работ и обновлённый процент: сколько готово и сколько осталось до 100% портирования.
