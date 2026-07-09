Продолжаю от **v8.62**. Следующий короткий batch делаю как **v8.82**.

План v8.82:
1. Essentia/alchemy: jar/tube suction pull priority, filtered jar behavior и thaumatorium corner cases против TC4.
2. Infusion: углубить weighted failure ejection/goo/gas side effects и проверить exact source pedestal behavior после instability events.
3. Research/Thaumonomicon: сверить original page payload drift против ConfigResearch, не добавляя fake pages/progression.
4. Golems/seals: выбрать узкий runtime gap и закрыть без новых выдуманных механик.
5. FX/render: проверить matrix/node/revealer визуальные parity gaps без новых ассетов, если можно использовать существующие.
6. Worldgen: перевести surface pass с compatibility chunk-load hook на полноценные 1.19.2 configured/placed features, но не возвращать Greatwood/Silverwood в player tick.

Оценка после v8.62: **75% готово**, осталось примерно **25%** до честного 100% TC4 parity. Важно: Greatwood/Silverwood больше не должны спавниться из player-tick fallback “на глазах” при движении игрока; деревья теперь только через new-chunk/worldgen path.

Ограничение: без новых предметов, блоков, рецептов и выдуманной прогрессии.
