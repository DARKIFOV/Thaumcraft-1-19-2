package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.entity.PechEntity;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ThaumcraftExtrasFocusItem extends Item {
    public enum Mode {
        BLINK,
        ARROW,
        HEAL,
        SPEED,
        PECH_SUMMON,
        EXPERIENCE,
        RETURN,
        EXCHANGE,
        SMELTING,
        DISPEL,
        DESTROY,
        FREEZE
    }

    private final Mode mode;

    public ThaumcraftExtrasFocusItem(Properties properties, Mode mode) {
        super(properties);
        this.mode = mode;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Стоимость: " + costAspect().displayName() + " " + visCost() + " vis").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Требуется заряженный жезл в инвентаре.").withStyle(ChatFormatting.DARK_PURPLE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        if (mode == Mode.EXCHANGE || mode == Mode.SMELTING || mode == Mode.DESTROY) {
            player.displayClientMessage(Component.literal("Этот фокус нужно использовать по блоку.").withStyle(ChatFormatting.GRAY), false);
            return InteractionResultHolder.success(stack);
        }

        if (!consumeFocusVis(player)) {
            return InteractionResultHolder.success(stack);
        }

        switch (mode) {
            case BLINK -> blink(level, player);
            case ARROW -> shootArrow(level, player);
            case HEAL -> {
                player.heal(6.0F);
                player.displayClientMessage(Component.literal("Фокус исцеления восстанавливает здоровье.").withStyle(ChatFormatting.GREEN), false);
            }
            case SPEED -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 20, 1));
                player.displayClientMessage(Component.literal("Фокус ускорения наполняет тело скоростью.").withStyle(ChatFormatting.AQUA), false);
            }
            case PECH_SUMMON -> summonPechPlaceholder(level, player);
            case EXPERIENCE -> drainExperience(player);
            case RETURN -> returnHome(player);
            case DISPEL -> {
                player.removeAllEffects();
                player.displayClientMessage(Component.literal("Фокус рассеивания снял активные эффекты.").withStyle(ChatFormatting.AQUA), false);
            }
            case FREEZE -> freezeNearby(level, player);
            default -> player.displayClientMessage(Component.literal("Этот фокус нужно использовать по блоку.").withStyle(ChatFormatting.GRAY), false);
        }

        player.getCooldowns().addCooldown(this, 40);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (player == null || level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos();

        if (mode == Mode.DESTROY) {
            if (level.getBlockState(pos).isAir()) {
                return InteractionResult.CONSUME;
            }

            if (!consumeFocusVis(player)) {
                return InteractionResult.CONSUME;
            }

            level.destroyBlock(pos, false, player);
            PlayerThaumData.addWarp(player, 1);
            player.displayClientMessage(Component.literal("Фокус разрушения стирает блок без дропа.").withStyle(ChatFormatting.DARK_PURPLE), false);
            sync(player);
            return InteractionResult.CONSUME;
        }

        if (mode == Mode.SMELTING) {
            boolean changed = false;

            if (level.getBlockState(pos).is(Blocks.COBBLESTONE)) {
                level.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
                changed = true;
            } else if (level.getBlockState(pos).is(Blocks.SAND)) {
                level.setBlock(pos, Blocks.GLASS.defaultBlockState(), 3);
                changed = true;
            } else if (level.getBlockState(pos).is(Blocks.RED_SAND)) {
                level.setBlock(pos, Blocks.GLASS.defaultBlockState(), 3);
                changed = true;
            } else if (level.getBlockState(pos).is(Blocks.CLAY)) {
                level.setBlock(pos, Blocks.TERRACOTTA.defaultBlockState(), 3);
                changed = true;
            } else if (level.getBlockState(pos).is(Blocks.NETHERRACK)) {
                level.setBlock(pos, Blocks.NETHER_BRICKS.defaultBlockState(), 3);
                changed = true;
            } else if (level.getBlockState(pos).is(Blocks.SMOOTH_STONE)) {
                level.setBlock(pos, Blocks.STONE_BRICKS.defaultBlockState(), 3);
                changed = true;
            } else if (level.getBlockState(pos).is(Blocks.RAW_IRON_BLOCK)) {
                level.setBlock(pos, Blocks.IRON_BLOCK.defaultBlockState(), 3);
                changed = true;
            } else if (level.getBlockState(pos).is(Blocks.RAW_GOLD_BLOCK)) {
                level.setBlock(pos, Blocks.GOLD_BLOCK.defaultBlockState(), 3);
                changed = true;
            } else if (level.getBlockState(pos).is(Blocks.RAW_COPPER_BLOCK)) {
                level.setBlock(pos, Blocks.COPPER_BLOCK.defaultBlockState(), 3);
                changed = true;
            }

            if (changed && !consumeFocusVis(player)) {
                return InteractionResult.CONSUME;
            }

            player.displayClientMessage(Component.literal(changed ? "Фокус переплавки изменяет блок." : "Фокус переплавки пока не может обработать этот блок.").withStyle(changed ? ChatFormatting.GOLD : ChatFormatting.GRAY), false);
            return InteractionResult.CONSUME;
        }

        if (mode == Mode.EXCHANGE) {
            boolean changed = false;

            if (level.getBlockState(pos).is(Blocks.STONE)) {
                level.setBlock(pos, Blocks.COBBLESTONE.defaultBlockState(), 3);
                changed = true;
            } else if (level.getBlockState(pos).is(Blocks.COBBLESTONE)) {
                level.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
                changed = true;
            } else if (level.getBlockState(pos).is(Blocks.DIRT)) {
                level.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                changed = true;
            } else if (level.getBlockState(pos).is(Blocks.GRASS_BLOCK)) {
                level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
                changed = true;
            } else if (level.getBlockState(pos).is(Blocks.SAND)) {
                level.setBlock(pos, Blocks.RED_SAND.defaultBlockState(), 3);
                changed = true;
            } else if (level.getBlockState(pos).is(Blocks.RED_SAND)) {
                level.setBlock(pos, Blocks.SAND.defaultBlockState(), 3);
                changed = true;
            } else if (level.getBlockState(pos).is(Blocks.OAK_PLANKS)) {
                level.setBlock(pos, Blocks.BIRCH_PLANKS.defaultBlockState(), 3);
                changed = true;
            } else if (level.getBlockState(pos).is(Blocks.BIRCH_PLANKS)) {
                level.setBlock(pos, Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
                changed = true;
            } else if (level.getBlockState(pos).is(Blocks.SPRUCE_PLANKS)) {
                level.setBlock(pos, Blocks.DARK_OAK_PLANKS.defaultBlockState(), 3);
                changed = true;
            } else if (level.getBlockState(pos).is(Blocks.DARK_OAK_PLANKS)) {
                level.setBlock(pos, Blocks.OAK_PLANKS.defaultBlockState(), 3);
                changed = true;
            }

            if (changed && !consumeFocusVis(player)) {
                return InteractionResult.CONSUME;
            }

            player.displayClientMessage(Component.literal(changed ? "Фокус обмена изменяет форму блока." : "Для этого блока пока нет правила обмена.").withStyle(changed ? ChatFormatting.YELLOW : ChatFormatting.GRAY), false);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    private Aspect costAspect() {
        return switch (mode) {
            case BLINK, RETURN -> Aspect.AER;
            case ARROW, SPEED -> Aspect.ORDO;
            case HEAL, DISPEL -> Aspect.AQUA;
            case PECH_SUMMON, EXPERIENCE -> Aspect.PRAECANTATIO;
            case EXCHANGE -> Aspect.TERRA;
            case SMELTING -> Aspect.IGNIS;
            case DESTROY -> Aspect.PERDITIO;
            case FREEZE -> Aspect.AQUA;
        };
    }

    private int visCost() {
        return switch (mode) {
            case BLINK, RETURN, DESTROY -> 5;
            case PECH_SUMMON -> 8;
            case EXPERIENCE -> 4;
            case HEAL, FREEZE -> 4;
            case DISPEL -> 3;
            default -> 2;
        };
    }

    private boolean consumeFocusVis(Player player) {
        if (WandItem.consumeVisFromInventory(player, costAspect(), visCost())) {
            return true;
        }

        player.displayClientMessage(
                Component.literal("Недостаточно vis для фокуса: нужно " + costAspect().displayName() + " " + visCost() + ". Заряди жезл от Aura Node.").withStyle(ChatFormatting.RED),
                false
        );

        return false;
    }

    private void blink(Level level, Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(16.0D));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 target = hit.getLocation().subtract(look.scale(1.25D));

        player.teleportTo(target.x, target.y, target.z);
        PlayerThaumData.addWarp(player, 1);
        sync(player);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.2F);
        player.displayClientMessage(Component.literal("Фокус рывка искривляет пространство впереди.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
    }

    private void shootArrow(Level level, Player player) {
        Arrow arrow = new Arrow(level, player);
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.2F, 1.0F);
        level.addFreshEntity(arrow);
        level.playSound(null, player.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.8F, 1.2F);
    }

    private void summonPechPlaceholder(Level level, Player player) {
        PechEntity pech = ThaumcraftMod.PECH.get().create(level);

        if (pech != null) {
            pech.randomizeVariant();
            pech.moveTo(player.getX() + 1.5D, player.getY(), player.getZ() + 1.5D, player.getYRot(), 0.0F);
            level.addFreshEntity(pech);
            PlayerThaumData.addWarp(player, 1);
            sync(player);
            player.displayClientMessage(Component.literal("На зов ответил Печ. Дай ему жетон торговли или подарок.").withStyle(ChatFormatting.GOLD), false);
        }
    }

    private void drainExperience(Player player) {
        if (player.experienceLevel <= 0 && !player.getAbilities().instabuild) {
            player.displayClientMessage(Component.literal("Нужен хотя бы один уровень опыта.").withStyle(ChatFormatting.RED), false);
            return;
        }

        if (!player.getAbilities().instabuild) {
            player.giveExperienceLevels(-1);
        }

        player.getInventory().add(new ItemStack(ThaumcraftMod.EXPERIENCE_SHARD.get()));
        player.displayClientMessage(Component.literal("Опыт кристаллизован в кристалл.").withStyle(ChatFormatting.GREEN), false);
    }

    private void returnHome(Player player) {
        if (player instanceof ServerPlayer serverPlayer && serverPlayer.level instanceof ServerLevel serverLevel) {
            BlockPos target = serverPlayer.getRespawnPosition();

            if (target == null) {
                target = serverLevel.getSharedSpawnPos();
            }

            serverPlayer.teleportTo(serverLevel, target.getX() + 0.5D, target.getY() + 1.0D, target.getZ() + 0.5D, serverPlayer.getYRot(), serverPlayer.getXRot());
            PlayerThaumData.addWarp(player, 1);
            sync(player);
            player.displayClientMessage(Component.literal("Фокус возвращения тянет тебя домой.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
        }
    }

    private void freezeNearby(Level level, Player player) {
        AABB box = player.getBoundingBox().inflate(6.0D);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box, entity -> entity != player);

        for (LivingEntity entity : entities) {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 8, 4));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 5, 0));
        }

        player.displayClientMessage(Component.literal("Фокус заморозки сковывает ближайших существ холодом.").withStyle(ChatFormatting.AQUA), false);
    }

    private void sync(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ThaumcraftNetwork.syncResearch(serverPlayer);
        }
    }
}
