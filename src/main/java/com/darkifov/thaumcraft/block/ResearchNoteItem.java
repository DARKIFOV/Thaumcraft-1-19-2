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

/**
 * Research note plus TC4 metadata-42 "unknown research" behaviour.
 *
 * <p>A blank note made from nine knowledge fragments does not open an empty
 * puzzle. On first use it deterministically selects an eligible hidden,
 * triggered research entry. When none remains, the note is consumed and
 * returns seven to nine fragments, matching ItemResearchNotes in TC4 4.2.3.5.</p>
 */
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
        if (!level.isClientSide && player instanceof ServerPlayer) {
            if (ResearchNoteState.solved(stack)) {
                ResearchNoteSolver.convertSolvedNote(player, stack);
                return InteractionResultHolder.success(stack);
            }
            // TC4 ItemResearchNotes only resolves unknown notes or consumes a
            // completed discovery. The hex puzzle belongs to GuiResearchTable;
            // an unfinished theory cannot be edited from the player's hand.
            player.displayClientMessage(Component.literal("Place this research note in a Research Table to continue the theory.")
                    .withStyle(ChatFormatting.DARK_AQUA), true);
        }

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

        // TC4: new Random(world.getWorldTime() / 10L / 5L).
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
        stack.setHoverName(Component.literal("Research Notes - " + selected.title())
                .withStyle(ChatFormatting.DARK_PURPLE));
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
            tooltip.add(Component.literal("Unknown research").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.literal("Right-click to reveal eligible hidden research.")
                    .withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.literal("If none remains, the note returns 7-9 knowledge fragments.")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        ResearchNoteState.initialize(stack, target);
        tooltip.add(Component.literal("Target: " + target).withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.literal("Theory progress: " + ResearchNoteState.progress(stack) + " / 100")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Required: " + ResearchNoteState.requiredAspects(stack).size()
                + " original TC4 aspects").withStyle(ChatFormatting.GRAY));
        int copies = ResearchNoteState.copyCount(stack);
        if (copies > 0) {
            tooltip.add(Component.literal("Copies made: " + copies).withStyle(ChatFormatting.DARK_AQUA));
        }
        tooltip.add(Component.literal("Right-click: open the research note puzzle.")
                .withStyle(ChatFormatting.DARK_AQUA));
        if (ResearchNoteState.solved(stack)) {
            tooltip.add(Component.literal("Solved: right-click to convert into completed research.")
                    .withStyle(ChatFormatting.GREEN));
        }
    }
}
