# Thaumcraft Legacy Rebuild 1.19.2 — Stage 87 Compile Helpers Fix

## Что исправлено

GitHub Actions показал ошибку:

```text
ThaumcraftMod.java: error: cannot find symbol
symbol: method ttParityItem(String, Mode)
symbol: method tceParityItem(String, Mode)
symbol: method ttParityBlock(String, Mode, Properties)
symbol: method tceParityBlock(String, Mode, Properties)
```

В Stage 87 добавлены недостающие helper-методы регистрации:

- `ttParityItem(...)`
- `tceParityItem(...)`
- `ttParityBlock(...)`
- `tceParityBlock(...)`

## Как запускать

1. Замени содержимое локального репозитория на содержимое этого архива.
2. В GitHub Desktop сделай commit.
3. Нажми `Push origin`.
4. Открой `Actions → Forge 1.19.2 Build`.

Если сборка снова упадёт, пришли первый блок ошибки из шага `Build and reobfuscate jar`.
