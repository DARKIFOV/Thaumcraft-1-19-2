# Stage 108 Test Upload Checklist

Static checks before upload:

- Run `python scripts/github_static_audit.py`.
- Run `python scripts/java_syntax_guard.py`.
- Confirm `src/main/resources/assets/thaumcraft/blockstates/essentia_tube.json` uses multipart state.
- Confirm `EssentiaSuctionResolver` reports competing destinations and blocked backflow.

Manual smoke checks:

- Place a tube between an alembic and a normal jar and confirm essentia moves toward the jar.
- Place two valid destinations and confirm the winning suction diagnostic is stable.
- Use a filtered jar with a mismatched aspect and confirm movement is blocked.
- Break and replace neighboring tubes and confirm side connection diagnostics update.

Upload note: include this checklist with the Stage108 package so CI can verify the report file expected by the static audit.
