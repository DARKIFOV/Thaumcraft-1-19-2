package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.infusion.TC4RunicArmorHelper;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.OriginalAspectWallet;
import com.darkifov.thaumcraft.research.OriginalResearchProgression;
import com.darkifov.thaumcraft.research.PlayerAspectKnowledge;
import com.darkifov.thaumcraft.research.ResearchEntry;
import com.darkifov.thaumcraft.research.ResearchRegistry;
import com.darkifov.thaumcraft.eldritch.TC4OuterLandsLootAdapter;
import com.darkifov.thaumcraft.wand.WandCapType;
import com.darkifov.thaumcraft.wand.WandComponentData;
import com.darkifov.thaumcraft.wand.WandRodType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Stage119: concrete 1.19.2 carrier for original TC4 research/recipe components.
 *
 * Each instance represents a de-metadata'd TC4 1.7.10 ItemStack variant that was
 * commonly used as a research icon, recipe output, catalyst or component. Behavior
 * is intentionally ported in later item-specific stages, but the item itself is
 * now registered, textured and usable by Thaumonomicon recipe/research mapping.
 */
public class TC4ResearchComponentItem extends Item {
    private final String originalSource;
    private final String legacyTexture;

    public TC4ResearchComponentItem(Properties properties, String originalSource, String legacyTexture) {
        super(properties);
        this.originalSource = originalSource;
        this.legacyTexture = legacyTexture;
    }

    public String originalSource() {
        return originalSource;
    }

    public String legacyTexture() {
        return legacyTexture;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isCreativeThaumonomiconCheat()) {
            if (!level.isClientSide) {
                int unlocked = grantAllResearch(player);
                player.displayClientMessage(Component.literal("Creative Thaumonomicon unlocked " + unlocked + " research keys ("
                        + PlayerThaumData.researchCount(player) + "/" + ResearchRegistry.size() + ").")
                        .withStyle(ChatFormatting.GOLD), false);
                if (player instanceof ServerPlayer serverPlayer) {
                    ThaumcraftNetwork.syncResearch(serverPlayer);
                    ThaumcraftNetwork.syncAspectKnowledge(serverPlayer);
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        int rarity = lootbagRarity();
        if (rarity < 0) {
            return super.use(level, player, hand);
        }
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            for (ItemStack loot : TC4OuterLandsLootAdapter.openLootBag(rarity, level.getRandom())) {
                ItemEntity item = new ItemEntity(serverLevel, player.getX(), player.getY(), player.getZ(), loot.copy());
                item.setDefaultPickUpDelay();
                serverLevel.addFreshEntity(item);
            }
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.75F, 1.0F);
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private boolean isCreativeThaumonomiconCheat() {
        // Stage463-482: TC4 already has a thaumonomiconcheat sprite/item mirror.
        // Give that exact original cheat-book mirror behavior instead of registering
        // another duplicate debug book. This is a Forge 1.19.2 utility adapter, not
        // part of survival progression or recipe gates.
        return "thaumonomiconcheat".equals(legacyTexture);
    }

    private int grantAllResearch(Player player) {
        int unlocked = 0;
        OriginalResearchProgression.seedAutoUnlocks(player);
        OriginalAspectWallet.seedIfEmpty(player);
        PlayerAspectKnowledge.seedPrimals(player);

        for (ResearchEntry entry : ResearchRegistry.entries()) {
            if (PlayerThaumData.unlockResearch(player, entry.key())) {
                OriginalResearchProgression.applyUnlockSideEffects(player, entry);
                unlocked++;
            }
        }
        return unlocked;
    }

    private int lootbagRarity() {
        if ("lootbag".equals(legacyTexture)) return 0;
        if ("lootbagunc".equals(legacyTexture)) return 1;
        if ("lootbagrare".equals(legacyTexture)) return 2;
        return -1;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (isCreativeThaumonomiconCheat()) {
            tooltip.add(Component.literal("Creative/debug TC4 cheat book: right-click to unlock every registered research key.")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.literal("No recipes are added; original progression remains untouched unless you use it.")
                    .withStyle(ChatFormatting.GRAY));
        }

        WandComponentData.rodFromComponent(stack).ifPresent(rod -> appendRodTooltip(tooltip, rod));
        WandComponentData.capFromComponent(stack).ifPresent(cap -> appendCapTooltip(tooltip, cap, false));
        inertCap().ifPresent(cap -> appendCapTooltip(tooltip, cap, true));

        // Source/debug metadata is useful for porting, but it should not clutter
        // normal survival tooltips. Hold F3+H to see it through advanced tooltips.
        if (flag.isAdvanced()) {
            tooltip.add(Component.literal("TC4 1.7.10 source: " + originalSource).withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(Component.literal("Legacy sprite: " + legacyTexture).withStyle(ChatFormatting.DARK_GRAY));
        }
        TC4RunicArmorHelper.appendTooltip(stack, tooltip);
    }

    private void appendRodTooltip(List<Component> tooltip, WandRodType rod) {
        tooltip.add(Component.literal((rod.staff() ? "Staff rod" : "Wand rod")
                + " • " + rod.baseCapacity() + " vis per primal aspect")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Assembly cost: " + rod.craftCost() + " of each primal vis • Research: " + rod.researchKey())
                .withStyle(ChatFormatting.GRAY));
        if (rod.regeneratesAllPrimals()) {
            tooltip.add(Component.literal("Regenerates every primal aspect up to 10% capacity.")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        } else if (rod.regeneratedAspect() != null) {
            tooltip.add(Component.literal("Regenerates " + rod.regeneratedAspect().displayName() + " up to 10% capacity.")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    private void appendCapTooltip(List<Component> tooltip, WandCapType cap, boolean inert) {
        if (inert) {
            tooltip.add(Component.literal("Inert wand cap • activate it through the original infusion recipe.")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.add(Component.literal("Wand cap • base vis cost " + Math.round(cap.visCostModifier() * 100.0F) + "%")
                    .withStyle(ChatFormatting.GOLD));
        }
        tooltip.add(Component.literal("Assembly cost: " + cap.craftCost() + " of each primal vis • Research: " + cap.researchKey())
                .withStyle(ChatFormatting.GRAY));
        if (!inert && cap == WandCapType.COPPER) {
            tooltip.add(Component.literal("Ordo and Perditio cost 100% instead of 110%.")
                    .withStyle(ChatFormatting.AQUA));
        } else if (!inert && cap == WandCapType.SILVER) {
            tooltip.add(Component.literal("Aer, Terra, Ignis and Aqua cost 95%.")
                    .withStyle(ChatFormatting.AQUA));
        }
    }

    private java.util.Optional<WandCapType> inertCap() {
        return switch (legacyTexture) {
            case "wand_cap_silver_inert" -> java.util.Optional.of(WandCapType.SILVER);
            case "wand_cap_thaumium_inert" -> java.util.Optional.of(WandCapType.THAUMIUM);
            case "wand_cap_void_inert" -> java.util.Optional.of(WandCapType.VOID);
            default -> java.util.Optional.empty();
        };
    }
}
