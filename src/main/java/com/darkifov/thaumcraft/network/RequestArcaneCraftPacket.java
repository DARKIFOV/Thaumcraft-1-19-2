package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.arcane.ArcaneWorkbenchRecipe;
import com.darkifov.thaumcraft.arcane.ArcaneWorkbenchRecipes;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class RequestArcaneCraftPacket {
    private final ResourceLocation recipeId;

    public RequestArcaneCraftPacket(ResourceLocation recipeId) {
        this.recipeId = recipeId;
    }

    public static void encode(RequestArcaneCraftPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.recipeId);
    }

    public static RequestArcaneCraftPacket decode(FriendlyByteBuf buffer) {
        return new RequestArcaneCraftPacket(buffer.readResourceLocation());
    }

    public static void handle(RequestArcaneCraftPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null) {
                return;
            }

            ArcaneWorkbenchRecipe recipe = ArcaneWorkbenchRecipes.findById(packet.recipeId);

            if (recipe == null) {
                player.displayClientMessage(Component.literal("Arcane recipe not found: " + packet.recipeId).withStyle(ChatFormatting.RED), false);
                return;
            }

            if (!PlayerThaumData.hasResearch(player, recipe.research())) {
                player.displayClientMessage(Component.literal("Research locked: " + recipe.research()).withStyle(ChatFormatting.RED), false);
                return;
            }

            if (!hasCatalyst(player.getInventory(), recipe)) {
                player.displayClientMessage(Component.literal("Missing catalyst for recipe.").withStyle(ChatFormatting.RED), false);
                return;
            }

            if (!hasIngredients(player.getInventory(), recipe.ingredients())) {
                player.displayClientMessage(Component.literal("Missing ingredients.").withStyle(ChatFormatting.RED), false);
                return;
            }

            if (!WandItem.consumeVisFromInventory(player, Aspect.ORDO, 2)) {
                player.displayClientMessage(Component.literal("Arcane crafting needs Ordo 2 vis in any wand in your inventory.").withStyle(ChatFormatting.RED), false);
                return;
            }

            ItemStack result = recipe.result();

            if (result.isEmpty()) {
                player.displayClientMessage(Component.literal("Recipe result item is missing.").withStyle(ChatFormatting.RED), false);
                return;
            }

            if (!player.getAbilities().instabuild) {
                consumeCatalyst(player.getInventory(), recipe);
                consumeIngredients(player.getInventory(), recipe.ingredients());
            }

            if (!player.getInventory().add(result.copy())) {
                Containers.dropItemStack(player.level, player.getX(), player.getY(), player.getZ(), result.copy());
            }

            player.displayClientMessage(Component.literal("Arcane crafting complete: ").append(result.getHoverName()).withStyle(ChatFormatting.GOLD), false);
        });

        context.setPacketHandled(true);
    }

    private static boolean hasCatalyst(Inventory inventory, ArcaneWorkbenchRecipe recipe) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (recipe.catalystMatches(inventory.getItem(i))) {
                return true;
            }
        }

        return false;
    }

    private static void consumeCatalyst(Inventory inventory, ArcaneWorkbenchRecipe recipe) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            if (recipe.catalystMatches(stack)) {
                stack.shrink(1);
                return;
            }
        }
    }

    private static boolean hasIngredients(Inventory inventory, List<ResourceLocation> ingredients) {
        Map<ResourceLocation, Integer> needed = new HashMap<>();

        for (ResourceLocation id : ingredients) {
            needed.put(id, needed.getOrDefault(id, 0) + 1);
        }

        Map<ResourceLocation, Integer> available = new HashMap<>();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());

            if (id != null && needed.containsKey(id)) {
                available.put(id, available.getOrDefault(id, 0) + stack.getCount());
            }
        }

        for (Map.Entry<ResourceLocation, Integer> entry : needed.entrySet()) {
            if (available.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return false;
            }
        }

        return true;
    }

    private static void consumeIngredients(Inventory inventory, List<ResourceLocation> ingredients) {
        for (ResourceLocation needed : ingredients) {
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());

                if (id != null && id.equals(needed)) {
                    stack.shrink(1);
                    break;
                }
            }
        }
    }
}
