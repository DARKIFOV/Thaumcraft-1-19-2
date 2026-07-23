package com.darkifov.thaumcraft.effect;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.warp.TC4WarpRuntimeParity;
import com.darkifov.thaumcraft.warp.TC4UnnaturalHungerParity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * Runtime port of the eight TC4 warp potion classes used by WarpEvents.
 *
 * <p>The original effects are intentionally small: several exist mainly as
 * state markers for client shaders or wand-cost code, while the remaining
 * effects perform their server action on a fixed cadence. Keeping them as
 * real registered effects restores duration/amplifier syncing and makes the
 * warp subsystem compatible with commands, milk/curatives and multiplayer.</p>
 */
public final class TC4WarpMobEffect extends MobEffect {
    public enum Mode {
        VIS_EXHAUST,
        INFECTIOUS_VIS_EXHAUST,
        UNNATURAL_HUNGER,
        DEATH_GAZE,
        BLURRED_VISION,
        SUN_SCORNED,
        THAUMARHIA,
        WARP_WARD
    }

    private final Mode mode;

    public TC4WarpMobEffect(Mode mode, MobEffectCategory category, int color) {
        super(category, color);
        this.mode = mode;
    }

    public Mode mode() {
        return mode;
    }

    /** Restores TC4's per-instance curative lists. */
    public static MobEffectInstance configureCuratives(MobEffectInstance instance) {
        if (!(instance.getEffect() instanceof TC4WarpMobEffect effect)) {
            return instance;
        }

        switch (effect.mode) {
            case UNNATURAL_HUNGER -> {
                java.util.ArrayList<ItemStack> cures = new java.util.ArrayList<>();
                cures.add(new ItemStack(Items.ROTTEN_FLESH));
                Item brain = ForgeRegistries.ITEMS.getValue(new ResourceLocation(ThaumcraftMod.MOD_ID, "tc4_brain"));
                if (brain != null && brain != Items.AIR) {
                    cures.add(new ItemStack(brain));
                }
                instance.setCurativeItems(cures);
            }
            case VIS_EXHAUST, INFECTIOUS_VIS_EXHAUST, DEATH_GAZE, SUN_SCORNED, THAUMARHIA ->
                    instance.setCurativeItems(java.util.List.of());
            default -> {
                // Blurred Vision and Warp Ward retained the default milk cure.
            }
        }
        return instance;
    }

    @Override
    public void applyEffectTick(LivingEntity target, int amplifier) {
        if (target.level.isClientSide) {
            return;
        }

        switch (mode) {
            case INFECTIOUS_VIS_EXHAUST -> spreadVisExhaustion(target, amplifier);
            case UNNATURAL_HUNGER -> {
                if (target instanceof Player player) {
                    player.causeFoodExhaustion(TC4UnnaturalHungerParity.exhaustionPerTick(amplifier));
                }
            }
            case SUN_SCORNED -> tickSunScorned(target);
            case THAUMARHIA -> tickThaumarhia(target);
            default -> {
                // Marker-only effects: behavior is supplied by WarpEvents,
                // wand cost calculation or client visual hooks.
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return switch (mode) {
            case UNNATURAL_HUNGER -> true;
            case INFECTIOUS_VIS_EXHAUST, SUN_SCORNED ->
                    duration % TC4WarpRuntimeParity.INFECTIOUS_SPREAD_INTERVAL_TICKS == 0;
            case THAUMARHIA -> duration % 20 == 0;
            default -> false;
        };
    }

    private static void spreadVisExhaustion(LivingEntity source, int amplifier) {
        List<LivingEntity> targets = source.level.getEntitiesOfClass(
                LivingEntity.class,
                source.getBoundingBox().inflate(TC4WarpRuntimeParity.INFECTIOUS_SPREAD_RADIUS),
                entity -> entity.isAlive() && entity != source
        );

        for (LivingEntity target : targets) {
            if (target.hasEffect(ThaumcraftMod.INFECTIOUS_VIS_EXHAUST.get())) {
                continue;
            }

            TC4WarpRuntimeParity.SpreadResult spread =
                    TC4WarpRuntimeParity.infectiousSpread(amplifier);
            MobEffect propagatedEffect = spread.infectious()
                    ? ThaumcraftMod.INFECTIOUS_VIS_EXHAUST.get()
                    : ThaumcraftMod.VIS_EXHAUST.get();

            // TC4 only cleared curatives on the initial warp-event instance.
            // Propagated PotionEffect instances retained the default milk cure.
            target.addEffect(new MobEffectInstance(
                    propagatedEffect,
                    TC4WarpRuntimeParity.INFECTIOUS_SPREAD_DURATION_TICKS,
                    spread.amplifier(),
                    false,
                    true,
                    true
            ));
        }
    }

    private static void tickSunScorned(LivingEntity target) {
        BlockPos pos = target.blockPosition();
        // TC4 called EntityLivingBase#getBrightness(1.0F), which samples the
        // world's brightness table. Dividing a raw light integer by 15 is not
        // equivalent around the 0.5/0.25 behavior thresholds.
        float brightness = target.getLightLevelDependentMagicValue();

        // Preserve legacy short-circuit RNG consumption: no random number is
        // drawn in the neutral brightness band.
        if (brightness > TC4WarpRuntimeParity.SUN_SCORNED_BURN_BRIGHTNESS) {
            if (TC4WarpRuntimeParity.sunScornedBurns(
                    brightness, target.getRandom().nextFloat(), target.level.canSeeSky(pos))) {
                target.setSecondsOnFire(4);
            }
        } else if (brightness < TC4WarpRuntimeParity.SUN_SCORNED_HEAL_BRIGHTNESS
                && TC4WarpRuntimeParity.sunScornedHeals(brightness, target.getRandom().nextFloat())) {
            target.heal(1.0F);
        }
    }

    private static void tickThaumarhia(LivingEntity target) {
        if (target.getRandom().nextInt(15) != 0) {
            return;
        }

        BlockPos pos = target.blockPosition();
        if (target.level.getBlockState(pos).isAir()) {
            target.level.setBlock(pos, ThaumcraftMod.FLUX_GOO.get().defaultBlockState(), 3);
        }
    }
}
