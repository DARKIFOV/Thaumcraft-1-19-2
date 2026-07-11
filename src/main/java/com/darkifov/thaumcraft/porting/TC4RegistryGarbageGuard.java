package com.darkifov.thaumcraft.porting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;

/**
 * Stage253-262 original-TC4 cleanup gate.
 *
 * <p>The port accumulated convenience, debug, addon and flattened placeholder
 * registry entries while individual systems were being reconstructed.  TC4 core
 * parity should not expose these entries to players.  We keep the ids registered
 * for save/data compatibility, but quarantine them from the Thaumcraft creative
 * tab, lootbag rolls and generated hand-off reports until the relevant addon is
 * intentionally ported in its own track.</p>
 */
public final class TC4RegistryGarbageGuard {
    public static final Set<String> EXACT = Set.of(
            "tc4_focus", "tc4_focus_fire", "tc4_focus_frost", "tc4_focus_shock", "tc4_focus_excavation",
            "tc4_focus_trade", "tc4_focus_portablehole", "tc4_focus_warding", "tc4_focus_primal",
            "tc4_focuspouch", "tc4_focuspouchbauble", "tc4_gogglesrevealing", "tc4_block_mnemonic_matrix",
            "aura_node", // Original TC4 nodes have no ordinary player-facing block item; this one used a debug icon.
            "addon_completion_ledger", "porting_ledger", "avaritia_creative_wand", "pech_ledger",
            "pech_trade_tier_1", "pech_trade_tier_2", "pech_trade_tier_3", "pech_trade_tier_4", "pech_trade_tier_5",
            "vitreus_shard", "metallum_shard", "praecantatio_shard", "vacuos_shard", "herba_shard", "lux_shard", "potentia_shard",
            "spellbinding_cloth", "osmotic_enchantment_focus", "tt_research_stamp", "transvector_binder",
            "tome_of_knowledge_sharing", "infused_scribing_tools", "bottomless_pouch", "helmet_of_revealing",
            "ichor", "ichorcloth", "ichorium_ingot", "kami_research_core", "ichor_pickaxe", "ichor_sword",
            "ichorcloth_hood", "ichorcloth_robe", "ichorcloth_leggings", "ichorcloth_boots",
            "aspect_aer", "aspect_terra", "aspect_ignis", "aspect_aqua", "aspect_ordo", "aspect_perditio", "aspect_vitreus", "aspect_metallum", "aspect_praecantatio",
            "essentia_digitizer_core", "essentia_storage_component_1k", "essentia_storage_component_4k", "essentia_storage_component_16k", "essentia_storage_component_64k",
            "encoded_essentia_pattern", "wireless_essentia_terminal", "essentia_partition_card", "essentia_capacity_upgrade", "essentia_view_card",
            "essentia_speed_card", "advanced_essentia_speed_card", "essentia_acceleration_card", "thaumic_coprocessor_card", "essentia_fuzzy_card",
            "digital_essentia_cell_1k", "digital_essentia_cell_4k", "digital_essentia_cell_16k", "digital_essentia_cell_64k", "creative_essentia_cell",
            "essentia_cell_casing", "focus_ae_wrench", "knowledge_core", "coalescence_core", "diffusion_core", "iron_gear", "crafting_aspect",
            "thaumic_grid_tool", "thaumic_crafting_cpu_core", "thaumic_channel_core",
            "focus_blink", "focus_arrow", "focus_heal", "focus_speed", "focus_pech_summon", "focus_experience", "focus_return", "focus_exchange", "focus_smelting", "focus_dispel", "focus_destroy", "focus_freeze",
            "tc4_nitor", "tc4_alumentum", "tc4_amber", "tc4_quicksilver", "tc4_tallow", "tc4_void_seed",
            "tc4_taint_slime", "tc4_brain", "tc4_mirrorglass", "tc4_fabric", "tc4_nugget_thaumium", "tc4_nugget_void",
            "tc4_thaumonomicon", "tc4_thaumonomiconcheat", "tc4_researchnotes", "tc4_golem_upgrade_air", "tc4_golem_upgrade_earth", "tc4_golem_upgrade_fire",
            "tc4_golem_upgrade_water", "tc4_golem_upgrade_order", "tc4_golem_upgrade_entropy",
            "golem_task_marker", "golem_filter"
    );

    public static final Set<String> PREFIXES = Set.of(
            "tt_", "tce_", "extras_", "thaumic_me_", "arcane_pattern_", "arcane_crafting_terminal", "essentia_import_bus", "essentia_export_bus",
            "essentia_interface", "essentia_terminal", "essentia_provider", "essentia_storage_bus", "essentia_level_emitter", "essentia_vibration_chamber",
            "essentia_conversion_monitor", "essentia_storage_monitor", "essentia_cell_workbench", "infusion_provider",
            "knowledge_inscriber", "distillation_pattern_encoder", "golem_gear_box", "gear_box", "osmotic_enchanter",
            "tc4_block_", "tc4_wand_cap_", "tc4_wand_rod_", "tc4_staff_rod_", "tc4_shard_", "tc4_cluster",
            "tc4_cultist", "tc4_golemdeco"
    );

    private TC4RegistryGarbageGuard() {
    }

    public static boolean isQuarantinedPath(String path) {
        String id = path;
        int slash = id.lastIndexOf('/');
        if (slash >= 0) {
            id = id.substring(slash + 1);
        }
        if (id.endsWith(".json") || id.endsWith(".png")) {
            id = id.substring(0, id.lastIndexOf('.'));
        }
        return isQuarantinedId(id);
    }

    public static boolean isQuarantinedId(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        if (EXACT.contains(id)) {
            return true;
        }
        // Every tc4_* entry is a migration alias or a reconstruction-era
        // placeholder. Keep it registered for old saves, but never expose it as
        // a second player-facing item even when a canonical id is temporarily absent.
        if (id.startsWith("tc4_") && id.length() > 4) {
            return true;
        }
        for (String prefix : PREFIXES) {
            if (id.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isHiddenFromCreative(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null && "thaumcraft".equals(key.getNamespace()) && isQuarantinedId(key.getPath());
    }

    /** Removes quarantined aliases and exact duplicate stack variants from the tab. */
    public static void filterCreativeItems(NonNullList<ItemStack> items) {
        Set<String> seen = new HashSet<>();
        items.removeIf(stack -> {
            if (isHiddenFromCreative(stack)) {
                return true;
            }
            ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (key == null) {
                return false;
            }
            String tag = stack.hasTag() ? stack.getTag().toString() : "";
            String signature = key + "|" + stack.getDamageValue() + "|" + tag;
            return !seen.add(signature);
        });
    }
}
