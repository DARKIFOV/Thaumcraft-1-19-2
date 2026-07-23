package com.darkifov.thaumcraft.aura;

import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.darkifov.thaumcraft.entity.ThaumGolemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

/** Exact, stable identities used by TC4's handheld "@" scan ledger. */
public final class TC4ThaumometerScanKeys {
    public static final String CONTRACT_VERSION = "11.64.25";
    public static final String NODE_PREFIX = "NODE";

    private TC4ThaumometerScanKeys() {}

    public static String itemKey(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "unknown:-1";
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        int meta = stack.isDamageableItem() ? -1 : Math.max(0, stack.getDamageValue());
        return (id == null ? "unknown" : id.toString()) + ":" + meta;
    }

    public static String itemRegistryKey(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "unknown";
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id == null ? "unknown" : id.toString();
    }

    public static ItemStack pickedBlockStack(Player player, BlockPos pos, BlockState state) {
        if (player == null || pos == null || state == null || player.level == null) return ItemStack.EMPTY;
        try {
            ItemStack picked = state.getBlock().getCloneItemStack(player.level, pos, state);
            if (!picked.isEmpty()) return picked;
        } catch (RuntimeException ignored) {
        }
        return new ItemStack(state.getBlock().asItem());
    }

    public static String blockKey(Player player, BlockPos pos, BlockState state) {
        ItemStack picked = pickedBlockStack(player, pos, state);
        if (!picked.isEmpty()) return itemKey(picked);
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        return (id == null ? "unknown" : id.toString()) + ":-1";
    }

    public static String blockRegistryKey(BlockState state) {
        if (state == null) return "unknown";
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        return id == null ? "unknown" : id.toString();
    }

    public static String entityKey(Entity entity) {
        if (entity == null) return "unknown";
        String hash;
        if (entity instanceof Player player) {
            hash = "player_" + player.getGameProfile().getName();
        } else {
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
            hash = id == null ? "unknown" : id.toString();
        }
        if (entity instanceof AgeableMob ageable && ageable.isBaby()) hash += "CHILD";
        else if (entity instanceof Zombie zombie && zombie.isBaby()) hash += "CHILD";
        if (entity instanceof ZombieVillager) hash += "VILLAGER";
        if (entity instanceof Creeper creeper) {
            if (creeper.getSwellDir() == 1 || creeper.isIgnited()) hash += "FLASHING";
            if (creeper.isPowered()) hash += "POWERED";
        }
        if (entity instanceof ThaumGolemEntity golem) hash += golem.getGolemMaterial().id().toUpperCase();
        return hash;
    }

    public static String entityRegistryKey(Entity entity) {
        if (entity == null) return "unknown";
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return id == null ? "unknown" : id.toString();
    }

    public static String nodeKey(AuraNodeBlockEntity node) {
        return node == null ? NODE_PREFIX + "unknown" : NODE_PREFIX + node.nodeId();
    }
}
