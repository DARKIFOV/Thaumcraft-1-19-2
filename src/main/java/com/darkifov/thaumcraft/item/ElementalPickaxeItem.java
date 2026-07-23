package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.client.ElementalPickScanClient;
import com.darkifov.thaumcraft.item.gear.TC4ElementalToolTier;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/** Forge 1.19.2 port of TC4's Pickaxe of the Core. */
public final class ElementalPickaxeItem extends PickaxeItem {
    public static final int SCAN_RADIUS = 8;
    public static final int SCAN_DURATION_TICKS = 100;

    public ElementalPickaxeItem(Properties properties) {
        super(TC4ElementalToolTier.INSTANCE, 1, -2.8F, properties.stacksTo(1));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.RARE;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity target) {
        if (!player.level.isClientSide) {
            MinecraftServer server = player.getServer();
            if (!(target instanceof Player) || server == null || server.isPvpAllowed()) {
                target.setSecondsOnFire(2);
            }
        }
        return super.onLeftClickEntity(stack, player, target);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) {
            return super.useOn(context);
        }

        var level = context.getLevel();
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ElementalPickScanClient.start(context.getClickedPos(), SCAN_RADIUS, SCAN_DURATION_TICKS));
        } else {
            stackDamage(context.getItemInHand(), player, context);
            level.playSound(null, context.getClickedPos(), TC4Sounds.event("wandfail"), SoundSource.PLAYERS,
                    0.2F, 0.2F + level.random.nextFloat() * 0.2F);
        }
        player.swing(context.getHand());
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void stackDamage(ItemStack stack, Player player, UseOnContext context) {
        stack.hurtAndBreak(5, player, p -> p.broadcastBreakEvent(context.getHand()));
    }
}
