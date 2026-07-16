# Полный статический аудит текстур и UV — Thaumcraft Legacy Rebuild 11.62.91

## Область

- База порта: `Thaumcraft_Legacy_Rebuild_Forge_1.19.2_v11.62.91_WISP_FLYING_MOB_SPAWN_RULES_COMPILE_HOTFIX_SOURCE`.
- Эталон ресурсов: инвентарь TC4 4.2.3.5 и канонический банк `textures/original/thaumcraft4`.
- Проверены все оригинальные PNG/MCMETA, все активные core-текстуры порта, все JSON-модели, все Java-рендереры в `client/render`, все Screen-классы и зарегистрированные render layers.
- Runtime side-by-side не выполнялся: визуальный итог в игре остаётся `NOT TESTED`.

## Точные результаты ресурсов

- Оригинальных PNG/MCMETA в инвентаре: **921**.
- Побайтово присутствуют в поставляемых ресурсах: **921/921**.
- Имеют отдельную активную core-копию вне канонического банка: **919/921**.
- Обнаружены прямые статические ссылки хотя бы на одну точную копию: **684/921**.
- Core PNG/MCMETA порта вне `textures/original/**`: **1653**; exact TC4 copies: **1314**; adapted/new: **339**.

Наличие байтов не доказывает правильное UV, blending, размер, свет или положение.

## JSON-модели

- Всего: **988** (item + block).
- Статусы: `{'ADAPTED_STATIC_VALID': 466, 'MATCH_STATIC_RESOURCE_CHAIN': 500, 'STATIC_RISK_DYNAMIC_RENDERER': 22}`.
- Прямое JSON→JSON сравнение невозможно: TC4 1.7.10 использует metadata icons, ModelBase/TESR/IItemRenderer. Для кастомной геометрии требуется сравнение исходного renderer/model кода и runtime screenshots.

## Кастомные рендереры

- Проаудировано Java-файлов: **70**.
- Статусы: `{'STATIC_CONTRACT_GUARDED': 38, 'STATIC_REVIEW_REQUIRED': 31, 'CONFIRMED_MISMATCH': 1}`.
- Подтверждённых source-level расхождений: **2**.

### Подтверждённое расхождение

- **TC4WispRenderer (P1, renderer)** — Uses full 0..1 Wisp atlas for both shell and core; PARTICLES constant points to Minecraft and is unused; no 4x4/16x1 frame UV; size and additive blend differ from TC4 RenderWisp.
- **AlchemicalFurnaceScreen (P0, gui)** — Uses vanilla minecraft furnace GUI instead of TC4 textures/gui/gui_alchemyfurnace.png and original gauge UV/coordinates.

## RenderType

- Зарегистрированных block IDs: **202**.
- Статусы: `{'STATIC_RISK_SOLID_WITH_ALPHA': 76, 'STATIC_VALID': 126}`.
- `STATIC_RISK_SOLID_WITH_ALPHA` — только сигнал для ручной проверки: альфа может находиться в неиспользуемой зоне текстуры или модель может направляться через кастомный renderer.

## Runtime-доказательства

Не выполнены: GUI/Ground/Fixed/First-person/Third-person/World, одинаковые FOV/освещение/GUI Scale, анимации, z-sorting и full-bright. Никакой строке не присвоен runtime visual PASS.

## Файлы

- `texture_audit.csv` — полный оригинальный инвентарь и наличие/использование.
- `port_texture_inventory.csv` — все core-текстуры порта.
- `model_audit.csv` — все item/block JSON.
- `custom_renderer_audit.csv` — все кастомные Java render paths.
- `gui_audit.csv`, `render_type_audit.csv`, `item_context_audit.csv`.
- `diffs/` — доступные автоматические diff/UV proof изображения.
