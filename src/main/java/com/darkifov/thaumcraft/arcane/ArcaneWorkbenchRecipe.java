package com.darkifov.thaumcraft.arcane;

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
import java.util.LinkedHashMap;
import java.util.Collections;

public class ArcaneWorkbenchRecipe {
    private final ResourceLocation id;
    private final ResourceLocation catalystId;
    private final ResourceLocation resultItemId;
    private final int resultCount;
    private final String research;
    private final String tc4Key;
    private final String tc4Kind;
    private final List<String> pattern = new ArrayList<>();
    private final List<ResourceLocation> ingredients = new ArrayList<>();
    private final EnumMap<Aspect, Integer> aspectCost = new EnumMap<>(Aspect.class);
    private final Map<Character, ResourceLocation> explicitPatternMap = new LinkedHashMap<>();

    public ArcaneWorkbenchRecipe(ResourceLocation id, ResourceLocation catalystId, ResourceLocation resultItemId, int resultCount, String research) {
        this.id = id;
        this.catalystId = catalystId;
        this.resultItemId = resultItemId;
        this.resultCount = Math.max(1, resultCount);
        this.research = research == null ? "" : research;
        this.tc4Key = "";
        this.tc4Kind = "";
    }

    private ArcaneWorkbenchRecipe(ResourceLocation id, ResourceLocation catalystId, ResourceLocation resultItemId, int resultCount, String research, String tc4Key, String tc4Kind) {
        this.id = id;
        this.catalystId = catalystId;
        this.resultItemId = resultItemId;
        this.resultCount = Math.max(1, resultCount);
        this.research = research == null ? "" : research;
        this.tc4Key = tc4Key == null ? "" : tc4Key;
        this.tc4Kind = tc4Kind == null ? "" : tc4Kind;
    }

