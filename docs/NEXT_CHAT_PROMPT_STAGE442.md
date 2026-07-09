Продолжай с архива Stage423–442. Делай следующий batch Stage443–462 строго как parity-порт оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge 1.19.2.

Главное правило: не придумывать новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение. Если код 1.7.10 нельзя перенести напрямую — сделать минимальный Forge 1.19.2 adapter и явно пометить его как adapter, сохранив оригинальные данные, research keys, страницы, иконки, аспекты, рецепты, координаты, флаги, родителей, hidden parents, siblings, warp, GUI-логику, звуки, текстуры и поведение.

Stage423–442 уже исправил важный визуальный drift: Aura Node HUD теперь масштабирует полный original node_bubble.png 256x256 и полный 64x64 кадр из nodes.png, а не кропает верхний левый угол; in-world Aura Node renderer больше не рисует leftover crossing-plane adapter; все оригинальные core GUI PNG, scanner/goggles/wand model assets и wand fallback textures зеркалированы в активные пути.

Следующий Stage443–462:
1. Продолжить pixel/coordinate parity для Research Table: guiresearchtable2.png, hex1/hex2, copy icon, aspect list, combine slots, note slot/tool slot, inventory offsets, tooltips. Никаких новых кнопок.
2. Продолжить Arcane Workbench parity: gui_arcaneworkbench.png, slot offsets, wand slot, primal aspect cost positions, insufficient-vis behavior, server craft gate.
3. Продолжить Thaumometer/Goggles parity: сверить scanner.obj transform, ModelGoggles geometry/UV, helmet/goggles revealer behavior без fake overlay.
4. Продолжить Aura Node renderer/HUD: frame strip mapping, alpha/blend, size modifiers, node type/modifier display и scan behavior.
5. Вернуться к Infusion Matrix craft parity: catalyst + component pedestal matching, essentia drain order, all ConfigRecipes materialized recipes, instability/failure events, recipe lock and source pedestal behavior.
6. Перед упаковкой прогнать audits и cleanup: не добавлять мусорные duplicate items/recipes/resources; если добавляешь активный asset — он должен быть byte-identical оригинальному TC4 asset либо явно помечен Forge 1.19.2 adapter.

В конце выдать ZIP, отчёт, что сделано, что дальше, и честную оценку оставшихся stage до 100% parity.
