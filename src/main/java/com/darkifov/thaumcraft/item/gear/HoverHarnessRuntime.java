package com.darkifov.thaumcraft.item.gear;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.darkifov.thaumcraft.runic.TC4BaubleSlotAdapter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/** Server-authoritative TC4 Hover.handleHoverArmor and hover-girdle runtime. */
@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HoverHarnessRuntime {
    private static final String TAG_GRANTED = "ThaumcraftHoverFlightGranted";
    private static final float GIRDLE_FALL_REDUCTION = 0.33F;

    private HoverHarnessRuntime() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        boolean girdle = hasHoverGirdle(player);
        if (girdle && player.fallDistance > 0.0F) {
            player.fallDistance = Math.max(0.0F, player.fallDistance - GIRDLE_FALL_REDUCTION);
        }

        ItemStack harness = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(harness.getItem() instanceof HoverHarnessItem)) {
            if (!player.level.isClientSide) {
                revokeGrantedFlight(player);
            }
            return;
        }

        boolean active = HoverHarnessItem.isHoverEnabled(harness);
        if (!player.level.isClientSide) {
            if (player.getAbilities().instabuild || player.isSpectator()) {
                player.getPersistentData().remove(TAG_GRANTED);
                return;
            }
            if (active && !HoverHarnessItem.expendCharge(harness, girdle)) {
                HoverHarnessItem.setHoverEnabled(harness, false);
                active = false;
            }
            if (active) {
                grantFlight(player);
                player.fallDistance = 0.0F;
                if (player.tickCount % 24 == 0) {
                    player.level.playSound(null, player.blockPosition(), TC4Sounds.event("jacobs"),
                            SoundSource.PLAYERS, 0.05F, 1.0F + player.getRandom().nextFloat() * 0.05F);
                }
            } else {
                revokeGrantedFlight(player);
                player.fallDistance *= 0.75F;
            }
        } else if (active && player.getAbilities().flying) {
            int haste = EnchantmentHelper.getItemEnchantmentLevel(ThaumcraftMod.HASTE_ENCHANTMENT.get(), harness);
            float modifier = 0.7F + 0.075F * haste + (girdle ? 0.21F : 0.0F);
            modifier = Math.min(1.0F, modifier);
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x * modifier, motion.y, motion.z * modifier);
        }
    }

    public static boolean toggle(ServerPlayer player) {
        ItemStack harness = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(harness.getItem() instanceof HoverHarnessItem)) {
            return false;
        }
        boolean current = HoverHarnessItem.isHoverEnabled(harness);
        if (!current && HoverHarnessItem.getStoredFuel(harness) <= 0) {
            return false;
        }
        boolean next = !current;
        HoverHarnessItem.setHoverEnabled(harness, next);
        if (next) {
            grantFlight(player);
        } else {
            revokeGrantedFlight(player);
        }
        player.level.playSound(null, player.blockPosition(), TC4Sounds.event(next ? "hhon" : "hhoff"),
                SoundSource.PLAYERS, 0.33F, 1.0F);
        return next;
    }

    public static boolean hasHoverGirdle(Player player) {
        List<ItemStack> equipped = TC4BaubleSlotAdapter.findEquippedBaubles(player);
        for (ItemStack stack : equipped) {
            if (stack.getItem() instanceof HoverGirdleItem) {
                return true;
            }
        }
        if (player.getOffhandItem().getItem() instanceof HoverGirdleItem) {
            return true;
        }
        // Existing Stage211 compatibility mirror for installations without Curios/Baubles.
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof HoverGirdleItem) {
                return true;
            }
        }
        return false;
    }

    private static void grantFlight(Player player) {
        boolean changed = !player.getAbilities().mayfly;
        player.getAbilities().mayfly = true;
        player.getPersistentData().putBoolean(TAG_GRANTED, true);
        if (changed && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.onUpdateAbilities();
        }
    }

    private static void revokeGrantedFlight(Player player) {
        if (!player.getPersistentData().getBoolean(TAG_GRANTED)) {
            return;
        }
        player.getPersistentData().remove(TAG_GRANTED);
        if (player.getAbilities().instabuild || player.isSpectator()) {
            return;
        }
        boolean changed = player.getAbilities().mayfly || player.getAbilities().flying;
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        if (changed && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.onUpdateAbilities();
        }
    }
}
