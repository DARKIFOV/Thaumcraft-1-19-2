Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2 строго с архива Stage205 hard parity reset.

Главное правило теперь обязательное: сначала сверяй оригинальный класс/ресурс TC4 1.7.10, потом делай Forge 1.19.2 adapter. Не добавляй fake GUI, fake recipes, fake items, placeholder textures, duplicate items или прогрессию, которой нет в оригинале. Если добавляешь правильную реализацию, в том же stage удаляй/карантини старую мусорную реализацию.

Текущий Stage205 исправил: текстурные пути GUI/items, Thaumonomicon hidden visibility, wiggly research links, Research Table fake buttons/debug labels, Research Note координаты, tree oversize, shard density и duplicate shard creative clutter.

Следующий stage сделай Stage206 + Stage207:
1. Stage206: Aura Node GUI/HUD + Goggles of Revealing parity: оригинальный node overlay/vis/aspect display, goggles renderer/model texture, helmet overlay без fake messages.
2. Stage207: Infusion Matrix parity start: multiblock scan, pedestal ring, matrix activation, instability, GUI/HUD/particles/sounds по TileInfusionMatrix/BlockStoneDevice.
3. Отдельно проверь крафты: материализуй недостающие original ConfigRecipes recipes и удали/карантини старые fake recipes.
4. Добавь audits для Stage206/207 и прогоняй Stage205 hard parity audit.
