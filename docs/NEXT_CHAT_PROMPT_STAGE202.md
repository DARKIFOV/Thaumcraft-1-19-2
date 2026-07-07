Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage202 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

Главное правило: это должен быть строгий оригинальный TC4 parity-порт. Если код 1.7.10 нельзя перенести напрямую, сделай адаптер под Forge 1.19.2, но сохрани оригинальные данные, NBT keys, GUI координаты, слоты, texture paths, research keys, аспекты, рецепты, звуки и поведение.

Текущий статус: Stage201 добавил golem GUI/container parity (`GuiGolem`, `ContainerGolem`, `guigolem.png`, scroll 66/67, toggles 50..57, colors, GUI pause). Stage202 добавил jar/tube interaction parity (`AspectFilter`, label/phial set/clear, tube filter set/clear) и сохранил resource-pack texture fix.

В следующем проходе сделай Stage203 + Stage204:

1. Stage203: golem GUI ghost-slot parity: перенести `SlotGhost`, `SlotGhostFluid`, precise amount/use/liquid toggle behavior, inventory filter stack copy semantics, без fake storage.
2. Stage204: jar/tube exact transfer edge cases: `TileJarFillable.fillJar`, void jar suction/overflow, tube filter/restrict/oneway/buffer edge cases, wand/label/phial exact interaction drift.
3. Добавь аудиты `scripts/tc4_stage203_golem_ghost_slot_audit.py` и `scripts/tc4_stage204_jar_tube_transfer_edges_audit.py`.
4. Проверь, что не сломаны текстуры: `pack.mcmeta`, `assets/thaumcraft/textures/...`, `guigolem.png`, jar/tube label paths.
5. В конце выдай новый ZIP, что сделал, что дальше, сколько stage осталось, и обнови prompt.
