package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.PlayerAspectKnowledge;
import com.darkifov.thaumcraft.research.ResearchEntry;
import com.darkifov.thaumcraft.research.ResearchRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Functional replacement for TC4's creative thaumonomicon cheat item. */
public class CreativeThaumonomiconItem extends ThaumonomiconItem {
    public CreativeThaumonomiconItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            int unlocked = 0;
            for (ResearchEntry entry : ResearchRegistry.entries()) {
                if (PlayerThaumData.unlockResearch(serverPlayer, entry.key())) {
                    unlocked++;
                }
            }
            for (Aspect aspect : Aspect.values()) {
                PlayerAspectKnowledge.discover(serverPlayer, aspect);
                PlayerAspectKnowledge.addPool(serverPlayer, aspect, 100);
            }
            ThaumcraftNetwork.syncResearch(serverPlayer);
            ThaumcraftNetwork.syncAspectKnowledge(serverPlayer);
            serverPlayer.displayClientMessage(Component.translatable(
                    "thaumcraft.creative_thaumonomicon.unlocked", unlocked, ResearchRegistry.size())
                    .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        }
        return super.use(level, player, hand);
    }
}
