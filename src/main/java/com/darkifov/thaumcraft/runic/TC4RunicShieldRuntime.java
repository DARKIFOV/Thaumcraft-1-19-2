package com.darkifov.thaumcraft.runic;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.config.ThaumcraftConfig;
import com.darkifov.thaumcraft.infusion.TC4RunicArmorHelper;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stage211 1.19.2 runtime port of TC4's EventHandlerRunic.
 *
 * Original TC4 source points:
 * - thaumcraft.common.lib.events.EventHandlerRunic#livingTick
 * - thaumcraft.common.lib.events.EventHandlerRunic#entityHurt
 * - PacketRunicCharge / PacketFXShield
 *
 * The implementation preserves TC4's public semantics while adapting Baubles to
 * the 1.19.2 no-hard-dependency inventory model used by this port: armor slots
 * are scanned exactly, and the first four runic bauble mirror items found in the
 * offhand/main inventory are treated as Baubles slots 0..3.
 */
public final class TC4RunicShieldRuntime {
    public static final int DEFAULT_SHIELD_RECHARGE_MS = 2000;
    public static final int DEFAULT_SHIELD_WAIT_TICKS = 80;
    public static final int DEFAULT_SHIELD_COST = 50;
    public static final String ORIGINAL_CHARGE_SOUND = "runicShieldCharge";
    public static final String ORIGINAL_EFFECT_SOUND = "runicShieldEffect";

    private static final Map<UUID, Integer> RUNIC_CHARGE = new HashMap<>();
    private static final Map<UUID, Long> NEXT_CYCLE = new HashMap<>();
    private static final Map<UUID, Integer> LAST_CHARGE = new HashMap<>();
    private static final Map<UUID, RunicInfo> RUNIC_INFO = new HashMap<>();
    private static final Map<String, Long> UPGRADE_COOLDOWN = new HashMap<>();
    private static final Map<UUID, Integer> RECHARGE_DELAY = new HashMap<>();

    private TC4RunicShieldRuntime() {
    }

    public static void tick(ServerPlayer player) {
        UUID key = player.getUUID();
        if (player.tickCount % 40 == 0 || !RUNIC_INFO.containsKey(key)) {
            refreshRunicInfo(player);
        }

        int delay = RECHARGE_DELAY.getOrDefault(key, 0);
        if (delay > 0) {
            RECHARGE_DELAY.put(key, delay - 1);
            return;
        }

        RunicInfo info = RUNIC_INFO.get(key);
        if (info == null || info.max <= 0) {
            return;
        }

        int charge = Math.min(RUNIC_CHARGE.getOrDefault(key, 0), info.max);
        long now = System.currentTimeMillis();
        long next = NEXT_CYCLE.getOrDefault(key, 0L);

        if (charge < info.max && next < now && consumeShieldVis(player)) {
            long interval = Math.max(500L, shieldRechargeMs() - (long) info.charged * 500L);
            NEXT_CYCLE.put(key, now + interval);
            charge++;
            RUNIC_CHARGE.put(key, charge);
        }

        if (LAST_CHARGE.getOrDefault(key, -1) != charge) {
            LAST_CHARGE.put(key, charge);
            ThaumcraftNetwork.sendRunicCharge(player, charge, info.max);
        }
    }

    public static void handleHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (isExcludedDamage(event.getSource())) {
            return;
        }

        UUID key = player.getUUID();
        RunicInfo info = RUNIC_INFO.get(key);
        int charge = RUNIC_CHARGE.getOrDefault(key, 0);
        if (info == null || info.max <= 0 || charge <= 0) {
            return;
        }

        int target = fxTargetId(event.getSource());
        if (player.level instanceof ServerLevel level) {
            ThaumcraftNetwork.sendRunicShieldFx(level, player, target, 64.0D);
        }

        float amount = event.getAmount();
        if (charge > amount) {
            charge = (int) (charge - amount);
            event.setAmount(0.0F);
            event.setCanceled(true);
        } else {
            event.setAmount(amount - charge);
            charge = 0;
        }

