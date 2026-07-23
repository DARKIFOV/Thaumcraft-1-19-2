package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.eldritch.TC4OuterLandsLootAdapter;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Exact de-metadata carrier for TC4 ItemLootBag meta 0..2. */
public final class TC4LootBagItem extends Item {
    private final int lootRarity;

    public TC4LootBagItem(Properties properties, int lootRarity) {
        super(properties.stacksTo(16).rarity(switch (Math.max(0, Math.min(2, lootRarity))) {
            case 1 -> Rarity.UNCOMMON;
            case 2 -> Rarity.RARE;
            default -> Rarity.COMMON;
        }));
        this.lootRarity = Math.max(0, Math.min(2, lootRarity));
    }

    public int lootRarity() {
        return lootRarity;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            for (ItemStack loot : TC4OuterLandsLootAdapter.openLootBag(lootRarity, level.getRandom())) {
                ItemEntity entity = new ItemEntity(serverLevel, player.getX(), player.getY(), player.getZ(), loot.copy());
                entity.setDefaultPickUpDelay();
                serverLevel.addFreshEntity(entity);
            }
            serverLevel.playSound(null, player.blockPosition(), TC4Sounds.event("coins"),
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.75F, 1.0F);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tc.lootbag").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
