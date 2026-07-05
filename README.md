# Thaumcraft Legacy Rebuild 1.19.2 — Stage 107 Directional Essentia Tubes Pass

## Что сделано

Stage 107 переводит essentia tubes ближе к оригинальному поведению.

Теперь труба — не просто один generic network block.  
У неё есть стороны подключения:

- north
- south
- west
- east
- up
- down

## Новые файлы

- `EssentiaTubeConnections.java`
- `EssentiaSuctionPath.java`
- `models/block/essentia_tube_center.json`
- `models/block/essentia_tube_arm_north.json`
- `blockstates/essentia_tube.json`
- `STAGE107_DIRECTIONAL_ESSENTIA_TUBES_REPORT.json`

## EssentiaTubeBlock

Добавлено:

- BooleanProperty для всех 6 сторон;
- updateShape;
- getStateForPlacement;
- dynamic shape center + arms;
- диагностика connected sides.

## Почему это важно

Оригинальные трубы Thaumcraft не должны выглядеть как один одинаковый блок.  
Они должны подключаться только к реальным соседям:

- другая труба;
- банка essentia;
- alembic;
- alchemical furnace.

Это фундамент для следующего этапа:

- backflow;
- suction conflict;
- directional suction;
- restrictive tubes;
- valves;
- filtered tubes.

## Честный статус

Это directional foundation.  
Нужна GitHub Actions compile-проверка после загрузки.

## GitHub commit

`Stage 107 directional essentia tubes pass`
