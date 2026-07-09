# Porting progress — v8.42

Estimated TC4 parity completion: **74%**.

Estimated remaining: **26%**.

## What changed from v8.22

v8.42 is a compact runtime parity batch. It moves the estimate from **73%** to **74%** because it closes several edge cases, but it does not add new TC4 systems or content.

Completed in this batch:

1. Infusion Matrix now keeps a parallel `pendingComponentSpecs` ledger beside the legacy `pendingComponents` ids.
2. Component matching and source pedestal selection now use `InfusionRecipe.ComponentSpec`, preserving damage/NBT-sensitive components.
3. The matrix saves `PendingComponentSpecList` and still keeps string `PendingComponentSpecs`/`PendingComponents` for migration/debug compatibility.
4. Thaumometer scans now complete through the 25 tick use lifecycle instead of instantly finishing on right-click.
5. Block target scans are stored as a pending target and resolved at `finishUsingItem`, preserving range/aspectless guards.
6. Entity scans also resolve at scan completion, not at use start.
7. Revealer HUD node targeting caches the resolved node for the current client tick/look vector, reducing per-frame node search churn while keeping the 10 block ray rule.

## Remaining high-risk areas

- Thaumometer still needs a deeper stable-target/cancel/release pass.
- Research and Thaumonomicon need another fake/stub/unlock drift cleanup.
- Essentia jars/tubes and thaumatorium suction need more TC4 corner-case work.
- Infusion still needs a pass over container item remainders and failure-time component behavior.
- Golem behavior/seals remain a large runtime parity area.
