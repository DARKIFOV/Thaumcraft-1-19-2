package com.darkifov.thaumcraft.infusion;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class InfusionRecipeManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final Logger LOGGER = LogUtils.getLogger();


    private static final List<String> BUNDLED_RECIPE_FILES = List.of(
            "infusion_core.json",
            "infusion_core_original_style.json",
            "primordial_pearl.json",
            "primordial_pearl_original_style.json",
            "tc4_advancedgolem_clay.json",
            "tc4_advancedgolem_flesh.json",
            "tc4_advancedgolem_iron.json",
            "tc4_advancedgolem_stone.json",
            "tc4_advancedgolem_straw.json",
            "tc4_advancedgolem_tallow.json",
            "tc4_advancedgolem_thaumium.json",
            "tc4_advancedgolem_wood.json",
            "tc4_arcane_bore.json",
            "tc4_bootstraveller.json",
            "tc4_corealchemy.json",
            "tc4_corefishing.json",
            "tc4_corelumber.json",
            "tc4_coresorting.json",
            "tc4_coreuse.json",
            "tc4_eldritch_eye.json",
            "tc4_elementalaxe.json",
            "tc4_elementalhoe.json",
            "tc4_elementalpick.json",
            "tc4_elementalshovel.json",
            "tc4_elementalsword.json",
            "tc4_essentia_reservoir.json",
            "tc4_focushellbat.json",
            "tc4_focusportablehole.json",
            "tc4_focuswarding.json",
            "tc4_helm_goggles.json",
            "tc4_hovergirdle.json",
            "tc4_hoverharness.json",
            "tc4_jarbrain.json",
            "tc4_lamp_fertility.json",
            "tc4_lamp_growth.json",
            "tc4_mask_angry_ghost.json",
            "tc4_mask_grinning_devil.json",
            "tc4_mask_sipping_fiend.json",
            "tc4_mirror.json",
            "tc4_mirroressentia.json",
            "tc4_mirrorhand.json",
            "tc4_nodestabilizeradv.json",
            "tc4_primal_crusher.json",
            "tc4_runicamulet.json",
            "tc4_runicamuletemergency.json",
            "tc4_runicgirdle.json",
            "tc4_runicgirdlekinetic.json",
            "tc4_runicgirdlekinetic_2.json",
            "tc4_runicring.json",
            "tc4_runicringcharged.json",
            "tc4_runicringhealing.json",
            "tc4_sanity_check.json",
            "tc4_sin_stone.json",
            "tc4_thaumium_fortress_chest.json",
            "tc4_thaumium_fortress_helm.json",
            "tc4_thaumium_fortress_legs.json",
            "tc4_traveltrunk.json",
            "tc4_visamulet.json",
            "tc4_void_robe_chest.json",
            "tc4_void_robe_helm.json",
            "tc4_void_robe_legs.json",
            "tc4_wand_cap_silver.json",
            "tc4_wand_cap_thaumium.json",
            "tc4_wand_cap_void.json",
            "tc4_wand_rod_blaze.json",
            "tc4_wand_rod_bone.json",
            "tc4_wand_rod_ice.json",
            "tc4_wand_rod_obsidian.json",
            "tc4_wand_rod_primal_staff.json",
            "tc4_wand_rod_quartz.json",
            "tc4_wand_rod_reed.json",
            "tc4_wand_rod_silverwood.json",
            "tc4_wandped.json",
            "tc4_wandpedfocus.json",
            "unstable_singularity.json",
            "unstable_singularity_original_style.json",
            "void_metal_ingot.json",
            "void_metal_ingot_original_style.json"

    );

    /**
     * JEI asks for recipes before the server-data reload listener has always
     * completed. Load the bundled TC4 JSON set directly from the mod jar so the
     * infusion category never registers with only the two emergency fallbacks.
     */
    public static List<InfusionRecipe> loadBundledRecipes() {
        List<InfusionRecipe> recipes = new ArrayList<>();
        ClassLoader loader = InfusionRecipeManager.class.getClassLoader();
        for (String file : BUNDLED_RECIPE_FILES) {
            String resourcePath = "data/thaumcraft/thaumcraft_infusion/" + file;
            try (InputStream stream = loader.getResourceAsStream(resourcePath)) {
                if (stream == null) {
                    LOGGER.warn("Missing bundled Thaumcraft infusion recipe {}", resourcePath);
                    continue;
                }
                JsonObject json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                        .getAsJsonObject();
                String path = file.substring(0, file.length() - ".json".length());
                recipes.add(InfusionRecipe.fromJson(new ResourceLocation("thaumcraft", path), json));
            } catch (Exception exception) {
                LOGGER.error("Failed to load bundled Thaumcraft infusion recipe {}", resourcePath, exception);
            }
        }
        return Collections.unmodifiableList(recipes);
    }

    public InfusionRecipeManager() {
        super(GSON, "thaumcraft_infusion");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<InfusionRecipe> recipes = new ArrayList<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                recipes.add(InfusionRecipe.fromJson(entry.getKey(), json));
            } catch (Exception exception) {
                LOGGER.error("Failed to load Thaumcraft infusion recipe {}", entry.getKey(), exception);
            }
        }

        InfusionRecipes.setLoadedRecipes(recipes);
        LOGGER.info("Loaded {} Thaumcraft infusion recipes", recipes.size());
    }
}
