package com.darkifov.thaumcraft.item.simple;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.ManaPodBlock;
import com.darkifov.thaumcraft.blockentity.ManaPodBlockEntity;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.PlayerAspectKnowledge;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Full TC4 ItemManaBean food, aspect tint and Mana Pod planting contract. */
public class TC4ManaBeanItem extends Item {
    private static final String TAG_ASPECT = "Aspect";
    private static final MobEffect[] RANDOM_EFFECTS = new MobEffect[] {
            MobEffects.MOVEMENT_SPEED,
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.DIG_SPEED,
            MobEffects.DIG_SLOWDOWN,
            MobEffects.DAMAGE_BOOST,
            MobEffects.WEAKNESS,
            MobEffects.REGENERATION,
            MobEffects.POISON,
            MobEffects.CONFUSION
    };

    public TC4ManaBeanItem(Properties properties) {
        super(properties);
    }

    public static Aspect getAspect(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_ASPECT)) {
            return null;
        }
        return Aspect.byId(tag.getString(TAG_ASPECT));
    }

    public static void setAspect(ItemStack stack, Aspect aspect) {
        if (aspect == null) {
            if (stack.hasTag()) {
                stack.getTag().remove(TAG_ASPECT);
            }
            return;
        }
        stack.getOrCreateTag().putString(TAG_ASPECT, aspect.id());
    }

    public static int tint(ItemStack stack) {
        Aspect aspect = getAspect(stack);
        if (aspect != null) {
            return aspect.nativeColor();
        }
        Aspect[] values = Aspect.values();
        int index = (int) ((System.currentTimeMillis() / 500L) % values.length);
        return values[index].nativeColor();
    }

    private static Aspect ensureAspect(ItemStack stack, Level level) {
        Aspect existing = getAspect(stack);
        if (existing != null) {
            return existing;
        }
        Aspect[] values = Aspect.values();
        Aspect chosen = values[level.random.nextInt(values.length)];
        setAspect(stack, chosen);
        return chosen;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide && getAspect(stack) == null) {
            ensureAspect(stack, level);
        }
    }


    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.DOWN) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos supportPos = context.getClickedPos();
        BlockPos podPos = supportPos.below();
        BlockState support = level.getBlockState(supportPos);
        BlockPlaceContext placeContext = new BlockPlaceContext(context);
        if (!ManaPodBlock.isSupportedLog(support)
                || !ManaPodBlock.isMagicalBiome(level, podPos)
                || !level.getBlockState(podPos).canBeReplaced(placeContext)) {
            return InteractionResult.PASS;
        }
        if (context.getPlayer() != null
                && !context.getPlayer().mayUseItemAt(podPos, Direction.DOWN, context.getItemInHand())) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide) {
            level.setBlock(podPos, ThaumcraftMod.TC4_MANA_POD.get().defaultBlockState(), 3);
            if (level.getBlockEntity(podPos) instanceof ManaPodBlockEntity pod) {
                pod.setAspect(getAspect(context.getItemInHand()));
            }
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
        Aspect aspect = ensureAspect(stack, level);
        ItemStack result = super.finishUsingItem(stack, level, living);
        if (!level.isClientSide && living instanceof ServerPlayer player) {
            MobEffect effect = RANDOM_EFFECTS[level.random.nextInt(RANDOM_EFFECTS.length)];
            player.addEffect(new MobEffectInstance(effect, 160 + level.random.nextInt(80), 0));
            if (level.random.nextInt(4) == 0) {
                PlayerAspectKnowledge.discover(player, aspect);
                PlayerAspectKnowledge.addPool(player, aspect, 1);
                ThaumcraftNetwork.syncAspectKnowledge(player);
            }
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        Aspect aspect = getAspect(stack);
        if (aspect == null) {
            tooltip.add(Component.literal("Unattuned aspect").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal(aspect.displayName()).withStyle(aspect.color()));
        }
    }
}
