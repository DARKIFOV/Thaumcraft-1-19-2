package com.darkifov.thaumcraft.alchemy;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.EnumMap;
import java.util.Map;

public class AlchemyRecipe {
    private static final String TAG_SENTINEL_NAMESPACE = "tc4_tag";
    private final ResourceLocation id;
    private final ResourceLocation catalystItemId;
    private final ResourceLocation resultItemId;
    private final int resultCount;
    private final String research;
    private final String tc4Key;
    private final String tc4Kind;
    private final EnumMap<Aspect, Integer> cost = new EnumMap<>(Aspect.class);

    public AlchemyRecipe(ResourceLocation id, ResourceLocation catalystItemId, ResourceLocation resultItemId, int resultCount) {
        this(id, catalystItemId, resultItemId, resultCount, "", "", "");
    }

    private AlchemyRecipe(ResourceLocation id, ResourceLocation catalystItemId, ResourceLocation resultItemId, int resultCount, String research, String tc4Key, String tc4Kind) {
        this.id = id;
        this.catalystItemId = catalystItemId;
        this.resultItemId = resultItemId;
        this.resultCount = Math.max(1, resultCount);
        this.research = research == null ? "" : research;
        this.tc4Key = tc4Key == null ? "" : tc4Key;
        this.tc4Kind = tc4Kind == null ? "" : tc4Kind;
    }

    public ResourceLocation id() {
        return id;
    }

    public ResourceLocation catalystItemId() {
        return catalystItemId;
    }

    /**
     * Returns the real Minecraft ingredient represented by the TC4 catalyst.
     * Legacy ore-dictionary catalysts are carried as a private sentinel
     * ResourceLocation and resolved to a modern item tag here.
     */
    public Ingredient catalystIngredient() {
        if (isTagIngredient(catalystItemId)) {
            ResourceLocation tagId = tagIngredientId(catalystItemId);
            return tagId == null ? Ingredient.EMPTY : Ingredient.of(TagKey.create(Registry.ITEM_REGISTRY, tagId));
        }
        Item item = ForgeRegistries.ITEMS.getValue(catalystItemId);
        return item == null ? Ingredient.EMPTY : Ingredient.of(item);
    }

    public String catalystText() {
        ResourceLocation tagId = tagIngredientId(catalystItemId);
        return tagId == null ? String.valueOf(catalystItemId) : "#" + tagId;
    }

    public ResourceLocation resultItemId() {
        return resultItemId;
    }

    public int resultCount() {
        return resultCount;
    }

    public String research() {
        return research;
    }

    public String tc4Key() {
        return tc4Key;
    }

    public String tc4Kind() {
        return tc4Kind;
    }

    public EnumMap<Aspect, Integer> cost() {
        return cost;
    }

    public AlchemyRecipe require(Aspect aspect, int amount) {
        if (aspect != null && amount > 0) {
            cost.put(aspect, amount);
        }

        return this;
    }

    public boolean catalystMatches(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        if (isTagIngredient(catalystItemId)) {
            ResourceLocation tagId = tagIngredientId(catalystItemId);
            return tagId != null && stack.is(TagKey.create(Registry.ITEM_REGISTRY, tagId));
        }

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && id.equals(catalystItemId);
    }

    public boolean canCraft(ItemStack catalyst, AspectList aspects) {
        if (!catalystMatches(catalyst)) {
            return false;
        }

        for (Map.Entry<Aspect, Integer> entry : cost.entrySet()) {
            if (aspects.get(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }

        return true;
    }

    public ItemStack craft(ItemStack catalyst, AspectList aspects) {
        if (!canCraft(catalyst, aspects)) {
            return ItemStack.EMPTY;
        }

        Item item = ForgeRegistries.ITEMS.getValue(resultItemId);

        if (item == null) {
            return ItemStack.EMPTY;
        }

        for (Map.Entry<Aspect, Integer> entry : cost.entrySet()) {
            aspects.remove(entry.getKey(), entry.getValue());
        }

        return new ItemStack(item, resultCount);
    }

    public String costText() {
        if (cost.isEmpty()) {
            return "none";
        }

        StringBuilder builder = new StringBuilder();
        boolean first = true;

        for (Map.Entry<Aspect, Integer> entry : cost.entrySet()) {
            if (!first) {
                builder.append(", ");
            }

            builder.append(entry.getKey().displayName()).append(" ").append(entry.getValue());
            first = false;
        }

        return builder.toString();
    }

    public static AlchemyRecipe fromJson(ResourceLocation id, JsonObject json) {
        ResourceLocation catalyst = json.has("catalyst")
                ? parseIngredient(json.get("catalyst"))
                : new ResourceLocation("minecraft", "glass_bottle");

        JsonObject resultObject = json.getAsJsonObject("result");
        ResourceLocation resultItem = new ResourceLocation(resultObject.get("item").getAsString());
        int count = resultObject.has("count") ? resultObject.get("count").getAsInt() : 1;
        String research = json.has("research") ? json.get("research").getAsString() : "";
        String tc4Key = json.has("tc4_key") ? json.get("tc4_key").getAsString() : "";
        String tc4Kind = json.has("tc4_kind") ? json.get("tc4_kind").getAsString() : (json.has("kind") ? json.get("kind").getAsString() : "");

        AlchemyRecipe recipe = new AlchemyRecipe(id, catalyst, resultItem, count, research, tc4Key, tc4Kind);

        JsonObject aspects = json.getAsJsonObject("aspects");

        for (Map.Entry<String, JsonElement> entry : aspects.entrySet()) {
            try {
                Aspect aspect = Aspect.valueOf(entry.getKey().toUpperCase());
                int amount = entry.getValue().getAsInt();
                recipe.require(aspect, amount);
            } catch (IllegalArgumentException ignored) {
            }
        }

        return recipe;
    }
    private static ResourceLocation parseIngredient(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return new ResourceLocation("minecraft", "glass_bottle");
        }
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("tag")) {
                return tagIngredient(new ResourceLocation(object.get("tag").getAsString()));
            }
            if (object.has("item")) {
                return new ResourceLocation(object.get("item").getAsString());
            }
            throw new IllegalArgumentException("Alchemy catalyst object requires 'item' or 'tag'");
        }

        String value = element.getAsString();
        if (value.startsWith("#")) {
            return tagIngredient(new ResourceLocation(value.substring(1)));
        }
        return new ResourceLocation(value);
    }

    private static ResourceLocation tagIngredient(ResourceLocation tagId) {
        return new ResourceLocation(TAG_SENTINEL_NAMESPACE, tagId.getNamespace() + "/" + tagId.getPath());
    }

    private static boolean isTagIngredient(ResourceLocation ingredient) {
        return ingredient != null && TAG_SENTINEL_NAMESPACE.equals(ingredient.getNamespace());
    }

    private static ResourceLocation tagIngredientId(ResourceLocation ingredient) {
        if (!isTagIngredient(ingredient)) {
            return null;
        }
        String path = ingredient.getPath();
        int separator = path.indexOf('/');
        if (separator <= 0 || separator == path.length() - 1) {
            return null;
        }
        return new ResourceLocation(path.substring(0, separator), path.substring(separator + 1));
    }

}
