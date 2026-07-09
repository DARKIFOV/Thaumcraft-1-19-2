Продолжай перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2 строго от Stage623–642.

Главное правило остаётся: parity-port оригинального TC4 без выдуманных механик, предметов, рецептов, GUI, прогрессии, текстур или поведения. Если прямой перенос 1.7.10 невозможен, делай Forge 1.19.2 adapter, но сохраняй оригинальные данные, research keys, страницы, иконки, аспекты, рецепты, координаты, флаги, родителей, hidden parents, siblings, warp, GUI-логику, звуки, текстуры и поведение.

Текущий фокус после Stage623–642:
1. Дальше добивать Research Table / Research Note до настоящего TC4: pixel parity, exact mouse zones, aspect drag/place/clear behaviour, scribing ink consumption, completion/copy flow, sounds.
2. Продолжить полный sweep Thaumonomicon: страницы, page type order, иконки, gated recipe pages, hidden/lost trigger paths, без вывода raw adapter/debug keys в видимую книгу.
3. Проверять, что research-note crafting recipes не возвращаются, а Scribing Tools refill остаётся оригинальным ink-sac crafting path.
4. Дальше переносить Arcane Workbench, Infusion Matrix, Aura Nodes, wands/foci, golems, taint, eldritch, worldgen строго по оригиналу.
5. Каждый stage завершать ZIP, отчётом, audit script, workflow hook и честной оценкой процента готовности.
