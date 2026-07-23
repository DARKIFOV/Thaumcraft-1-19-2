# Registry and resource audit — v11.63.26

- Item model JSON files: **726**
- Parsed successfully: **726**
- Statically detected registered item/block-item models: **431**
- Quarantined exact IDs: **122**
- Quarantined prefixes: **31**
- Exact duplicate model groups: **26**
- Visual duplicate groups: **67**
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
- `essentia_reservoir`, `tc4_block_essentia_reservoir`
- `golem_deco_armor`, `tc4_golemdecoarmor`
- `golem_deco_bowtie`, `tc4_golemdecobowtie`
- `golem_deco_dart_launcher`, `tc4_golemdecodart`
- `golem_deco_fez`, `tc4_golemdecofez`
- `golem_deco_glasses`, `tc4_golemdecoglasses`
- `golem_deco_mace`, `tc4_golemdecomace`
- `golem_deco_tophat`, `tc4_golemdecotophat`
- `golem_deco_visor`, `tc4_golemdecovisor`
- `golem_upgrade_air`, `tc4_golem_upgrade_air`
- `golem_upgrade_earth`, `tc4_golem_upgrade_earth`
- `golem_upgrade_entropy`, `tc4_golem_upgrade_entropy`
- `golem_upgrade_fire`, `tc4_golem_upgrade_fire`
- `golem_upgrade_order`, `tc4_golem_upgrade_order`
- `golem_upgrade_water`, `tc4_golem_upgrade_water`
- `tc4_block_banner`, `vis_charge_relay`
- `tc4_shard`, `tc4_shard_aer`, `tc4_shard_aqua`, `tc4_shard_ignis`, `tc4_shard_ordo`, `tc4_shard_perditio`, `tc4_shard_terra`
- `tc4_thaumonomiconcheat`, `thaumonomicon_cheat`

## Resource problems

None: every item model JSON parsed and every Thaumcraft texture/model reference resolved.

## Policy

Legacy registry IDs are retained only for old-world compatibility. They must not appear as normal craftable/player-facing duplicates until a separate migration removes them safely.
