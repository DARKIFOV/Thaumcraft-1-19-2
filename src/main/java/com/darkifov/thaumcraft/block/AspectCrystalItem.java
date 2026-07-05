package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class AspectCrystalItem extends Item {
    private final Aspect aspect;

    public AspectCrystalItem(Properties properties, Aspect aspect) {
        super(properties);
        this.aspect = aspect;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            String key = "ASPECT_" + aspect.name();
            boolean unlocked = PlayerThaumData.unlockResearch(player, key);
            player.displayClientMessage(Component.literal(unlocked
                    ? "Aspect insight unlocked: " + aspect.displayName()
                    : "Aspect already known: " + aspect.displayName())
                    .withStyle(aspect.color()), false);

            if (unlocked && !player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Aspect crystal: ").append(Component.literal(aspect.displayName()).withStyle(aspect.color())));
        tooltip.add(Component.literal("Right-click: unlock minor aspect insight.").withStyle(ChatFormatting.GRAY));
    }
}
