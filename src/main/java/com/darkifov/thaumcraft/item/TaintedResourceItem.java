package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** TC4 ItemResource metadata 11/12 inventory infection behavior. */
public final class TaintedResourceItem extends TC4ResearchComponentItem {
    public TaintedResourceItem(Properties properties, String originalSource, String legacyTexture) {
        super(properties, originalSource, legacyTexture);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide || !(entity instanceof LivingEntity living)
                || living.isInvertedHealAndHarm()
                || living.hasEffect(ThaumcraftMod.TAINT_POISON.get())) {
            return;
        }

        // Original ItemResource used nextInt(4321) <= stackSize.
        if (level.random.nextInt(4321) > stack.getCount()) return;

        living.addEffect(new MobEffectInstance(ThaumcraftMod.TAINT_POISON.get(), 120, 0, false, true, true));
        if (living instanceof Player player) {
            Component itemName = stack.getHoverName().copy()
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC);
            player.displayClientMessage(Component.translatable("tc.taint_item_poison", itemName), false);
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
    }
}
