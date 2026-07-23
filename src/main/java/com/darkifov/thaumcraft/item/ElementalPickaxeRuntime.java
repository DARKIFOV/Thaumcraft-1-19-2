package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.porting.TC4ResearchItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.level.BlockEvent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/** Deferred HarvestDropsEvent replacement shared by the Core Pickaxe and Primal Crusher. */
public final class ElementalPickaxeRuntime {
    private static final WeakHashMap<ServerLevel, List<Pending>> PENDING = new WeakHashMap<>();

    private ElementalPickaxeRuntime() {}

    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)
                || !(event.getLevel() instanceof ServerLevel level)
                || (!(player.getMainHandItem().getItem() instanceof ElementalPickaxeItem)
                    && !(player.getMainHandItem().getItem() instanceof PrimalCrusherItem))) {
            return;
        }
        BlockState state = event.getState();
        if (!state.is(Tags.Blocks.ORES)) {
            return;
        }
        ClusterRule rule = findRule(state);
        if (rule == null) {
            return;
        }
        ItemStack tool = player.getMainHandItem();
        int fortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
        BlockPos pos = event.getPos().immutable();
        AABB range = new AABB(pos).inflate(2.0D);
        Set<UUID> existing = new HashSet<>();
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, range)) {
            existing.add(item.getUUID());
        }
        PENDING.computeIfAbsent(level, ignored -> new ArrayList<>())
                .add(new Pending(new WeakReference<>(level), pos, rule, fortune, existing, level.getGameTime() + 1L));
    }

    public static void tick(ServerLevel level) {
        List<Pending> queue = PENDING.get(level);
        if (queue == null || queue.isEmpty()) {
            return;
        }
        Iterator<Pending> iterator = queue.iterator();
        while (iterator.hasNext()) {
            Pending pending = iterator.next();
            if (pending.readyTick > level.getGameTime()) {
                continue;
            }
            convert(level, pending);
            iterator.remove();
        }
        if (queue.isEmpty()) {
            PENDING.remove(level);
        }
    }

    private static void convert(ServerLevel level, Pending pending) {
        Item resultItem = TC4ResearchItems.registered(pending.rule.resultId)
                .map(registryObject -> registryObject.get()).orElse(null);
        if (resultItem == null) {
            return;
        }
        float chance = (0.2F + pending.fortune * 0.075F) * pending.rule.multiplier;
        boolean changed = false;
        AABB range = new AABB(pending.pos).inflate(2.0D);
        for (ItemEntity drop : level.getEntitiesOfClass(ItemEntity.class, range,
                entity -> !entity.isRemoved() && !pending.existing.contains(entity.getUUID()))) {
            ItemStack old = drop.getItem();
            if (old.isEmpty() || level.random.nextFloat() > chance) {
                continue;
            }
            ItemStack replacement = new ItemStack(resultItem, old.getCount());
            if (old.hasTag()) {
                replacement.setTag(old.getTag().copy());
            }
            drop.setItem(replacement);
            changed = true;
        }
        if (changed) {
            level.playSound(null, pending.pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS,
                    0.2F, 0.7F + level.random.nextFloat() * 0.2F);
        }
    }

    private static ClusterRule findRule(BlockState state) {
        if (state.is(Tags.Blocks.ORES_IRON)) return new ClusterRule("tc4_clusteriron", 1.0F);
        if (state.is(Tags.Blocks.ORES_GOLD)) return new ClusterRule("tc4_clustergold", 0.9F);
        if (state.is(Tags.Blocks.ORES_COPPER)) return new ClusterRule("tc4_clustercopper", 1.0F);
        if (state.is(ThaumcraftMod.CINNABAR_ORE.get())) return new ClusterRule("tc4_clustercinnabar", 0.9F);
        if (state.is(oreTag("tin"))) return new ClusterRule("tc4_clustertin", 1.0F);
        if (state.is(oreTag("silver"))) return new ClusterRule("tc4_clustersilver", 1.0F);
        if (state.is(oreTag("lead"))) return new ClusterRule("tc4_clusterlead", 1.0F);
        return null;
    }

    private static TagKey<Block> oreTag(String material) {
        return TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("forge", "ores/" + material));
    }

    private record ClusterRule(String resultId, float multiplier) {}
    private record Pending(WeakReference<ServerLevel> level, BlockPos pos, ClusterRule rule,
                           int fortune, Set<UUID> existing, long readyTick) {}
}
