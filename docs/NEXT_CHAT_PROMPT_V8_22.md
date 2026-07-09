Продолжаю от **v8.22**. Следующий короткий batch делаю как **v8.42**.

План v8.42:
1. Infusion Matrix: добить component spec ledger для одинаковых item id с разными damage/NBT, чтобы pending list не теряла точность.
2. Thaumometer: приблизить lifecycle к TC4 hold-to-scan — startScan, стабильная цель, завершение после удержания, без instant-complete где не надо.
3. Goggles HUD: оптимизировать 10-block scan ray, чтобы не сканировать слишком много позиций каждый кадр.
4. Research/Thaumonomicon: проверить fake/stub pages, unlock drift и мусорные рецепты.
5. Essentia/alchemy: проверить jar/tube suction edge cases против TC4.

Оценка после v8.22: **73% готово**, осталось примерно **27%** до честного 100% TC4 parity.

Ограничение: без новых предметов, блоков, рецептов и выдуманной прогрессии.
