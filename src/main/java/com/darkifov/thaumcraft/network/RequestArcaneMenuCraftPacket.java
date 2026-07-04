package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.arcane.ArcaneWorkbenchRecipe;
import com.darkifov.thaumcraft.arcane.ArcaneWorkbenchRecipes;
import com.darkifov.thaumcraft.blockentity.ArcaneWorkbenchBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestArcaneMenuCraftPacket {
    private final BlockPos pos;
    private final ResourceLocation recipeId;

    public RequestArcaneMenuCraftPacket(BlockPos pos, ResourceLocation recipeId) {
        this.pos = pos;
        this.recipeId = recipeId;
    }

    public static void encode(RequestArcaneMenuCraftPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeResourceLocation(packet.recipeId);
    }

    public static RequestArcaneMenuCraftPacket decode(FriendlyByteBuf buffer) {
        return new RequestArcaneMenuCraftPacket(buffer.readBlockPos(), buffer.readResourceLocation());
    }

    public static void handle(RequestArcaneMenuCraftPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null) {
                return;
            }

            if (!player.blockPosition().closerThan(packet.pos, 8.0D)) {
                player.displayClientMessage(Component.literal("Too far away from Arcane Workbench.").withStyle(ChatFormatting.RED), false);
                return;
            }

            BlockEntity blockEntity = player.level.getBlockEntity(packet.pos);

            if (!(blockEntity instanceof ArcaneWorkbenchBlockEntity workbench)) {
                player.displayClientMessage(Component.literal("Arcane Workbench is missing.").withStyle(ChatFormatting.RED), false);
                return;
            }

            ArcaneWorkbenchRecipe recipe = ArcaneWorkbenchRecipes.findById(packet.recipeId);

            if (recipe == null) {
                player.displayClientMessage(Component.literal("Arcane recipe not found: " + packet.recipeId).withStyle(ChatFormatting.RED), false);
                return;
            }

            workbench.tryCraft(recipe, player);
        });

        context.setPacketHandled(true);
    }
}
