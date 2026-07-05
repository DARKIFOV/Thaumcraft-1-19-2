package com.darkifov.thaumcraft.infusion;

import com.darkifov.thaumcraft.Aspect;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class InfusionRecipe {
    private final ResourceLocation id;
    private final ResourceLocation catalystId;
    private final ResourceLocation resultItemId;
    private final int resultCount;
    private final int instability;
    private final String research;
    private final List<ResourceLocation> components = new ArrayList<>();
    private final EnumMap<Aspect, Integer> aspectCost = new EnumMap<>(Aspect.class);

    public InfusionRecipe(ResourceLocation id, ResourceLocation catalystId, ResourceLocation resultItemId, int resultCount, int instability) {
        this(id, catalystId, resultItemId, resultCount, instability, "");
    }

    public InfusionRecipe(ResourceLocation id, ResourceLocation catalystId, ResourceLocation resultItemId, int resultCount, int instability, String research) {
        this.id = id;
        this.catalystId = catalystId;
        this.resultItemId = resultItemId;
        this.resultCount = Math.max(1, resultCount);
        this.instability = Math.max(0, instability);
        this.research = research == null ? "" : research;
    }

    public ResourceLocation id() {
        return id;
    }

    public ResourceLocation catalystId() {
        return catalystId;
    }

    public ResourceLocation resultItemId() {
        return resultItemId;
    }

    public int resultCount() {
        return resultCount;
    }

    public int instability() {
        return instability;
    }

    public String research() {
        return research;
    }

    public List<ResourceLocation> components() {
        return components;
    }

    public EnumMap<Aspect, Integer> aspectCost() {
        return aspectCost;
    }

    public InfusionRecipe component(ResourceLocation id) {
        components.add(id);
        return this;
    }

    public InfusionRecipe require(Aspect aspect, int amount) {
        if (aspect != null && amount > 0) {
            aspectCost.put(aspect, amount);
        }

        return this;
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

    public static InfusionRecipe fromJson(ResourceLocation id, JsonObject json) {
        ResourceLocation catalyst = new ResourceLocation(json.get("catalyst").getAsString());

        JsonObject resultObject = json.getAsJsonObject("result");
        ResourceLocation resultItem = new ResourceLocation(resultObject.get("item").getAsString());
        int count = resultObject.has("count") ? resultObject.get("count").getAsInt() : 1;
        int instability = json.has("instability") ? json.get("instability").getAsInt() : 0;
        String research = json.has("research") ? json.get("research").getAsString() : "";

        InfusionRecipe recipe = new InfusionRecipe(id, catalyst, resultItem, count, instability, research);

        JsonArray components = json.getAsJsonArray("components");
        for (JsonElement element : components) {
            recipe.component(new ResourceLocation(element.getAsString()));
        }

        JsonObject aspects = json.getAsJsonObject("aspects");
        for (Map.Entry<String, JsonElement> entry : aspects.entrySet()) {
            try {
                Aspect aspect = Aspect.valueOf(entry.getKey().toUpperCase());
                recipe.require(aspect, entry.getValue().getAsInt());
            } catch (IllegalArgumentException ignored) {
            }
        }

        return recipe;
    }
}
