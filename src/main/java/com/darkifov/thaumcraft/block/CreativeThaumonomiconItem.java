package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.OriginalResearchProgression;
import com.darkifov.thaumcraft.research.PlayerAspectKnowledge;
import com.darkifov.thaumcraft.research.ResearchEntry;
import com.darkifov.thaumcraft.research.ResearchRegistry;
import com.darkifov.thaumcraft.research.TC4ThaumonomiconParity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Metadata-42 TC4 cheat-sheet behavior represented by a dedicated 1.19.2 item id. */
public class CreativeThaumonomiconItem extends ThaumonomiconItem {
    public CreativeThaumonomiconItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            for (ResearchEntry entry : ResearchRegistry.originalEntries()) {
                if (PlayerThaumData.unlockResearch(serverPlayer, entry.key())) {
                    OriginalResearchProgression.applyUnlockSideEffects(serverPlayer, entry);
                }
            }
            for (Aspect aspect : Aspect.values()) {
                if (!PlayerAspectKnowledge.knows(serverPlayer, aspect)) {
                    PlayerAspectKnowledge.discover(serverPlayer, aspect);
                    PlayerAspectKnowledge.setPoolAmount(serverPlayer, aspect, TC4ThaumonomiconParity.CHEAT_ASPECT_POOL);
                }
            }
            ThaumcraftNetwork.syncResearch(serverPlayer);
            ThaumcraftNetwork.syncAspectKnowledge(serverPlayer);
        }
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Cheat Sheet").withStyle(ChatFormatting.DARK_PURPLE));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
