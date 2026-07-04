package com.darkifov.thaumcraft.alchemy;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlchemyRecipeManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final Logger LOGGER = LogUtils.getLogger();

    public AlchemyRecipeManager() {
        super(GSON, "thaumcraft_alchemy");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<AlchemyRecipe> recipes = new ArrayList<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                recipes.add(AlchemyRecipe.fromJson(entry.getKey(), json));
            } catch (Exception exception) {
                LOGGER.error("Failed to load Thaumcraft alchemy recipe {}", entry.getKey(), exception);
            }
        }

        AlchemyRecipes.setLoadedRecipes(recipes);
        LOGGER.info("Loaded {} Thaumcraft alchemy recipes", recipes.size());
    }
}
