Продолжи с архива Stage209. Обязательно сверяй с оригинальным `Thaumcraft4-1.7.10-master.zip`, а не с современными форками. Следующий Stage210 должен закрыть оставшуюся parity-ветку infusion/runic:

1. Перенеси `InfusionRunicAugmentRecipe` / runic augment behavior, включая `RS.HARDEN`, runic charge/augment NBT и корректный output на центральном pedestal.
2. Расширь `TC4InfusionItemMatcher` до modern tag/OreDictionary-equivalent matching, если materialization может восстановить ore keys из оригинального рецепта.
3. Проверь все `tc4_*.json` infusion recipes на необходимость `damage`, `meta`, `damage_wildcard` и `nbt` object-form components.
4. Доведи `PacketFXInfusionSource` visuals ближе к original `sourceFX` TTL/pedestal/entity behavior.
5. После переноса добавь Stage210 report, docs, audit script и собери новый ZIP.

Не добавляй новых механик. Только parity-перенос TC4 1.7.10.
