# Registry and resource audit — v11.63.29

- Item model JSON files: **663**
- Parsed successfully: **663**
- Statically detected registered item/block-item models: **452**
- Quarantined exact IDs: **122**
- Quarantined prefixes: **31**
- Exact duplicate model groups: **9**
- Visual duplicate groups: **32**
- Visible clone leaks: **0**
- Missing/invalid resource problems: **0**

## Visible clone leaks

None. Duplicate compatibility IDs are quarantined from the player-facing creative list.

## Exact duplicate model groups

- `advanced_node_stabilizer`, `node_stabilizer`
- `alchemical_centrifuge`, `alembic`, `bellows`, `hungry_chest`, `infusion_matrix`, `tallow_candle`, `tallow_candle_black`, `tallow_candle_blue`, `tallow_candle_brown`, `tallow_candle_cyan`, `tallow_candle_gray`, `tallow_candle_green`, `tallow_candle_light_blue`, `tallow_candle_light_gray`, `tallow_candle_lime`, `tallow_candle_magenta`, `tallow_candle_orange`, `tallow_candle_pink`, `tallow_candle_purple`, `tallow_candle_red`, `tallow_candle_yellow`, `tc4_jar_brain`, `vis_relay`
- `brainy_zombie_spawn_egg`, `giant_brainy_zombie_spawn_egg`, `inhabited_zombie_spawn_egg`, `thaumic_slime_spawn_egg`, `wisp_spawn_egg`
- `crimson_plate_boots`, `tc4_cultistboots`
- `crimson_plate_chest`, `tc4_cultistplatechest`
- `crimson_plate_helm`, `tc4_cultistplatehelm`
- `crimson_plate_legs`, `tc4_cultistplatelegs`
- `essentia_jar`, `filtered_essentia_jar`, `void_essentia_jar`
- `tc4_block_banner`, `vis_charge_relay`

## Resource problems

None: every item model JSON parsed and every Thaumcraft texture/model reference resolved.

## Policy

Legacy registry IDs are retained only for old-world compatibility. They must not appear as normal craftable/player-facing duplicates until a separate migration removes them safely.
