Продолжай перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2 строго с архива Stage603–622.

Главное правило: это строгий TC4 parity-порт. Не добавляй новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4. Если код 1.7.10 нельзя перенести напрямую, делай Forge 1.19.2 adapter, но сохраняй оригинальные данные, research keys, страницы, иконки, аспекты, рецепты, координаты, флаги, родителей, hidden parents, siblings, warp, GUI-логику, звуки, текстуры и поведение.

Текущее состояние после Stage603–622:
- версия 6.22.0;
- Research Table получил общий `TC4ResearchTableParity` ledger для оригинальных координат GuiResearchTable и action codes;
- table slot 0/1 теперь authoritative при открытом контейнере: нет fallback к предметам в руке/инвентаре;
- создание research note идёт только через Research Table, с all-or-nothing paper + scribing ink;
- копирование solved note precheck-ит paper + ink sac + original aspect cost перед расходом;
- старые duplicate crafting-table recipes для research_note удалены;
- звуки create/open/copy/complete идут через оригинальные TC4 sound keys write/page/learn;
- audit `tc4_stage603_622_research_table_deep_parity_audit.py` подключён в GitHub Actions.

Следующий Stage623–642:
1. Добить Research Table pixel/mouse parity: все hotzones, hover, note/copy/combine/page arrows, инвентарь, no vanilla buttons.
2. Проверить Scribing Tools по оригинальному source: maxDamage/refill/repair/ink расход, не менять без source-подтверждения.
3. Довести Research Note GUI: координаты hex grid, линии-ниточки, anchor/fixed/blank slot rendering, completion/copy/convert flow.
4. Сделать полный Thaumonomicon sweep: ConfigResearch pages/icons/recipe gates, hidden/lost trigger paths, parents/hidden parents/siblings/warp/flags.
5. Не добавлять новые рецепты, предметы или placeholder GUI; если найдёшь старый мусор/дубли, удалить или пометить как quarantine adapter.

В конце выдай новый ZIP, отчёт, audit-скрипт, cleanup-проверку и честно напиши процент готовности до 100% parity.
