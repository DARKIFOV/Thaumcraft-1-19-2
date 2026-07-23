package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.blockentity.AlembicBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaReservoirBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaTubeBlockEntity;
import com.darkifov.thaumcraft.block.VoidEssentiaJarBlock;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

/** TC4 Essentia Resonator readout for the port's jars, reservoirs, alembics and tube network. */
public final class TC4ResonatorItem extends Item {
    public TC4ResonatorItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        Reading reading = read(blockEntity, context.getClickedFace());
        if (reading == null) return InteractionResult.PASS;
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        if (reading.buffer != null && !reading.buffer.isEmpty()) {
            for (Aspect aspect : reading.buffer.entries().keySet()) {
                sendEssentia(player, reading.buffer.get(aspect), aspect);
            }
        } else if (reading.aspect != null && reading.amount > 0) {
            sendEssentia(player, reading.amount, reading.aspect);
        } else {
            player.sendSystemMessage(Component.translatable("message.thaumcraft.resonator.empty")
                    .withStyle(ChatFormatting.GRAY));
        }

        String suctionName = reading.suctionAspect == null
                ? Component.translatable("message.thaumcraft.resonator.untyped").getString()
                : reading.suctionAspect.displayName();
        player.sendSystemMessage(Component.translatable("message.thaumcraft.resonator.suction",
                reading.suction, suctionName).withStyle(ChatFormatting.DARK_AQUA));
        context.getLevel().playSound(null, context.getClickedPos(), TC4Sounds.event("alembicknock"),
                SoundSource.BLOCKS, 0.5F, 1.9F + context.getLevel().random.nextFloat() * 0.1F);
        return InteractionResult.CONSUME;
    }

    private static void sendEssentia(Player player, int amount, Aspect aspect) {
        player.sendSystemMessage(Component.translatable("message.thaumcraft.resonator.essentia",
                amount, aspect.displayName()).withStyle(aspect.color()));
    }

    private static Reading read(BlockEntity blockEntity, Direction clickedFace) {
        if (blockEntity instanceof EssentiaTubeBlockEntity tube) {
            AspectList buffer = tube.bufferAmount() > 0 ? new AspectList().add(tube.bufferAspect(), tube.bufferAmount()) : null;
            return new Reading(tube.getEssentiaType(clickedFace), tube.getEssentiaAmount(clickedFace),
                    tube.getSuctionType(clickedFace), tube.getSuctionAmount(clickedFace), buffer);
        }
        if (blockEntity instanceof EssentiaJarBlockEntity jar) {
            Aspect aspect = jar.storedAspect();
            return new Reading(aspect, jar.amount(), aspect, jar.originalSuctionAmount(jar.getBlockState().getBlock() instanceof VoidEssentiaJarBlock), null);
        }
        if (blockEntity instanceof EssentiaReservoirBlockEntity reservoir) {
            Aspect aspect = reservoir.firstAspect();
            return new Reading(aspect, reservoir.amount(), aspect, reservoir.originalSuctionAmount(aspect), reservoir.aspects());
        }
        if (blockEntity instanceof AlembicBlockEntity alembic) {
            Aspect aspect = alembic.storedAspect();
            return new Reading(aspect, aspect == null ? 0 : alembic.aspects().get(aspect), null, 0, alembic.aspects());
        }
        return null;
    }

    private record Reading(Aspect aspect, int amount, Aspect suctionAspect, int suction, AspectList buffer) { }
}
