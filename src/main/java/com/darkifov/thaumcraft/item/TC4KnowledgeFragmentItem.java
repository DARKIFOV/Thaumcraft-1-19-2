package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.PlayerAspectKnowledge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import javax.annotation.Nullable;
import java.util.List;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** TC4 itemResource:9 parity: consume one fragment for 1-2 points of every primal aspect. */
public class TC4KnowledgeFragmentItem extends Item {
    public TC4KnowledgeFragmentItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.thaumcraft.tc4_knowledgefragment.desc"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            for (Aspect aspect : Aspect.values()) {
                if (aspect.isPrimal()) {
                    PlayerAspectKnowledge.addPool(serverPlayer, aspect, 1 + level.random.nextInt(2));
                }
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            ThaumcraftNetwork.syncAspectKnowledge(serverPlayer);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
