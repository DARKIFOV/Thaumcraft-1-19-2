Continue from `thaumcraft_legacy_rebuild_STAGE210_TC4_RUNIC_AUGMENTATION_1192_PARITY.zip`.

Target remains Minecraft/Forge 1.19.2. Do not port by reintroducing 1.7.10 runtime APIs; use 1.19.2-safe adapters.

Stage210 completed:
- TC4 `InfusionRunicAugmentRecipe` runtime recipe;
- `RS.HARDEN` NBT parity;
- catalyst-dependent components/aspects/instability;
- base runic charge map for de-metadata'd TC4 runic items;
- matrix finish path for runic augmentation;
- Stage210 1.19.2 compatibility audit.

Next Stage211 priorities:
1. Port `EventHandlerRunic` runtime shielding: absorb incoming damage from a runic shield pool before health damage.
2. Add recharge timing and shield pool NBT/state parity using 1.19.2 events.
3. Port charged/emergency/regeneration ring/girdle special behaviours where feasible without Baubles hard dependency.
4. Add network/client feedback for `runicShieldCharge` and `runicShieldEffect` sounds/particles.
5. Audit runic item variants whose metadata was flattened into individual 1.19.2 ids and preserve variant output semantics.

Run before packaging:
- `python3 scripts/java_syntax_guard.py`
- `python3 scripts/github_static_audit.py`
- `python3 scripts/github_ci_guard.py`
- all Stage205-Stage210 audits
- attempt `./gradlew --no-daemon build`; if sandbox has no internet, report the Gradle wrapper download failure honestly.
