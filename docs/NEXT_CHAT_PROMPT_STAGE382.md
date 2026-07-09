Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2 строго от архива Stage363–382. Это parity-порт: не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение. Если код 1.7.10 нельзя перенести напрямую, делай Forge 1.19.2 adapter, но сохраняй оригинальные данные, research keys, страницы, иконки, аспекты, рецепты, координаты, флаги, parents/hidden parents/siblings, warp, GUI-логику, звуки, текстуры и поведение.

Текущий Stage363–382 сделал только parity-ремонт без новых фич: aura node renderer переведён на original nodes.png + node_bubble.png billboard adapter, revealer/thaumometer HUD переведён на original hud.png ring/column adapter, Arcane Workbench теперь использует original gui_arcaneworkbench.png, Research Table убрал современные текстовые блоки и рисует аспекты/bonus в parchment/icon style, Infusion Matrix теперь lock-ит travelling component к конкретному pedestal source перед consume, чтобы дубли компонентов не съезжали на другой pedestal.

Следующий Stage383–402 делай по приоритету:
1. Research Table deep parity: открытие/работа research note внутри логики оригинального стола, hex-grid/ниточки/аспектовые иконки/ink/copy без modern debug UI.
2. Aura Node/Goggles/Thaumometer visual parity: сверить original TileNodeRenderer/HUD координаты, node_bubble/nodes strips, scanned/unscanned поведение, revealer rules.
3. Arcane Workbench parity: проверить все slot координаты, wand slot, output slot, vis cost icons, craft consume, research gates, recipes from ConfigRecipes.
4. Infusion Matrix parity: finish craft, instability events, source pedestal FX, essentia drain order, component order, enchantment index, research gates, materialized infusion recipes.
5. Wand/staff/sceptre renderer and focus use parity: сверить ModelWand/ItemWandRenderer constants, caps/rods/focus layers/runes/animations.
6. Texture drift audit: если активная texture отличается от original_tc4_1710, заменить на original или явно пометить как Forge 1.19.2 adapter.

В конце выдай новый ZIP, отчёт, audit script и честно скажи, что осталось до полного 100% TC4 parity без выдумок.
