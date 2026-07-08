package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.infusion.TC4RunicArmorHelper;
import com.darkifov.thaumcraft.eldritch.TC4OuterLandsLootAdapter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
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

    private int lootbagRarity() {
        if ("lootbag".equals(legacyTexture)) return 0;
        if ("lootbagunc".equals(legacyTexture)) return 1;
        if ("lootbagrare".equals(legacyTexture)) return 2;
        return -1;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("TC4 1.7.10 source: " + originalSource).withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.literal("Legacy sprite: " + legacyTexture).withStyle(ChatFormatting.DARK_GRAY));
        TC4RunicArmorHelper.appendTooltip(stack, tooltip);
    }
}
