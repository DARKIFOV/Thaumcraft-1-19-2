package com.darkifov.thaumcraft.item.gear;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/** Server/client movement bridge for TC4 ItemBootsTraveller. */
@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BootsOfTravellerRuntime {
    private static final UUID STEP_HEIGHT_UUID = UUID.fromString("d47d8a4e-195d-4b43-8713-b6c53ea40417");
    private static final AttributeModifier STEP_HEIGHT = new AttributeModifier(
            STEP_HEIGHT_UUID,
            "Thaumcraft Boots of the Traveller step height",
            0.4D,
            AttributeModifier.Operation.ADDITION
    );

    private static final float GROUND_ACCELERATION = 0.055F;
    private static final float WATER_DIVISOR = 4.0F;
    private static final float AIR_ACCELERATION = 0.03F;
    private static final double JUMP_BONUS = 0.2750000059604645D;
    private static final float FALL_DISTANCE_REDUCTION = 0.25F;

    private BootsOfTravellerRuntime() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;
        boolean wearing = isWearing(player);
        updateStepHeight(player, wearing);
        if (!wearing) {
            return;
        }

        if (!player.getAbilities().flying && player.zza > 0.0F) {
            if (player.isOnGround()) {
                float bonus = GROUND_ACCELERATION;
                if (player.isInWater()) {
                    bonus /= WATER_DIVISOR;
                }
                player.moveRelative(bonus, new Vec3(0.0D, 0.0D, 1.0D));
            } else if (!player.isInWater() && !player.isInLava()) {
                /* TC4 raised jumpMovementFactor from vanilla 0.02 to 0.05. */
                player.moveRelative(AIR_ACCELERATION, new Vec3(0.0D, 0.0D, 1.0D));
            }
        }

        if (player.fallDistance > 0.0F) {
            player.fallDistance = Math.max(0.0F, player.fallDistance - FALL_DISTANCE_REDUCTION);
        }
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player) || !isWearing(player)) {
            return;
        }
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x, motion.y + JUMP_BONUS, motion.z);
        player.hasImpulse = true;
    }

    private static void updateStepHeight(Player player, boolean wearing) {
        AttributeInstance attribute = player.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
        if (attribute == null) {
            return;
        }
        attribute.removeModifier(STEP_HEIGHT_UUID);
        if (wearing && !player.getAbilities().flying && !player.isShiftKeyDown() && player.zza > 0.0F) {
            attribute.addTransientModifier(STEP_HEIGHT);
        }
    }

    public static boolean isWearing(Player player) {
        return player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof BootsOfTravellerItem;
    }
}
