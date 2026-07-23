# Aura node and stabilizer parity audit v11.62.85

Сопоставление переноса с TC4 `TileNodeRenderer`, `ItemNodeRenderer`, `ItemJarNodeRenderer` и `TileNodeStabilizerRenderer`.

## Итоги

- Проверок: **16**
- Пройдено: **16**
- Ошибок: **0**

## Что перенесено

- Узел мира: 32-кадровый `nodes.png`, отдельный слой каждого аспекта и слой типа, TC4 blend-режимы, дистанционная альфа, модификаторы Bright/Pale/Fading, Thaumometer-конус и Goggles reveal.
- Откачка жезлом: цвет аспекта, плавная wispy-линия и затухание первых десяти тиков использования.
- Узел в банке: три взаимно перпендикулярные плоскости, единый nanosecond-кадр, оригинальный размер слоёв, белый type-strip и индивидуальный additive/alpha blend.
- Стабилизатор: оригинальная OBJ-геометрия/UV, четыре поршня, 0..37 выдвижение, динамическая lightmap-яркость overlay и additive `node_bubble.png`.

## Контрольные точки

- **OK** `world_node_original_sheet` — `src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java`
- **OK** `world_node_32_frames` — `src/main/java/com/darkifov/thaumcraft/client/TC4AuraNodeHudParity.java`
- **OK** `world_node_nanotime` — `src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java`
- **OK** `world_node_camera_billboard` — `src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java`
- **OK** `world_node_view_cone` — `src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java`
- **OK** `world_node_hidden_fallback` — `src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java`
- **OK** `world_node_additive_types` — `src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java`
- **OK** `drain_wispy_texture` — `src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java`
- **OK** `jar_node_nanotime` — `src/main/java/com/darkifov/thaumcraft/client/render/NodeJarItemRenderer.java`
- **OK** `jar_node_same_frame` — `src/main/java/com/darkifov/thaumcraft/client/render/NodeJarItemRenderer.java`
- **OK** `jar_node_white_type_layer` — `src/main/java/com/darkifov/thaumcraft/client/render/NodeJarItemRenderer.java`
- **OK** `jar_node_additive_contract` — `src/main/java/com/darkifov/thaumcraft/client/render/NodeJarItemRenderer.java`
- **OK** `stabilizer_original_mesh` — `src/main/java/com/darkifov/thaumcraft/client/render/NodeStabilizerRenderer.java`
- **OK** `stabilizer_lightmap_not_alpha` — `src/main/java/com/darkifov/thaumcraft/client/render/NodeStabilizerRenderer.java`
- **OK** `stabilizer_overlay_cutout` — `src/main/java/com/darkifov/thaumcraft/client/render/NodeStabilizerRenderer.java`
- **OK** `stabilizer_additive_field` — `src/main/java/com/darkifov/thaumcraft/client/render/NodeStabilizerRenderer.java`

## Ограничение

Статический аудит подтверждает формулы, ресурсы и render-state контракты. Финальная визуальная приёмка всё равно требует запуска клиента Forge: узел без reveal, узел через Таумометр, узел через очки, откачка жезлом, обычный и продвинутый стабилизатор, банка в GUI/руке/на земле.
