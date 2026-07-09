# TC4 Porting Progress — v8.02

This is an estimate for strict Thaumcraft 4 → Forge 1.19.2 parity. It is not a claim that every class/file is complete; it is a functional parity tracker based on implemented runtime systems, audits, and remaining high-risk drift areas.

## Current estimate

- Completed parity: **~72%**
- Remaining to 100%: **~28%**
- Confidence: medium. Static audits are strong, but full runtime parity still needs in-game testing and GitHub/Gradle jar builds.

## Why not higher yet

The port has broad coverage across research, arcane crafting, infusion, aura/node HUD, essentia containers/tubes, golems, eldritch/Outer Lands, renderers, and assets. The remaining work is mostly not “add more registry entries”; it is edge-case parity, runtime validation, GUI exactness, persistence, and original TC4 behaviour under bad player actions.

## Remaining functional buckets

| Bucket | Approx. remaining | Notes |
|---|---:|---|
| Infusion Matrix | 5% | More pedestal/component source-lock tests, cancellation/failure semantics, special recipe edge cases, source FX exactness. |
| Arcane Workbench | 4% | Exact vanilla/arcane priority edge cases, slot interaction corner cases, hover/sound parity, virtual output hardening. |
| Aura Nodes / Thaumometer / Goggles | 4% | Scan screen vs HUD parity, raycast/distance limits, type/modifier alpha/sprite exactness, node save/load edge cases. |
| Research / Thaumonomicon | 4% | Page micro-layout, hidden/lost/research-note edge cases, text/icon drift cleanup. |
| Essentia / Alchemy / Thaumatorium | 3% | Suction priority edge cases, container interactions, automated crafting edge cases. |
| Golems / Wards / Utility systems | 3% | AI goal exactness, seals/tasks, ward persistence and interactions. |
| Eldritch / worldgen / mobs | 3% | Dungeon room exactness, boss/event edge cases, loot/portal persistence. |
| Final build/runtime QA | 2% | Full Gradle/GitHub jar build, in-game smoke pass, duplicate/stub cleanup. |

Total remaining estimate: **28%**.

## v8.02 plan used

1. Keep the batch short and parity-only.
2. Fix the most dangerous dupe/drift problems first.
3. Do not add new items, recipes, GUI, progression, or invented systems.
4. Preserve v7.82 infusion/stabilizer fixes.
5. Add a persistent progress document so the next batch can continue with a clear percent-to-100 tracker.

## Next planned v8.22 targets

1. Infusion Matrix: component source locks should carry exact ItemStack/spec identity, not only id, for recipes where modern ports still encode damage/NBT-relevant components.
2. Arcane Workbench: compare vanilla remaining-item logic against `Recipe#getRemainingItems` instead of relying only on per-stack crafting remainders.
3. Aura/Node: harden scan/HUD distance and raycast parity and audit node type/modifier rendering constants.
4. Add a small runtime smoke checklist for manual in-game verification.
