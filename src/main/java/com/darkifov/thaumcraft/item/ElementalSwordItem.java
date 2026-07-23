package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.entity.ThaumGolemEntity;
import com.darkifov.thaumcraft.item.gear.TC4ElementalToolTier;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.mojang.math.Vector3f;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/** Forge 1.19.2 port of TC4's Sword of the Zephyr. */
public final class ElementalSwordItem extends SwordItem {
    private static final ThreadLocal<Boolean> SWEEPING = ThreadLocal.withInitial(() -> false);
    private static final DustParticleOptions WIND_PARTICLE =
            new DustParticleOptions(new Vector3f(0.866F, 0.866F, 0.866F), 0.65F);

    public ElementalSwordItem(Properties properties) {
        super(TC4ElementalToolTier.INSTANCE, 3, -2.4F, properties.stacksTo(1));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.RARE;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72_000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) return;
        int ticks = getUseDuration(stack) - remainingUseDuration;

        Vec3 velocity = player.getDeltaMovement();
        double y = velocity.y;
        if (y < 0.0D) {
            y /= 1.2000000476837158D;
            player.fallDistance /= 1.2F;
        }
        y += 0.07999999821186066D;
        if (y > 0.5D) y = 0.20000000298023224D;
        player.setDeltaMovement(velocity.x, y, velocity.z);
        player.hasImpulse = true;

        AABB range = player.getBoundingBox().inflate(2.5D);
        List<Entity> targets = level.getEntities(player, range,
                entity -> !(entity instanceof Player) && !entity.isRemoved() && player.getVehicle() != entity);
        for (Entity entity : targets) {
            Vec3 away = entity.position().subtract(player.position());
            double distance = away.length() + 0.1D;
            entity.setDeltaMovement(entity.getDeltaMovement().add(away.scale(1.0D / (2.5D * distance))));
            entity.hasImpulse = true;
        }

        if (level instanceof ServerLevel serverLevel) {
            if (ticks == 0 || ticks % 20 == 0) {
                serverLevel.playSound(null, player.blockPosition(), TC4Sounds.event("wind"), SoundSource.PLAYERS,
                        0.5F, 0.9F + serverLevel.random.nextFloat() * 0.2F);
            }
            if (ticks % 20 == 0) {
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
            }
            for (int i = 0; i < 5; i++) {
                double angle = serverLevel.random.nextDouble() * Math.PI * 2.0D;
                double radius = 0.3D + serverLevel.random.nextDouble() * 1.2D;
                serverLevel.sendParticles(WIND_PARTICLE,
                        player.getX() + Math.cos(angle) * radius,
                        player.getY() + player.getBbHeight() * 0.5D + serverLevel.random.nextDouble() * 0.8D - 0.4D,
                        player.getZ() + Math.sin(angle) * radius,
                        1, 0.0D, 0.03D, 0.0D, 0.0D);
            }
            if (player.isOnGround()) {
                float angle = serverLevel.random.nextFloat() * ((float) Math.PI * 2.0F);
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        player.getX(), player.getY() + 0.1D, player.getZ(),
                        1, -Mth.sin(angle) / 5.0D, 0.0D, Mth.cos(angle) / 5.0D, 0.0D);
            }
        }
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity primaryTarget) {
        if (!player.level.isClientSide && !SWEEPING.get() && primaryTarget.isAlive()) {
            int count = 0;
            SWEEPING.set(true);
            try {
                AABB area = primaryTarget.getBoundingBox().inflate(1.2D, 1.1D, 1.2D);
                for (LivingEntity target : player.level.getEntitiesOfClass(LivingEntity.class, area,
                        target -> validSecondary(player, primaryTarget, target))) {
                    player.attack(target);
                    count++;
                }
            } finally {
                SWEEPING.remove();
            }
            if (count > 0 && player.level instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, primaryTarget.blockPosition(), TC4Sounds.event("swing"), SoundSource.PLAYERS,
                        1.0F, 0.9F + serverLevel.random.nextFloat() * 0.2F);
            }
        }
        return super.onLeftClickEntity(stack, player, primaryTarget);
    }

    private static boolean validSecondary(Player player, Entity primaryTarget, LivingEntity target) {
        if (!target.isAlive() || target == primaryTarget || target == player) {
            return false;
        }
        if (target instanceof Player) {
            return player.getServer() != null && player.getServer().isPvpAllowed();
        }
        UUID owner = player.getUUID();
        if (target instanceof TamableAnimal tameable && owner.equals(tameable.getOwnerUUID())) {
            return false;
        }
        return !(target instanceof ThaumGolemEntity golem) || !owner.equals(golem.getOwnerUuid());
    }
}
