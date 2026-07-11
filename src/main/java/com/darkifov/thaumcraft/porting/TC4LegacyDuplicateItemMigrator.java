package com.darkifov.thaumcraft.porting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts safe legacy mirror stacks into their functional canonical items.
 *
 * <p>The registry ids remain registered so old worlds can load. Migration only
 * touches mappings where the modern replacement is known to represent the same
 * TC4 object. NBT, enchantments, custom names and counts are preserved.</p>
 */
public final class TC4LegacyDuplicateItemMigrator {
    private static final Map<String, String> LEGACY_TO_CANONICAL = createMappings();

    private TC4LegacyDuplicateItemMigrator() {
    }

    private static Map<String, String> createMappings() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("tc4_nitor", "nitor");
        map.put("tc4_amber", "amber");
        map.put("tc4_quicksilver", "quicksilver_drop");
        map.put("tc4_taint_slime", "tainted_slime");
        map.put("tc4_thaumonomicon", "thaumonomicon");
        map.put("tc4_thaumonomiconcheat", "thaumonomicon_cheat");
        map.put("tc4_researchnotes", "research_note");
        map.put("tc4_gogglesrevealing", "goggles_of_revealing");

        map.put("tc4_shard_aer", "aer_shard");
        map.put("tc4_shard_terra", "terra_shard");
        map.put("tc4_shard_ignis", "ignis_shard");
        map.put("tc4_shard_aqua", "aqua_shard");
        map.put("tc4_shard_ordo", "ordo_shard");
        map.put("tc4_shard_perditio", "perditio_shard");
        map.put("tc4_shard_balanced", "balanced_shard");

        map.put("tc4_wand_cap_iron", "iron_wand_cap");
        map.put("tc4_wand_cap_gold", "gold_wand_cap");
        map.put("tc4_wand_cap_thaumium", "thaumium_wand_cap");
        map.put("tc4_wand_rod_greatwood", "greatwood_wand_core");
        map.put("tc4_wand_rod_silverwood", "silverwood_wand_core");

        map.put("tc4_block_greatwood_log", "greatwood_log");
        map.put("tc4_block_greatwood_leaves", "greatwood_leaves");
        map.put("tc4_block_greatwood_planks", "greatwood_planks");
        map.put("tc4_block_greatwood_sapling", "greatwood_sapling");
        map.put("tc4_block_silverwood_log", "silverwood_log");
        map.put("tc4_block_silverwood_leaves", "silverwood_leaves");
        map.put("tc4_block_silverwood_planks", "silverwood_planks");
        map.put("tc4_block_silverwood_sapling", "silverwood_sapling");
        map.put("tc4_block_infusion_matrix", "infusion_matrix");
        map.put("tc4_block_node_stabilizer", "node_stabilizer");
        map.put("tc4_block_node_stabilizer_advanced", "advanced_node_stabilizer");
        map.put("tc4_block_node_transducer", "node_transducer");
        map.put("tc4_block_arcane_pedestal", "arcane_pedestal");
        map.put("tc4_block_alembic", "alembic");
        map.put("tc4_block_essentia_jar", "essentia_jar");
        map.put("tc4_block_essentia_reservoir", "essentia_reservoir");
        map.put("tc4_block_essentia_tube", "essentia_tube");
        return Map.copyOf(map);
    }

    public static int migratePlayerInventory(Player player) {
        if (player == null || player.level.isClientSide) {
            return 0;
        }
        int changed = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack original = player.getInventory().getItem(slot);
            ItemStack migrated = migrateStack(original);
            if (migrated != original) {
                player.getInventory().setItem(slot, migrated);
                changed++;
            }
        }
        if (changed > 0) {
            player.getInventory().setChanged();
        }
        return changed;
    }

    public static void migrateJoinedEntity(Entity entity) {
        if (!(entity instanceof ItemEntity itemEntity) || entity.level.isClientSide) {
            return;
        }
        ItemStack original = itemEntity.getItem();
        ItemStack migrated = migrateStack(original);
        if (migrated != original) {
            itemEntity.setItem(migrated);
        }
    }

    public static ItemStack migrateStack(ItemStack original) {
        if (original == null || original.isEmpty()) {
            return original;
        }
        ResourceLocation legacyId = ForgeRegistries.ITEMS.getKey(original.getItem());
        if (legacyId == null || !"thaumcraft".equals(legacyId.getNamespace())) {
            return original;
        }
        String canonicalPath = LEGACY_TO_CANONICAL.get(legacyId.getPath());
        if (canonicalPath == null) {
            return original;
        }
        Item canonical = ForgeRegistries.ITEMS.getValue(new ResourceLocation("thaumcraft", canonicalPath));
        if (canonical == null || canonical == original.getItem()) {
            return original;
        }
        ItemStack migrated = new ItemStack(canonical, original.getCount());
        if (original.hasTag()) {
            migrated.setTag(original.getTag().copy());
        }
        return migrated;
    }

    public static Map<String, String> mappings() {
        return LEGACY_TO_CANONICAL;
    }
}
