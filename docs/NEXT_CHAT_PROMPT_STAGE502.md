Продолжай с архива Stage483-502 и делай следующий strict TC4 parity batch Stage503-522.

Правила:
- Никаких новых механик, предметов, рецептов, GUI, прогрессии, текстур или поведения, пока не перенесён оригинальный Thaumcraft 4.
- Если код 1.7.10 нельзя перенести напрямую, делай Forge 1.19.2 adapter, но сохраняй оригинальные данные, NBT keys, research keys, aspects, recipes, GUI-логику, текстуры, звуки и поведение.
- Не добавляй дубли предметов/recipes. Если есть активный core item/block, переноси resolver/materialized recipes на него, а tc4_* mirror оставляй только как карантин/legacy placeholder до настоящего переноса.

Текущее состояние Stage483-502:
- Furnace -> Alembic -> Tube/Jar flow восстановлен: трубы больше не тянут напрямую из Alchemical Furnace.
- TC4DistillationRuntime добавлен: furnace distills essentia into stacked alembics above it.
- Jar label facing сохраняется через original facing NBT и рендерится на выбранной стороне.
- Alembic хранит Aspect/Amount/facing NBT compatibility.
- Активные tube assets используют original pipe_1/pipe_2/pipe_filter/pipe_filter_core/pipe_oneway/pipe_restrict/pipe_buffer/pipe_valve.
- Удалены duplicate recipe files essentia_jar_original_style/alembic_original_style.

Следующий batch Stage503-522:
1. Довести Alembic renderer/model parity: полный OBJ/model adapter или численный порт оригинальной модели, без cube/placeholder.
2. Довести jar renderer: original jar.png/jar_void model texture alignment, label overlay, fill height and aspect color parity.
3. Довести tube suction parity: TileTubeFilter/Restrict/Oneway/Buffer/Valve side behavior, choked sides, suction conflict/venting, no direct furnace source.
4. Проверить и материализовать только оригинальные TC4 recipes/research gates для essentia jars/tubes/alembic/furnace.
5. Начать Thaumatorium / Essentia Reservoir / Advanced Alchemical Furnace parity только если core jar/alembic/tube checks не сломаны.
6. Добавить новый audit Stage503-522 и не ломать все предыдущие audits.
