# v11.62.39 — Texture / HUD / Armor visual fixes

Исправлены три проблемных зоны по новым скриншотам:

1. **Таумометр**
   - увеличен и сдвинут в третьем лице, чтобы больше не висеть крошечным предметом возле ноги/края модели;
   - скорректирован GUI-поворот для иконки в хотбаре и инвентаре;
   - уменьшены размеры пузыря/узла/аспектов на стекле, чтобы иконки больше не вылезали за круг и не дублировались визуально;
   - уплотнена сетка аспектов на экране прибора.

2. **Очки откровения / шлем откровения**
   - отключён ванильный золотой шлем как базовый armor-texture;
   - предмет теперь оставляет только кастомный TC4-слой `TC4GogglesLayer`;
   - слой смещён и уменьшен, чтобы не превращаться в жёлтый куб на голове.

3. **Таумономикон / рецептурные страницы**
   - аспекты затрат слегка подняты и уплотнены;
   - числа смещены ближе к иконкам, чтобы правая страница не выглядела «разваленной».

## Изменённые файлы
- `src/main/java/com/darkifov/thaumcraft/client/render/ThaumometerItemRenderer.java`
- `src/main/java/com/darkifov/thaumcraft/client/render/TC4GogglesLayer.java`
- `src/main/java/com/darkifov/thaumcraft/block/GogglesOfRevealingItem.java`
- `src/main/java/com/darkifov/thaumcraft/block/HelmetOfRevealingItem.java`
- `src/main/java/com/darkifov/thaumcraft/client/screen/TC4ResearchPageScreen.java`
