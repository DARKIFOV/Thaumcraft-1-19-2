package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;

import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ThaumicTinkererParityItem extends Item {
    public enum Mode {
        FOCUS_DISLOCATION,
        FOCUS_TELEKINESIS,
        FOCUS_FLIGHT,
        FOCUS_DEFLECT,
        FOCUS_ENDER_CHEST,
        FOCUS_XP_DRAIN,
        FOCUS_RECALL,
        FOCUS_SHADOWBEAM,
        SKY_PEARL,
        PROTOCLAY,
        CAT_AMULET,
        PLACEMENT_MIRROR,
        ICHOR_POUCH,
        CLEANSING_TALISMAN,
        XP_TALISMAN,
        INFUSED_INKWELL,
        INFUSED_POTION,
        GAS_REMOVER,
        SOUL_MOULD,
        MOB_DISPLAY,
        CONNECTOR,
        DARK_QUARTZ,
        INFUSED_SEED,
        INFUSED_GRAIN,
        KAMI_RESOURCE,
        ICHOR_TOOL,
        ICHOR_GEM_ARMOR,
        ICHOR_WAND_PART,
        BRIGHT_NITOR,
        SPELL_CLOTH
    }

    private final Mode mode;

    public ThaumicTinkererParityItem(Properties properties, Mode mode) {
        super(properties);
        this.mode = mode;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            PlayerThaumData.unlockResearch(player, "THAUMIC_TINKERER_TRUE_PARITY");

            switch (mode) {
                case FOCUS_DISLOCATION -> teleportForward(level, player, 12.0D, "Focus Dislocation");
                case FOCUS_TELEKINESIS -> pullNearbyItems(level, player);
                case FOCUS_FLIGHT -> {
                    player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 20, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 3 * 20, 0));
                    player.displayClientMessage(Component.literal("Focus Flight gives controlled lift and slow falling.").withStyle(ChatFormatting.AQUA), false);
                }
                case FOCUS_DEFLECT -> {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 15 * 20, 1));
                    player.displayClientMessage(Component.literal("Focus Deflect creates a short defensive ward.").withStyle(ChatFormatting.AQUA), false);
                }
                case FOCUS_ENDER_CHEST -> player.displayClientMessage(Component.literal("Focus Ender Chest parity: remote storage link placeholder; use Ender Chest nearby for storage.").withStyle(ChatFormatting.DARK_PURPLE), false);
                case FOCUS_XP_DRAIN -> {
                    player.giveExperiencePoints(7);
                    player.displayClientMessage(Component.literal("Focus XP Drain condenses ambient experience.").withStyle(ChatFormatting.GREEN), false);
                }
                case FOCUS_RECALL -> {
                    BlockPos spawn = level.getSharedSpawnPos();
                    player.teleportTo(spawn.getX() + 0.5D, spawn.getY() + 1.0D, spawn.getZ() + 0.5D);
                    player.displayClientMessage(Component.literal("Focus Recall returns you to world spawn.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
                }
                case FOCUS_SHADOWBEAM -> {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 30 * 20, 0));
                    player.displayClientMessage(Component.literal("Focus Shadowbeam channels eldritch light/shadow energy.").withStyle(ChatFormatting.DARK_PURPLE), false);
                }
                case SKY_PEARL -> {
                    player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60 * 20, 0));
                    player.displayClientMessage(Component.literal("Sky Pearl stabilizes air and prevents falling damage.").withStyle(ChatFormatting.AQUA), false);
                }
                case PROTOCLAY -> player.displayClientMessage(Component.literal("Protoclay is a KAMI morphic crafting resource.").withStyle(ChatFormatting.GOLD), false);
                case CAT_AMULET -> {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60 * 20, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.JUMP, 60 * 20, 1));
                    player.displayClientMessage(Component.literal("Cat Amulet grants agility.").withStyle(ChatFormatting.GREEN), false);
                }
                case PLACEMENT_MIRROR -> player.displayClientMessage(Component.literal("Placement Mirror parity: remote placement predictor/utility.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
                case ICHOR_POUCH -> player.displayClientMessage(Component.literal("Ichor Pouch parity: portable KAMI inventory layer.").withStyle(ChatFormatting.GOLD), false);
                case CLEANSING_TALISMAN -> {
                    player.clearFire();
                    player.removeEffect(MobEffects.POISON);
                    player.removeEffect(ThaumcraftMod.TAINT_POISON.get());
                    player.removeEffect(MobEffects.WITHER);
                    player.removeEffect(MobEffects.BLINDNESS);
                    player.removeEffect(MobEffects.CONFUSION);
                    player.displayClientMessage(Component.literal("Cleansing Talisman removes harmful effects.").withStyle(ChatFormatting.AQUA), false);
                }
                case XP_TALISMAN -> {
                    player.giveExperiencePoints(3);
                    player.displayClientMessage(Component.literal("XP Talisman stores and releases experience.").withStyle(ChatFormatting.GREEN), false);
                }
                case INFUSED_INKWELL -> player.displayClientMessage(Component.literal("Infused Inkwell: reusable ink source for research and spell cloth recipes.").withStyle(ChatFormatting.AQUA), false);
                case INFUSED_POTION -> {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 12 * 20, 0));
                    player.displayClientMessage(Component.literal("Infused Potion releases primal restorative magic.").withStyle(ChatFormatting.GREEN), false);
                }
                case GAS_REMOVER -> player.displayClientMessage(Component.literal("Gas Remover clears gaseous light/shadow/nitor blocks in the original TT branch.").withStyle(ChatFormatting.GRAY), false);
                case SOUL_MOULD -> player.displayClientMessage(Component.literal("Soul Mould is used for soul/KAMI crafting chains.").withStyle(ChatFormatting.DARK_PURPLE), false);
                case MOB_DISPLAY -> player.displayClientMessage(Component.literal("Mob Display stores and displays mob aspect data.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
                case CONNECTOR -> player.displayClientMessage(Component.literal("Connector links TT machines such as golem connector, funnel and transvector systems.").withStyle(ChatFormatting.AQUA), false);
                case DARK_QUARTZ -> player.displayClientMessage(Component.literal("Dark Quartz: construction and transvector/KAMI component.").withStyle(ChatFormatting.DARK_GRAY), false);
                case INFUSED_SEED -> player.displayClientMessage(Component.literal("Infused Seed grows primal infused crops on infused farmland.").withStyle(ChatFormatting.GREEN), false);
                case INFUSED_GRAIN -> {
                    player.getFoodData().eat(2, 0.25F);
                    player.displayClientMessage(Component.literal("Infused Grain restores a small amount of hunger.").withStyle(ChatFormatting.GREEN), false);
                }
                case KAMI_RESOURCE -> player.displayClientMessage(Component.literal("KAMI Resource: high-tier crafting material from original Thaumic Tinkerer KAMI.").withStyle(ChatFormatting.GOLD), false);
                case ICHOR_TOOL -> {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 30 * 20, 1));
                    player.displayClientMessage(Component.literal("Ichor tool parity: high-tier KAMI tool boost.").withStyle(ChatFormatting.GOLD), false);
                }
                case ICHOR_GEM_ARMOR -> {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30 * 20, 0));
                    player.displayClientMessage(Component.literal("Gemmed Ichor armor parity: defensive KAMI armor layer.").withStyle(ChatFormatting.GOLD), false);
                }
                case ICHOR_WAND_PART -> player.displayClientMessage(Component.literal("Ichor wand part: rod/cap for KAMI wand crafting.").withStyle(ChatFormatting.GOLD), false);
                case BRIGHT_NITOR -> {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 60 * 20, 0));
                    player.displayClientMessage(Component.literal("Bright Nitor illuminates the area with primal light.").withStyle(ChatFormatting.YELLOW), false);
                }
                case SPELL_CLOTH -> player.displayClientMessage(Component.literal("Spell Cloth: KAMI/TT magical fabric component.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
            }

            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.4F, 1.2F);
        }

        return InteractionResultHolder.success(stack);
    }

    private void teleportForward(Level level, Player player, double distance, String name) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(distance));
        BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 target = hit.getLocation().subtract(player.getLookAngle().scale(1.0D));

        player.teleportTo(target.x, target.y, target.z);
        player.displayClientMessage(Component.literal(name + " blinked/dislocated you forward.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
    }

    private void pullNearbyItems(Level level, Player player) {
        int moved = 0;

        for (Entity entity : level.getEntities(player, player.getBoundingBox().inflate(8.0D))) {
            if (entity instanceof ItemEntity itemEntity) {
                itemEntity.setDeltaMovement(player.position().subtract(itemEntity.position()).normalize().scale(0.45D));
                moved++;
            }
        }

        player.displayClientMessage(Component.literal("Focus Telekinesis pulls nearby items: " + moved).withStyle(ChatFormatting.AQUA), false);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return mode.name().contains("KAMI") || mode.name().contains("ICHOR") || mode == Mode.SKY_PEARL;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Thaumic Tinkerer true parity: " + mode.name()).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("Ported from 1.7.10 public item/focus/KAMI surface.").withStyle(ChatFormatting.GRAY));
    }
}
