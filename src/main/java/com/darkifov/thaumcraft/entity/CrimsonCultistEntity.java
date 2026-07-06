package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CrimsonCultistEntity extends Monster {
    public enum Role {
        CULTIST,
        KNIGHT,
        CLERIC,
        LEADER
    }

    private Role role;
    private int castCooldown = 0;

    public CrimsonCultistEntity(EntityType<? extends Monster> type, Level level, Role role) {
        super(type, level);
        this.role = role == null ? Role.CULTIST : role;
        Role effective = effectiveRole();
        xpReward = effective == Role.LEADER ? 28 : effective == Role.KNIGHT ? 14 : 10;
        refreshName();
    }

    public static AttributeSupplier.Builder createCultistAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    public static AttributeSupplier.Builder createKnightAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 34.0D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.15D);
    }

    public static AttributeSupplier.Builder createClericAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 28.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.ARMOR, 3.0D);
    }

    public static AttributeSupplier.Builder createLeaderAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 52.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D);
    }

    public Role role() {
        return effectiveRole();
    }

    @Override
    protected void registerGoals() {
        Role effective = effectiveRole();
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, effective == Role.KNIGHT || effective == Role.LEADER ? 1.05D : 0.95D, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.72D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (castCooldown > 0) {
            castCooldown--;
        }

        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (tickCount % 30 == 0) {
            Role effective = effectiveRole();
            serverLevel.sendParticles(effective == Role.CLERIC ? ParticleTypes.WITCH : ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 1.0D, getZ(), 4, 0.3D, 0.45D, 0.3D, 0.02D);
        }

        Role effective = effectiveRole();
        LivingEntity target = getTarget();
        if (target != null && castCooldown <= 0 && (effective == Role.CLERIC || effective == Role.LEADER)) {
            if (distanceToSqr(target) < 144.0D) {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, effective == Role.LEADER ? 1 : 0));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 70, 0));
                level.playSound(null, blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 0.7F, 0.75F);
            }

            if (getHealth() < getMaxHealth() * 0.65F) {
                heal(effective == Role.LEADER ? 6.0F : 3.0F);
                serverLevel.sendParticles(ParticleTypes.HEART, getX(), getY() + 1.1D, getZ(), 3, 0.25D, 0.25D, 0.25D, 0.01D);
            }

            castCooldown = effective == Role.LEADER ? 80 : 120;
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity entity) {
        boolean result = super.doHurtTarget(entity);

        if (result && entity instanceof Player player) {
            PlayerThaumData.addWarpTemporary(player, effectiveRole() == Role.LEADER ? 2 : 1);
        }

        return result;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);

        Item blade = ThaumcraftMod.TC4_RESEARCH_ITEMS.containsKey("tc4_crimson_blade")
                ? ThaumcraftMod.TC4_RESEARCH_ITEMS.get("tc4_crimson_blade").get()
                : ThaumcraftMod.CRIMSON_KEY.get();

        Role effective = effectiveRole();

        if ((effective == Role.KNIGHT || effective == Role.LEADER) && random.nextFloat() < 0.18F + looting * 0.05F) {
            spawnAtLocation(new ItemStack(blade, 1));
        }

        if (random.nextFloat() < (effective == Role.LEADER ? 0.55F : 0.22F) + looting * 0.05F) {
            spawnAtLocation(new ItemStack(ThaumcraftMod.CRIMSON_KEY.get(), 1));
        }

        if (effective == Role.LEADER && random.nextFloat() < 0.25F + looting * 0.05F) {
            Item rites = ThaumcraftMod.TC4_RESEARCH_ITEMS.containsKey("tc4_crimson_rites")
                    ? ThaumcraftMod.TC4_RESEARCH_ITEMS.get("tc4_crimson_rites").get()
                    : ThaumcraftMod.ELDRITCH_RELIC.get();
            spawnAtLocation(new ItemStack(rites, 1));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Role", effectiveRole().name());
        tag.putInt("CastCooldown", castCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("Role")) {
            try {
                role = Role.valueOf(tag.getString("Role"));
            } catch (IllegalArgumentException ignored) {
                role = Role.CULTIST;
            }
        }

        castCooldown = tag.getInt("CastCooldown");
        refreshName();
    }

    private Role effectiveRole() {
        return role == null ? Role.CULTIST : role;
    }

    private void refreshName() {
        Component name = switch (effectiveRole()) {
            case CULTIST -> Component.literal("Crimson Cultist").withStyle(ChatFormatting.DARK_RED);
            case KNIGHT -> Component.literal("Crimson Knight").withStyle(ChatFormatting.RED);
            case CLERIC -> Component.literal("Crimson Cleric").withStyle(ChatFormatting.LIGHT_PURPLE);
            case LEADER -> Component.literal("Crimson Praetor").withStyle(ChatFormatting.GOLD);
        };

        setCustomName(name);
        setCustomNameVisible(true);
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }
}
