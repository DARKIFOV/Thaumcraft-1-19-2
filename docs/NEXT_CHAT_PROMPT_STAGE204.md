Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage204 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

Главное правило: это должен быть строгий оригинальный TC4 parity-порт. Если код 1.7.10 нельзя перенести напрямую, сделай адаптер под Forge 1.19.2, но сохрани оригинальные данные, NBT keys, GUI координаты, слоты, texture paths, research keys, аспекты, рецепты, звуки и поведение.

Текущий статус: Stage203 восстановил `ContainerGolem` ghost-slot parity (`SlotGhost`, `SlotGhostFluid`, copy-only filter stacks, fill-core limit 256, liquid fluid validation, click/shift-click count semantics). Stage204 восстановил jar/tube exact transfer edge cases (`TileJarFillable.addToContainer/takeFromContainer`, `TileJarFillableVoid`, void jar overflow, jar suction 32/64/48, `TileTubeOneway` direction gates, filter/restrict/buffer checks) и сохранил resource-pack/texture fix.

В следующем проходе сделай Stage205 + Stage206:

1. Stage205: golem renderer/model parity polish: сверить `RenderGolemBase`, decorations, carried item rendering, core/marker visual feedback, animation offsets, sounds, без новых моделей.
2. Stage206: tube raytrace/subHit wand interaction parity: перенести точные side subHit/cuboid semantics для `TileTube`, `TileTubeBuffer`, valve/openSides/choke side toggles вместо простого `BlockHitResult.getDirection()` adapter.
3. Добавь аудиты `scripts/tc4_stage205_golem_renderer_model_audit.py` и `scripts/tc4_stage206_tube_raytrace_wand_audit.py`.
4. Проверь, что не сломаны текстуры: `pack.mcmeta`, `assets/thaumcraft/textures/...`, `guigolem.png`, tube/jar label paths, golem renderer paths.
5. В конце выдай новый ZIP, что сделал, что дальше, сколько stage осталось, и обнови prompt.