        long now = System.currentTimeMillis();
        if (charge <= 0) {
            charge = applyRunicBreakUpgrades(player, info, charge, now);
            if (charge <= 0) {
                RECHARGE_DELAY.put(key, shieldWaitTicks());
            }
        }

        RUNIC_CHARGE.put(key, Math.min(charge, info.max));
        LAST_CHARGE.put(key, Math.min(charge, info.max));
        ThaumcraftNetwork.sendRunicCharge(player, Math.min(charge, info.max), info.max);
    }

    public static int getCharge(Player player) {
        return RUNIC_CHARGE.getOrDefault(player.getUUID(), 0);
    }

    public static int getMaxCharge(Player player) {
        RunicInfo info = RUNIC_INFO.get(player.getUUID());
        return info == null ? 0 : info.max;
    }

    public static RunicInfo currentInfo(Player player) {
        return RUNIC_INFO.getOrDefault(player.getUUID(), RunicInfo.EMPTY);
    }

    private static void refreshRunicInfo(ServerPlayer player) {
        RunicInfo info = collectRunicInfo(player);
        UUID key = player.getUUID();
        if (info.max > 0) {
            RUNIC_INFO.put(key, info);
            int charge = Math.min(RUNIC_CHARGE.getOrDefault(key, 0), info.max);
            RUNIC_CHARGE.put(key, charge);
            ThaumcraftNetwork.sendRunicCharge(player, charge, info.max);
        } else {
            RUNIC_INFO.remove(key);
            RUNIC_CHARGE.put(key, 0);
            LAST_CHARGE.put(key, 0);
            NEXT_CYCLE.remove(key);
            RECHARGE_DELAY.remove(key);
            ThaumcraftNetwork.sendRunicCharge(player, 0, 0);
        }
    }

    private static RunicInfo collectRunicInfo(ServerPlayer player) {
        int max = 0;
        int charged = 0;
        int kinetic = 0;
        int healing = 0;
        int emergency = 0;

        for (ItemStack armor : player.getArmorSlots()) {
            max += TC4RunicArmorHelper.getFinalCharge(armor);
        }

        int baubleSlots = 0;
        for (ItemStack stack : TC4BaubleSlotAdapter.findEquippedBaubles(player)) {
            if (!TC4RunicArmorHelper.isRunicArmor(stack) || !TC4RunicArmorHelper.isRunicBauble(stack)) {
                continue;
            }
            max += TC4RunicArmorHelper.getFinalCharge(stack);
            charged += TC4RunicArmorHelper.isChargedVariant(stack) ? 1 : 0;
            kinetic += TC4RunicArmorHelper.isKineticVariant(stack) ? 1 : 0;
            healing += TC4RunicArmorHelper.isHealingVariant(stack) ? 1 : 0;
            emergency += TC4RunicArmorHelper.isEmergencyVariant(stack) ? 1 : 0;
            baubleSlots++;
            if (baubleSlots >= TC4BaubleSlotAdapter.TC4_BAUBLE_SLOT_LIMIT) {
                break;
            }
        }

        ItemStack offhand = player.getOffhandItem();
        if (baubleSlots < TC4BaubleSlotAdapter.TC4_BAUBLE_SLOT_LIMIT && TC4RunicArmorHelper.isRunicArmor(offhand) && TC4RunicArmorHelper.isRunicBauble(offhand)) {
            max += TC4RunicArmorHelper.getFinalCharge(offhand);
            charged += TC4RunicArmorHelper.isChargedVariant(offhand) ? 1 : 0;
            kinetic += TC4RunicArmorHelper.isKineticVariant(offhand) ? 1 : 0;
            healing += TC4RunicArmorHelper.isHealingVariant(offhand) ? 1 : 0;
            emergency += TC4RunicArmorHelper.isEmergencyVariant(offhand) ? 1 : 0;
            baubleSlots++;
        }

        for (int i = 0; i < player.getInventory().items.size() && baubleSlots < TC4BaubleSlotAdapter.TC4_BAUBLE_SLOT_LIMIT; i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (!TC4RunicArmorHelper.isRunicArmor(stack) || !TC4RunicArmorHelper.isRunicBauble(stack)) {
                continue;
            }
            max += TC4RunicArmorHelper.getFinalCharge(stack);
            charged += TC4RunicArmorHelper.isChargedVariant(stack) ? 1 : 0;
            kinetic += TC4RunicArmorHelper.isKineticVariant(stack) ? 1 : 0;
            healing += TC4RunicArmorHelper.isHealingVariant(stack) ? 1 : 0;
            emergency += TC4RunicArmorHelper.isEmergencyVariant(stack) ? 1 : 0;
            baubleSlots++;
        }

        return new RunicInfo(max, charged, kinetic, healing, emergency);
    }

    private static int applyRunicBreakUpgrades(ServerPlayer player, RunicInfo info, int charge, long now) {
        String kineticKey = player.getUUID() + ":2";
        if (charge <= 0 && info.kinetic > 0 && cooldownReady(kineticKey, now)) {
            UPGRADE_COOLDOWN.put(kineticKey, now + 20_000L);
            player.level.explode(player, player.getX(), player.getY() + player.getBbHeight() * 0.5D, player.getZ(), 1.5F + info.kinetic * 0.5F, false, net.minecraft.world.level.Explosion.BlockInteraction.NONE);
        }

        String healingKey = player.getUUID() + ":3";
        if (charge <= 0 && info.healing > 0 && cooldownReady(healingKey, now)) {
            UPGRADE_COOLDOWN.put(healingKey, now + 20_000L);
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 240, info.healing));
            player.level.playSound(null, player.blockPosition(), TC4Sounds.event(ORIGINAL_EFFECT_SOUND), SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        String emergencyKey = player.getUUID() + ":4";
        if (charge <= 0 && info.emergency > 0 && cooldownReady(emergencyKey, now)) {
            UPGRADE_COOLDOWN.put(emergencyKey, now + 60_000L);
            charge = Math.min(info.max, 8 * info.emergency);
            player.level.playSound(null, player.blockPosition(), TC4Sounds.event(ORIGINAL_CHARGE_SOUND), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return charge;
    }

    private static boolean cooldownReady(String key, long now) {
        return !UPGRADE_COOLDOWN.containsKey(key) || UPGRADE_COOLDOWN.get(key) < now;
    }

    private static boolean consumeShieldVis(ServerPlayer player) {
        int cost = shieldCost();
        if (cost <= 0 || player.getAbilities().instabuild) {
            return true;
        }
        if (!hasInventoryVis(player, Aspect.AER, cost) || !hasInventoryVis(player, Aspect.TERRA, cost)) {
            return false;
        }
        return WandItem.consumeVisFromInventory(player, Aspect.AER, cost) && WandItem.consumeVisFromInventory(player, Aspect.TERRA, cost);
    }

    private static boolean hasInventoryVis(Player player, Aspect aspect, int amount) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof WandItem && (WandItem.hasInfiniteVis(stack) || WandItem.getVis(stack, aspect) >= amount)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isExcludedDamage(DamageSource source) {
        return source == DamageSource.FALL || source == DamageSource.ANVIL || source == DamageSource.OUT_OF_WORLD || source == DamageSource.DROWN;
    }

    public static int fxTargetId(DamageSource source) {
        Entity direct = source.getDirectEntity();
        if (direct != null) {
            return direct.getId();
        }
        Entity attacker = source.getEntity();
        if (attacker != null) {
            return attacker.getId();
        }
        if (source == DamageSource.FALLING_BLOCK) {
            return -2;
        }
        if (source == DamageSource.FALLING_STALACTITE) {
            return -3;
        }
        return -1;
    }

    private static int shieldRechargeMs() {
        return Math.max(500, ThaumcraftConfig.RUNIC_SHIELD_RECHARGE_MS.get());
    }

    private static int shieldWaitTicks() {
        return Math.max(0, ThaumcraftConfig.RUNIC_SHIELD_WAIT_TICKS.get());
    }

    private static int shieldCost() {
        return Math.max(0, ThaumcraftConfig.RUNIC_SHIELD_COST.get());
    }

    public record RunicInfo(int max, int charged, int kinetic, int healing, int emergency) {
        public static final RunicInfo EMPTY = new RunicInfo(0, 0, 0, 0, 0);
    }
}
