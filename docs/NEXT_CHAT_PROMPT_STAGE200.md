Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage200 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

Главное правило: это должен быть строгий original TC4 parity-port. Если код 1.7.10 нельзя перенести напрямую, сделай Forge 1.19.2 adapter, но сохрани оригинальные NBT keys, research keys, страницы, иконки, аспекты, рецепты, координаты, флаги, GUI-логику, звуки, текстуры и поведение.

Текущий статус:
* Stage198 завершил golem task AI batch 2 и tube subclass parity.
* Stage199 добавил golem bell/marker UI parity: original `ItemGolemBell` NBT `golemid`, `golemhomex/y/z`, `golemhomeface`, `markers[{x,y,z,dim,side,color}]`, bell binding и marker toggle/cycle adapter.
* Stage200 добавил tube/jar renderer/label/filter visual parity и усилил resource-pack/texture audit: `pack.mcmeta` должен оставаться валидным, subtype tube models/items должны ссылаться на свои texture paths.

В следующем проходе сделай Stage201 + Stage202:
1. Stage201: golem GUI/container parity: original `GuiGolem` / `ContainerGolem`, inventory slots, upgrade/color marker editing, bell-bound golem interaction.
2. Stage202: jar/tube interaction parity: jar label item behavior, filter clear/set, warded/void jar interactions, tube filter aspect selection via phial/label/wand adapters.
3. Используй оригинальные `ItemGolemBell`, `GuiGolem`, `ContainerGolem`, `TileJarRenderer`, `BlockJar`, `BlockTube`, `TileTube*` как источник правды.
4. Добавь audits `scripts/tc4_stage201_golem_gui_container_audit.py` и `scripts/tc4_stage202_jar_tube_interaction_audit.py`.
5. Не ломай pack.mcmeta и texture paths; если текстуры ломаются, сначала проверь `src/main/resources/pack.mcmeta` и `assets/thaumcraft/**`.
6. В конце выдай новый ZIP, что сделал, что дальше, сколько stage осталось, и обнови prompt.
