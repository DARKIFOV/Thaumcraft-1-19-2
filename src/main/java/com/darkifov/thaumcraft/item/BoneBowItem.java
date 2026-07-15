package com.darkifov.thaumcraft.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;

/**
 * TC4 ItemBowBone gameplay port.
 *
 * <p>The original reaches full power from j/10 instead of the vanilla j/20,
 * fires at f*2.5, adds 0.5 base arrow damage, has durability 512 and stops
 * drawing after the original 18-tick cap.</p>
 */
public final class BoneBowItem extends BowItem {
    public BoneBowItem(Properties properties) {
        super(properties.durability(512).stacksTo(1));
    }

    public static float getBonePowerForTime(int charge) {
        float power = charge / 10.0F;
        power = (power * power + power * 2.0F) / 3.0F;
        return Math.min(power, 1.0F);
    }

    /**
     * Visual pull progress from the original icon thresholds (0, 8 and 13 ticks)
     * while the gameplay velocity keeps the faster TC4 j/10 curve.
     */
    public static float getPullModelValue(int charge) {
        return Math.min(Math.max(charge, 0) / 18.0F, 1.0F);
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, living, stack, remainingUseDuration);
        int used = getUseDuration(stack) - remainingUseDuration;
        if (used > 18) {
            living.stopUsingItem();
        }
    }

    @Override
    public void releaseUsing(ItemStack bow, Level level, LivingEntity living, int timeLeft) {
        if (!(living instanceof Player player)) {
            return;
        }

        boolean infinite = player.getAbilities().instabuild
                || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow) > 0;
        ItemStack projectile = player.getProjectile(bow);
        int charge = getUseDuration(bow) - timeLeft;
        charge = ForgeEventFactory.onArrowLoose(bow, level, player, charge, !projectile.isEmpty() || infinite);
        if (charge < 0 || (projectile.isEmpty() && !infinite)) {
            return;
        }
        if (projectile.isEmpty()) {
            projectile = new ItemStack(Items.ARROW);
        }

        float power = getBonePowerForTime(charge);
        if (power < 0.1F) {
            return;
        }

        boolean creativePickup = player.getAbilities().instabuild
                || (projectile.getItem() instanceof ArrowItem arrowItem
                && arrowItem.isInfinite(projectile, bow, player));
        if (!level.isClientSide) {
            ArrowItem arrowItem = projectile.getItem() instanceof ArrowItem item ? item : (ArrowItem) Items.ARROW;
            AbstractArrow arrow = arrowItem.createArrow(level, projectile, player);
            arrow = customArrow(arrow);
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 2.5F, 1.0F);
            arrow.setBaseDamage(arrow.getBaseDamage() + 0.5D);

            int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, bow);
            if (powerLevel > 0) {
                arrow.setBaseDamage(arrow.getBaseDamage() + powerLevel * 0.5D + 0.5D);
            }
            int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, bow);
            if (punchLevel > 0) {
                arrow.setKnockback(punchLevel);
            }
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, bow) > 0) {
                arrow.setSecondsOnFire(100);
            }

            bow.hurtAndBreak(1, player, owner -> owner.broadcastBreakEvent(player.getUsedItemHand()));
            if (creativePickup) {
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }
            level.addFreshEntity(arrow);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS, 1.0F,
                1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);

        if (!creativePickup && !player.getAbilities().instabuild) {
            projectile.shrink(1);
            if (projectile.isEmpty()) {
                player.getInventory().removeItem(projectile);
            }
        }
        player.awardStat(Stats.ITEM_USED.get(this));
    }

    @Override
    public int getEnchantmentValue() {
        return 3;
    }
}
