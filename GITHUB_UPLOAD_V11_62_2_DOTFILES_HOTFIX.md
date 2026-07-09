# v11.62.2 DOTFILES / GITHUB ACTIONS HOTFIX

Причина последнего падения GitHub Actions:

```text
missing GitHub build file: .gitattributes
```

В прошлом архиве dotfiles не попали в zip, потому что архив был собран через маску обычных файлов. Из-за этого GitHub не получил:

- `.gitattributes`
- `.gitignore`
- `.github/workflows/main.yml`

Этот архив пересобран от корня проекта через `zip -r ... .`, поэтому скрытые файлы и папка `.github` включены.

Проверено локально:

- `python scripts/github_ci_guard.py` — OK
- `python scripts/java_syntax_guard.py` — OK
- `python scripts/github_static_audit.py` — OK
- `python scripts/tc4_stage146_worldgen_resources_audit.py` — OK
- `python scripts/tc4_v11_62_2_integrated_server_world_load_hotfix_audit.py` — OK

Версия кода: `v11.62.2`.
Новых предметов, блоков, рецептов и прогрессии не добавлялось.
