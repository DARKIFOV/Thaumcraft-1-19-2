Продолжай с архива Stage211. Проверяй соответствие с оригинальным Thaumcraft 4 для Minecraft 1.7.10, но перенос сохраняй строго на Forge/Minecraft 1.19.2.

База: `thaumcraft_legacy_rebuild_STAGE211_TC4_RUNIC_SHIELD_RUNTIME_1192_PARITY.zip`.

Что уже сделано в Stage211:
- перенесён runtime `EventHandlerRunic` для игроков;
- добавлен `TC4RunicShieldRuntime`;
- добавлены `PacketRunicCharge` и `PacketFXShield`;
- добавлен client shield FX bridge и HUD mirror;
- добавлены TC4 runic shield config defaults: recharge 2000ms, wait 80 ticks, cost 50 Aer + Terra vis;
- перенесены charged/kinetic/healing/emergency runic variants;
- сохранён `RS.HARDEN` из Stage210;
- аудит Stage205–Stage211 проходит.

Следующая цель Stage212:
1. Перенести оставшуюся ветку `EventHandlerRunic#entityHurt`:
   - fortress armor mask 1 weakness-on-attacker behavior;
   - fortress armor mask 2 leech healing behavior;
   - champion/eldritch mob shield FX branch;
   - champion modifier offense/defense hooks если уже есть соответствующие entity adapters.
2. Добавить optional dependency-free Curios/Baubles adapter через reflection или capability-safe bridge, если это не ломает 1.19.2 build.
3. Уточнить соответствие damage-source sentinels `-1/-2/-3` для `PacketFXShield`.
4. Добавить docs/report/audit Stage212.
5. Прогнать static guards и все parity audits.
