package com.darkifov.thaumcraft.enchantment;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectDatabase;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.WandItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.EnumMap;
import java.util.Map;

/** Runtime effects for the two custom TC4 infusion enchantments. */
@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TC4EnchantmentEvents {
    private TC4EnchantmentEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        applyHaste(event.player);

        if (!event.player.level.isClientSide
                && event.player instanceof ServerPlayer serverPlayer
                && serverPlayer.tickCount > 0
                && serverPlayer.tickCount % 40 == 0
                && !serverPlayer.getAbilities().instabuild) {
            repairInventory(serverPlayer);
        }
    }

    /**
     * TC4 EventHandlerEntity.updateSpeed parity: +0.015 movement impulse per
     * level, halved while airborne and halved again while swimming.
     */
    private static void applyHaste(Player player) {
        if (player.isSpectator() || player.getAbilities().flying || player.zza <= 0.0F) {
            return;
        }

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        ItemStack harness = player.getItemBySlot(EquipmentSlot.CHEST);
        int level = Math.max(
                EnchantmentHelper.getItemEnchantmentLevel(ThaumcraftMod.HASTE_ENCHANTMENT.get(), boots),
                isHoverHarness(harness)
                        ? EnchantmentHelper.getItemEnchantmentLevel(ThaumcraftMod.HASTE_ENCHANTMENT.get(), harness)
                        : 0
        );
        if (level <= 0) {
            return;
        }

        double bonus = level * 0.015D;
        if (!player.isOnGround()) {
            bonus *= 0.5D;
        }
        if (player.isInWater()) {
            bonus *= 0.5D;
        }

        Vec3 look = player.getLookAngle();
        double horizontal = Math.sqrt(look.x * look.x + look.z * look.z);
        if (horizontal > 1.0E-6D) {
            player.setDeltaMovement(player.getDeltaMovement().add(
                    look.x / horizontal * bonus,
                    0.0D,
                    look.z / horizontal * bonus
            ));
        }
    }

    private static boolean isHoverHarness(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null
                && ThaumcraftMod.MOD_ID.equals(id.getNamespace())
                && "tc4_hoverharness".equals(id.getPath());
    }

    /**
     * TC4 repairs enchanted equipment once every 40 ticks. The hover harness
     * keeps the original exception: it is repaired while worn, but not while
     * merely carried in the main inventory.
     */
    private static void repairInventory(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (!isHoverHarness(stack)) {
                repairStack(player, stack);
            }
        }
        for (ItemStack stack : player.getInventory().armor) {
            repairStack(player, stack);
        }
    }

    private static void repairStack(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamaged()) {
            return;
        }

        int level = Math.min(2,
                EnchantmentHelper.getItemEnchantmentLevel(ThaumcraftMod.REPAIR_ENCHANTMENT.get(), stack));
        if (level <= 0 || !ThaumcraftMod.REPAIR_ENCHANTMENT.get().canEnchant(stack)) {
            return;
        }

        EnumMap<Aspect, Integer> cost = repairCost(stack, level);
        if (cost.isEmpty() || !consumeInventoryVis(player, cost)) {
            return;
        }

        stack.setDamageValue(Math.max(0, stack.getDamageValue() - level));
    }

    /**
     * Original formula: reduce object aspects to primals, then
     * {@code floor(sqrt(amount * 2)) * enchantmentLevel} for each primal.
     */
    public static EnumMap<Aspect, Integer> repairCost(ItemStack stack, int level) {
        AspectList objectAspects = AspectDatabase.getAspectsForItem(stack);
        EnumMap<Aspect, Integer> primals = new EnumMap<>(Aspect.class);
        for (Map.Entry<Aspect, Integer> entry : objectAspects.entries().entrySet()) {
            reduceToPrimals(primals, entry.getKey(), entry.getValue());
        }

        EnumMap<Aspect, Integer> out = new EnumMap<>(Aspect.class);
        int clampedLevel = Math.max(1, Math.min(2, level));
        for (Map.Entry<Aspect, Integer> entry : primals.entrySet()) {
            int amount = (int) Math.sqrt(entry.getValue() * 2.0D) * clampedLevel;
            if (amount > 0) {
                out.put(entry.getKey(), amount);
            }
        }
        return out;
    }

    private static void reduceToPrimals(EnumMap<Aspect, Integer> out, Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) {
            return;
        }
        if (aspect.isPrimal()) {
            out.merge(aspect, amount, Integer::sum);
            return;
        }
        reduceToPrimals(out, aspect.firstComponent(), amount);
        reduceToPrimals(out, aspect.secondComponent(), amount);
    }

    /**
     * TC4 WandManager checks one vis amulet or one wand at a time. It does not
     * pool aspect balances across several wands, so the modern adapter keeps
     * that all-costs-from-one-source rule and scans the inventory backwards.
     */
    private static boolean consumeInventoryVis(Player player, EnumMap<Aspect, Integer> cost) {
        for (int i = player.getInventory().getContainerSize() - 1; i >= 0; i--) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!(stack.getItem() instanceof WandItem)) {
                continue;
            }
            if (WandItem.hasInfiniteVis(stack)) {
                return true;
            }

            boolean enough = true;
            for (Map.Entry<Aspect, Integer> entry : cost.entrySet()) {
                if (WandItem.getVis(stack, entry.getKey()) < entry.getValue()) {
                    enough = false;
                    break;
                }
            }
            if (!enough) {
                continue;
            }

            for (Map.Entry<Aspect, Integer> entry : cost.entrySet()) {
                if (!WandItem.consumeVis(stack, entry.getKey(), entry.getValue())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
