Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage198 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

Текущий статус: Stage197 добавил golem task AI parity batch 2, Stage198 добавил essentia tube subclass parity batch, также добавлен Forge 1.19.2 `pack.mcmeta` (`pack_format: 9`) для исправления предупреждения Minecraft о resource pack info.

В следующем проходе сделай Stage199 + Stage200:

1. Stage199: golem bell/marker UI parity batch. Перенеси original ItemGolemBell interactions, marker list semantics, home/guard/use/liquid/essentia marker editing и display text как Forge 1.19.2 adapter.
2. Stage200: tube/jar renderer and label/filter visual parity batch. Улучши TileTubeFilter/Restrict/Oneway/Buffer/Valve renderer paths, jar label/filter display, venting visuals и buffer aspect display ближе к original TC4 renderers без новых эффектов.
3. Используй оригинальные `ItemGolemBell`, `EntityGolemBase`, `TileTube*`, `TileJarFillable`, `TileTube*Renderer` как источник правды.
4. Добавь аудиты `scripts/tc4_stage199_golem_bell_marker_audit.py` и `scripts/tc4_stage200_tube_jar_visual_audit.py`.
5. Не ломай предыдущие stages 144–198 и не удаляй `src/main/resources/pack.mcmeta`, иначе Minecraft снова выдаст предупреждение о resource pack info.
6. В конце выдай новый ZIP, напиши что сделал, что будешь делать дальше, сколько stage ещё осталось, и обнови universal prompt.
