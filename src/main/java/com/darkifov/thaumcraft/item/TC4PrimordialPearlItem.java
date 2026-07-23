package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Full ItemEldritchObject metadata-3 behavior from TC4 4.2.3.5. */
public final class TC4PrimordialPearlItem extends Item {
    public TC4PrimordialPearlItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!(level.getBlockEntity(pos) instanceof AuraNodeBlockEntity node)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack pearl = context.getItemInHand();
        // TC4 decremented the metadata-3 stack unconditionally, including creative players.
        pearl.shrink(1);
        boolean researched = context.getPlayer() != null
                && PlayerThaumData.hasResearch(context.getPlayer(), "PRIMNODE");
        node.applyPrimordialPearl(researched, level.random);

        level.explode(null, pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D,
                3.0F + level.random.nextFloat() * (researched ? 3.0F : 5.0F),
                Explosion.BlockInteraction.BREAK);
        for (int attempt = 0; attempt < 33; attempt++) {
            BlockPos fluxPos = pos.offset(
                    level.random.nextInt(6) - level.random.nextInt(6),
                    level.random.nextInt(6) - level.random.nextInt(6),
                    level.random.nextInt(6) - level.random.nextInt(6));
            if (level.getBlockState(fluxPos).isAir()) {
                level.setBlock(fluxPos, (fluxPos.getY() < pos.getY()
                        ? ThaumcraftMod.FLUX_GOO.get()
                        : ThaumcraftMod.FLUX_GAS.get()).defaultBlockState(), 3);
            }
        }
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.PORTAL, pos.getX() + 0.5D, pos.getY() + 0.5D,
                    pos.getZ() + 0.5D, 33, 1.25D, 1.25D, 1.25D, 0.08D);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemEldritchObject.text.5")
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("item.ItemEldritchObject.text.6")
                .withStyle(ChatFormatting.DARK_PURPLE));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
