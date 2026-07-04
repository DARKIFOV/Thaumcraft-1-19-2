# Thaumcraft Legacy Rebuild 1.19.2 — Stage 93 Original Backend Behavior Bridge Pass

## Что сделано

Stage 93 продолжает движение к оригинальному поведению, а не только картинкам.

Добавлены backend bridge-классы:

- `OriginalAspectWallet`
- `OriginalResearchBridge`
- `OriginalArcaneCostBridge`

## Что они делают

### OriginalAspectWallet

Хранит primal research points у игрока:

- aer
- terra
- ignis
- aqua
- ordo
- perditio

Данные сохраняются в persistent NBT игрока.

### OriginalResearchBridge

Даёт original-like research flow:

- проверка parent requirements;
- поиск первой доступной research-ноды;
- unlock research;
- aspect-cost based completion;
- сообщение игроку при завершении исследования.

### OriginalArcaneCostBridge

Даёт bridge-логику для arcane costs:

- vis cost по типу предмета;
- primal aspect costs;
- canCraft check.

## Патчи экранов / предметов

- `ResearchNoteItem` получил серверный bridge для завершения первой доступной research-ноды.
- `ResearchTableScreen` теперь прямо показывает, что Research Note используется для завершения доступной ноды.
- `ArcaneWorkbenchScreen` помечен как bridge-backed cost flow.

## Визуальный bridge

Добавлены texture variants:

- aspect-filled jar textures;
- aura node aspect variants;
- completed research note texture.

## Честный статус

Это не финальный TC4 backend, но это уже слой настоящего поведения:

- есть сохранение аспектов игрока;
- есть проверка requirements;
- есть unlock research;
- есть aspect costs;
- есть arcane cost bridge.

Следующий этап для оригинала:

1. packet из ThaumonomiconScreen на server для unlock конкретной выбранной ноды;
2. настоящая research table minigame;
3. привязка aspect wallet к реальному GUI;
4. dynamic renderer банок;
5. aura/node renderer pass.

## GitHub commit

`Stage 93 original backend behavior bridge pass`
