# Runtime protocol artifacts — 11.63.45

Эта папка предназначена только для реальных логов клиента/сервера, скриншотов,
видео и результатов миграционных тестов. Статический source guard не является
runtime-доказательством.

## Порядок заполнения

1. Скопировать `runtime_test_manifest.template.json` в `runtime_test_manifest.json`.
2. Выполнить тесты с одинаковыми FOV, GUI Scale, освещением и ракурсом для TC4
   1.7.10 и Forge-порта 1.19.2.
3. Для каждого результата `PASS`, `PARTIAL` или `FAIL` добавить объект артефакта:

```json
{
  "path": "runtime_artifacts/screenshots/bone_bow_pull_port.png",
  "kind": "screenshot",
  "sha256": "<64 hex characters>"
}
```

4. Проверить манифест:

```bash
python3 tools/validate_runtime_manifest.py \
  --manifest runtime_artifacts/runtime_test_manifest.json \
  --version 11.63.45
```

5. Повторно создать отчёт. SHA-256 JAR добавляется только после успешного build:

```bash
python3 tools/generate_port_status_v3.py --version 11.63.45 --jar build/libs/<main.jar>
```

`PASS` без существующего файла и совпадающего SHA-256 запрещён. Текущий статус:
**runtime-протокол не выполнялся**.
