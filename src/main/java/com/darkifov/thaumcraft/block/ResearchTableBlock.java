package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectDatabase;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.PlayerAspectKnowledge;
import com.darkifov.thaumcraft.research.ResearchTableFoundation;
import com.darkifov.thaumcraft.research.ResearchNoteState;
import com.darkifov.thaumcraft.research.OriginalResearchBridge;
import com.darkifov.thaumcraft.research.ResearchEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Optional;

public class ResearchTableBlock extends Block {
    // Stage135 keeps the legacy TheoryProgress NBT token on ResearchNoteState, but the table now opens the TC4 note puzzle instead of incrementing it directly.
    public ResearchTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ResearchTableFoundation.seed(player);

        if (held.is(Items.PAPER)) {
            ItemStack tools = findScribingTools(player);

            if (tools.isEmpty()) {
                player.displayClientMessage(Component.literal("You need Scribing Tools with ink.").withStyle(ChatFormatting.RED), false);
                return InteractionResult.CONSUME;
            }

            if (!ScribingToolsItem.consumeInk(tools, 1)) {
                player.displayClientMessage(Component.literal("Your Scribing Tools are out of ink.").withStyle(ChatFormatting.RED), false);
                return InteractionResult.CONSUME;
            }

            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }

            ItemStack note = new ItemStack(ThaumcraftMod.RESEARCH_NOTE.get());
            ResearchEntry targetResearch = OriginalResearchBridge.selectedOrFirstAvailable(player).orElse(null);
            ResearchNoteState.initialize(note, targetResearch == null ? "" : targetResearch.key());
            note.getOrCreateTag().putInt("KnownAspectCount", PlayerAspectKnowledge.knownCount(player));

            if (!player.getInventory().add(note)) {
                Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, note);
            }

            player.displayClientMessage(Component.literal("Research note prepared with ink and paper.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
            player.displayClientMessage(ResearchTableFoundation.knowledgeSummary(player), false);
            if (player instanceof ServerPlayer serverPlayer) {
                ThaumcraftNetwork.syncAspectKnowledge(serverPlayer);
            }
            return InteractionResult.CONSUME;
        }

        if (held.getItem() instanceof ScribingToolsItem) {
            player.displayClientMessage(ResearchTableFoundation.knowledgeSummary(player), false);
            player.displayClientMessage(Component.literal("Ink left: " + ScribingToolsItem.inkLeft(held)).withStyle(ChatFormatting.DARK_AQUA), false);
            if (player instanceof ServerPlayer serverPlayer) {
                ThaumcraftNetwork.syncAspectKnowledge(serverPlayer);
                ThaumcraftNetwork.openResearchTable(serverPlayer);
            }
            return InteractionResult.CONSUME;
        }

        if (held.is(ThaumcraftMod.RESEARCH_NOTE.get())) {
            ResearchNoteState.initialize(held, ResearchNoteState.target(held));

            if (player instanceof ServerPlayer serverPlayer) {
                if (ResearchNoteState.solved(held)) {
                    Optional<ResearchEntry> target = OriginalResearchBridge.byKey(ResearchNoteState.target(held));

                    if (target.isPresent() && OriginalResearchBridge.canUnlock(player, target.get())) {
                        OriginalResearchBridge.unlock(player, target.get());

                        if (!player.getAbilities().instabuild) {
                            held.shrink(1);
                        }

                        player.displayClientMessage(Component.literal("Research completed: ").withStyle(ChatFormatting.GOLD)
                                .append(Component.literal(target.get().title()).withStyle(ChatFormatting.YELLOW)), false);
                        ThaumcraftNetwork.syncResearch(serverPlayer);
                        ThaumcraftNetwork.syncAspectKnowledge(serverPlayer);
                        return InteractionResult.CONSUME;
                    }
                }

                ThaumcraftNetwork.openResearchNote(serverPlayer, held);
            }

            return InteractionResult.CONSUME;
        }

        if (!held.isEmpty()) {
            AspectList aspects = AspectDatabase.getAspectsForItem(held);
            int added = 0;

            for (Aspect aspect : aspects.entries().keySet()) {
                PlayerAspectKnowledge.addPool(player, aspect, Math.max(1, aspects.get(aspect)));
                if (aspect.isPrimal() || knownComponents(player, aspect)) {
                    if (PlayerAspectKnowledge.discover(player, aspect)) {
                        added++;
                    }
                }
            }

            player.displayClientMessage(Component.literal("Table notes aspects from item: ").append(aspects.toComponent()), false);
            if (added > 0) {
                player.displayClientMessage(Component.literal("New aspects understood: " + added).withStyle(ChatFormatting.LIGHT_PURPLE), false);
            }
            if (player instanceof ServerPlayer serverPlayer) {
                ThaumcraftNetwork.syncAspectKnowledge(serverPlayer);
            }
            return InteractionResult.CONSUME;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            ThaumcraftNetwork.syncAspectKnowledge(serverPlayer);
                ThaumcraftNetwork.openResearchTable(serverPlayer);
        }

        return InteractionResult.CONSUME;
    }

    private boolean knownComponents(Player player, Aspect aspect) {
        return aspect.isPrimal()
                || (PlayerAspectKnowledge.knows(player, aspect.firstComponent())
                && PlayerAspectKnowledge.knows(player, aspect.secondComponent()));
    }

    private ItemStack findScribingTools(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (stack.getItem() instanceof ScribingToolsItem && ScribingToolsItem.hasInk(stack)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }
}
