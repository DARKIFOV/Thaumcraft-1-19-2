Продолжи с архива Stage212. Это Minecraft/Forge 1.19.2 порт TC4 1.7.10.

База: `thaumcraft_legacy_rebuild_STAGE212_TC4_FORTRESS_MASK_CURIOS_1192_PARITY.zip`.

Что уже сделано в Stage212:
- `TC4FortressArmorItem` / `TC4FortressArmorMaterial` для wearable fortress armor;
- generic `Tag` output в `InfusionRecipe` для TC4 `new Object[] { tagName, NBTTagByte/Int }`;
- infusion JSON для `HELMGOGGLES`, `MASKGRINNINGDEVIL`, `MASKANGRYGHOST`, `MASKSIPPINGFIEND`;
- runtime mask effects из `EventHandlerRunic` и `WarpEvents`;
- optional reflective Curios/Baubles adapter для runic baubles;
- Stage205–Stage212 audits проходят; Gradle build не проходит только из-за отсутствия сети для загрузки wrapper.

Следующий Stage213:
1. Перенести full fortress armor set-bonus parity из `ItemFortressArmor`: magic/fire reduction, special armor hooks, goggles/revealer bridge, model/texture mask overlay.
2. Добавить renderer/armor layer для mask textures из `ModelFortressArmor`.
3. Сверить и закрыть `IWarpingGear` warp-from-gear adapter: armor/baubles суммирование в 1.19.2.
4. Расширить champion modifier runtime beyond shield FX: type 1/2 attack/defense effect hooks.
5. Добавить новый audit script Stage213 и сохранить 1.19.2 API guard.
