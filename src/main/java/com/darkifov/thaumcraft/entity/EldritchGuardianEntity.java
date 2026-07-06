package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EldritchGuardianEntity extends Monster {
    private int blinkCooldown = 0;

    public EldritchGuardianEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        xpReward = 18;
        setCustomName(Component.literal("Eldritch Guardian").withStyle(ChatFormatting.DARK_PURPLE));
        setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 42.0D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.31D)
                .add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.35D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1D, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (blinkCooldown > 0) {
            blinkCooldown--;
        }

        if (!level.isClientSide && tickCount % 20 == 0 && level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY() + 1.0D, getZ(), 6, 0.35D, 0.55D, 0.35D, 0.03D);
        }

        LivingEntity target = getTarget();
        if (!level.isClientSide && target != null && blinkCooldown <= 0 && distanceToSqr(target) > 64.0D && random.nextInt(100) < 24) {
            double ox = target.getX() + (random.nextDouble() - 0.5D) * 5.0D;
            double oz = target.getZ() + (random.nextDouble() - 0.5D) * 5.0D;
            teleportTo(ox, target.getY(), oz);
            blinkCooldown = 100;
            level.playSound(null, blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.8F, 0.7F);
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity entity) {
        boolean result = super.doHurtTarget(entity);

        if (result && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
        }

        return result;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);

        if (random.nextFloat() < 0.35F + looting * 0.08F) {
            spawnAtLocation(new ItemStack(ThaumcraftMod.ELDRITCH_RELIC.get(), 1));
        }

        if (random.nextFloat() < 0.18F + looting * 0.06F) {
            spawnAtLocation(new ItemStack(ThaumcraftMod.ELDRITCH_GUARDIAN_CORE.get(), 1));
        }
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }
}
