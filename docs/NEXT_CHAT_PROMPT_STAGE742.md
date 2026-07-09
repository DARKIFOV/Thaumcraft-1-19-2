Продолжай строго от Stage723–742. Это parity-порт оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не перенесён оригинальный TC4.

Текущий фокус Stage743–762:
1. Infusion Matrix: глубже failure/stabilizer/symmetry/FX parity, сверить стабилизаторы и нестабильность с оригинальным TileInfusionMatrix, сохранить оригинальные NBT keys и не возвращать catalyst-only lookup.
2. Arcane Workbench: точный hover/click/shift-click/container item behavior, sounds, slot coordinates и vis-cost/cap modifier behavior как в TC4.
3. Aura Nodes / Thaumometer / Goggles: довести HUD/scan GUI pixel parity, node type/modifier frame mapping, alpha/order, exact revealer rules.
4. Проверить, что новые адаптеры явно помечены Forge 1.19.2 adapter и не добавляют survival-контент.
5. Обновить audit, отчёт и новый NEXT_CHAT_PROMPT_STAGE762.md.

Всегда проверяй drift: если адаптер отличается от оригинала TC4 1.7.10, исправь или явно пометь как Forge 1.19.2 adapter.
