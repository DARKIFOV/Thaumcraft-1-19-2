# Stage194 GitHub compile hotfix 2

This hotfix fixes the second GitHub Actions compile failure reported in `logs_78043955500(1).zip`.

## Fixed compile errors

1. `ArcaneWorkbenchBlockEntity.TC4DummyCraftingMenu` now implements `quickMoveStack(Player, int)` and returns `ItemStack.EMPTY` because the dummy menu is only an unopened Forge 1.19.2 adapter for vanilla recipe lookup.
2. `TC4FocusProjectileEntity` now uses `ProjectileUtil.getHitResult(this, this::canHitEntity)` because the GitHub Forge 1.19.2 official mappings did not expose `getHitResultOnMoveVector` for this target.
3. `TC4EmberEntity` now uses `this.onGround` instead of `onGround()` because the target mappings expose it as a field.
4. `TC4SaplingBlock.isValidBonemealTarget` now uses `BlockGetter` as required by the Forge 1.19.2 `BonemealableBlock` interface.

## Audit hardening

- Stage174 projectile audit now accepts/requires the 1.19.2 `ProjectileUtil.getHitResult` adapter.
- Stage180 ember audit now accepts/requires `this.onGround`.
- `github_static_audit.py` now forbids the exact API patterns that caused this GitHub compile failure.

No gameplay/parity mechanics were changed; this is a compile compatibility hotfix.
