package com.darkifov.thaumcraft.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraftforge.registries.ForgeRegistries;

/** Vanilla-furnace-compatible serializer whose result stack may contain more than one item. */
public final class CountedSmeltingRecipeSerializer implements RecipeSerializer<SmeltingRecipe> {
    @Override
    public SmeltingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        String group = GsonHelper.getAsString(json, "group", "");
        JsonElement ingredientJson = GsonHelper.isArrayNode(json, "ingredient")
                ? GsonHelper.getAsJsonArray(json, "ingredient")
                : GsonHelper.getAsJsonObject(json, "ingredient");
        Ingredient ingredient = Ingredient.fromJson(ingredientJson);

        JsonElement resultJson = json.get("result");
        String itemId;
        int count;
        if (resultJson != null && resultJson.isJsonObject()) {
            JsonObject object = resultJson.getAsJsonObject();
            itemId = GsonHelper.getAsString(object, "item");
            count = GsonHelper.getAsInt(object, "count", 1);
        } else {
            itemId = GsonHelper.getAsString(json, "result");
            count = GsonHelper.getAsInt(json, "count", 1);
        }
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
        if (item == null) throw new IllegalStateException("Unknown counted smelting result: " + itemId);
        ItemStack result = new ItemStack(item, Math.max(1, count));
        float experience = GsonHelper.getAsFloat(json, "experience", 0.0F);
        int cookingTime = GsonHelper.getAsInt(json, "cookingtime", 200);
        return new SmeltingRecipe(recipeId, group, ingredient, result, experience, cookingTime);
    }

    @Override
    public SmeltingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        String group = buffer.readUtf();
        Ingredient ingredient = Ingredient.fromNetwork(buffer);
        ItemStack result = buffer.readItem();
        float experience = buffer.readFloat();
        int cookingTime = buffer.readVarInt();
        return new SmeltingRecipe(recipeId, group, ingredient, result, experience, cookingTime);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, SmeltingRecipe recipe) {
        buffer.writeUtf(recipe.getGroup());
        recipe.getIngredients().get(0).toNetwork(buffer);
        buffer.writeItem(recipe.getResultItem());
        buffer.writeFloat(recipe.getExperience());
        buffer.writeVarInt(recipe.getCookingTime());
    }
}
