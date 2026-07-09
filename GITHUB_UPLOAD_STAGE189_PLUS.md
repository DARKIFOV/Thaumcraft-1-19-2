# v11.42.1 GitHub hotfix: Stage189 + chained CI audit repair

Use this archive as the repository root replacement/update for the v11.42.1 line.

Main GitHub Actions failure fixed:
- `Stage189 arcane workbench GUI audit failed: insufficient_vis_adapter`
- `Stage189 arcane workbench GUI audit failed: screen_has_original_aspect_locs`
- `Stage189 arcane workbench GUI audit failed: screen_has_original_dimensions`

Also repaired follow-up audit conflicts that would fail later in the same workflow:
- Stage191 Arcane Workbench slot edge-case audit marker
- Stage205/206/207/208/210 audit token drift
- Stage323 vs v8.82 worldgen lifecycle conflict
- Stage503 tube reservoir/destination markers
- Stage683/703/723 Arcane Workbench/Node HUD audit conflicts
- v8.62/v8.82/v10.02 worldgen and ore-rarity audit conflicts

Important runtime note:
- Greatwood/Silverwood surface generation remains on the new-chunk path, not player walking/tick fallback.
- Outer Lands populate remains on the chunk generation path.

Checked locally with targeted workflow guards/audits listed in the assistant response.
