package com.darkifov.thaumcraft.runic;

import com.darkifov.thaumcraft.entity.EldritchGuardianEntity;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * Stage212 1.19.2 port of the fortress-mask branches embedded in TC4
 * EventHandlerRunic#entityHurt and WarpEvents.  The original used ItemFortressArmor
 * NBT tags: int "mask" and byte "goggles" on the helmet ItemStack.
 */
public final class TC4FortressMaskRuntime {
    public static final String GOGGLES_TAG = "goggles";
    public static final String MASK_TAG = "mask";
    public static final int MASK_GRINNING_DEVIL = 0;
    public static final int MASK_ANGRY_GHOST = 1;
    public static final int MASK_SIPPING_FIEND = 2;
    public static final String TC4_CHAMPION_MOD_TAG = TC4ChampionModifierRuntime.TC4_CHAMPION_MOD_TAG;

    private TC4FortressMaskRuntime() {
    }

    public static boolean hasGoggles(ItemStack stack) {
        CompoundTag tag = stack == null ? null : stack.getTag();
        return tag != null && tag.contains(GOGGLES_TAG) && tag.getByte(GOGGLES_TAG) != 0;
    }

    public static int mask(ItemStack stack) {
        CompoundTag tag = stack == null ? null : stack.getTag();
        return tag != null && tag.contains(MASK_TAG) ? tag.getInt(MASK_TAG) : -1;
    }

    public static boolean hasMask(Player player, int expected) {
        return player != null && mask(player.getInventory().armor.get(3)) == expected;
    }

    public static boolean hasGrinningDevil(Player player) {
        return hasMask(player, MASK_GRINNING_DEVIL);
    }

    public static void handleHurt(LivingHurtEvent event) {
        handleSippingFiend(event);
        handleAngryGhost(event);
        handleChampionOrEldritchShieldFx(event);
    }

    private static void handleSippingFiend(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        if (!(attacker instanceof ServerPlayer leecher)) {
            return;
        }
        if (!hasMask(leecher, MASK_SIPPING_FIEND)) {
            return;
        }
        float chance = Math.min(1.0F, Math.max(0.0F, event.getAmount() / 12.0F));
        if (leecher.getRandom().nextFloat() < chance) {
            leecher.heal(1.0F);
        }
    }

    private static void handleAngryGhost(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!hasMask(player, MASK_ANGRY_GHOST)) {
            return;
        }
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof LivingEntity living)) {
            return;
        }
        float chance = Math.min(1.0F, Math.max(0.0F, event.getAmount() / 10.0F));
        if (player.getRandom().nextFloat() < chance) {
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 80));
        }
    }

    private static void handleChampionOrEldritchShieldFx(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Monster monster) || !(monster.level instanceof ServerLevel level)) {
            return;
        }
        int championMod = TC4ChampionModifierRuntime.championMod(monster);
        boolean eldritch = monster instanceof EldritchGuardianEntity;
        if ((championMod == 5 || eldritch) && monster.getAbsorptionAmount() > 0.0F) {
            ThaumcraftNetwork.sendRunicShieldFx(level, monster, TC4RunicShieldRuntime.fxTargetId(event.getSource()), 32.0D);
            monster.level.playSound(null, monster.blockPosition(), TC4Sounds.event(TC4RunicShieldRuntime.ORIGINAL_EFFECT_SOUND), SoundSource.HOSTILE, 0.66F, 1.1F + monster.getRandom().nextFloat() * 0.1F);
        }
    }

    /** Returns the shared Stage213 champion-mod id bridge; value 5 is the shielded branch. */
    public static int championMod(Mob mob) {
        return TC4ChampionModifierRuntime.championMod(mob);
    }
}
