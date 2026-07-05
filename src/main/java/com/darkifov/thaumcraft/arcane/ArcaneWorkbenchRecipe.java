package com.darkifov.thaumcraft.arcane;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class ArcaneWorkbenchRecipe {
    private final ResourceLocation id;
    private final ResourceLocation catalystId;
    private final ResourceLocation resultItemId;
    private final int resultCount;
    private final String research;
    private final List<ResourceLocation> ingredients = new ArrayList<>();

    public ArcaneWorkbenchRecipe(ResourceLocation id, ResourceLocation catalystId, ResourceLocation resultItemId, int resultCount, String research) {
        this.id = id;
        this.catalystId = catalystId;
        this.resultItemId = resultItemId;
        this.resultCount = Math.max(1, resultCount);
        this.research = research == null ? "" : research;
    }

    public ResourceLocation id() {
        return id;
    }

    public String research() {
        return research;
    }

    public ResourceLocation catalystItemId() {
        return catalystId;
    }

    public ResourceLocation resultItemId() {
        return resultItemId;
    }

    public int resultCount() {
        return resultCount;
    }

    public List<ResourceLocation> ingredients() {
        return ingredients;
    }

    public boolean catalystMatches(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && id.equals(catalystId);
    }

    public ItemStack result() {
        Item item = ForgeRegistries.ITEMS.getValue(resultItemId);

        if (item == null) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(item, resultCount);
    }

    public ArcaneWorkbenchRecipe ingredient(ResourceLocation id) {
        ingredients.add(id);
        return this;
    }

    public static ArcaneWorkbenchRecipe fromJson(ResourceLocation id, JsonObject json) {
        ResourceLocation catalyst = new ResourceLocation(json.get("catalyst").getAsString());

        JsonObject resultObject = json.getAsJsonObject("result");
        ResourceLocation resultItem = new ResourceLocation(resultObject.get("item").getAsString());
        int count = resultObject.has("count") ? resultObject.get("count").getAsInt() : 1;
        String research = json.has("research") ? json.get("research").getAsString() : "";

        ArcaneWorkbenchRecipe recipe = new ArcaneWorkbenchRecipe(id, catalyst, resultItem, count, research);

        JsonArray ingredients = json.getAsJsonArray("ingredients");

        for (JsonElement element : ingredients) {
            recipe.ingredient(new ResourceLocation(element.getAsString()));
        }

        return recipe;
    }
}
