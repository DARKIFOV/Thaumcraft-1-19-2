package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.porting.TC4ResearchItems;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.darkifov.thaumcraft.research.OriginalResearchProgression;
import com.darkifov.thaumcraft.research.ResearchEntry;
import com.darkifov.thaumcraft.research.ResearchNoteSolver;
import com.darkifov.thaumcraft.research.ResearchNoteState;
import com.darkifov.thaumcraft.research.ResearchRegistry;
import com.darkifov.thaumcraft.research.TC4ResearchFlagPolicy;
import com.darkifov.thaumcraft.research.TC4ResearchMetadataIndex;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Research note/discovery with the original TC4 right-click transaction. */
public class ResearchNoteItem extends Item {
    public ResearchNoteItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        String target = ResearchNoteState.target(stack);
        if (target.isBlank()) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                resolveUnknownResearchNote(serverPlayer, stack);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        ResearchNoteState.initialize(stack, target);
        if (!level.isClientSide && ResearchNoteState.solved(stack)) {
            ResearchNoteSolver.convertSolvedNote(player, stack);
            return InteractionResultHolder.success(stack);
        }
        // Unfinished notes are edited only in GuiResearchTable; hand use is silent.
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private static void resolveUnknownResearchNote(ServerPlayer player, ItemStack stack) {
        List<ResearchEntry> candidates = new ArrayList<>();
        for (ResearchEntry entry : ResearchRegistry.originalEntries()) {
            if (!TC4ResearchFlagPolicy.has(entry, TC4ResearchFlagPolicy.HIDDEN)
                    || entry.aspects().isEmpty()
                    || PlayerThaumData.hasResearch(player, entry.key())
                    || !OriginalResearchProgression.parentsComplete(player, entry)
                    || !hasDiscoveryTrigger(entry)) {
                continue;
            }
            candidates.add(entry);
        }
        Random deterministic = new Random(player.level.getGameTime() / 50L);
        if (candidates.isEmpty()) {
            stack.shrink(1);
            int fragments = 7 + player.level.random.nextInt(3);
            TC4ResearchItems.registered("tc4_knowledgefragment")
                    .ifPresent(item -> player.drop(new ItemStack(item.get(), fragments), false));
            player.level.playSound(null, player.blockPosition(), TC4Sounds.event("erase"),
                    SoundSource.PLAYERS, 0.75F, 1.0F);
            return;
        }
        ResearchEntry selected = candidates.get(deterministic.nextInt(candidates.size()));
        ResearchNoteState.initialize(stack, selected.key(), player.getRandom().nextLong());
        player.level.playSound(null, player.blockPosition(), TC4Sounds.event("write"),
                SoundSource.PLAYERS, 0.75F, 1.0F);
    }

    private static boolean hasDiscoveryTrigger(ResearchEntry entry) {
        return entry.entityTriggers().length > 0
                || entry.aspectTriggers().length > 0
                || !TC4ResearchMetadataIndex.itemTriggers(entry.key()).isEmpty();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        String target = ResearchNoteState.target(stack);
        if (target.isBlank()) {
            tooltip.add(Component.translatable("item.researchnotes.unknown.1").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.researchnotes.unknown.2").withStyle(ChatFormatting.BLUE));
            return;
        }
        ResearchNoteState.initialize(stack, target);
        tooltip.add(Component.translatable("tc.research_name." + target).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tc.research_text." + target).withStyle(ChatFormatting.ITALIC));
        ResearchRegistry.byKey(target).ifPresent(entry -> {
            int warp = Math.min(5, entry.warp());
            if (warp > 0) {
                tooltip.add(Component.translatable("tc.forbidden")
                        .append(" ")
                        .append(Component.translatable("tc.forbidden.level." + warp))
                        .withStyle(ChatFormatting.DARK_PURPLE));
            }
        });
    }
}
