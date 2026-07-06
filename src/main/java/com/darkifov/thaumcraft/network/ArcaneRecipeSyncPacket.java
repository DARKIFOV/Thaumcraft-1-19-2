package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.arcane.ArcaneWorkbenchRecipe;
import com.darkifov.thaumcraft.client.arcane.ClientArcaneRecipePage;
import com.darkifov.thaumcraft.client.arcane.ClientSyncedArcaneRecipes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ArcaneRecipeSyncPacket {
    private final List<Entry> entries;

    public ArcaneRecipeSyncPacket(List<Entry> entries) {
        this.entries = entries;
    }

    public static ArcaneRecipeSyncPacket fromRecipes(List<ArcaneWorkbenchRecipe> recipes) {
        List<Entry> entries = new ArrayList<>();

        for (ArcaneWorkbenchRecipe recipe : recipes) {
            entries.add(new Entry(
                    recipe.id(),
                    recipe.research(),
                    recipe.catalystItemId(),
                    recipe.ingredients(),
                    recipe.resultItemId(),
                    recipe.resultCount(),
                    recipe.aspectCostText(),
                    recipe.pattern(),
                    recipe.tc4Kind(),
                    recipe.tc4Key()
            ));
        }

        return new ArcaneRecipeSyncPacket(entries);
    }

    public static void encode(ArcaneRecipeSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.entries.size());

        for (Entry entry : packet.entries) {
            buffer.writeResourceLocation(entry.id);
            buffer.writeUtf(entry.research);
            buffer.writeResourceLocation(entry.catalystId);
            buffer.writeInt(entry.ingredients.size());

            for (ResourceLocation ingredient : entry.ingredients) {
                buffer.writeResourceLocation(ingredient);
            }

            buffer.writeResourceLocation(entry.resultId);
            buffer.writeInt(entry.resultCount);
            buffer.writeUtf(entry.visCost);
            buffer.writeInt(entry.patternRows.size());
            for (String row : entry.patternRows) {
                buffer.writeUtf(row);
            }
            buffer.writeUtf(entry.tc4Kind);
            buffer.writeUtf(entry.tc4Key);
        }
    }

    public static ArcaneRecipeSyncPacket decode(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            ResourceLocation id = buffer.readResourceLocation();
            String research = buffer.readUtf();
            ResourceLocation catalystId = buffer.readResourceLocation();
            int ingredientCount = buffer.readInt();
            List<ResourceLocation> ingredients = new ArrayList<>();

            for (int j = 0; j < ingredientCount; j++) {
                ingredients.add(buffer.readResourceLocation());
            }

            ResourceLocation resultId = buffer.readResourceLocation();
            int resultCount = buffer.readInt();
            String visCost = buffer.readUtf();
            int patternCount = buffer.readInt();
            List<String> patternRows = new ArrayList<>();
            for (int j = 0; j < patternCount; j++) {
                patternRows.add(buffer.readUtf());
            }
            String tc4Kind = buffer.readUtf();
            String tc4Key = buffer.readUtf();

            entries.add(new Entry(id, research, catalystId, ingredients, resultId, resultCount, visCost, patternRows, tc4Kind, tc4Key));
        }

        return new ArcaneRecipeSyncPacket(entries);
    }

    public static void handle(ArcaneRecipeSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> applyClient(packet.entries))
        );

        context.setPacketHandled(true);
    }

    private static void applyClient(List<Entry> entries) {
        List<ClientArcaneRecipePage> pages = new ArrayList<>();

        for (Entry entry : entries) {
            String title = displayName(entry.resultId);
            String catalyst = displayName(entry.catalystId);
            String[] ingredients = new String[entry.ingredients.size()];

            for (int i = 0; i < entry.ingredients.size(); i++) {
                ingredients[i] = displayName(entry.ingredients.get(i));
            }

            String result = displayName(entry.resultId) + (entry.resultCount > 1 ? " x" + entry.resultCount : "");

            String[] ingredientIds = new String[entry.ingredients.size()];

            for (int i = 0; i < entry.ingredients.size(); i++) {
                ingredientIds[i] = entry.ingredients.get(i).toString();
            }

            pages.add(new ClientArcaneRecipePage(
                    entry.id.toString(),
                    title,
                    entry.research,
                    catalyst,
                    entry.catalystId.toString(),
                    ingredients,
                    ingredientIds,
                    result,
                    entry.resultId.toString(),
                    entry.visCost,
                    "Synced from server JSON arcane recipe" + (entry.tc4Key.isBlank() ? "." : ": " + entry.tc4Key),
                    entry.patternRows.toArray(new String[0]),
                    entry.tc4Kind,
                    entry.tc4Key
            ));
        }

        ClientSyncedArcaneRecipes.set(pages);
    }

    private static String displayName(ResourceLocation id) {
        Item item = ForgeRegistries.ITEMS.getValue(id);

        if (item == null) {
            return id.toString();
        }

        return new ItemStack(item).getHoverName().getString();
    }

    public record Entry(
            ResourceLocation id,
            String research,
            ResourceLocation catalystId,
            List<ResourceLocation> ingredients,
            ResourceLocation resultId,
            int resultCount,
            String visCost,
            List<String> patternRows,
            String tc4Kind,
            String tc4Key
    ) {
    }
}