    public ResourceLocation id() {
        return id;
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

    public List<String> pattern() {
        return Collections.unmodifiableList(pattern);
    }

    public String patternText() {
        return pattern.isEmpty() ? "" : String.join(" / ", pattern);
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

    public EnumMap<Aspect, Integer> aspectCost() {
        return aspectCost;
    }

    public ArcaneWorkbenchRecipe require(Aspect aspect, int amount) {
        if (aspect != null && amount > 0) {
            aspectCost.put(aspect, amount);
        }
        return this;
    }


    /**
     * Stage138: TC4 arcane recipes came from vararg patterns such as
     * {"AAA","ASA","AAA", 'A', shard, 'S', arrow}. The Stage120
     * materializer preserved rows plus catalyst/ingredients, but did not always
     * persist the char-to-stack map. Infer the map deterministically so shaped
     * recipes consume the correct slot counts instead of falling back to a loose
     * one-of-each ingredient check.
     */
    public Map<Character, ResourceLocation> inferredPatternMap() {
        Map<Character, ResourceLocation> result = new LinkedHashMap<>();
        if (!explicitPatternMap.isEmpty()) {
            result.putAll(explicitPatternMap);
            return result;
        }

        List<Character> symbols = patternSymbols();

        if (symbols.isEmpty()) {
            return result;
        }

        Character catalystSymbol = inferredCatalystSymbol(symbols);
        int ingredientIndex = 0;

        for (Character symbol : symbols) {
            if (catalystSymbol != null && symbol.equals(catalystSymbol)) {
                result.put(symbol, catalystId);
                continue;
            }

            if (ingredients.size() == 1) {
                result.put(symbol, ingredients.get(0));
            } else if (ingredientIndex < ingredients.size()) {
                result.put(symbol, ingredients.get(ingredientIndex++));
            }
        }

        return result;
    }


    public Map<Character, ResourceLocation> explicitPatternMap() {
        return Collections.unmodifiableMap(explicitPatternMap);
    }

    public boolean hasExplicitPatternMap() {
        return !explicitPatternMap.isEmpty();
    }

    public String patternMapMode() {
        return hasExplicitPatternMap() ? "exact_tc4_json_key" : "stage139_deterministic_inference";
    }

    public List<Character> patternSymbols() {
        List<Character> symbols = new ArrayList<>();

        for (String row : pattern) {
            for (int i = 0; i < row.length(); i++) {
                char symbol = row.charAt(i);
                if (symbol != ' ' && !symbols.contains(symbol)) {
                    symbols.add(symbol);
                }
            }
        }

        return symbols;
    }

    public Character inferredCatalystSymbol() {
        return inferredCatalystSymbol(patternSymbols());
    }

    private Character inferredCatalystSymbol(List<Character> symbols) {
        if (symbols.isEmpty() || catalystId == null) {
            return null;
        }

        if (ingredients.isEmpty() && symbols.size() == 1) {
            return symbols.get(0);
        }

        if (symbols.size() == ingredients.size() + 1 || (ingredients.size() == 1 && symbols.size() == 2)) {
            Character center = centerPatternSymbol();
            if (center != null && countSymbol(center) == 1) {
                return center;
            }

            Character rarest = null;
            int rarestCount = Integer.MAX_VALUE;
            for (Character symbol : symbols) {
                int count = countSymbol(symbol);
                if (count < rarestCount) {
                    rarest = symbol;
                    rarestCount = count;
                }
            }
            return rarest;
        }

        return null;
    }

    private Character centerPatternSymbol() {
        if (pattern.size() < 2 || pattern.get(1).length() < 2) {
            return null;
        }
        char symbol = pattern.get(1).charAt(1);
        return symbol == ' ' ? null : symbol;
    }

    private int countSymbol(char symbol) {
        int count = 0;
        for (String row : pattern) {
            for (int i = 0; i < row.length(); i++) {
                if (row.charAt(i) == symbol) {
                    count++;
                }
            }
        }
        return count;
    }

    public String aspectCostText() {
        if (aspectCost.isEmpty()) {
            return "none";
        }
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<Aspect, Integer> entry : aspectCost.entrySet()) {
            if (!first) {
                builder.append(", ");
            }
            builder.append(entry.getKey().displayName()).append(" ").append(entry.getValue());
            first = false;
        }
        return builder.toString();
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

        String tc4Key = json.has("tc4_key") ? json.get("tc4_key").getAsString() : "";
        String tc4Kind = json.has("tc4_kind") ? json.get("tc4_kind").getAsString() : (json.has("kind") ? json.get("kind").getAsString() : "");
        ArcaneWorkbenchRecipe recipe = new ArcaneWorkbenchRecipe(id, catalyst, resultItem, count, research, tc4Key, tc4Kind);

        JsonArray ingredients = json.getAsJsonArray("ingredients");

        for (JsonElement element : ingredients) {
            recipe.ingredient(new ResourceLocation(element.getAsString()));
        }

        if (json.has("pattern")) {
            JsonArray rows = json.getAsJsonArray("pattern");
            for (JsonElement row : rows) {
                recipe.pattern.add(row.getAsString());
            }
        }

        if (json.has("key")) {
            readPatternKey(recipe, json.getAsJsonObject("key"));
        }
        if (json.has("symbol_map")) {
            readPatternKey(recipe, json.getAsJsonObject("symbol_map"));
        }

        if (json.has("aspects")) {
            JsonObject aspects = json.getAsJsonObject("aspects");
            for (Map.Entry<String, JsonElement> entry : aspects.entrySet()) {
                try {
                    Aspect aspect = Aspect.valueOf(entry.getKey().toUpperCase());
                    recipe.require(aspect, entry.getValue().getAsInt());
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        return recipe;
    }

    private static void readPatternKey(ArcaneWorkbenchRecipe recipe, JsonObject key) {
        for (Map.Entry<String, JsonElement> entry : key.entrySet()) {
            String symbol = entry.getKey();
            if (symbol == null || symbol.isBlank()) {
                continue;
            }
            String itemId = entry.getValue().getAsString();
            if (itemId == null || itemId.isBlank()) {
                continue;
            }
            recipe.explicitPatternMap.put(symbol.charAt(0), new ResourceLocation(itemId));
        }
    }

}
