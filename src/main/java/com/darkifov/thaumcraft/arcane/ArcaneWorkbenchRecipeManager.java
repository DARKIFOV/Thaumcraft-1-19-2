package com.darkifov.thaumcraft.arcane;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArcaneWorkbenchRecipeManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final Logger LOGGER = LogUtils.getLogger();

    public ArcaneWorkbenchRecipeManager() {
        super(GSON, "thaumcraft_arcane_workbench");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<ArcaneWorkbenchRecipe> recipes = new ArrayList<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                recipes.add(ArcaneWorkbenchRecipe.fromJson(entry.getKey(), json));
            } catch (Exception exception) {
                LOGGER.error("Failed to load Thaumcraft arcane workbench recipe {}", entry.getKey(), exception);
            }
        }

        ArcaneWorkbenchRecipes.setLoadedRecipes(recipes);
        LOGGER.info("Loaded {} Thaumcraft arcane workbench recipes", recipes.size());
    }
}
