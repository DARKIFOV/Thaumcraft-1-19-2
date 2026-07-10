package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.entity.ThaumGolemEntity;
import com.darkifov.thaumcraft.golem.GolemCoreType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Functional de-metadata'd replacement for TC4 ItemGolemCore. */
public class TC4GolemCoreComponentItem extends TC4ResearchComponentItem {
    private final GolemCoreType coreType;

    public TC4GolemCoreComponentItem(Properties properties, GolemCoreType coreType,
                                     String originalSource, String legacyTexture) {
        super(properties, originalSource, legacyTexture);
        this.coreType = coreType == null ? GolemCoreType.BLANK : coreType;
    }

    public GolemCoreType coreType() {
        return coreType;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof ThaumGolemEntity golem) || coreType == GolemCoreType.BLANK) {
            return InteractionResult.PASS;
        }
        if (player.level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (golem.getOwnerUuid() != null && !player.getUUID().equals(golem.getOwnerUuid())
                && !player.getAbilities().instabuild) {
            player.displayClientMessage(Component.literal("That golem belongs to another thaumaturge.")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.CONSUME;
        }
        if (golem.getCoreType() != GolemCoreType.BLANK) {
            player.displayClientMessage(Component.literal("This golem already has a functional core.")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.CONSUME;
        }
        golem.setCoreType(coreType);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.level.playSound(null, golem.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.NEUTRAL, 0.6F, 1.1F);
        player.displayClientMessage(Component.literal("Installed golem core: ").append(coreType.displayName()), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal(coreType == GolemCoreType.BLANK
                ? "Blank animation core used by original golem recipes."
                : "Install into a coreless golem body.").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Original TC4 metadata: " + (coreType == GolemCoreType.BLANK ? 100 : coreType.originalId()))
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
