package com.darkifov.thaumcraft.porting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Converts safe legacy mirror stacks into their functional canonical items.
 *
 * <p>Since v11.63.52 exact duplicate registry ids are no longer registered.
 * Forge MissingMappingsEvent remaps old save ids to canonical objects while this
 * migrator rewrites already decoded and nested stacks. Migration only touches
 * mappings where the replacement is proven to represent the same TC4 object.
 * Count, damage, custom NBT, enchantments, names and Forge capabilities survive
 * the serialized id replacement.</p>
 */
public final class TC4LegacyDuplicateItemMigrator {
    private static final Map<String, String> LEGACY_TO_CANONICAL = createMappings();
    private static final int MAX_NESTED_DEPTH = 4;

    private TC4LegacyDuplicateItemMigrator() {
    }

    private static Map<String, String> createMappings() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("tc4_amber", "amber");
        map.put("tc4_shard_balanced", "balanced_shard");
        map.put("tc4_eldritch_object", "eldritch_eye");
        map.put("tc4_phial", "essentia_phial");
        map.put("tc4_focuspouch", "focus_pouch");
        map.put("tc4_focuspouchbauble", "focus_pouch");
        map.put("tc4_gogglesrevealing", "goggles_of_revealing");
        map.put("tc4_ironbell", "golem_bell");
        map.put("tc4_golemdecoarmor", "golem_deco_armor");
        map.put("tc4_golemdecobowtie", "golem_deco_bowtie");
        map.put("tc4_golemdecodart", "golem_deco_dart_launcher");
        map.put("tc4_golemdecofez", "golem_deco_fez");
        map.put("tc4_golemdecoglasses", "golem_deco_glasses");
        map.put("tc4_golemdecomace", "golem_deco_mace");
        map.put("tc4_golemdecotophat", "golem_deco_tophat");
        map.put("tc4_golemdecovisor", "golem_deco_visor");
        map.put("tc4_golem_upgrade_air", "golem_upgrade_air");
        map.put("tc4_golem_upgrade_earth", "golem_upgrade_earth");
        map.put("tc4_golem_upgrade_entropy", "golem_upgrade_entropy");
        map.put("tc4_golem_upgrade_fire", "golem_upgrade_fire");
        map.put("tc4_golem_upgrade_order", "golem_upgrade_order");
        map.put("tc4_golem_upgrade_water", "golem_upgrade_water");
        map.put("tc4_inkwell", "scribing_tools");
        map.put("tc4_label", "jar_label");
        map.put("tc4_nuggetthaumium", "thaumium_nugget");
        map.put("tc4_quicksilver", "quicksilver_drop");
        map.put("tc4_shard_aer", "aer_shard");
        map.put("tc4_shard_aqua", "aqua_shard");
        map.put("tc4_shard_ignis", "ignis_shard");
        map.put("tc4_shard_terra", "terra_shard");
        map.put("tc4_shard_ordo", "ordo_shard");
        map.put("tc4_shard_perditio", "perditio_shard");
        map.put("tc4_soap", "sanity_soap");
        map.put("tc4_thaumiumingot", "thaumium_ingot");
        map.put("tc4_voidingot", "void_metal_ingot");
        map.put("tc4_eldritch_object_3", "primordial_pearl");
        map.put("tc4_nitor", "nitor");
        map.put("tc4_taint_slime", "tainted_slime");
        map.put("tc4_thaumonomicon", "thaumonomicon");
        map.put("tc4_thaumonomiconcheat", "thaumonomicon_cheat");
        map.put("tc4_researchnotes", "research_note");

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
        int changed = migrateContainer(player.getInventory());
        changed += migrateContainer(player.getEnderChestInventory());
        return changed;
    }

    public static int migrateContainer(Container container) {
        if (container == null) {
            return 0;
        }
        int changed = 0;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack original = container.getItem(slot);
            MigrationResult result = migrateStackDeepWithStatus(original);
            if (result.changed()) {
                container.setItem(slot, result.stack());
                changed++;
            }
        }
        if (changed > 0) {
            container.setChanged();
        }
        return changed;
    }

    public static int migrateItemHandler(IItemHandler handler) {
        if (!(handler instanceof IItemHandlerModifiable modifiable)) {
            return 0;
        }
        int changed = 0;
        for (int slot = 0; slot < modifiable.getSlots(); slot++) {
            try {
                ItemStack original = modifiable.getStackInSlot(slot);
                MigrationResult result = migrateStackDeepWithStatus(original);
                if (result.changed()) {
                    modifiable.setStackInSlot(slot, result.stack());
                    changed += Math.max(1, result.changedStacks());
                }
            } catch (RuntimeException ignored) {
                // Some automation wrappers expose IItemHandlerModifiable but
                // deliberately reject direct writes. Leave those slots intact.
            }
        }
        return changed;
    }

    public static int migrateBlockEntity(BlockEntity blockEntity) {
        if (blockEntity == null || blockEntity.getLevel() == null || blockEntity.getLevel().isClientSide) {
            return 0;
        }
        int changed = 0;
        if (blockEntity instanceof TC4LegacyStackMigrationTarget target) {
            changed += target.migrateLegacyStacks();
        }
        boolean containerHandled = false;
        if (blockEntity instanceof Container container) {
            changed += migrateContainer(container);
            containerHandled = true;
        }
        if (!containerHandled) {
            IItemHandler handler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
            changed += migrateItemHandler(handler);
        }
        if (changed > 0) {
            blockEntity.setChanged();
        }
        return changed;
    }

    public static int migrateJoinedEntity(Entity entity) {
        if (entity == null || entity.level.isClientSide) {
            return 0;
        }
        int changed = 0;
        if (entity instanceof ItemEntity itemEntity) {
            MigrationResult result = migrateStackDeepWithStatus(itemEntity.getItem());
            if (result.changed()) {
                itemEntity.setItem(result.stack());
                changed++;
            }
            return changed;
        }
        if (entity instanceof Player player) {
            changed += migratePlayerInventory(player);
        } else if (entity instanceof Container container) {
            changed += migrateContainer(container);
        }
        if (entity instanceof LivingEntity living) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack original = living.getItemBySlot(slot);
                MigrationResult result = migrateStackDeepWithStatus(original);
                if (result.changed()) {
                    living.setItemSlot(slot, result.stack());
                    changed++;
                }
            }
        }
        if (!(entity instanceof Player) && !(entity instanceof Container)) {
            IItemHandler handler = entity.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
            changed += migrateItemHandler(handler);
        }
        return changed;
    }

    /**
     * Rewrites only the current stack id. Nested portable inventories are left
     * untouched; callers that migrate world storage should use migrateStackDeep.
     */
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
        ResourceLocation canonicalId = new ResourceLocation("thaumcraft", canonicalPath);
        Item canonical = ForgeRegistries.ITEMS.getValue(canonicalId);
        if (canonical == null || canonical == original.getItem()) {
            return original;
        }

        // ItemStack#save includes ForgeCaps in addition to normal tag data.
        CompoundTag serialized = original.save(new CompoundTag());
        serialized.putString("id", canonicalId.toString());
        ItemStack migrated = ItemStack.of(serialized);
        return migrated.isEmpty() ? original : migrated;
    }

    public static ItemStack migrateStackDeep(ItemStack original) {
        return migrateStackDeepWithStatus(original).stack();
    }

    public static MigrationResult migrateStackDeepWithStatus(ItemStack original) {
        return migrateStackDeepWithStatus(original, 0);
    }

    private static MigrationResult migrateStackDeepWithStatus(ItemStack original, int depth) {
        if (original == null || original.isEmpty()) {
            return new MigrationResult(original, false, 0);
        }
        ItemStack migrated = migrateStack(original);
        boolean changed = migrated != original;
        int changedStacks = changed ? 1 : 0;

        if (depth < MAX_NESTED_DEPTH) {
            IItemHandler nested = migrated.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
            if (nested instanceof IItemHandlerModifiable modifiable) {
                for (int slot = 0; slot < modifiable.getSlots(); slot++) {
                    try {
                        ItemStack child = modifiable.getStackInSlot(slot);
                        MigrationResult childResult = migrateStackDeepWithStatus(child, depth + 1);
                        if (childResult.changed()) {
                            modifiable.setStackInSlot(slot, childResult.stack());
                            changed = true;
                            changedStacks += childResult.changedStacks();
                        }
                    } catch (RuntimeException ignored) {
                        // Do not make an old portable inventory unloadable if a
                        // third-party wrapper rejects direct slot replacement.
                    }
                }
            }
        }
        return new MigrationResult(migrated, changed, changedStacks);
    }

    public static Optional<String> canonicalPath(String legacyPath) {
        return Optional.ofNullable(LEGACY_TO_CANONICAL.get(legacyPath));
    }

    public static boolean isRemovedDuplicateId(String path) {
        return LEGACY_TO_CANONICAL.containsKey(path);
    }

    public static Map<String, String> mappings() {
        return LEGACY_TO_CANONICAL;
    }

    public record MigrationResult(ItemStack stack, boolean changed, int changedStacks) {
    }
}
