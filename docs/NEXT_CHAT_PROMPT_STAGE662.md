Продолжай перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2 строго от Stage643–662.

Главное правило остаётся: parity-port оригинального TC4 без выдуманных механик, предметов, рецептов, GUI, прогрессии, текстур или поведения. Если прямой перенос 1.7.10 невозможен, делай Forge 1.19.2 adapter, но сохраняй оригинальные данные, research keys, страницы, иконки, аспекты, рецепты, координаты, флаги, родителей, hidden parents, siblings, warp, GUI-логику, звуки, текстуры и поведение.

Текущий фокус после Stage643–662:
1. Дальше добивать Research Table / Research Note pixel + mouse parity: точные зоны hex, drag/place/clear, copy/complete, ink/paper costs, звуки.
2. Не возвращать held-note fallback для edit/clear/solve packets, когда открыт Research Table: slot 0/slot 1 должны оставаться authoritative.
3. Продолжить полный sweep Thaumonomicon: page type order, icons, recipe gates, hidden/lost triggers, без raw adapter labels в видимой книге.
4. Вернуться к Arcane Workbench / Infusion Matrix / Aura Nodes: GUI, крафты, renderer/HUD, behavior strictly как TC4 1.7.10.
5. Каждый stage завершать ZIP, отчётом, audit script, workflow hook и честной оценкой процента готовности.

Следующий batch: Stage663–682.
